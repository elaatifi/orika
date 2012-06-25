/*
 * Orika - simpler, better and faster Java bean mapping
 * 
 * Copyright (C) 2011 Orika authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ma.glasnost.orika.constructor;

import static ma.glasnost.orika.impl.Specifications.aMappingOfTheRequiredClassProperty;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.TreeMap;

import ma.glasnost.orika.metadata.ClassMap;
import ma.glasnost.orika.metadata.FieldMap;
import ma.glasnost.orika.metadata.Property;
import ma.glasnost.orika.metadata.Type;

import com.thoughtworks.paranamer.AdaptiveParanamer;
import com.thoughtworks.paranamer.AnnotationParanamer;
import com.thoughtworks.paranamer.BytecodeReadingParanamer;
import com.thoughtworks.paranamer.CachingParanamer;
import com.thoughtworks.paranamer.ParameterNamesNotFoundException;
import com.thoughtworks.paranamer.Paranamer;

/**
 * SimpleConstructorResolverStrategy attempts to resolve the appropriate constructor
 * to use in a field mapping by the following algorithm:
 * <ol>
 * <li>If an explicit constructor has been defined (based on parameter names), then use it
 * <li>Attempt to find a constructor which has parameter names matching all of the mapped
 * property names of the destination class
 * <li>Return the first constructor in the list
 * </ol>
 *
 */
public class SimpleConstructorResolverStrategy implements ConstructorResolverStrategy {
    
	private Paranamer paranamer = new CachingParanamer(new AdaptiveParanamer(new BytecodeReadingParanamer(), new AnnotationParanamer()));
	
    @SuppressWarnings({ "unchecked" })
    public <T, A, B> Constructor<T> resolve(ClassMap<A, B> classMap, Type<T> sourceType) {
        boolean aToB = classMap.getBType().equals(sourceType);
        
        
        Type<?> targetClass = aToB ? classMap.getBType() : classMap.getAType();
        
        String[] argumentNames = aToB ? classMap.getConstructorB() : classMap.getConstructorA();
        
        Collection<String> targetParameterNames = null;
        if (argumentNames != null) {
        	/*
        	 * An override to the property names was provided
        	 */
        	targetParameterNames = Arrays.asList(argumentNames);
        } else {
        	/*
        	 * Determine the set of constructor argument names
        	 * from the field mapping
        	 */
        	targetParameterNames = new HashSet<String>();
        	for(FieldMap fieldMap: classMap.getFieldsMapping()) {
        		if (!fieldMap.is(aMappingOfTheRequiredClassProperty())) {
	        		Property destination = aToB ? fieldMap.getDestination() : fieldMap.getSource();
	        		targetParameterNames.add(destination.getName());
        		}
        	}
        	
        }
        
        Constructor<T>[] constructors = (Constructor<T>[]) targetClass.getRawType().getConstructors();
        TreeMap<Integer, Constructor<T>> constructorsByMatchedParams = new TreeMap<Integer, Constructor<T>>();
        for (Constructor<T> constructor: constructors) {
        	
        	try {
        		String[] parameterNames = paranamer.lookupParameterNames(constructor);
        		if (targetParameterNames.containsAll(Arrays.asList(parameterNames))) {
        			constructorsByMatchedParams.put(parameterNames.length, constructor);
        		}
        	} catch (ParameterNamesNotFoundException e) {
        	    throw new IllegalStateException("Constructor matching cannot be performed against types which" +
        	    		" have not been compiled with debug information",e);
        	}
        }
        
        if (constructorsByMatchedParams.size() > 0) {
            return constructorsByMatchedParams.get(constructorsByMatchedParams.lastKey());
        }
        
        /* fail-safe if we couldn't find any better match 
         * */
        return constructors.length == 0 ? null : constructors[0];
    }
}
