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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import ma.glasnost.orika.DefaultFieldMapper;
import ma.glasnost.orika.Mapper;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.MappingException;
import ma.glasnost.orika.ObjectFactory;
import ma.glasnost.orika.constructor.ConstructorResolverStrategy;
import ma.glasnost.orika.converter.ConverterFactory;
import ma.glasnost.orika.impl.generator.CompilerStrategy;
import ma.glasnost.orika.impl.generator.CompilerStrategy.SourceCodeGenerationException;
import ma.glasnost.orika.impl.generator.MapperGenerator;
import ma.glasnost.orika.impl.generator.ObjectFactoryGenerator;
import ma.glasnost.orika.impl.util.ClassUtil;
import ma.glasnost.orika.inheritance.DefaultSuperTypeResolverStrategy;
import ma.glasnost.orika.inheritance.SuperTypeResolverStrategy;
import ma.glasnost.orika.metadata.ClassMap;
import ma.glasnost.orika.metadata.ClassMapBuilder;
import ma.glasnost.orika.metadata.MapperKey;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.metadata.TypeFactory;
import ma.glasnost.orika.unenhance.BaseUnenhancer;
import ma.glasnost.orika.unenhance.UnenhanceStrategy;

/**
 * The mapper factory is the heart of Orika, a small container where metadata
 * are stored, it's used by other components, to look up for generated mappers,
 * converters, object factories ... etc.
 * 
 * @author S.M. El Aatifi
 * 
 */
public class DefaultMapperFactory implements MapperFactory {
    
    private final MapperFacade mapperFacade;
    private final MapperGenerator mapperGenerator;
    private final ObjectFactoryGenerator objectFactoryGenerator;
    
    private final Map<MapperKey, ClassMap<Object, Object>> classMapRegistry;
    private final Map<MapperKey, GeneratedMapperBase> mappersRegistry;
    private final ConcurrentHashMap<Type<? extends Object>, ObjectFactory<? extends Object>> objectFactoryRegistry;
    private final Map<Type<?>, Set<Type<?>>> aToBRegistry;
    private final Map<Type<?>, Type<?>> mappedConverters;
    private final List<DefaultFieldMapper> defaultFieldMappers;
    private final UnenhanceStrategy unenhanceStrategy;
    private final ConverterFactory converterFactory;
    private final CompilerStrategy compilerStrategy;
    private volatile boolean isBuilt = false;
    
    private final ConcurrentHashMap<WeakReference<Object>, Lock> locks = new ConcurrentHashMap<WeakReference<Object>, Lock>();
    
    /**
     * Place-holder object factory used to represent the default constructor in
     * registry lookup; prevents repeated lookup of constructor
     */
    private static final ObjectFactory<Object> USE_DEFAULT_CONSTRUCTOR = new ObjectFactory<Object>() {
        public Object create(Object source, MappingContext context) {
            return null;
        }
    };
    
    private final Map<MapperKey, Set<ClassMap<Object, Object>>> usedMapperMetadataRegistry;
    
    private DefaultMapperFactory(Set<ClassMap<?, ?>> classMaps, UnenhanceStrategy delegateStrategy,
            SuperTypeResolverStrategy superTypeStrategy, ConstructorResolverStrategy constructorResolverStrategy,
            ConverterFactory converterFactory, CompilerStrategy compilerStrategy) {
        
        this.converterFactory = converterFactory;
        this.compilerStrategy = compilerStrategy;
        this.classMapRegistry = new ConcurrentHashMap<MapperKey, ClassMap<Object, Object>>();
        this.mappersRegistry = new ConcurrentHashMap<MapperKey, GeneratedMapperBase>();
        this.aToBRegistry = new ConcurrentHashMap<Type<?>, Set<Type<?>>>();
        // TODO: note that this map is now always empty because the ConverterFactory
        // now contains all mapped converters; thus it should be removed -- but that
        // also means that super-type resolution is ignoring converters (which is a problem)
        this.mappedConverters = new ConcurrentHashMap<Type<?>, Type<?>>();
        this.usedMapperMetadataRegistry = new ConcurrentHashMap<MapperKey, Set<ClassMap<Object, Object>>>();
        this.objectFactoryRegistry = new ConcurrentHashMap<Type<? extends Object>, ObjectFactory<? extends Object>>();
        this.defaultFieldMappers = new CopyOnWriteArrayList<DefaultFieldMapper>();
        this.unenhanceStrategy = buildUnenhanceStrategy(delegateStrategy, superTypeStrategy);
        this.mapperFacade = new MapperFacadeImpl(this, unenhanceStrategy);
        
        if (classMaps != null) {
            for (final ClassMap<?, ?> classMap : classMaps) {
                registerClassMap(classMap);
            }
        }
        
        this.mapperGenerator = new MapperGenerator(this, compilerStrategy);
        this.objectFactoryGenerator = new ObjectFactoryGenerator(this, constructorResolverStrategy, compilerStrategy);
    }
    
