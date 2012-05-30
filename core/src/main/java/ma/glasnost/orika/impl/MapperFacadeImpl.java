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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ma.glasnost.orika.Converter;
import ma.glasnost.orika.MapEntry;
import ma.glasnost.orika.Mapper;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.MappingException;
import ma.glasnost.orika.ObjectFactory;
import ma.glasnost.orika.OrikaSystemProperties;
import ma.glasnost.orika.converter.ConverterFactory;
import ma.glasnost.orika.impl.mapping.strategy.MappingStrategy;
import ma.glasnost.orika.impl.mapping.strategy.MappingStrategyKey;
import ma.glasnost.orika.impl.mapping.strategy.MappingStrategyRecorder;
import ma.glasnost.orika.impl.util.ClassUtil;
import ma.glasnost.orika.metadata.MapperKey;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.metadata.TypeFactory;
import ma.glasnost.orika.unenhance.UnenhanceStrategy;
import ma.glasnost.orika.util.CacheLRULinkedHashMap;

public class MapperFacadeImpl implements MapperFacade {
    
    private final MapperFactory mapperFactory;
    private final UnenhanceStrategy unenhanceStrategy;
    private final ConcurrentHashMap<java.lang.reflect.Type, Type<?>> resolvedTypes = new ConcurrentHashMap<java.lang.reflect.Type, Type<?>>();
    private final Map<MappingStrategyKey, MappingStrategy> strategyCache = new CacheLRULinkedHashMap<MappingStrategyKey, MappingStrategy>(500);
    private final boolean useStrategyCache;
    
    private final Logger log = LoggerFactory.getLogger(getClass());
    
    public MapperFacadeImpl(MapperFactory mapperFactory, UnenhanceStrategy unenhanceStrategy) {
        this.mapperFactory = mapperFactory;
        this.unenhanceStrategy = unenhanceStrategy;
        this.useStrategyCache = Boolean.valueOf(System.getProperty(OrikaSystemProperties.USE_STRATEGY_CACHE, "true"));
    }
    
    @SuppressWarnings("unchecked")
    private <S, D> Type<S> normalizeSourceType(S sourceObject, Type<S> sourceType, Type<D> destinationType) {
        
        /*
         * Use the raw type in cases where the sourceType is null or not
         * providing any extra information
         */
        java.lang.reflect.Type typeKey = sourceType;
        if (sourceType == null || !sourceType.isParameterized()) {
            typeKey = sourceObject.getClass();
        }
        
        Type<?> resolvedType = resolvedTypes.get(typeKey);
        if (resolvedType == null) {
            Type<?> newlyResolvedType;
            if (sourceType != null) {
                if (destinationType != null &&
                        (canCopyByReference(destinationType, sourceType)
                        || canConvert(sourceType, destinationType))) {
                    /*
                     * We shouldn't bother further resolving the source type
                     * if we already have a converter or copy-by-reference
                     * for the originally specified type -- since these operations
                     * override the use of a custom mapper which needs the resolution.
                     */
                    newlyResolvedType = sourceType;
                } else {
                
                    if (sourceType.isAssignableFrom(sourceObject.getClass())) {
                        sourceType = (Type<S>) TypeFactory.valueOf(sourceObject.getClass());
                    }
                    if (ClassUtil.isConcrete(sourceType)) {
                        newlyResolvedType = unenhanceStrategy.unenhanceType(sourceObject, sourceType);
                    } else {
                        newlyResolvedType = unenhanceStrategy.unenhanceType(sourceObject, TypeFactory.resolveTypeOf(sourceObject, sourceType));
                    }
                }
                
                resolvedType = resolvedTypes.putIfAbsent(typeKey, newlyResolvedType);
                if (resolvedType == null) {
                    resolvedType = newlyResolvedType;
                }
            } else {
                resolvedType = unenhanceStrategy.unenhanceType(sourceObject, TypeFactory.typeOf(sourceObject));
            }
            
        }
        return (Type<S>) resolvedType;
    }
    
