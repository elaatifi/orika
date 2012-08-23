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
package ma.glasnost.orika.impl.generator;


import static ma.glasnost.orika.impl.Specifications.aCollection;
import static ma.glasnost.orika.impl.Specifications.aConversionToString;
import static ma.glasnost.orika.impl.Specifications.aPrimitive;
import static ma.glasnost.orika.impl.Specifications.aPrimitiveToWrapper;
import static ma.glasnost.orika.impl.Specifications.aStringToPrimitiveOrWrapper;
import static ma.glasnost.orika.impl.Specifications.aWrapperToPrimitive;
import static ma.glasnost.orika.impl.Specifications.anArray;
import static ma.glasnost.orika.impl.Specifications.immutable;
import static ma.glasnost.orika.metadata.TypeFactory.valueOf;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javassist.CannotCompileException;
import ma.glasnost.orika.Converter;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.MappingException;
import ma.glasnost.orika.constructor.ConstructorResolverStrategy;
import ma.glasnost.orika.converter.ConverterFactory;
import ma.glasnost.orika.impl.GeneratedObjectFactory;
import ma.glasnost.orika.metadata.ClassMap;
import ma.glasnost.orika.metadata.FieldMap;
import ma.glasnost.orika.metadata.MapperKey;
import ma.glasnost.orika.metadata.MappingDirection;
import ma.glasnost.orika.metadata.Property;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.metadata.TypeFactory;
import ma.glasnost.orika.property.PropertyResolverStrategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.paranamer.AdaptiveParanamer;
import com.thoughtworks.paranamer.AnnotationParanamer;
import com.thoughtworks.paranamer.BytecodeReadingParanamer;
import com.thoughtworks.paranamer.CachingParanamer;
import com.thoughtworks.paranamer.Paranamer;

public class ObjectFactoryGenerator {
    
    private final static Logger LOGGER = LoggerFactory.getLogger(ObjectFactoryGenerator.class);
    
    private final ConstructorResolverStrategy constructorResolverStrategy;
    private final MapperFactory mapperFactory;
    private final Paranamer paranamer;
    private final CompilerStrategy compilerStrategy;
    private final PropertyResolverStrategy propertyResolver;
    private final String nameSuffix;
    
    public ObjectFactoryGenerator(MapperFactory mapperFactory, ConstructorResolverStrategy constructorResolverStrategy,
    		CompilerStrategy compilerStrategy, PropertyResolverStrategy propertyResolver) {
        this.mapperFactory = mapperFactory;
        this.compilerStrategy = compilerStrategy;
        this.nameSuffix = Integer.toHexString(System.identityHashCode(compilerStrategy));
        this.paranamer = new CachingParanamer(new AdaptiveParanamer(new BytecodeReadingParanamer(), new AnnotationParanamer()));
        this.constructorResolverStrategy = constructorResolverStrategy;
        this.propertyResolver = propertyResolver;
        
    }
    
    public GeneratedObjectFactory build(Type<?> type) {
        
        final String className = type.getSimpleName() + "ObjectFactory" + nameSuffix;
        
        try {
            final GeneratedSourceCode factoryCode = 
        			new GeneratedSourceCode(className,GeneratedObjectFactory.class,compilerStrategy);
        	
            StringBuilder logDetails;
            if (LOGGER.isDebugEnabled()) {
            	logDetails = new StringBuilder();
            	logDetails.append("Generating new object factory for (" + type +")");
            } else {
            	logDetails = null;
            }
            
            UsedTypesContext usedTypes = new UsedTypesContext();
            UsedConvertersContext usedConverters = new UsedConvertersContext();
            
            addCreateMethod(factoryCode, usedTypes, usedConverters, type, logDetails);
            
            GeneratedObjectFactory objectFactory = (GeneratedObjectFactory) factoryCode.getInstance();
            objectFactory.setMapperFacade(mapperFactory.getMapperFacade());
            
            Type<Object>[] usedTypesArray = usedTypes.toArray();
            Converter<Object,Object>[] usedConvertersArray = usedConverters.toArray();
            if (logDetails != null) {
            	if (usedTypesArray.length > 0) {
            		logDetails.append("\n\tTypes used: " + Arrays.toString(usedTypesArray));
            	}
            	if (usedConvertersArray.length > 0) {
            		logDetails.append("\n\tConverters used: " + Arrays.toString(usedConvertersArray));
            	}
            	// TODO: what about doing the same thing for custom mappers?
            } 
            objectFactory.setUsedTypes(usedTypesArray);
            objectFactory.setUsedConverters(usedConvertersArray);
            
            if (logDetails != null) {
            	LOGGER.debug(logDetails.toString());
            }
            
            return objectFactory;
            
        } catch (final Exception e) {
            throw new MappingException("exception while creating object factory for " + type.getName(), e);
        } 
    }
    