    /**
     * Use this builder to generate instances of DefaultMapperFactory with the
     * desired customizations.<br>
     * <br>
     * 
     * For example, an instance with no customizations could be generated with
     * the following code:
     * 
     * <pre>
     * {
     *     &#064;code
     *     MapperFactory factory = new DefaultMapperFactory.Builder().build();
     * }
     * </pre>
     * 
     * @author matt.deboer@gmail.com
     */
    public static final class Builder {
        private UnenhanceStrategy unenhanceStrategy;
        private SuperTypeResolverStrategy superTypeStrategy;
        private ConstructorResolverStrategy constructorResolverStrategy;
        private CompilerStrategy compilerStrategy;
        private Set<ClassMap<?, ?>> classMaps;
        private ConverterFactory converterFactory;
        
        public Builder classMaps(Set<ClassMap<?, ?>> classMaps) {
            this.classMaps = classMaps;
            return this;
        }
        
        public Builder unenhanceStrategy(UnenhanceStrategy unenhanceStrategy) {
            this.unenhanceStrategy = unenhanceStrategy;
            return this;
        }
        
        public Builder superTypeResolverStrategy(SuperTypeResolverStrategy superTypeStrategy) {
            this.superTypeStrategy = superTypeStrategy;
            return this;
        }
        
        public Builder constructorResolverStrategy(ConstructorResolverStrategy constructorResolverStrategy) {
            this.constructorResolverStrategy = constructorResolverStrategy;
            return this;
        }
        
        public Builder converterFactory(ConverterFactory converterFactory) {
            this.converterFactory = converterFactory;
            return this;
        }
        
        public Builder compilerStrategy(CompilerStrategy compilerStrategy) {
            this.compilerStrategy = compilerStrategy;
            return this;
        }
        
        public DefaultMapperFactory build() {
            
            if (converterFactory == null) {
                converterFactory = UtilityResolver.getDefaultConverterFactory();
            }
            if (constructorResolverStrategy == null) {
                constructorResolverStrategy = UtilityResolver.getDefaultConstructorResolverStrategy();
            }
            if (compilerStrategy == null) {
                compilerStrategy = UtilityResolver.getDefaultCompilerStrategy();
            }
            
            return new DefaultMapperFactory(classMaps, unenhanceStrategy, superTypeStrategy, constructorResolverStrategy, converterFactory,
                    compilerStrategy);
        }
        
    }
    
    /**
     * Gets a lock(ed) lock to be used for a given object
     * 
     * @param object
     * @return
     */
    private Lock getLock(Object object) {
    	WeakReference<Object> ref = new WeakReference<Object>(object);
    	Lock lock = locks.get(ref);
    	if (lock == null) {
    		lock = new ReentrantLock();
    		Lock existingLock = locks.putIfAbsent(ref, lock);
    		if (existingLock != null) {
    			lock = existingLock;
    		}
    	}
    	lock.lock();
    	return lock;
    }
    
    
    /**
     * Generates the UnenhanceStrategy to be used for this MapperFactory,
     * applying the passed delegateStrategy if not null.
     * 
     * @param unenhanceStrategy
     * @param overrideDefaultUnenhanceBehavior
     *            true if the passed UnenhanceStrategy should take full
     *            responsibility for un-enhancement; false if the default
     *            behavior should be applied as a fail-safe after consulting the
     *            passed strategy.
     * 
     * @return
     */
    protected UnenhanceStrategy buildUnenhanceStrategy(UnenhanceStrategy unenhanceStrategy, SuperTypeResolverStrategy superTypeStrategy) {
        
        BaseUnenhancer unenhancer = new BaseUnenhancer();
        
        if (unenhanceStrategy != null) {
            unenhancer.addUnenhanceStrategy(unenhanceStrategy);
        } 
        
        if (superTypeStrategy != null) {
            unenhancer.addSuperTypeResolverStrategy(superTypeStrategy);
        } else {
            
            /*
             * This strategy attempts to lookup super-type that has a registered
             * mapper or converter whenever it is offered a class that is not
             * currently mapped
             */
            final SuperTypeResolverStrategy registeredMappersStrategy = new DefaultSuperTypeResolverStrategy() {
                
                @Override
                public boolean isAcceptable(Type<?> type) {
                    return type != null && (aToBRegistry.containsKey(type) || mappedConverters.containsKey(type));
                }
            };
            
            unenhancer.addSuperTypeResolverStrategy(registeredMappersStrategy);
        }
        
        /*
         * This strategy produces super-types whenever the proposed class type
         * is not accessible to the compilerStrategy and/or the current thread
         * context class-loader; it is added last as a fail-safe in case a
         * suggested type cannot be used. It is automatically included, as
         * there's no case when skipping it would be desired....
         */
        final SuperTypeResolverStrategy inaccessibleTypeStrategy = new DefaultSuperTypeResolverStrategy() {
            
            /**
             * Tests whether the specified type is accessible to both the
             * current thread's class-loader, and also to the compilerStrategy.
             * 
             * @param type
             * @return
             */
            public boolean isTypeAccessible(Type<?> type) {
                
                try {
                    Class<?> loadedType = Thread.currentThread().getContextClassLoader().loadClass(type.getName());
                    if (!type.getRawType().equals(loadedType)) {
                        return false;
                    }
                    compilerStrategy.assureTypeIsAccessible(type.getRawType());
                    return true;
                } catch (ClassNotFoundException e) {
                    return false;
                } catch (SourceCodeGenerationException e) {
                    return false;
                }
            }
            
            @Override
            public boolean isAcceptable(Type<?> type) {
                return isTypeAccessible(type) && !java.lang.reflect.Proxy.class.equals(type.getRawType());
            }
            
        };
        
        unenhancer.addSuperTypeResolverStrategy(inaccessibleTypeStrategy);
        
        return unenhancer;
        
    }
    
