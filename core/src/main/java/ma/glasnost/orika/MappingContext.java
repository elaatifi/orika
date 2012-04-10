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

package ma.glasnost.orika;

import java.util.HashMap;
import java.util.Map;

import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.metadata.TypeFactory;

public class MappingContext {
    
    private final Map<Type<?>, Type<?>> mapping;
    private final Map<Object, Object> cache;
    
    public MappingContext() {
        mapping = new HashMap<Type<?>, Type<?>>();
        cache = new HashMap<Object, Object>();
    }
    
    @SuppressWarnings("unchecked")
    public <S, D> Type<? extends D> getConcreteClass(Type<S> sourceType, Type<D> destinationType) {
        
        final Type<?> type = mapping.get(sourceType);
        if (type != null && destinationType.isAssignableFrom(type)) {
            return (Type<? extends D>) type;
        }
        return null;
    }
    
    public void registerConcreteClass(Type<?> subjectClass, Type<?> concreteClass) {
        mapping.put(subjectClass, concreteClass);
    }
    
    @Deprecated
    public <S, D> void cacheMappedObject(S source, D destination) {
        cache.put(hashMappedObject(source, TypeFactory.typeOf(destination)), destination);
    }
    
    public <S, D> void cacheMappedObject(S source, Type<D> destinationType, D destination) {
        cache.put(hashMappedObject(source, destinationType), destination);
    }
    
    public <S, D> boolean isAlreadyMapped(S source, Type<D> destinationClass) {
        return cache.containsKey(hashMappedObject(source, destinationClass));
    }
    
    @SuppressWarnings("unchecked")
    public <D> D getMappedObject(Object source, Type<D> destinationType) {
        
        return (D) cache.get(hashMappedObject(source, destinationType));
    }
    
    private static Integer hashMappedObject(Object source, Type<?> destinationType) {
        return System.identityHashCode(source) * 31 + System.identityHashCode(destinationType);
    }
}