    public <S, D> D map(S sourceObject, Type<S> sourceType, Type<D> destinationClass) {
        return map(sourceObject, sourceType, destinationClass, new MappingContext());
    }
    
    public <S, D> D map(final S sourceObject, final Type<S> sourceType, final Type<D> destinationType, final MappingContext context) {
        if (destinationType == null) {
            throw new MappingException("Can not map to a null class.");
        }
        if (sourceObject == null) {
            // throw new MappingException("Can not map a null object.");
            return null;
        }
        
        if (context.isAlreadyMapped(sourceObject, destinationType)) {
            D result = context.getMappedObject(sourceObject, destinationType);
            return result;
        }
        
        /*
         * Note: we cache a MappingStrategy as one of the possible combinations of paths
         * through the below code; this allows us to avoid repeating the if-then checks
         * and the individual hash lookups at each stage; we resolve them only once and
         * then save the "path" we took by using one of the defined strategies
         * 
         * We use a thread-local for the strategy key so that we don't have to instantiate
         * a new key every time we perform a lookup; this approach relies on the assumption
         * that resolving the thread local is faster than instantiating a new strategy key
         */
        MappingStrategyKey key = null;
        if (useStrategyCache) {
            key = MappingStrategyKey.getCurrent();
            key.initialize(sourceObject.getClass(), sourceType, destinationType, false);
            
            MappingStrategy strategy = strategyCache.get(key);
            if (strategy != null) {
                @SuppressWarnings("unchecked")
                D result = (D)strategy.map(sourceObject, null, context);
                return result;
            } 
        }
        
        MappingStrategyRecorder strategyRecorder = null;
        if (useStrategyCache) {
            /*
             * Convert the current key to an immutable copy (and clear it) so that other 
             * lookups on the same thread can use it
             */
            key = key.toImmutableCopy();
            strategyRecorder = new MappingStrategyRecorder(key, unenhanceStrategy);
        }
        
        final Type<S> resolvedSourceType = normalizeSourceType(sourceObject, sourceType, destinationType);
        final S resolvedSourceObject = unenhanceStrategy.unenhanceObject(sourceObject, sourceType);
        
        if (useStrategyCache) {
            strategyRecorder.setResolvedSourceType(resolvedSourceType);
            strategyRecorder.setResolvedDestinationType(destinationType);
            if (sourceObject != resolvedSourceObject) {
                strategyRecorder.setUnenhance(true);
            }
            strategyRecorder.setInstantiate(true);
        }
        
        // We can copy by reference when source and destination types are the
        // same and immutable.
        if (canCopyByReference(destinationType, resolvedSourceType)) {
            if (useStrategyCache) {
                strategyRecorder.setCopyByReference(true);
                strategyCache.put(key, strategyRecorder.playback());
                if (log.isDebugEnabled()) {
                    log.debug(strategyRecorder.describeDetails());
                }
            }
            @SuppressWarnings("unchecked")
            D result = (D) resolvedSourceObject;
            return result;
        }
        
        // Check if we have a converter
        if (canConvert(resolvedSourceType, destinationType)) {
            if (useStrategyCache) {
                strategyRecorder.setResolvedConverter(mapperFactory.getConverterFactory().getConverter(resolvedSourceType, destinationType));
                strategyCache.put(key, strategyRecorder.playback());
                if (log.isDebugEnabled()) {
                    log.debug(strategyRecorder.describeDetails());
                }
            }
            return convert(resolvedSourceObject, sourceType, destinationType, null);
        }
        
        Type<? extends D> resolvedDestinationType = mapperFactory.lookupConcreteDestinationType(resolvedSourceType, destinationType,
                context);
        if (resolvedDestinationType == null) {
            if (!ClassUtil.isConcrete(destinationType)) {
                throw new MappingException("No concrete class mapping defined for source class " + resolvedSourceType.getName());
            } else {
                resolvedDestinationType = destinationType;
            }
        }
        
        if (useStrategyCache) {
            strategyRecorder.setResolvedDestinationType(resolvedDestinationType);
        }
        
        final Mapper<Object, Object> mapper = prepareMapper(resolvedSourceType, resolvedDestinationType);
        
        if (useStrategyCache) {
            strategyRecorder.setResolvedMapper(mapper);
        }
        
        final D destinationObject = newObject(resolvedSourceObject, resolvedDestinationType, context, strategyRecorder);
        
        context.cacheMappedObject(sourceObject, destinationType, destinationObject);
        
        mapDeclaredProperties(resolvedSourceObject, destinationObject, resolvedSourceType, resolvedDestinationType, context, mapper, strategyRecorder);
        
        if (useStrategyCache) {
            strategyCache.put(key, strategyRecorder.playback());
            if (log.isDebugEnabled()) {
                log.debug(strategyRecorder.describeDetails());
            }
        }
        
        return destinationObject;
        
    }
    