    private void addCreateMethod(GeneratedSourceCode context, UsedTypesContext usedTypes, 
    		UsedConvertersContext usedConverters, Type<?> clazz, StringBuilder logDetails) throws CannotCompileException {
    	
        final CodeSourceBuilder out = new CodeSourceBuilder(usedTypes, usedConverters, mapperFactory);
        out.append("public Object create(Object s, " + MappingContext.class.getCanonicalName() + " mappingContext) {");
        out.append("if(s == null) throw new %s(\"source object must be not null\");", IllegalArgumentException.class.getCanonicalName());
        
        Set<Type<? extends Object>> sourceClasses = mapperFactory.lookupMappedClasses(clazz);
        
        if (sourceClasses != null && !sourceClasses.isEmpty()) {
            for (Type<? extends Object> sourceType : sourceClasses) {
                addSourceClassConstructor(out, clazz, sourceType, logDetails);
            }
        } else {
            throw new MappingException("Cannot generate ObjectFactory for " + clazz);
        }
        
        // TODO: this error is unclear, and should never really be reached;
        // if object factory generation failed, we should not create the factory
        // which is unable to construct an instance of anything.
        out.append("throw new %s(s.getClass().getCanonicalName() + \" is an unsupported source class : \"+s.getClass().getCanonicalName());",
                IllegalArgumentException.class.getCanonicalName());
        out.append("\n}");
        
        context.addMethod(out.toString());
    }
    
