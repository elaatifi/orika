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

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import ma.glasnost.orika.DefaultFieldMapper;
import ma.glasnost.orika.MapEntry;
import ma.glasnost.orika.Mapper;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.MappingException;
import ma.glasnost.orika.ObjectFactory;
import ma.glasnost.orika.constructor.ConstructorResolverStrategy;
import ma.glasnost.orika.converter.ConverterFactory;
import ma.glasnost.orika.converter.builtin.BuiltinConverters;
import ma.glasnost.orika.impl.generator.CompilerStrategy;
import ma.glasnost.orika.impl.generator.CompilerStrategy.SourceCodeGenerationException;
import ma.glasnost.orika.impl.generator.MapperGenerator;
import ma.glasnost.orika.impl.generator.ObjectFactoryGenerator;
import ma.glasnost.orika.impl.util.ClassUtil;
import ma.glasnost.orika.inheritance.DefaultSuperTypeResolverStrategy;
import ma.glasnost.orika.inheritance.SuperTypeResolverStrategy;
import ma.glasnost.orika.metadata.ClassMap;
import ma.glasnost.orika.metadata.ClassMapBuilder;
import ma.glasnost.orika.metadata.ClassMapBuilderFactory;
import ma.glasnost.orika.metadata.MapperKey;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.metadata.TypeFactory;
import ma.glasnost.orika.property.PropertyResolverStrategy;
import ma.glasnost.orika.unenhance.BaseUnenhancer;
import ma.glasnost.orika.unenhance.UnenhanceStrategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The mapper factory is the heart of Orika, a small container where metadata
 * are stored, it's used by other components, to look up for generated mappers,
 * converters, object factories ... etc.
 * 
 * @author S.M. El Aatifi
 * 
 */