    public GeneratedMapperBase lookupMapper(MapperKey mapperKey) {
        if (!mappersRegistry.containsKey(mapperKey)) {
            Lock lock = getLock(mapperKey);
            if (!mappersRegistry.containsKey(mapperKey)) {
	        	final ClassMap<?, ?> classMap = ClassMapBuilder.map(mapperKey.getAType(), mapperKey.getBType())
	                    .byDefault(this.defaultFieldMappers.toArray(new DefaultFieldMapper[0]))
	                    .toClassMap();
	            buildObjectFactories(classMap);
	            buildMapper(classMap);
	            initializeUsedMappers(classMap);
            }
            lock.unlock();
        }
        return mappersRegistry.get(mapperKey);
    }
    
    public MapperFacade getMapperFacade() {
        if (!isBuilt) {
            synchronized (mapperFacade) {
                if (!isBuilt) {
                    build();
                }
            }
        }
        return mapperFacade;
    }
    
    public <D> void registerObjectFactory(ObjectFactory<D> objectFactory, Type<D> destinationType) {
        objectFactoryRegistry.put(destinationType, objectFactory);
    }
    
    @Deprecated
    public void registerMappingHint(ma.glasnost.orika.MappingHint... hints) {
        
        DefaultFieldMapper[] mappers = new DefaultFieldMapper[hints.length];
        for (int i = 0, len = hints.length; i < len; ++i) {
            mappers[i] = new ma.glasnost.orika.MappingHint.DefaultFieldMappingConverter(hints[i]);
        }
        registerDefaultFieldMapper(mappers);
    }
    
    public void registerDefaultFieldMapper(DefaultFieldMapper... mappers) {
        this.defaultFieldMappers.addAll(Arrays.asList(mappers));
    }
    
    @SuppressWarnings("unchecked")
    public <T> ObjectFactory<T> lookupObjectFactory(Type<T> targetType) {
        if (targetType == null) {
            return null;
        }
        
        ObjectFactory<T> result = (ObjectFactory<T>) objectFactoryRegistry.get(targetType);
        if (result == null) {
        	Lock lock = getLock(targetType);
        	// Check if we can use default constructor...
            try {
                targetType.getRawType().getConstructor(/* new Class[0] */);
                // Mark the class with null value in the registry
                // to avoid repeating the getConstructor call
                objectFactoryRegistry.put(targetType, USE_DEFAULT_CONSTRUCTOR);
            } catch (Exception e) {
                // Generate an object factory
                synchronized (objectFactoryGenerator) {
                    result = (ObjectFactory<T>) objectFactoryGenerator.build(targetType);
                    objectFactoryRegistry.put(targetType, result);
                }
            }
            lock.unlock();
        } else if (USE_DEFAULT_CONSTRUCTOR.equals(result)) {
            result = null;
        }
        
        return result;
    }
    
