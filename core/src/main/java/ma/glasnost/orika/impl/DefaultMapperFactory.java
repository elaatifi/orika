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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import ma.glasnost.orika.Mapper;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.MappingException;
import ma.glasnost.orika.MappingHint;
import ma.glasnost.orika.ObjectFactory;
import ma.glasnost.orika.OrikaSystemProperties;
import ma.glasnost.orika.constructor.ConstructorResolverStrategy;
import ma.glasnost.orika.constructor.SimpleConstructorResolverStrategy;
import ma.glasnost.orika.converter.ConverterFactory;
import ma.glasnost.orika.converter.DefaultConverterFactory;
import ma.glasnost.orika.impl.generator.CompilerStrategy;
import ma.glasnost.orika.impl.generator.CompilerStrategy.SourceCodeGenerationException;
import ma.glasnost.orika.impl.generator.JavassistCompilerStrategy;
import ma.glasnost.orika.impl.generator.MapperGenerator;
import ma.glasnost.orika.impl.generator.ObjectFactoryGenerator;
import ma.glasnost.orika.inheritance.DefaultSuperTypeResolverStrategy;
import ma.glasnost.orika.inheritance.SuperTypeResolverStrategy;
import ma.glasnost.orika.metadata.ClassMap;
import ma.glasnost.orika.metadata.ClassMapBuilder;
import ma.glasnost.orika.metadata.MapperKey;
import ma.glasnost.orika.unenhance.BaseUnenhancer;
import ma.glasnost.orika.unenhance.HibernateUnenhanceStrategy;
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
    private final ConcurrentHashMap<Class<?>, ObjectFactory<?>> objectFactoryRegistry;
    private final Map<Class<?>, Set<Class<?>>> aToBRegistry;
    private final Map<Class<?>, Class<?>> mappedConverters;
    private final List<MappingHint> mappingHints;
    private final UnenhanceStrategy unenhanceStrategy;
    private final ConverterFactory converterFactory;
    private final CompilerStrategy compilerStrategy;
    private volatile boolean isBuilt = false;
    
    
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
        this.aToBRegistry = new ConcurrentHashMap<Class<?>, Set<Class<?>>>();
        this.mappedConverters = new ConcurrentHashMap<Class<?>, Class<?>>();
        this.usedMapperMetadataRegistry = new ConcurrentHashMap<MapperKey, Set<ClassMap<Object, Object>>>();
        this.objectFactoryRegistry = new ConcurrentHashMap<Class<?>, ObjectFactory<?>>();
        this.mappingHints = new CopyOnWriteArrayList<MappingHint>();
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
     * Use this builder to generate instances of DefaultMapperFactory
     * with the desired customizations.<br><br>
     * 
     * For example, an instance with no customizations could be generated with the following code:
     * <pre>
     * {@code
     * 		MapperFactory factory = new DefaultMapperFactory.Builder().build();
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
    		
    		if (converterFactory==null) {
    		    converterFactory = getDefaultConverterFactory();
    		}
    		if (constructorResolverStrategy == null) {
    		    constructorResolverStrategy = getDefaultConstructorResolverStrategy(); 
    		}
    		if (compilerStrategy==null) {
    			compilerStrategy = getDefaultCompilerStrategy();
    		}
    		
    		return new DefaultMapperFactory(
    			classMaps,
    			unenhanceStrategy,
    			superTypeStrategy,
    			constructorResolverStrategy,
    			converterFactory,
    			compilerStrategy);
    	}
    	
    }
    
    /**
     * Provides a default compiler strategy, favoring a type specified in 
     * the appropriate system property if found.
     * 
     * @return 
     */
	private static CompilerStrategy getDefaultCompilerStrategy() {
		CompilerStrategy compilerStrategy = null;
		String strategy = System.getProperty(OrikaSystemProperties.COMPILER_STRATEGY);
		if (strategy != null) {
			// User may specify the compiler strategy using a system property
			try {
				@SuppressWarnings("unchecked")
				Class<? extends CompilerStrategy> strategyType = 
						(Class<? extends CompilerStrategy>) Class.forName(strategy, true, Thread.currentThread().getContextClassLoader());
				compilerStrategy = strategyType.newInstance();

			} catch (Exception e) {
				throw new IllegalArgumentException(
						"compiler strategy specified was invalid", e);
			}

		} else {
			compilerStrategy = new JavassistCompilerStrategy();
		}
		return compilerStrategy;
	}
    
    /**
     * Provides a default constructor resolver strategy, favoring a type specified in 
     * the appropriate system property if found.
     * 
     * @return
     */
	private static ConverterFactory getDefaultConverterFactory() {
		ConverterFactory converterFactory = null;
		String strategy = System
				.getProperty(OrikaSystemProperties.CONVERTER_FACTORY);
		if (strategy != null) {

			try {
				@SuppressWarnings("unchecked")
				Class<? extends ConverterFactory> strategyType = (Class<? extends ConverterFactory>) Class.forName(strategy, true, Thread.currentThread().getContextClassLoader());
				converterFactory = strategyType.newInstance();

			} catch (Exception e) {
				throw new IllegalArgumentException("converter factory specified was invalid", e);
			}

		} else {
			converterFactory = new DefaultConverterFactory();
		}
		return converterFactory;
	}
    
    /**
     * Provides a default constructor resolver strategy, favoring a type specified in 
     * the appropriate system property if found.
     * 
     * @return
     */
	private static ConstructorResolverStrategy getDefaultConstructorResolverStrategy() {
		ConstructorResolverStrategy constructorResolverStrategy = null;
		String strategy = System.getProperty(OrikaSystemProperties.CONSTRUCTOR_RESOLVER_STRATEGY);
		if (strategy != null) {

			try {
				@SuppressWarnings("unchecked")
				Class<? extends ConstructorResolverStrategy> strategyType = (Class<? extends ConstructorResolverStrategy>) Class.forName(strategy, true, Thread.currentThread().getContextClassLoader());
				constructorResolverStrategy = strategyType.newInstance();

			} catch (Exception e) {
				throw new IllegalArgumentException("constructor resolver strategy specified was invalid",e);
			}

		} else {
			constructorResolverStrategy = new SimpleConstructorResolverStrategy();
			//constructorResolverStrategy = new BestFitConstructorResolverStrategy();
		}
		return constructorResolverStrategy;
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
        } else {
            // TODO: this delegate strategy may no longer be needed...
            try {
                Class.forName("org.hibernate.proxy.HibernateProxy");
                unenhancer.addUnenhanceStrategy(new HibernateUnenhanceStrategy());
            } catch (final Throwable e) {
                // TODO add warning
            }
            
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
                public boolean isAcceptable(Class<?> proposedClass) {
                    return aToBRegistry.containsKey(proposedClass) || mappedConverters.containsKey(proposedClass);
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
	    public boolean isTypeAccessible(Class<?> type) {

		try {
		    Class<?> loadedType = Thread.currentThread().getContextClassLoader().loadClass(type.getName());
		    if (!type.equals(loadedType)) {
			return false;
		    }
		    compilerStrategy.assureTypeIsAccessible(type);
		    return true;
		} catch (ClassNotFoundException e) {
		    return false;
		} catch (SourceCodeGenerationException e) {
		    return false;
		}
	    }

	    @Override
	    public boolean isAcceptable(Class<?> proposedClass) {
		return isTypeAccessible(proposedClass)
			&& !java.lang.reflect.Proxy.class.equals(proposedClass);
	    }

	};
        
        unenhancer.addSuperTypeResolverStrategy(inaccessibleTypeStrategy);
        
        return unenhancer;
        
    }
    
    public GeneratedMapperBase lookupMapper(MapperKey mapperKey) {
        if (!mappersRegistry.containsKey(mapperKey)) {
            final ClassMap<?, ?> classMap = ClassMapBuilder.map(mapperKey.getAType(), mapperKey.getBType())
                    .byDefault(this.mappingHints.toArray(new MappingHint[0]))
                    .toClassMap();
            buildObjectFactories(classMap);
            buildMapper(classMap);
        }
        return mappersRegistry.get(mapperKey);
    }
    
    public MapperFacade getMapperFacade() {
        if (!isBuilt) {
        	synchronized(mapperFacade) {
        		if (!isBuilt) {
        			build();
        		}
        	}
        }
    	return mapperFacade;
    }
    
    public <D> void registerObjectFactory(ObjectFactory<D> objectFactory, Class<D> destinationClass) {
        objectFactoryRegistry.put(destinationClass, objectFactory);
    }
    
    public void registerMappingHint(MappingHint... hints) {
        this.mappingHints.addAll(Arrays.asList(hints));
    }
    
    @SuppressWarnings("unchecked")
    public <T> ObjectFactory<T> lookupObjectFactory(Class<T> targetClass) {
        if (targetClass == null) {
            return null;
        }
        
        ObjectFactory<T> result = (ObjectFactory<T>) objectFactoryRegistry.get(targetClass);
        if (result==null) {
        	// Check if we can use default constructor...
        	try {
				targetClass.getConstructor(/*new Class[0]*/);
				// Mark the class with null value in the registry
				// to avoid repeating the getConstructor call
				objectFactoryRegistry.put(targetClass, USE_DEFAULT_CONSTRUCTOR);
			} catch (Exception e) {
				// Generate an object factory
				synchronized (objectFactoryGenerator) {
					result = (ObjectFactory<T>) objectFactoryGenerator.build((Class<Object>)targetClass);
					objectFactoryRegistry.put(targetClass, result);
				}
			}
        } else if (USE_DEFAULT_CONSTRUCTOR.equals(result)) {
        	result = null;
        }
        
        return result;
    }
    
    @SuppressWarnings("unchecked")
    public <S, D> Class<? extends D> lookupConcreteDestinationClass(Class<S> sourceClass, Class<D> destinationClass, MappingContext context) {
        final Class<? extends D> concreteClass = context.getConcreteClass(sourceClass, destinationClass);
        
        if (concreteClass != null) {
            return concreteClass;
        }
        
        final Set<Class<?>> destinationSet = aToBRegistry.get(sourceClass);
        if (destinationSet == null || destinationSet.isEmpty()) {
            return null;
        }
        
        for (final Class<?> clazz : destinationSet) {
            if (destinationClass.isAssignableFrom(clazz)) {
                return (Class<? extends D>) clazz;
                
            }
        }
        return concreteClass;
    }
    
    @SuppressWarnings("unchecked")
    public <A, B> void registerClassMap(ClassMap<A, B> classMap) {
        classMapRegistry.put(new MapperKey(classMap.getAType(), classMap.getBType()), (ClassMap<Object, Object>) classMap);
    }
    
    public void build() {
        
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
        Map<MapperKey, ClassMap<Object, Object>> classMapsDictionnary = new HashMap<MapperKey, ClassMap<Object, Object>>();
        
        Set<ClassMap<Object, Object>> classMaps = new HashSet<ClassMap<Object, Object>>(classMapRegistry.values());
        
        for (final ClassMap<Object, Object> classMap : classMaps) {
            classMapsDictionnary.put(new MapperKey(classMap.getAType(), classMap.getBType()), classMap);
        }
        
        for (final ClassMap<?, ?> classMap : classMaps) {
            MapperKey key = new MapperKey(classMap.getAType(), classMap.getBType());
            
            Set<ClassMap<Object, Object>> usedClassMapSet = new HashSet<ClassMap<Object, Object>>();
            
            for (final MapperKey parentMapperKey : classMap.getUsedMappers()) {
                ClassMap<Object, Object> usedClassMap = classMapsDictionnary.get(parentMapperKey);
                if (usedClassMap == null) {
                    throw new MappingException("Cannot find class mapping using mapper : " + classMap.getMapperClassName());
                }
                usedClassMapSet.add(usedClassMap);
            }
            usedMapperMetadataRegistry.put(key, usedClassMapSet);
        }
        
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private <S, D> void buildObjectFactories(ClassMap<S, D> classMap) {
        Class aType = classMap.getAType();
        Class bType = classMap.getBType();
        if (classMap.getConstructorA() != null && lookupObjectFactory(aType) == null) {
            GeneratedObjectFactory objectFactory = objectFactoryGenerator.build(aType);
            registerObjectFactory(objectFactory, aType);
        }
        
        if (classMap.getConstructorB() != null && lookupObjectFactory(bType) == null) {
            GeneratedObjectFactory objectFactory = objectFactoryGenerator.build(bType);
            registerObjectFactory(objectFactory, bType);
        }
    }
    
    @SuppressWarnings("unchecked")
    private void initializeUsedMappers(ClassMap<?, ?> classMap) {
        Mapper<Object, Object> mapper = lookupMapper(new MapperKey(classMap.getAType(), classMap.getBType()));
        
        List<Mapper<Object, Object>> parentMappers = new ArrayList<Mapper<Object, Object>>();
        
        for (MapperKey parentMapperKey : classMap.getUsedMappers()) {
            collectUsedMappers(classMap, parentMappers, parentMapperKey);
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
        for (ClassMap<Object, Object> cm : usedClassMapSet) {
            collectUsedMappers(cm, parentMappers, new MapperKey(cm.getAType(), cm.getBType()));
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
        classMapRegistry.put(mapperKey, (ClassMap<Object, Object>)classMap);
    }
    
    private <S, D> void register(Class<S> sourceClass, Class<D> destinationClass) {
        Set<Class<?>> destinationSet = aToBRegistry.get(sourceClass);
        if (destinationSet == null) {
            destinationSet = new HashSet<Class<?>>();
            aToBRegistry.put(sourceClass, destinationSet);
        }
        destinationSet.add(destinationClass);
    }
    
    @SuppressWarnings("unchecked")
    public <A, B> ClassMap<A, B> getClassMap(MapperKey mapperKey) {
        return (ClassMap<A, B>) classMapRegistry.get(mapperKey);
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Set<Class<Object>> lookupMappedClasses(Class<Object> clazz) {
        return (Set) aToBRegistry.get(clazz);
    }
    
    public ConverterFactory getConverterFactory() {
        return converterFactory;
    }
}