    /**
     * Resolves whether the given mapping operation can use copy-by-reference semantics;
     * should be true if one of the following is true:
     * <ol>
     * <li>resolvedSourceType and destinationType are the same, and one of the immutable types
     * <li>resolvedSourceType is the primitive wrapper for destinationType
     * <li>resolvedSourceType is primitive and destinationType is it's primitive wrapper
     * </ol>
     * @param destinationType
     * @param resolvedSourceType
     * @return
     */
    private <D, S> boolean canCopyByReference(Type<D> destinationType, final Type<S> resolvedSourceType) {
        if ( ClassUtil.isImmutable(resolvedSourceType) 
        		&& (resolvedSourceType.equals(destinationType))) {
        	return true;
        } else if (resolvedSourceType.isPrimitiveWrapper() 
        		&& resolvedSourceType.getRawType().equals(ClassUtil.getWrapperType(destinationType.getRawType()))) {
        	return true;
        } else if (resolvedSourceType.isPrimitive() 
        		&& destinationType.getRawType().equals(ClassUtil.getWrapperType(resolvedSourceType.getRawType()))) {
        	return true;
        } else {
        	return false;
        }
    }
    
    public <S, D> void map(S sourceObject, D destinationObject, Type<S> sourceType, Type<D> destinationType, MappingContext context) {
        if (destinationObject == null) {
            throw new MappingException("[destinationObject] can not be null.");
        }
        if (sourceObject == null) {
            throw new MappingException("[sourceObject] can not be null.");
        }
        
        MappingStrategyKey key = MappingStrategyKey.getCurrent();
        key.initialize(sourceObject.getClass(), sourceType, destinationType, true);
        
        MappingStrategy strategy = strategyCache.get(key);
        if (strategy != null) {
            strategy.map(sourceObject, destinationObject, context);
        } else {
            key = key.toImmutableCopy();
            MappingStrategyRecorder strategyRecorder = new MappingStrategyRecorder(key, unenhanceStrategy);
        
            final Type<S> theSourceType = normalizeSourceType(sourceObject, sourceType != null ? sourceType : TypeFactory.typeOf(sourceObject), null);
            final Type<D> theDestinationType = destinationType != null ? destinationType : TypeFactory.typeOf(destinationObject);
            
            final Mapper<Object, Object> mapper = prepareMapper(theSourceType, theDestinationType);
            
            strategyRecorder.setResolvedSourceType(theSourceType);
            strategyRecorder.setResolvedDestinationType(theDestinationType);
            strategyRecorder.setResolvedMapper(mapper);
            
            mapDeclaredProperties(sourceObject, destinationObject, theSourceType, theDestinationType, context, mapper, strategyRecorder);
            
            strategyCache.put(key, strategyRecorder.playback());
        }
    }
    
    public <S, D> void map(S sourceObject, D destinationObject, Type<S> sourceType, Type<D> destinationType) {
        map(sourceObject, destinationObject, sourceType, destinationType, new MappingContext());
    }
    
    public <S, D> void map(S sourceObject, D destinationObject, MappingContext context) {
        map(sourceObject, destinationObject, null, null, context);
    }
    
    public <S, D> void map(S sourceObject, D destinationObject) {
        map(sourceObject, destinationObject, new MappingContext());
    }
    