    @SuppressWarnings("unchecked")
    public <S, D> Type<? extends D> lookupConcreteDestinationType(Type<S> sourceType, Type<D> destinationType, MappingContext context) {
        final Type<? extends D> concreteType = context.getConcreteClass(sourceType, destinationType);
        
        if (concreteType != null) {
            return concreteType;
        }
        
        Set<Type<?>> destinationSet = aToBRegistry.get(sourceType);
        if (destinationSet == null || destinationSet.isEmpty()) {
            return null;
        }
        
        for (final Type<?> type : destinationSet) {
            if (destinationType.isAssignableFrom(type) && ClassUtil.isConcrete(type)) {
                return (Type<? extends D>) type;
            }
        }
        
        return concreteType;
    }
    
    @SuppressWarnings("unchecked")
    public <A, B> void registerClassMap(ClassMap<A, B> classMap) {
        classMapRegistry.put(new MapperKey(classMap.getAType(), classMap.getBType()), (ClassMap<Object, Object>) classMap);
    }
    
    public synchronized void build() {
        
    	isBuilt = true;
        
        buildClassMapRegistry();
        
        for (final ClassMap<?, ?> classMap : classMapRegistry.values()) {
            buildMapper(classMap);
        }
        
        for (final ClassMap<?, ?> classMap : classMapRegistry.values()) {
            buildObjectFactories(classMap);
            initializeUsedMappers(classMap);
        }
        
    }
    
    public Set<ClassMap<Object, Object>> lookupUsedClassMap(MapperKey mapperKey) {
        Set<ClassMap<Object, Object>> usedClassMapSet = usedMapperMetadataRegistry.get(mapperKey);
        if (usedClassMapSet == null) {
            usedClassMapSet = Collections.emptySet();
        }
        return usedClassMapSet;
    }
    
    private void buildClassMapRegistry() {
        // prepare a map for classmap (stored as set)
        Map<MapperKey, ClassMap<Object, Object>> classMapsDictionary = new HashMap<MapperKey, ClassMap<Object, Object>>();
        
        Set<ClassMap<Object, Object>> classMaps = new HashSet<ClassMap<Object, Object>>(classMapRegistry.values());
        
        for (final ClassMap<Object, Object> classMap : classMaps) {
            classMapsDictionary.put(new MapperKey(classMap.getAType(), classMap.getBType()), classMap);
        }
        
        for (final ClassMap<?, ?> classMap : classMaps) {
            MapperKey key = new MapperKey(classMap.getAType(), classMap.getBType());
            
            Set<ClassMap<Object, Object>> usedClassMapSet = new HashSet<ClassMap<Object, Object>>();
            
            for (final MapperKey parentMapperKey : classMap.getUsedMappers()) {
                ClassMap<Object, Object> usedClassMap = classMapsDictionary.get(parentMapperKey);
                if (usedClassMap == null) {
                    throw new MappingException("Cannot find class mapping using mapper : " + classMap.getMapperClassName());
                }
                usedClassMapSet.add(usedClassMap);
            }
            usedMapperMetadataRegistry.put(key, usedClassMapSet);
        }
        
    }
    
    @SuppressWarnings({ "unchecked" })
    private <S, D> void buildObjectFactories(ClassMap<S, D> classMap) {
        Type<?> aType = classMap.getAType();
        Type<?> bType = classMap.getBType();
        if (classMap.getConstructorA() != null && lookupObjectFactory(aType) == null) {
            GeneratedObjectFactory objectFactory = objectFactoryGenerator.build(aType);
            registerObjectFactory(objectFactory, (Type<Object>) aType);
        }
        
        if (classMap.getConstructorB() != null && lookupObjectFactory(bType) == null) {
            GeneratedObjectFactory objectFactory = objectFactoryGenerator.build(bType);
            registerObjectFactory(objectFactory, (Type<Object>) bType);
        }
    }
    
    private static final Comparator<MapperKey> mapperComparator = new Comparator<MapperKey>() {
        
        public int compare(MapperKey key1, MapperKey key2) {
            if (key1.getAType().isAssignableFrom(key2.getAType()) && key1.getBType().isAssignableFrom(key2.getBType())) {
                return 1;
            } else if (key2.getAType().isAssignableFrom(key1.getAType()) && key2.getBType().isAssignableFrom(key1.getBType())) {
                return -1;
            } else if (key1.getAType().equals(key2.getAType()) && key1.getBType().equals(key2.getBType())) {
                return 0;
            } else {
                throw new IllegalArgumentException("keys " + key1 + " and " + key2 + " are unrelated");
            }
        }
    };
    
