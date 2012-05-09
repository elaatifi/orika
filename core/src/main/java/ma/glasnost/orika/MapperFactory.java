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

import java.util.Set;

import ma.glasnost.orika.converter.ConverterFactory;
import ma.glasnost.orika.metadata.ClassMap;
import ma.glasnost.orika.metadata.MapperKey;
import ma.glasnost.orika.metadata.Type;

/**
 * MapperFactory
 * 
 * The mapper factory is the heart of Orika, a small container where metadata
 * are stored, it's used by other component of engine, to look for generated
 * mappers, converter, object factories ... etc.
 * 
 * @author S.M. El Aatifi
 * 
 */
public interface MapperFactory {
    
    <A, B> Mapper<A, B> lookupMapper(MapperKey mapperKey);
    
    <A, B> void registerClassMap(ClassMap<A, B> classMap);
    
    @Deprecated
    <T> void registerObjectFactory(ObjectFactory<T> objectFactory, Class<T> targetClass);
    
    <T> void registerObjectFactory(ObjectFactory<T> objectFactory, Type<T> targetType);
    
    <T> ObjectFactory<T> lookupObjectFactory(Type<T> targetType);
    
    <S, D> Type<? extends D> lookupConcreteDestinationType(Type<S> sourceType, Type<D> destinationType, MappingContext context);
    
    @Deprecated
    void registerMappingHint(MappingHint... hints);
    
    void registerDefaultFieldMapper(DefaultFieldMapper... fieldDefaults);
    
    void registerConcreteType(Type<?> abstractType, Type<?> concreteType);
    
    void registerConcreteType(Class<?> abstractType, Class<?> concreteType);
    
    Set<ClassMap<Object, Object>> lookupUsedClassMap(MapperKey mapperKey);
    
    <A, B> ClassMap<A, B> getClassMap(MapperKey mapperKey);
    
    Set<Type<? extends Object>> lookupMappedClasses(Type<?> type);
    
    MapperFacade getMapperFacade();
    
    ConverterFactory getConverterFactory();
    
    void build();
    
}