    private void addSourceClassConstructor(CodeSourceBuilder out, Type<?> type, Type<?> sourceClass, StringBuilder logDetails) {
        List<FieldMap> properties = new ArrayList<FieldMap>();
        ClassMap<Object, Object> classMap = mapperFactory.getClassMap(new MapperKey(type,sourceClass)); 
        if (classMap==null) {
        	classMap = mapperFactory.getClassMap(new MapperKey(sourceClass,type));
        }
        boolean aToB = classMap.getBType().equals(type);
        
        try {
            Constructor<?> constructor = (Constructor<?>) constructorResolverStrategy.resolve(classMap, type);
            if (logDetails != null) {
            	logDetails.append("\n\tUsing constructor: " + constructor);
            }
            if (constructor == null) {
                throw new IllegalArgumentException("no constructors found for " + type);
            } else if (LOGGER.isDebugEnabled()) {
            	LOGGER.debug("Using constructor: " + constructor);
            }
            
            String[] parameters = paranamer.lookupParameterNames(constructor);
            Class<?>[] constructorArguments = constructor.getParameterTypes();
            if (LOGGER.isDebugEnabled()) {
            	LOGGER.debug("Attempting to resolve constructor parameters " + Arrays.toString(parameters) + " against class map");
            }
            
            // TODO need optimizations
            int argIndex = 0;
            for (String param : parameters) {
                for (FieldMap fieldMap : classMap.getFieldsMapping()) {
                    if (!aToB)
                        fieldMap = fieldMap.flip();
                    if (param.equals(fieldMap.getDestination().getName())) {
                    	// destination property should be compared against
                    	// the constructor argument 
                    	fieldMap = fieldMap.copy();
                    	fieldMap.getDestination().setType(TypeFactory.valueOf(constructorArguments[argIndex]));
                    	properties.add(fieldMap);
                        break;
                    }
                }
                ++argIndex;
            }
            
            if (parameters.length != properties.size()) {
                /*
                 * Attempt to map the constructor parameters to the properties of the source type;
                 * these may not exist in the field mappings, since the destination may not have
                 * properties defined which directly match them
                 */
                Map<String, Property> sourceProps = propertyResolver.getProperties(sourceClass);
                for (int p = 0, len = parameters.length; p < len; ++p) {
                    String name = parameters[p];
                    Class<?> rawType = constructorArguments[p];
                    if (sourceProps.get(name) != null) {
                        Property destProp = new Property();
                        destProp.setName(name);
                        destProp.setType(TypeFactory.valueOf(rawType));
                        properties.add(new FieldMap(sourceProps.get(name), destProp, null, null, MappingDirection.A_TO_B, false,
                                null, null));
                    }
                }
                // Still couldn't find all of the properties?
                if (parameters.length != properties.size()) {
                    throw new MappingException("While generating object factory for " + type + ": " +
                            "could not match all of the resolved constructor's parameters against the class-map.\n" +
                            "constructor = " + constructor + "\n" +
                            "parameters = " + Arrays.toString(parameters) + "\n" +
                            "resolved = " + properties);
                }
            }
            
            out.ifInstanceOf("s", sourceClass).then();
            out.append("%s source = (%s) s;", sourceClass.getCanonicalName(), sourceClass.getCanonicalName());
            argIndex = 0;
            for (FieldMap fieldMap : properties) {
            	
                Class<?> targetClass = constructorArguments[argIndex];
                VariableRef v = new VariableRef(valueOf(targetClass), "arg" + argIndex++);
                VariableRef s = new VariableRef(fieldMap.getSource(), "source");
               
                out.statement(v.declare());
                
                if (generateConverterCode(out, v, fieldMap)) {
                    continue;
                }
                try {
                    // TODO: should we use CodeSourceBuilder.mapFields here?
                    if (fieldMap.is(aWrapperToPrimitive())) {
                        out.ifNotNull(s).fromPrimitiveOrWrapperToPrimitive(v, s);
                    } else if (fieldMap.is(aPrimitiveToWrapper())) {
                        out.fromPrimitiveToWrapper(v, s);
                    } else if (fieldMap.is(aPrimitive())) {
                        out.copyByReference(v, s);
                    } else if (fieldMap.is(immutable())) {
                        out.ifNotNull(s).copyByReference(v, s);
                    } else if (fieldMap.is(anArray())) {
                        out.fromArrayOrCollectionToArray(v, s);
                    } else if (fieldMap.is(aCollection())) {
                        out.fromArrayOrCollectionToCollection(v, s, fieldMap.getDestination(), fieldMap.getDestination().getType());
                    } else if (fieldMap.is(aStringToPrimitiveOrWrapper())) { 
                        out.fromStringToStringConvertable(v, s);
                    } else if (fieldMap.is(aConversionToString())) {
                        out.fromAnyTypeToString(v, s);
                    } else { /**/
                        out.fromObjectToObject(v, s, null);
                    }
                    
                } catch (final Exception e) {
                }
            }
            
            out.append("return new %s", type.getCanonicalName()).append("(");
            for (int i = 0; i < properties.size(); i++) {
                out.append("arg%d", i);
                if (i < properties.size() - 1) {
                    out.append(",");
                }
            }
            out.append(");").end();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.warn("Could not find " + type.getName() + " constructor's parameters name");
            /* SKIP */
        }
    }
    
    private boolean generateConverterCode(final CodeSourceBuilder code, VariableRef v, FieldMap fieldMap) {
        
        VariableRef s = new VariableRef(fieldMap.getSource(), "source");
        final Type<?> destinationType = fieldMap.getDestination().getType();
        
        Converter<Object, Object> converter = null;
        ConverterFactory converterFactory = mapperFactory.getConverterFactory();
        if (fieldMap.getConverterId() != null) {
            converter = converterFactory.getConverter(fieldMap.getConverterId());
        } else {
            converter = converterFactory.getConverter(s.type(), destinationType);
        }
        
        if (converter != null) {
            code.ifNotNull(s).then().convert(v, s, converter).end();
            return true;
        } else {
            return false;
        }
    }
}