    public final <S, D> Set<D> mapAsSet(Iterable<S> source, Type<S> sourceType, Type<D> destinationType) {
        return mapAsSet(source, sourceType, destinationType, new MappingContext());
    }
    
    public final <S, D> Set<D> mapAsSet(Iterable<S> source, Type<S> sourceType, Type<D> destinationType, MappingContext context) {
        return (Set<D>) mapAsCollection(source, sourceType, destinationType, new HashSet<D>(), context);
    }
    
    public final <S, D> List<D> mapAsList(Iterable<S> source, Type<S> sourceType, Type<D> destinationType) {
        return (List<D>) mapAsCollection(source, sourceType, destinationType, new ArrayList<D>(), new MappingContext());
    }
    
    public final <S, D> List<D> mapAsList(Iterable<S> source, Type<S> sourceType, Type<D> destinationType, MappingContext context) {
        return (List<D>) mapAsCollection(source, sourceType, destinationType, new ArrayList<D>(), context);
    }
    
    public <S, D> D[] mapAsArray(D[] destination, Iterable<S> source, Type<S> sourceType, Type<D> destinationType) {
        return mapAsArray(destination, source, sourceType, destinationType, new MappingContext());
    }
    
    public <S, D> D[] mapAsArray(D[] destination, S[] source, Type<S> sourceType, Type<D> destinationType) {
        return mapAsArray(destination, source, sourceType, destinationType, new MappingContext());
    }
    
    public <S, D> D[] mapAsArray(D[] destination, Iterable<S> source, Type<S> sourceType, Type<D> destinationType, MappingContext context) {
        
        if (source == null) {
            return null;
        }
        
        int i = 0;
        for (final S s : source) {
            destination[i++] = map(s, sourceType, destinationType);
        }
        return destination;
    }
    
    public <S, D> D[] mapAsArray(D[] destination, S[] source, Type<S> sourceType, Type<D> destinationType, MappingContext context) {
        
        if (source == null) {
            return null;
        }
        
        int i = 0;
        for (final S s : source) {
            destination[i++] = map(s, sourceType, destinationType, context);
        }
        return destination;
    }
    
    public <S, D> List<D> mapAsList(S[] source, Type<S> sourceType, Type<D> destinationType) {
        return mapAsList(source, sourceType, destinationType, new MappingContext());
    }
    
    public <S, D> List<D> mapAsList(S[] source, Type<S> sourceType, Type<D> destinationType, MappingContext context) {
        final List<D> destination = new ArrayList<D>(source.length);
        for (final S s : source) {
            destination.add(map(s, sourceType, destinationType, context));
        }
        return destination;
    }
    
    public <S, D> Set<D> mapAsSet(S[] source, Type<S> sourceType, Type<D> destinationType) {
        return mapAsSet(source, sourceType, destinationType, new MappingContext());
    }
    
    public <S, D> Set<D> mapAsSet(S[] source, Type<S> sourceType, Type<D> destinationType, MappingContext context) {
        final Set<D> destination = new HashSet<D>(source.length);
        for (final S s : source) {
            destination.add(map(s, sourceType, destinationType, context));
        }
        return destination;
    }
    
    Mapper<Object, Object> prepareMapper(Type<?> sourceType, Type<?> destinationType) {
        final MapperKey mapperKey = new MapperKey(sourceType, destinationType);
        final Mapper<Object, Object> mapper = mapperFactory.lookupMapper(mapperKey);
        
        if (mapper == null) {
            throw new IllegalStateException(String.format("Can not create a mapper for classes : %s, %s", destinationType, sourceType));
        }
        return mapper;
    }
    
