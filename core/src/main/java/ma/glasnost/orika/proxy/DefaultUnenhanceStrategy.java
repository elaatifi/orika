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

import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import ma.glasnost.orika.inheritance.SuperTypeResolver;
import ma.glasnost.orika.inheritance.SuperTypeResolverStrategy;

public class DefaultUnenhanceStrategy implements UnenhanceStrategy {
    
	private final ConcurrentHashMap<Class<?>,Class<?>> mappedSuperTypes;
	private final Queue<UnenhanceStrategy> strategyChain = new LinkedBlockingQueue<UnenhanceStrategy>();
	private final SuperTypeResolver superTypeUtil; 
	
	public DefaultUnenhanceStrategy(final SuperTypeResolverStrategy strategy) {
		
		this.mappedSuperTypes = new ConcurrentHashMap<Class<?>,Class<?>>();
		this.superTypeUtil = new SuperTypeResolver(strategy);
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
    		if (delegateUnenhanced!=null && !unenhancedClass.equals(delegateUnenhanced)) {
    			unenhancedClass = delegateUnenhanced;
    			break;
    		}
    	}
    	
    	Class<?> superType = superTypeUtil.getSuperType(unenhancedClass);
    	if (superType!=null && !unenhancedClass.equals(superType)) {
    		mappedSuperTypes.putIfAbsent(unenhancedClass, superType);
    		unenhancedClass = (Class<T>)superType;
    	}
    	return unenhancedClass;	
    }
    
}