    @SuppressWarnings("unchecked")
    private void initializeUsedMappers(ClassMap<?, ?> classMap) {
        
        Mapper<Object, Object> mapper = lookupMapper(new MapperKey(classMap.getAType(), classMap.getBType()));
        
        List<Mapper<Object, Object>> parentMappers = new ArrayList<Mapper<Object, Object>>();
        
        if (!classMap.getUsedMappers().isEmpty()) {
            for (MapperKey parentMapperKey : classMap.getUsedMappers()) {
                collectUsedMappers(classMap, parentMappers, parentMapperKey);
            }
        } else {
            /*
             * Attempt to auto-determine used mappers for this classmap;
             * however, we should only add the most-specific of the available
             * mappers to avoid calling the same mapper multiple times during a
             * single map request
             */
            Set<MapperKey> usedMappers = new TreeSet<MapperKey>(mapperComparator);
            for (MapperKey key : this.classMapRegistry.keySet()) {
                if (!key.getAType().equals(classMap.getAType()) || !key.getBType().equals(classMap.getBType())) {
                    if (key.getAType().isAssignableFrom(classMap.getAType()) && key.getBType().isAssignableFrom(classMap.getBType())) {
                        usedMappers.add(key);
                    }
                }
            }
            if (!usedMappers.isEmpty()) {
                // Set<ClassMap<Object, Object>> usedClassMapSet = new
                // HashSet<ClassMap<Object, Object>>();
                MapperKey parentKey = usedMappers.iterator().next();
                // usedClassMapSet.add(classMapRegistry.get(parentKey));
                // usedMapperMetadataRegistry.put(parentKey, usedClassMapSet);
                collectUsedMappers(classMap, parentMappers, parentKey);
            }
        }
        
        mapper.setUsedMappers(parentMappers.toArray(new Mapper[parentMappers.size()]));
    }
    
    private void collectUsedMappers(ClassMap<?, ?> classMap, List<Mapper<Object, Object>> parentMappers, MapperKey parentMapperKey) {
        Mapper<Object, Object> parentMapper = lookupMapper(parentMapperKey);
        if (parentMapper == null) {
            throw new MappingException("Cannot find used mappers for : " + classMap.getMapperClassName());
        }
        parentMappers.add(parentMapper);
        
        Set<ClassMap<Object, Object>> usedClassMapSet = usedMapperMetadataRegistry.get(parentMapperKey);
        if (usedClassMapSet != null) {
	        for (ClassMap<Object, Object> cm : usedClassMapSet) {
	            collectUsedMappers(cm, parentMappers, new MapperKey(cm.getAType(), cm.getBType()));
	        }
        }
    }
    
    @SuppressWarnings("unchecked")
    private void buildMapper(ClassMap<?, ?> classMap) {
        register(classMap.getAType(), classMap.getBType());
        register(classMap.getBType(), classMap.getAType());
        
        final MapperKey mapperKey = new MapperKey(classMap.getAType(), classMap.getBType());
        final GeneratedMapperBase mapper = this.mapperGenerator.build(classMap);
        mapper.setMapperFacade(mapperFacade);
        if (classMap.getCustomizedMapper() != null) {
            final Mapper<Object, Object> customizedMapper = (Mapper<Object, Object>) classMap.getCustomizedMapper();
            mapper.setCustomMapper(customizedMapper);
        }
        mappersRegistry.put(mapperKey, mapper);
        classMapRegistry.put(mapperKey, (ClassMap<Object, Object>) classMap);
    }
    
    private <S, D> void register(Type<S> sourceClass, Type<D> destinationClass) {
        Set<Type<?>> destinationSet = aToBRegistry.get(sourceClass);
        if (destinationSet == null) {
            destinationSet = new HashSet<Type<?>>();
            aToBRegistry.put(sourceClass, destinationSet);
        }
        destinationSet.add(destinationClass);
    }
    
    @SuppressWarnings("unchecked")
    public <A, B> ClassMap<A, B> getClassMap(MapperKey mapperKey) {
        return (ClassMap<A, B>) classMapRegistry.get(mapperKey);
    }
    
    public Set<Type<? extends Object>> lookupMappedClasses(Type<?> type) {
        return aToBRegistry.get(type);
    }
    
    public ConverterFactory getConverterFactory() {
        return converterFactory;
    }
    
    public <T> void registerObjectFactory(ObjectFactory<T> objectFactory, Class<T> targetClass) {
        registerObjectFactory(objectFactory, TypeFactory.valueOf(targetClass));
    }
    
}