    void mapDeclaredProperties(Object sourceObject, Object destinationObject, Type<?> sourceClass, Type<?> destinationType,
            MappingContext context, Mapper<Object, Object> mapper, MappingStrategyRecorder strategyBuilder) {
        
        if (mapper.getAType().equals(sourceClass)) {
            mapper.mapAtoB(sourceObject, destinationObject, context);
        } else if (mapper.getAType().equals(destinationType)) {
            mapper.mapBtoA(sourceObject, destinationObject, context);
            if (strategyBuilder != null)
                strategyBuilder.setMapReverse(true);
        } else if (mapper.getAType().isAssignableFrom(sourceClass)) {
            mapper.mapAtoB(sourceObject, destinationObject, context);
        } else if (mapper.getAType().isAssignableFrom(destinationType)) {
            mapper.mapBtoA(sourceObject, destinationObject, context);
            if (strategyBuilder != null)
                strategyBuilder.setMapReverse(true);
        } else {
            throw new IllegalStateException(String.format("Source object type's must be one of '%s' or '%s'.", mapper.getAType(),
                    mapper.getBType()));
            
        }
    }
    
    private <S, D> D newObject(S sourceObject, Type<? extends D> destinationType, MappingContext context, MappingStrategyRecorder strategyBuilder) {
        
        try {
            final ObjectFactory<? extends D> objectFactory = mapperFactory.lookupObjectFactory(destinationType);
            if (objectFactory != null) {
                if (strategyBuilder != null) {
                    strategyBuilder.setResolvedObjectFactory(objectFactory);
                }
                return objectFactory.create(sourceObject, context);
            } else {
                return destinationType.getRawType().newInstance();
            }
        } catch (final InstantiationException e) {
            throw new MappingException(e);
        } catch (final IllegalAccessException e) {
            throw new MappingException(e);
        }
    }
    
    public <S, D> D newObject(S sourceObject, Type<? extends D> destinationType, MappingContext context) {
        return newObject(sourceObject, destinationType, context, null);
    }
    
    <S, D> Collection<D> mapAsCollection(Iterable<S> source, Type<S> sourceType, Type<D> destinationType, Collection<D> destination,
            MappingContext context) {
        
        if (source == null) {
            return null;
        }
        
        for (final S item : source) {
            destination.add(map(item, sourceType, destinationType, context));
        }
        return destination;
    }
    
    @SuppressWarnings("unchecked")
    public <S, D> D convert(S source, Type<S> sourceType, Type<D> destinationType, String converterId) {
        final Type<? extends Object> sourceClass = normalizeSourceType(source, sourceType, destinationType);
        Converter<S, D> converter;
        ConverterFactory converterFactory = mapperFactory.getConverterFactory();
        if (converterId == null) {
            converter = (Converter<S, D>) converterFactory.getConverter(sourceClass, destinationType);
        } else {
            converter = (Converter<S, D>) converterFactory.getConverter(converterId);
        }
        
        return converter.convert(source, destinationType);
    }
    
    private <S, D> boolean canConvert(Type<S> sourceType, Type<D> destinationType) {
        return mapperFactory.getConverterFactory().canConvert(sourceType, destinationType);
    }
    
    public <S, D> D map(S sourceObject, Class<D> destinationClass) {
        return map(sourceObject, TypeFactory.typeOf(sourceObject), TypeFactory.<D>valueOf(destinationClass));
    }
    
    public <S, D> D map(S sourceObject, Class<D> destinationClass, MappingContext context) {
        return map(sourceObject, TypeFactory.typeOf(sourceObject), TypeFactory.<D>valueOf(destinationClass), context);
    }
    
    public <S, D> Set<D> mapAsSet(Iterable<S> source, Class<D> destinationClass) {
        return mapAsSet(source, TypeFactory.elementTypeOf(source), TypeFactory.<D>valueOf(destinationClass));
    }
    
    public <S, D> Set<D> mapAsSet(Iterable<S> source, Class<D> destinationClass, MappingContext context) {
        return mapAsSet(source, TypeFactory.elementTypeOf(source), TypeFactory.<D>valueOf(destinationClass), context);
    }
    
    public <S, D> Set<D> mapAsSet(S[] source, Class<D> destinationClass) {
        return mapAsSet(source, TypeFactory.componentTypeOf(source), TypeFactory.<D>valueOf(destinationClass));
    }
    
