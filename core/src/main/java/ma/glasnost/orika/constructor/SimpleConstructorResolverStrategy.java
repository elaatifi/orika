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

import java.lang.reflect.Constructor;

import ma.glasnost.orika.metadata.ClassMap;

public class SimpleConstructorResolverStrategy implements ConstructorResolverStrategy {
    
    @SuppressWarnings({ "unchecked" })
    public <T, A, B> Constructor<T> resolve(ClassMap<A, B> classMap, Class<T> sourceClass) {
        boolean aToB = classMap.getBType().equals(sourceClass);
        // String[] argumentNames = aToB ? classMap.getConstructorB() :
        // classMap.getConstructorA();
        Class<?> targetClass = aToB ? classMap.getBType() : classMap.getAType();
        
        // TODO to specify
        return (Constructor<T>) targetClass.getConstructors()[0];
        
    }
}
