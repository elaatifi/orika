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

package ma.glasnost.orika.impl;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import ma.glasnost.orika.Converter;
import ma.glasnost.orika.Mapper;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.ObjectFactory;
import ma.glasnost.orika.metadata.ClassMap;
import ma.glasnost.orika.metadata.ClassMapBuilder;
import ma.glasnost.orika.metadata.ConverterKey;
import ma.glasnost.orika.metadata.MapperKey;
import ma.glasnost.orika.proxy.HibernateUnenhanceStrategy;
import ma.glasnost.orika.proxy.UnenhanceStrategy;

/**
 * The mapper factory is the heart of Orika, a small container where metadata
 * are stored, it's used by other component of engine, to look for generated
 * mappers, converter, object factories ... etc.
 * 
 * @author S.M. El Aatifi
 * 
 */
public class DefaultMapperFactory implements MapperFactory {
    
    private final MapperFacade mapperFacade;
    private final MapperGenerator mapperGenerator;
    private final Map<MapperKey, GeneratedMapperBase> mappersRegistry;
    private final Map<Object, Converter<?, ?>> convertersRegistry;
    private final Map<Class<?>, ObjectFactory<?>> objectFactoryRegistry;
    private final Map<Class<?>, Set<Class<?>>> aToBRegistry;
    
    private DefaultMapperFactory(Set<ClassMap<?, ?>> classMaps, Set<ObjectFactory<?>> objectFactories) {
        this.mappersRegistry = new ConcurrentHashMap<MapperKey, GeneratedMapperBase>();
        this.convertersRegistry = new ConcurrentHashMap<Object, Converter<?, ?>>();
        this.aToBRegistry = new ConcurrentHashMap<Class<?>, Set<Class<?>>>();
        this.mapperGenerator = new MapperGenerator(this);
        this.mapperFacade = new MapperFacadeImpl(this, getUnenhanceStrategy());
        
        if (classMaps != null) {
            for (ClassMap<?, ?> classMap : classMaps) {
                registerClassMap(classMap);
            }
        }
        
        objectFactoryRegistry = new ConcurrentHashMap<Class<?>, ObjectFactory<?>>();
        if (objectFactories != null) {
            for (ObjectFactory<?> objectFactory : objectFactories) {
                objectFactoryRegistry.put(objectFactory.getTargetClass(), objectFactory);
            }
        }
    }
    
    public DefaultMapperFactory() {
        this(null, null);
    }
    
    UnenhanceStrategy getUnenhanceStrategy() {
        try {
            Class.forName("org.hibernate.proxy.HibernateProxy");
            return new HibernateUnenhanceStrategy();
        } catch (Throwable e) {
            // TODO add warning
            return new UnenhanceStrategy() {
                
                public <T> T unenhanceObject(T object) {
                    return object;
                }
                
                @SuppressWarnings("unchecked")
                public <T> Class<T> unenhanceClass(T object) {
                    return (Class<T>) object.getClass();
                }
            };
        }
    }
    
    public GeneratedMapperBase lookupMapper(MapperKey mapperKey) {
        if (!mappersRegistry.containsKey(mapperKey)) {
            ClassMap<?, ?> classMap = ClassMapBuilder.map(mapperKey.getAType(), mapperKey.getBType()).byDefault().toClassMap();
            registerClassMap(classMap);
        }
        return mappersRegistry.get(mapperKey);
    }
    
    public <S, D> void registerConverter(Converter<S, D> converter, Class<? extends S> sourceClass, Class<? extends D> destinationClass) {
        convertersRegistry.put(new ConverterKey(sourceClass, destinationClass), converter);
    }
    
    @SuppressWarnings("unchecked")
    public <S, D> Converter<S, D> lookupConverter(Class<S> source, Class<D> destination) {
        return (Converter<S, D>) convertersRegistry.get(new ConverterKey(source, destination));
    }
    
    public MapperFacade getMapperFacade() {
        return mapperFacade;
    }
    
    public <T> void registerObjectFactory(ObjectFactory<T> objectFactory, Class<T> targetClass) {
        objectFactoryRegistry.put(targetClass, objectFactory);
    }
    
    @SuppressWarnings("unchecked")
    public <T> ObjectFactory<T> lookupObjectFactory(Class<T> targetClass) {
        if (targetClass == null) {
            return null;
        }
        return (ObjectFactory<T>) objectFactoryRegistry.get(targetClass);
    }
    
    @SuppressWarnings("unchecked")
    public <S, D> Class<? extends D> lookupConcreteDestinationClass(Class<S> sourceClass, Class<D> destinationClass, MappingContext context) {
        Class<? extends D> concreteClass = context.getConcreteClass(sourceClass, destinationClass);
        
        if (concreteClass != null) {
            return concreteClass;
        }
        
        Set<Class<?>> destinationSet = aToBRegistry.get(sourceClass);
        if (destinationSet == null || destinationSet.isEmpty()) {
            return null;
        }
        
        for (Class<?> clazz : destinationSet) {
            if (destinationClass.isAssignableFrom(clazz)) {
                return (Class<? extends D>) clazz;
                
            }
        }
        return concreteClass;
    }
    
    @SuppressWarnings("unchecked")
    public <S, D> void registerClassMap(ClassMap<S, D> classMap) {
        register(classMap.getAType(), classMap.getBType());
        register(classMap.getBType(), classMap.getAType());
        
        MapperKey mapperKey = new MapperKey(classMap.getAType(), classMap.getBType());
        GeneratedMapperBase mapper = this.mapperGenerator.build(classMap);
        mapper.setMapperFacade(mapperFacade);
        mapper.setCustomMapper((Mapper<Object, Object>) classMap.getCustomizedMapper());
        mappersRegistry.put(mapperKey, mapper);
    }
    
    private <S, D> void register(Class<S> sourceClass, Class<D> destinationClass) {
        Set<Class<?>> destinationSet = aToBRegistry.get(sourceClass);
        if (destinationSet == null) {
            destinationSet = new HashSet<Class<?>>();
            aToBRegistry.put(sourceClass, destinationSet);
        }
        destinationSet.add(destinationClass);
    }
}