    public <S, D> Set<D> mapAsSet(S[] source, Class<D> destinationClass, MappingContext context) {
        return mapAsSet(source, TypeFactory.componentTypeOf(source), TypeFactory.<D>valueOf(destinationClass), context);
    }
    
    public <S, D> List<D> mapAsList(Iterable<S> source, Class<D> destinationClass) {
        return mapAsList(source, TypeFactory.elementTypeOf(source), TypeFactory.<D>valueOf(destinationClass));
    }
    
    public <S, D> List<D> mapAsList(Iterable<S> source, Class<D> destinationClass, MappingContext context) {
        return mapAsList(source, TypeFactory.elementTypeOf(source), TypeFactory.<D>valueOf(destinationClass), context);
    }
    
    public <S, D> List<D> mapAsList(S[] source, Class<D> destinationClass) {
        return mapAsList(source, TypeFactory.componentTypeOf(source), TypeFactory.<D>valueOf(destinationClass));
    }
    
    public <S, D> List<D> mapAsList(S[] source, Class<D> destinationClass, MappingContext context) {
        return mapAsList(source, TypeFactory.componentTypeOf(source), TypeFactory.<D>valueOf(destinationClass), context);
    }
    
    public <S, D> D[] mapAsArray(D[] destination, Iterable<S> source, Class<D> destinationClass) {
        return mapAsArray(destination, source, TypeFactory.elementTypeOf(source), TypeFactory.<D>valueOf(destinationClass));
    }
    
    public <S, D> D[] mapAsArray(D[] destination, S[] source, Class<D> destinationClass) {
        return mapAsArray(destination, source, TypeFactory.componentTypeOf(source), TypeFactory.<D>valueOf(destinationClass));
    }
    
    public <S, D> D[] mapAsArray(D[] destination, Iterable<S> source, Class<D> destinationClass, MappingContext context) {
        return mapAsArray(destination, source, TypeFactory.elementTypeOf(source), TypeFactory.<D>valueOf(destinationClass), context);
    }
    
    public <S, D> D[] mapAsArray(D[] destination, S[] source, Class<D> destinationClass, MappingContext context) {
        return mapAsArray(destination, source, TypeFactory.componentTypeOf(source), TypeFactory.<D>valueOf(destinationClass), context);
    }
    
    public <S, D> D convert(S source, Class<D> destinationClass, String converterId) {
        return convert(source, TypeFactory.typeOf(source), TypeFactory.<D>valueOf(destinationClass), converterId);
    }
    
    /*
     * New mapping type: Map to Map
     */
    public <Sk, Sv, Dk, Dv> Map<Dk,Dv> mapAsMap(Map<Sk,Sv> source, Type<? extends Map<Sk,Sv>> sourceType, Type<? extends Map<Dk,Dv>> destinationType) {
        return mapAsMap(source, sourceType, destinationType, new MappingContext());
    }
    
    public <Sk, Sv, Dk, Dv> Map<Dk,Dv> mapAsMap(Map<Sk,Sv> source, Type<? extends Map<Sk,Sv>> sourceType, Type<? extends Map<Dk,Dv>> destinationType, MappingContext context) {
        Map<Dk,Dv> destination = new HashMap<Dk,Dv>(source.size());
        for (Entry<Sk,Sv> entry: source.entrySet()) {
            Dk key = map(entry.getKey(), sourceType.<Sk>getNestedType(0), destinationType.<Dk>getNestedType(0), context);
            Dv value = map(entry.getValue(), sourceType.<Sv>getNestedType(1), destinationType.<Dv>getNestedType(1), context);
            destination.put(key, value);
        }
        return destination;
    }
    
    
    public <S, Dk, Dv> Map<Dk, Dv> mapAsMap(Iterable<S> source, Type<S> sourceType, Type<? extends Map<Dk, Dv>> destinationType) {
        return mapAsMap(source, sourceType, destinationType, new MappingContext());
    }
    
