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

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ma.glasnost.orika.Converter;
import ma.glasnost.orika.Mapper;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.MappingException;
import ma.glasnost.orika.ObjectFactory;
import ma.glasnost.orika.impl.util.ClassUtil;
import ma.glasnost.orika.metadata.MapperKey;
import ma.glasnost.orika.proxy.UnenhanceStrategy;

public class MapperFacadeImpl implements MapperFacade {
    
    private final MapperFactory mapperFactory;
    private final UnenhanceStrategy unenhanceStrategy;
    
    public MapperFacadeImpl(MapperFactory mapperFactory, UnenhanceStrategy unenhanceStrategy) {
        this.mapperFactory = mapperFactory;
        this.unenhanceStrategy = unenhanceStrategy;
    }
    
    public <S, D> D map(S sourceObject, Class<D> destinationClass) {
        return map(sourceObject, destinationClass, new MappingContext());
    }
    
    @SuppressWarnings("unchecked")
    public <S, D> D map(S sourceObject, Class<D> destinationClass, MappingContext context) {
        if (destinationClass == null)
            throw new MappingException("Can not map to a null class.");
        if (sourceObject == null)
            throw new MappingException("Can not map a null object.");
        
        if (context.isAlreadyMapped(sourceObject)) {
            return (D) context.getMappedObject(sourceObject);
        }
        
        S unenhancedSourceObject = unenhanceStrategy.unenhanceObject(sourceObject);
        Class<S> sourceClass = (Class<S>) unenhancedSourceObject.getClass();
        
        // XXX when it's immutable it's ok to copy by ref
        if (ClassUtil.isImmutable(unenhancedSourceObject.getClass()) && sourceClass.equals(destinationClass)) {
            return (D) unenhancedSourceObject;
        }
        
        if (Modifier.isAbstract(destinationClass.getModifiers())) {
            destinationClass = (Class<D>) mapperFactory.lookupConcreteDestinationClass(sourceClass, destinationClass, context);
            if (destinationClass == null) {
                throw new MappingException("No concrete class mapping defined for source class " + sourceClass.getName());
            }
        }
        
        D destinationObject = newObject(unenhancedSourceObject, destinationClass);
        
        context.cacheMappedObject(sourceObject, destinationObject);
        
        mapDeclaredProperties(unenhancedSourceObject, destinationObject, sourceClass, destinationClass, context);
        
        return destinationObject;
    }
    
    public <S, D> void map(S sourceObject, D destinationObject, MappingContext context) {
        if (destinationObject == null)
            throw new MappingException("[destinationObject] can not be null.");
        if (sourceObject == null)
            throw new MappingException("[sourceObject] can not be null.");
        
        S unenhancedSourceObject = unenhanceStrategy.unenhanceObject(sourceObject);
        @SuppressWarnings("unchecked")
        Class<S> sourceClass = (Class<S>) unenhancedSourceObject.getClass();
        Class<?> destinationClass = destinationObject.getClass();
        
        mapDeclaredProperties(sourceObject, destinationObject, sourceClass, destinationClass, context);
    }
    
    public <S, D> void map(S sourceObject, D destinationObject) {
        map(sourceObject, destinationObject, new MappingContext());
    }
    
    public final <S, D> Set<D> mapAsSet(Iterable<S> source, Class<D> destinationClass) {
        return mapAsSet(source, destinationClass, new MappingContext());
    }
    
    public final <S, D> Set<D> mapAsSet(Iterable<S> source, Class<D> destinationClass, MappingContext context) {
        return (Set<D>) mapAsCollection(source, destinationClass, new HashSet<D>(), context);
    }
    
    public final <S, D> List<D> mapAsList(Iterable<S> source, Class<D> destinationClass) {
        return (List<D>) mapAsCollection(source, destinationClass, new ArrayList<D>(), new MappingContext());
    }
    
    public final <S, D> List<D> mapAsList(Iterable<S> source, Class<D> destinationClass, MappingContext context) {
        return (List<D>) mapAsCollection(source, destinationClass, new ArrayList<D>(), context);
    }
    