public class DefaultMapperFactory implements MapperFactory {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultMapperFactory.class);
    
    private final MapperFacade mapperFacade;
    private final MapperGenerator mapperGenerator;
    private final ObjectFactoryGenerator objectFactoryGenerator;
    
    private final Map<MapperKey, ClassMap<Object, Object>> classMapRegistry;
    private final Map<MapperKey, Mapper<?, ?>> mappersRegistry;
    private final ConcurrentHashMap<Type<? extends Object>, ObjectFactory<? extends Object>> objectFactoryRegistry;
    private final Map<Type<?>, Set<Type<?>>> aToBRegistry;
    private final List<DefaultFieldMapper> defaultFieldMappers;
    private final UnenhanceStrategy unenhanceStrategy;
    private final ConverterFactory converterFactory;
    private final CompilerStrategy compilerStrategy;
    private final PropertyResolverStrategy propertyResolverStrategy;
    private final Map<java.lang.reflect.Type, Type<?>> concreteTypeRegistry;
    private final ClassMapBuilderFactory classMapBuilderFactory;
    private final Map<MapperKey, Set<ClassMap<Object, Object>>> usedMapperMetadataRegistry;
    
    private final boolean useAutoMapping;
    private volatile boolean isBuilt = false;
    private volatile boolean isBuilding = false;
    
    /**
     * Place-holder object factory used to represent the default constructor in
     * registry lookup; prevents repeated lookup of constructor
     */
    private static final ObjectFactory<Object> USE_DEFAULT_CONSTRUCTOR = new ObjectFactory<Object>() {
        public Object create(Object source, MappingContext context) {
            return null;
        }
    };
    
    /**
     * Constructs a new instance of DefaultMapperFactory
     * 
     * @param builder
     */
    protected DefaultMapperFactory(MapperFactoryBuilder<?, ?> builder) {
        
        this.converterFactory = builder.converterFactory;
        this.compilerStrategy = builder.compilerStrategy;
        this.classMapRegistry = new ConcurrentHashMap<MapperKey, ClassMap<Object, Object>>();
        this.mappersRegistry = new TreeMap<MapperKey, Mapper<?, ?>>();
        this.aToBRegistry = new ConcurrentHashMap<Type<?>, Set<Type<?>>>();
        this.usedMapperMetadataRegistry = new ConcurrentHashMap<MapperKey, Set<ClassMap<Object, Object>>>();
        this.objectFactoryRegistry = new ConcurrentHashMap<Type<? extends Object>, ObjectFactory<? extends Object>>();
        this.defaultFieldMappers = new CopyOnWriteArrayList<DefaultFieldMapper>();
        this.unenhanceStrategy = buildUnenhanceStrategy(builder.unenhanceStrategy, builder.superTypeStrategy);
        this.mapperFacade = new MapperFacadeImpl(this, unenhanceStrategy);
        this.concreteTypeRegistry = new ConcurrentHashMap<java.lang.reflect.Type, Type<?>>();
        
        if (builder.classMaps != null) {
            for (final ClassMap<?, ?> classMap : builder.classMaps) {
                registerClassMap(classMap);
            }
        }
        
        this.propertyResolverStrategy = builder.propertyResolverStrategy;
        this.classMapBuilderFactory = builder.classMapBuilderFactory;
        this.classMapBuilderFactory.setPropertyResolver(this.propertyResolverStrategy);
        this.mapperGenerator = new MapperGenerator(this, builder.compilerStrategy);
        this.objectFactoryGenerator = new ObjectFactoryGenerator(this, builder.constructorResolverStrategy, builder.compilerStrategy);
        this.useAutoMapping = builder.useAutoMapping;
        
        if (builder.useBuiltinConverters) {
            BuiltinConverters.register(converterFactory);
        }
        
        /*
         * Register default concrete types for common collection types;
         * these can be overridden as needed by user code.
         */
        this.registerConcreteType(Collection.class, ArrayList.class);
        this.registerConcreteType(List.class, ArrayList.class);
        this.registerConcreteType(Set.class, LinkedHashSet.class);
        this.registerConcreteType(Map.class, LinkedHashMap.class);
        this.registerConcreteType(Map.Entry.class, MapEntry.class);
    }
    
    /**
     * MapperFactoryBuilder provides an extensible Builder definition usable for
     * providing your own Builder class for subclasses of DefaultMapperFactory.<br>
     * <br>
     * 
     * See the defined {@link Builder} below for example of how to subclass.
     * 
     * @author matt.deboer@gmail.com
     * 
     * @param <F>
     * @param <B>
     */
    public static abstract class MapperFactoryBuilder<F extends DefaultMapperFactory, B extends MapperFactoryBuilder<F, B>> {
        
        /**
         * The UnenhanceStrategy configured for the MapperFactory
         */
        protected UnenhanceStrategy unenhanceStrategy;
        /**
         * The SuperTypeResolverStrategy configured for the MapperFactory
         */
        protected SuperTypeResolverStrategy superTypeStrategy;
        /**
         * The ConstructorResolverStrategy configured for the MapperFactory
         */
        protected ConstructorResolverStrategy constructorResolverStrategy;
        /**
         * The CompilerStrategy configured for the MapperFactory
         */
        protected CompilerStrategy compilerStrategy;
        /**
         * The class maps configured to initialize the MapperFactory
         */
        protected Set<ClassMap<?, ?>> classMaps;
        /**
         * The ConverterFactory configured for the MapperFactory
         */
        protected ConverterFactory converterFactory;
        /**
         * The PropertyResolverStrategy configured for the MapperFactory
         */
        protected PropertyResolverStrategy propertyResolverStrategy;
        /**
         * The ClassMapBuilderFactory configured for the MapperFactory
         */
        protected ClassMapBuilderFactory classMapBuilderFactory;
        /**
         * The configured value of whether or not to use built-in converters for
         * the MapperFactory
         */
        protected boolean useBuiltinConverters = false;
        /**
         * The configured value of whether or not to use auto-mapping for the
         * MapperFactory
         */
        protected boolean useAutoMapping = true;
        
        /**
         * Instantiates a new MapperFactoryBuilder
         */
        public MapperFactoryBuilder() {
            converterFactory = UtilityResolver.getDefaultConverterFactory();
            constructorResolverStrategy = UtilityResolver.getDefaultConstructorResolverStrategy();
            compilerStrategy = UtilityResolver.getDefaultCompilerStrategy();
            propertyResolverStrategy = UtilityResolver.getDefaultPropertyResolverStrategy();
            classMapBuilderFactory = UtilityResolver.getDefaultClassMapBuilderFactory();
        }
        
        /**
         * @return an appropriately type-cast reference to <code>this</code>
         *         MapperFactoryBuilder
         */
        protected abstract B self();
        
        /**
         * Set the class maps to be used in initializing this mapper factory
         * 
         * @param classMaps
         * @return a reference to <code>this</code> MapperFactoryBuilder
         */
        public B classMaps(Set<ClassMap<?, ?>> classMaps) {
            this.classMaps = classMaps;
            return self();
        }
        
        /**
         * Configure the UnenhanceStrategy to use with the generated
         * MapperFactory
         * 
         * @param unenhanceStrategy
         * @return a reference to <code>this</code> MapperFactoryBuilder
         */
        public B unenhanceStrategy(UnenhanceStrategy unenhanceStrategy) {
            this.unenhanceStrategy = unenhanceStrategy;
            return self();
        }
        
        /**
         * Configure the SuperTypeResolverStrategy to use with the generated
         * MapperFactory
         * 
         * @param superTypeStrategy
         * @return a reference to <code>this</code> MapperFactoryBuilder
         */
        public B superTypeResolverStrategy(SuperTypeResolverStrategy superTypeStrategy) {
            this.superTypeStrategy = superTypeStrategy;
            return self();
        }
        
        /**
         * Configure the ConstructorResolverStrategy to use with the generated
         * MapperFactory
         * 
         * @param constructorResolverStrategy
         * @return a reference to <code>this</code> MapperFactoryBuilder
         */
        public B constructorResolverStrategy(ConstructorResolverStrategy constructorResolverStrategy) {
            this.constructorResolverStrategy = constructorResolverStrategy;
            return self();
        }
        
        /**
         * Configure the ConverterFactory to use with the generated
         * MapperFactory
         * 
         * @param converterFactory
         * @return a reference to <code>this</code> MapperFactoryBuilder
         */
        public B converterFactory(ConverterFactory converterFactory) {
            this.converterFactory = converterFactory;
            return self();
        }
        
        /**
         * Configure the CompilerStrategy to use with the generated
         * MapperFactory
         * 
         * @param compilerStrategy
         * @return a reference to <code>this</code> MapperFactoryBuilder
         */
        public B compilerStrategy(CompilerStrategy compilerStrategy) {
            this.compilerStrategy = compilerStrategy;
            return self();
        }
        
        /**
         * Configure the PropertyResolverStrategy to use with the generated
         * MapperFactory
         * 
         * @param propertyResolverStrategy
         * @return a reference to <code>this</code> MapperFactoryBuilder
         */
        public B propertyResolverStrategy(PropertyResolverStrategy propertyResolverStrategy) {
            this.propertyResolverStrategy = propertyResolverStrategy;
            return self();
        }
        
        /**
         * Configure the ClassMapBuilderFactory to use with the generated
         * MapperFactory
         * 
         * @param classMapBuilderFactory
         * @return a reference to <code>this</code> MapperFactoryBuilder
         */
        public B classMapBuilderFactory(ClassMapBuilderFactory classMapBuilderFactory) {
            this.classMapBuilderFactory = classMapBuilderFactory;
            return self();
        }
        
        /**
         * Configure whether to use auto-mapping with the generated
         * MapperFactory
         * 
         * @param useAutoMapping
         * @return a reference to <code>this</code> MapperFactoryBuilder
         */
        public B useAutoMapping(boolean useAutoMapping) {
            this.useAutoMapping = useAutoMapping;
            return self();
        }
        
        /**
         * Configure whether to use built-in converters with the generated
         * MapperFactory
         * 
         * @param useBuiltinConverters
         * @return a reference to <code>this</code> MapperFactoryBuilder
         */
        public B usedBuiltinConverters(boolean useBuiltinConverters) {
            this.useBuiltinConverters = useBuiltinConverters;
            return self();
        }
        
        /**
         * @return a new instance of the Factory for which this builder is
         *         defined. The construction should be performed via the
         *         single-argument constructor which takes in a builder; no
         *         initialization code should be performed here, as it will not
         *         be inherited by subclasses; instead, place such
         *         initialization (defaults, etc.) in the Builder's constructor.
         */
        public abstract F build();
        
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
     * MapperFactory factory = new DefaultMapperFactory.Builder().build();
     * </pre>
     * 
     * @author matt.deboer@gmail.com
     */
    public static class Builder extends MapperFactoryBuilder<DefaultMapperFactory, Builder> {
        
        /*
         * (non-Javadoc)
         * 
         * @see
         * ma.glasnost.orika.impl.DefaultMapperFactory.MapperFactoryBuilder#
         * build()
         */
        @Override
        public DefaultMapperFactory build() {
            return new DefaultMapperFactory(this);
        }
        
        /*
         * (non-Javadoc)
         * 
         * @see
         * ma.glasnost.orika.impl.DefaultMapperFactory.MapperFactoryBuilder#
         * self()
         */
        @Override
        protected Builder self() {
            return this;
        }
        
    }
    
    /**
     * Generates the UnenhanceStrategy to be used for this MapperFactory,
     * applying the passed delegateStrategy if not null.<br>
     * This allows the MapperFactory a chance to fill in the unenhance strategy
     * with references to other parts of the factory (registered mappers,
     * converters, object factories) which may be important in the "unenhancing"
     * process.
     * 
     * @param unenhanceStrategy
     * @param overrideDefaultUnenhanceBehavior
     *            true if the passed UnenhanceStrategy should take full
     *            responsibility for un-enhancement; false if the default
     *            behavior should be applied as a fail-safe after consulting the
     *            passed strategy.
     * 
     * @return the resulting UnenhanceStrategy
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
                    return type != null && aToBRegistry.containsKey(type);
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
             * @return true if the type is accessible
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
    
    public Mapper<Object, Object> lookupMapper(MapperKey mapperKey) {
        if (!existsRegisteredMapper(mapperKey.getAType(), mapperKey.getBType(), true)) {
            if (useAutoMapping) {
                synchronized (this) {
                    try {
                        /*
                         * We shouldn't create a mapper for an immutable type;
                         * although it will succeed in generating an empty
                         * mapper, it won't actually result in a valid mapping,
                         * so it's better to throw an exception to indicate more
                         * clearly that something went wrong. However, there is
                         * a possibility that a custom ObjectFactory was
                         * registered for the immutable type, which would be
                         * valid.
                         */
                        if (ClassUtil.isImmutable(mapperKey.getBType()) && !objectFactoryRegistry.containsKey(mapperKey.getBType())) {
                            throw new MappingException("No converter registered for conversion from " + mapperKey.getAType() + " to "
                                    + mapperKey.getBType() + ", nor any ObjectFactory which can generate " + mapperKey.getBType()
                                    + " from " + mapperKey.getAType());
                        }
                        
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("No mapper registered for " + mapperKey + ": attempting to generate");
                        }
                        final ClassMap<?, ?> classMap = classMap(mapperKey.getAType(), mapperKey.getBType()).byDefault().toClassMap();
                        buildObjectFactories(classMap);
                        buildMapper(classMap, true);
                        initializeUsedMappers(classMap);
                    } catch (MappingException e) {
                        e.setSourceType(mapperKey.getAType());
                        e.setDestinationType(mapperKey.getBType());
                        throw e;
                    }
                }
            }
        }
        return getRegisteredMapper(mapperKey);
    }
    
    public boolean existsRegisteredMapper(Type<?> sourceType, Type<?> destinationType, boolean includeAutoGeneratedMappers) {
        for (Mapper<?, ?> mapper : mappersRegistry.values()) {
            if ((mapper.getAType().isAssignableFrom(sourceType) && mapper.getBType().isAssignableFrom(destinationType))
                    || (mapper.getAType().isAssignableFrom(destinationType) && mapper.getBType().isAssignableFrom(sourceType))) {
                if (includeAutoGeneratedMappers || !(mapper instanceof GeneratedMapperBase)) {
                    return true;
                } else if ((mapper instanceof GeneratedMapperBase) && !((GeneratedMapperBase) mapper).isFromAutoMapping()) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * @param mapperKey
     * @return a registered Mapper which is able to map the specified types
     */
    @SuppressWarnings("unchecked")
    protected <A, B> Mapper<A, B> getRegisteredMapper(MapperKey mapperKey) {
        return getRegisteredMapper((Type<A>) mapperKey.getAType(), (Type<B>) mapperKey.getBType());
    }
    
    /**
     * @param typeA
     * @param typeB
     * @return a registered Mapper which is able to map the specified types
     */
    @SuppressWarnings("unchecked")
    protected <A, B> Mapper<A, B> getRegisteredMapper(Type<A> typeA, Type<B> typeB) {
        for (Mapper<?, ?> mapper : mappersRegistry.values()) {
            if ((mapper.getAType().isAssignableFrom(typeA) && mapper.getBType().isAssignableFrom(typeB))
                    || (mapper.getAType().isAssignableFrom(typeB) && mapper.getBType().isAssignableFrom(typeA))) {
                return (Mapper<A, B>) mapper;
            }
        }
        return null;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see ma.glasnost.orika.MapperFactory#getMapperFacade()
     * 
     * Since getMapperFacade() triggers the build() process, it is important
     * that none of the methods called during the build() invoke
     * getMapperFacade() again.
     */
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
    
    public void registerConcreteType(Type<?> abstractType, Type<?> concreteType) {
        this.concreteTypeRegistry.put(abstractType, concreteType);
    }
    
    public void registerConcreteType(Class<?> abstractType, Class<?> concreteType) {
        this.concreteTypeRegistry.put(abstractType, TypeFactory.valueOf(concreteType));
    }
    
    @SuppressWarnings("unchecked")
    public <T> ObjectFactory<T> lookupObjectFactory(Type<T> targetType) {
        if (targetType == null) {
            return null;
        }
        
        ObjectFactory<T> result = (ObjectFactory<T>) objectFactoryRegistry.get(targetType);
        if (result == null) {
            // Check if we can use default constructor...
            synchronized (this) {
                Constructor<?>[] constructors = targetType.getRawType().getConstructors();
                if (useAutoMapping || !isBuilt) {
                    if (constructors.length == 1 && constructors[0].getParameterTypes().length == 0) {
                        /*
                         * Use the default constructor in the case where it is
                         * the only option
                         */
                        result = (ObjectFactory<T>) USE_DEFAULT_CONSTRUCTOR;
                    } else {
                        try {
                            result = (ObjectFactory<T>) objectFactoryGenerator.build(targetType);
                        } catch (MappingException e) {
                            for (Constructor<?> c : constructors) {
                                if (c.getParameterTypes().length == 0) {
                                    result = (ObjectFactory<T>) USE_DEFAULT_CONSTRUCTOR;
                                    break;
                                }
                            }
                            if (result == null) {
                                throw e;
                            }
                        }
                    }
                    ObjectFactory<T> existing = (ObjectFactory<T>) objectFactoryRegistry.putIfAbsent(targetType, result);
                    if (existing != null) {
                        result = existing;
                    }
                    
                } else {
                    for (Constructor<?> constructor : constructors) {
                        if (constructor.getParameterTypes().length == 0) {
                            result = (ObjectFactory<T>) USE_DEFAULT_CONSTRUCTOR;
                            break;
                        }
                    }
                }
                
            }
        }
        
        if (USE_DEFAULT_CONSTRUCTOR.equals(result)) {
            result = null;
        }
        
        return result;
    }
    
    @SuppressWarnings("unchecked")
    public <S, D> Type<? extends D> lookupConcreteDestinationType(Type<S> sourceType, Type<D> destinationType, MappingContext context) {
        
        Type<? extends D> concreteType = context.getConcreteClass(sourceType, destinationType);
        
        if (concreteType != null) {
            return concreteType;
        }
        
        /*
         * Locate the destination set by it's resolved mapper
         */
        Set<Type<?>> destinationSet = aToBRegistry.get(sourceType);
        if (destinationSet == null || destinationSet.isEmpty()) {
            Mapper<S, D> registeredMapper = getRegisteredMapper(sourceType, destinationType);
            if (registeredMapper != null) {
                concreteType = (Type<? extends D>) (registeredMapper.getAType().isAssignableFrom(sourceType) ? registeredMapper.getBType()
                        : registeredMapper.getAType());
                if (!ClassUtil.isConcrete(concreteType)) {
                    concreteType = (Type<? extends D>) resolveConcreteType(concreteType, destinationType);
                } else {
                    return null;
                }
            } else {
                concreteType = (Type<? extends D>) resolveConcreteType(destinationType, destinationType);
            }
        } else {
            for (final Type<?> type : destinationSet) {
                if (destinationType.isAssignableFrom(type) && ClassUtil.isConcrete(type)) {
                    if (type.equals(destinationType) || existsRegisteredMapper(sourceType, type, false)
                            || !ClassUtil.isConcrete(destinationType)) {
                        return (Type<? extends D>) type;
                    }
                }
            }
        }
        
        if (concreteType == null) {
            concreteType = (Type<? extends D>) resolveConcreteType(destinationType, destinationType);
        }
        
        return concreteType;
    }
    
    /**
     * @param type
     * @param originalType
     * @return a concrete type (if any) which has been registered for the
     *         specified abstract type
     */
    protected Type<?> resolveConcreteType(Type<?> type, Type<?> originalType) {
        
        Type<?> concreteType = (Type<?>) this.concreteTypeRegistry.get(type);
        if (concreteType == null) {
            concreteType = (Type<?>) this.concreteTypeRegistry.get(type.getRawType());
            if (concreteType != null) {
                concreteType = TypeFactory.resolveValueOf(concreteType.getRawType(), type);
            }
        }
        
        if (concreteType != null && !concreteType.isAssignableFrom(originalType)) {
            if (ClassUtil.isConcrete(originalType)) {
                concreteType = originalType;
            } else {
                concreteType = (Type<?>) this.concreteTypeRegistry.get(originalType);
                if (concreteType == null) {
                    concreteType = (Type<?>) this.concreteTypeRegistry.get(originalType.getRawType());
                    if (concreteType != null) {
                        concreteType = TypeFactory.resolveValueOf(concreteType, originalType);
                    }
                }
            }
        }
        return concreteType;
    }
    
    @SuppressWarnings("unchecked")
    public <A, B> void registerClassMap(ClassMap<A, B> classMap) {
        classMapRegistry.put(new MapperKey(classMap.getAType(), classMap.getBType()), (ClassMap<Object, Object>) classMap);
        if (isBuilding || isBuilt) {
            buildMapper(classMap, /* isAutoGenerated== */isBuilding);
            
            buildObjectFactories(classMap);
            initializeUsedMappers(classMap);
        }
    }
    
    public <A, B> void registerClassMap(ClassMapBuilder<A, B> builder) {
        registerClassMap(builder.toClassMap());
    }
    
    public synchronized void build() {
        
        if (!isBuilding) {
            isBuilding = true;
            
            converterFactory.setMapperFacade(mapperFacade);
            
            buildClassMapRegistry();
            
            for (final ClassMap<?, ?> classMap : classMapRegistry.values()) {
                buildMapper(classMap, false);
            }
            
            for (final ClassMap<?, ?> classMap : classMapRegistry.values()) {
                buildObjectFactories(classMap);
                initializeUsedMappers(classMap);
            }
            
            isBuilt = true;
            isBuilding = false;
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
    
    private static final Comparator<MapperKey> MAPPER_COMPARATOR = new Comparator<MapperKey>() {
        
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
            Set<MapperKey> usedMappers = new TreeSet<MapperKey>(MAPPER_COMPARATOR);
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
    private void buildMapper(ClassMap<?, ?> classMap, boolean isAutoGenerated) {
        register(classMap.getAType(), classMap.getBType());
        register(classMap.getBType(), classMap.getAType());
        
        final MapperKey mapperKey = new MapperKey(classMap.getAType(), classMap.getBType());
        final GeneratedMapperBase mapper = this.mapperGenerator.build(classMap);
        mapper.setMapperFacade(mapperFacade);
        mapper.setFromAutoMapping(isAutoGenerated);
        if (classMap.getCustomizedMapper() != null) {
            final Mapper<Object, Object> customizedMapper = (Mapper<Object, Object>) classMap.getCustomizedMapper();
            mapper.setCustomMapper(customizedMapper);
        }
        mappersRegistry.put(mapperKey, mapper);
        classMapRegistry.put(mapperKey, (ClassMap<Object, Object>) classMap);
    }
    
    /**
     * Registers that a mapping exists from the specified source type to the
     * specified destination type
     * 
     * @param sourceType
     * @param destinationType
     */
    protected <S, D> void register(Type<S> sourceType, Type<D> destinationType) {
        Set<Type<?>> destinationSet = aToBRegistry.get(sourceType);
        if (destinationSet == null) {
            destinationSet = new TreeSet<Type<?>>();
            aToBRegistry.put(sourceType, destinationSet);
        }
        destinationSet.add(destinationType);
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
        registerObjectFactory(objectFactory, TypeFactory.<T> valueOf(targetClass));
    }
    
    /**
     * @return the (initialized) ClassMapBuilderFactory configured for this
     *         mapper factory
     */
    protected ClassMapBuilderFactory getClassMapBuilderFactory() {
        if (!classMapBuilderFactory.isInitialized()) {
            classMapBuilderFactory.setDefaultFieldMappers(defaultFieldMappers.toArray(new DefaultFieldMapper[defaultFieldMappers.size()]));
        }
        return classMapBuilderFactory;
    }
    
    public <A, B> ClassMapBuilder<A, B> classMap(Type<A> aType, Type<B> bType) {
        return getClassMapBuilderFactory().map(aType, bType);
    }
    
    public <A, B> ClassMapBuilder<A, B> classMap(Class<A> aType, Type<B> bType) {
        return classMap(TypeFactory.<A> valueOf(aType), bType);
    }
    
    public <A, B> ClassMapBuilder<A, B> classMap(Type<A> aType, Class<B> bType) {
        return classMap(aType, TypeFactory.<B> valueOf(bType));
    }
    
    public <A, B> ClassMapBuilder<A, B> classMap(Class<A> aType, Class<B> bType) {
        return classMap(TypeFactory.<A> valueOf(aType), TypeFactory.<B> valueOf(bType));
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * ma.glasnost.orika.MapperFactory#registerMapper(ma.glasnost.orika.Mapper)
     */
    public <A, B> void registerMapper(Mapper<A, B> mapper) {
        synchronized (this) {
            this.mappersRegistry.put(new MapperKey(mapper.getAType(), mapper.getBType()), mapper);
            mapper.setMapperFacade(this.mapperFacade);
            register(mapper.getAType(), mapper.getBType());
            register(mapper.getBType(), mapper.getAType());
        }
        
    }
    
}