    public <S, Dk, Dv> Map<Dk, Dv> mapAsMap(Iterable<S> source, Type<S> sourceType, Type<? extends Map<Dk, Dv>> destinationType, MappingContext context) {
        
        Map<Dk,Dv> destination = new HashMap<Dk,Dv>();
        
        for (S element: source) {
            Type<?> entryType = TypeFactory.valueOf(Map.Entry.class, destinationType.getNestedType(0), destinationType.getNestedType(1));
            @SuppressWarnings("unchecked")
            Map.Entry<Dk, Dv> entry = (Map.Entry<Dk, Dv>) map(element, sourceType, entryType, context);
            destination.put(entry.getKey(), entry.getValue());
        }
        
        return destination;
    }
    
    public <S, Dk, Dv> Map<Dk, Dv> mapAsMap(S[] source, Type<S> sourceType, Type<? extends Map<Dk, Dv>> destinationType) {
        return mapAsMap(source, sourceType, destinationType, new MappingContext());
    }
    
    public <S, Dk, Dv> Map<Dk, Dv> mapAsMap(S[] source, Type<S> sourceType, Type<? extends Map<Dk, Dv>> destinationType, MappingContext context) {
        
        Map<Dk,Dv> destination = new HashMap<Dk,Dv>();
        
        for (S element: source) {
            Type<MapEntry<Dk, Dv>> entryType = MapEntry.concreteEntryType(destinationType);
            MapEntry<Dk, Dv> entry = (MapEntry<Dk, Dv>) map(element, sourceType, entryType, context);
            destination.put(entry.getKey(), entry.getValue());
        }
        
        return destination;
    }
    
    
    /*
     * New mapping type: Map to List, Set or Array
     */
    public <Sk, Sv, D> List<D> mapAsList(Map<Sk, Sv> source, Type<? extends Map<Sk, Sv>> sourceType, Type<D> destinationType) {
        return mapAsList(source, sourceType, destinationType, new MappingContext());
    }
    
    public <Sk, Sv, D> List<D> mapAsList(Map<Sk, Sv> source, Type<? extends Map<Sk, Sv>> sourceType, Type<D> destinationType, MappingContext context) {
        /*
         * Use map as collection to map the entry set to a list;
         * requires an existing mapping for Map.Entry to to type D.
         */
        List<D> destination = new ArrayList<D>(source.size());
        
        Type<MapEntry<Sk,Sv>> entryType = MapEntry.concreteEntryType(sourceType);
        
        return (List<D>) mapAsCollection(MapEntry.entrySet(source), entryType, destinationType, destination, context);
    }
    
    
    public <Sk, Sv, D> Set<D> mapAsSet(Map<Sk, Sv> source, Type<? extends Map<Sk, Sv>> sourceType, Type<D> destinationType) {
        return mapAsSet(source, sourceType, destinationType, new MappingContext());
    }
    
    public <Sk, Sv, D> Set<D> mapAsSet(Map<Sk, Sv> source, Type<? extends Map<Sk, Sv>> sourceType, Type<D> destinationType, MappingContext context) {
        /*
         * Use map as collection to map the entry set to a list;
         * requires an existing mapping for Map.Entry to to type D.
         */
        Set<D> destination = new HashSet<D>(source.size());
        Type<Entry<Sk,Sv>> entryType = TypeFactory.resolveTypeOf(source.entrySet(), sourceType).getNestedType(0);
        return (Set<D>) mapAsCollection(source.entrySet(), entryType, destinationType, destination, context);
    }
    
    public <Sk, Sv, D> D[] mapAsArray(D[] destination, Map<Sk, Sv> source, Type<? extends Map<Sk, Sv>> sourceType, Type<D> destinationType) {
        return mapAsArray(destination, source, sourceType, destinationType, new MappingContext());
    }
    
    public <Sk, Sv, D> D[] mapAsArray(D[] destination, Map<Sk, Sv> source, Type<? extends Map<Sk, Sv>> sourceType, Type<D> destinationType, MappingContext context) {
       
        Type<MapEntry<Sk,Sv>> entryType = MapEntry.concreteEntryType(sourceType);
        
        return mapAsArray(destination, MapEntry.entrySet(source), entryType, destinationType, context);
    }
    
}
