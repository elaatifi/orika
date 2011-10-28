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

package ma.glasnost.orika.inheritance;


public class SuperTypeResolver {
	
	private final SuperTypeResolverStrategy strategy;
	
	public SuperTypeResolver(SuperTypeResolverStrategy strategy) {
		this.strategy = strategy;
	}
	
	@SuppressWarnings("unchecked")
    public <T> Class<T> getSuperType(final Class<?> enhancedClass) {
    	
		Class<T> mappedClass = (Class<T>) enhancedClass;
    	if (strategy.shouldLookupSuperType(mappedClass)) {
    		
    		Class<T> mappedSuper = (Class<T>) tryFirstLookupOption(mappedClass);
    		if (mappedSuper!=null) {
    			mappedClass = mappedSuper;
    		} else {
    			mappedSuper = (Class<T>) trySecondLookupOption(mappedClass);
    			if (mappedSuper!=null) {
        			mappedClass = mappedSuper;
        		}
    		}
    		
    	}
    	return mappedClass;
    }
	
	private Class<?> tryFirstLookupOption(final Class<?> theClass) {
		if (strategy.shouldPreferClassOverInterface()) {
			return lookupMappedSuperType(theClass);
		} else {
			return lookupMappedInterface(theClass);
		}
	}
	
	private Class<?> trySecondLookupOption(final Class<?> theClass) {
		if (strategy.shouldPreferClassOverInterface()) {
			return lookupMappedInterface(theClass);
		} else {
			return lookupMappedSuperType(theClass);
		}
	}
	
	private Class<?> lookupMappedSuperType(final Class<?> theClass) {
    	
		Class<?> targetClass = theClass.getSuperclass();
		Class<?> mappedClass = null;
    	
    	while (mappedClass==null && !targetClass.equals(Object.class)) {
    		if(strategy.accept(targetClass)) {
    			mappedClass = targetClass;
    			break;
    		} 
    		targetClass = targetClass.getSuperclass();
    	}
    	
    	return mappedClass;
    }
    
    private Class<?> lookupMappedInterface(final Class<?> theClass) {
    	
    	Class<?> targetClass = theClass;
		Class<?> mappedClass = null;
    	
		while (mappedClass==null && !targetClass.equals(Object.class)) {
	    	
    		for (Class<?> theInterface: targetClass.getInterfaces()) {
	    		if(strategy.accept(theInterface)) {
	    			mappedClass = theInterface;
	    			break;
	    		} 
    		}
    		targetClass = targetClass.getSuperclass();
		}
    	
    	return mappedClass;

    }
    
}
