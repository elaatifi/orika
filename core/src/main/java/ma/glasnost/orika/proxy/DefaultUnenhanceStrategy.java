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

package ma.glasnost.orika.proxy;

import java.util.Collections;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class DefaultUnenhanceStrategy implements UnenhanceStrategy {
    
	private final Map<Class<?>, Set<Class<?>>> aToBRegistry;
	private final ConcurrentHashMap<Class<?>,Class<?>> mappedSuperTypes;
	private final Queue<UnenhanceStrategy> strategyChain = new LinkedBlockingQueue<UnenhanceStrategy>();
	
	public DefaultUnenhanceStrategy(Map<Class<?>, Set<Class<?>>> aToBRegistry) {
		this.aToBRegistry = aToBRegistry!=null ? aToBRegistry : Collections.<Class<?>, Set<Class<?>>>emptyMap();
		this.mappedSuperTypes = new ConcurrentHashMap<Class<?>,Class<?>>();
	}

	public void addDelegateStrategy(UnenhanceStrategy strategy) {
		strategyChain.add(strategy);
	}
	
	
    public <T> T unenhanceObject(T object) {
        return object;
    }
    
    @SuppressWarnings("unchecked")
    public <T> Class<T> unenhanceClass(T object) {
        
    	Class<T> unenhancedClass = (Class<T>) object.getClass();
    	for(UnenhanceStrategy strategy: strategyChain) {
    		Class<T> delegateUnenhanced = (Class<T>) strategy.unenhanceClass(object);
    		// Accept the first delegate strategy result which produces 
    		// something different than the object's getClass method
    		if (!delegateUnenhanced.equals(unenhancedClass)) {
    			unenhancedClass = delegateUnenhanced;
    			break;
    		}
    	}
    	
    	unenhancedClass = getUnenhancedClass(unenhancedClass);
    	
    	return unenhancedClass;	
    }
    
    @SuppressWarnings("unchecked")
    protected <T> Class<T> getUnenhancedClass(final Class<?> enhancedClass) {
    	
		Class<T> mappedClass = (Class<T>) enhancedClass;
    	if (!aToBRegistry.containsKey(mappedClass)) {
    		Class<T> mappedSuper = (Class<T>) lookupMappedSuperType(mappedClass);
    		if (mappedSuper!=null) {
    			mappedClass = mappedSuper;
    		} else {
    			mappedSuper = (Class<T>) lookupMappedInterface(mappedClass);
    			if (mappedSuper!=null) {
        			mappedClass = mappedSuper;
        		}
    		}
    	}
    	return mappedClass;
    }
    
	private Class<?> lookupMappedSuperType(final Class<?> theClass) {
    	
		Class<?> targetClass = theClass.getSuperclass();
		Class<?> mappedClass = mappedSuperTypes.get(targetClass);
    	
    	while (mappedClass==null && !targetClass.equals(Object.class)) {
    		if(aToBRegistry.containsKey(targetClass)) {
    			mappedClass = targetClass;
    			mappedSuperTypes.putIfAbsent(theClass, mappedClass);
    			break;
    		} 
    		targetClass = targetClass.getSuperclass();
    	}
    	
    	return mappedClass;
    }
    
    private Class<?> lookupMappedInterface(final Class<?> theClass) {
    	
    	Class<?> targetClass = theClass;
		Class<?> mappedClass = mappedSuperTypes.get(targetClass);
    	
		while (mappedClass==null && !targetClass.equals(Object.class)) {
	    	
    		for (Class<?> theInterface: targetClass.getInterfaces()) {
	    		if(aToBRegistry.containsKey(theInterface)) {
	    			mappedClass = theInterface;
	    			mappedSuperTypes.putIfAbsent(theClass, mappedClass);
	    			break;
	    		} 
    		}
    		targetClass = targetClass.getSuperclass();
		}
    	
    	return mappedClass;

    }
    
}