    public <S, D> D[] mapAsArray(D[] destination, Iterable<S> source, Class<D> destinationClass) {
        return mapAsArray(destination, source, destinationClass, new MappingContext());
    }
    
    public <S, D> D[] mapAsArray(D[] destination, S[] source, Class<D> destinationClass) {
        return mapAsArray(destination, source, destinationClass, new MappingContext());
    }
    
    public <S, D> D[] mapAsArray(D[] destination, Iterable<S> source, Class<D> destinationClass, MappingContext context) {
        int i = 0;
        for (S s : source) {
            destination[i++] = map(s, destinationClass);
        }
        return destination;
    }
    
    public <S, D> D[] mapAsArray(D[] destination, S[] source, Class<D> destinationClass, MappingContext context) {
        int i = 0;
        for (S s : source) {
            destination[i++] = map(s, destinationClass);
        }
        return destination;
    }
    
    public <S, D> List<D> mapAsList(S[] source, Class<D> destinationClass) {
        return mapAsList(source, destinationClass, new MappingContext());
    }
    
    public <S, D> List<D> mapAsList(S[] source, Class<D> destinationClass, MappingContext context) {
        List<D> destination = new ArrayList<D>(source.length);
        for (S s : source) {
            destination.add(map(s, destinationClass, context));
        }
        return destination;
    }
    
    public <S, D> Set<D> mapAsSet(S[] source, Class<D> destinationClass) {
        return mapAsSet(source, destinationClass, new MappingContext());
    }
    
    public <S, D> Set<D> mapAsSet(S[] source, Class<D> destinationClass, MappingContext context) {
        Set<D> destination = new HashSet<D>(source.length);
        for (S s : source) {
            destination.add(map(s, destinationClass));
        }
        return destination;
    }
    
    void mapDeclaredProperties(Object sourceObject, Object destinationObject, Class<?> sourceClass, Class<?> destinationClass,
            MappingContext context) {
        MapperKey mapperKey = new MapperKey(sourceClass, destinationClass);
        Mapper<Object, Object> mapper = mapperFactory.lookupMapper(mapperKey);
        
        if (mapper == null) {
            throw new IllegalStateException(String.format("Can not create a mapper for classes : %s, %s", destinationClass,
                    sourceObject.getClass()));
        }
        
        if (mapper.getAType().equals(sourceClass)) {
            mapper.mapAtoB(sourceObject, destinationObject, context);
        } else if (mapper.getAType().equals(destinationClass)) {
            mapper.mapBtoA(sourceObject, destinationObject, context);
        } else {
            throw new IllegalStateException(String.format("Source object type's must be one of '%s' or '%s'.", mapper.getAType(),
                    mapper.getBType()));
        }
    }
    
    public <S, D> D newObject(S sourceObject, Class<? extends D> destinationClass) {
        
        try {
            ObjectFactory<? extends D> objectFactory = mapperFactory.lookupObjectFactory(destinationClass);
            if (objectFactory != null) {
                return objectFactory.create(sourceObject);
            } else {
                return destinationClass.newInstance();
            }
        } catch (InstantiationException e) {
            throw new MappingException(e);
        } catch (IllegalAccessException e) {
            throw new MappingException(e);
        }
    }
    
    <S, D> Collection<D> mapAsCollection(Iterable<S> source, Class<D> destinationClass, Collection<D> destination, MappingContext context) {
        for (S item : source) {
            destination.add(map(item, destinationClass, context));
        }
        return destination;
    }
    
    @SuppressWarnings("unchecked")
    public <S, D> D convert(S source, Class<D> destinationClass) {
        Class<? extends Object> sourceClass = unenhanceStrategy.unenhanceClass(source);
        Converter<S, D> converter = (Converter<S, D>) mapperFactory.lookupConverter(sourceClass, destinationClass);
        return converter.convert(source);
    }
    
}
