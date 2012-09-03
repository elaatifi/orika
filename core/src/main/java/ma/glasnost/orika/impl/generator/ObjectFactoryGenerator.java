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


import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javassist.CannotCompileException;
import ma.glasnost.orika.Converter;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.MappingException;
import ma.glasnost.orika.constructor.ConstructorResolverStrategy;
import ma.glasnost.orika.constructor.ConstructorResolverStrategy.ConstructorMapping;
import ma.glasnost.orika.converter.ConverterFactory;
import ma.glasnost.orika.impl.GeneratedObjectFactory;
import ma.glasnost.orika.metadata.ClassMap;
import ma.glasnost.orika.metadata.FieldMap;
import ma.glasnost.orika.metadata.MapperKey;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.metadata.TypeFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ObjectFactoryGenerator {
    
    private final static Logger LOGGER = LoggerFactory.getLogger(ObjectFactoryGenerator.class);
    
    private final ConstructorResolverStrategy constructorResolverStrategy;
    private final MapperFactory mapperFactory;
    private final CompilerStrategy compilerStrategy;
    private final String nameSuffix;
    
    public ObjectFactoryGenerator(MapperFactory mapperFactory, ConstructorResolverStrategy constructorResolverStrategy,
    		CompilerStrategy compilerStrategy) {
        this.mapperFactory = mapperFactory;
        this.compilerStrategy = compilerStrategy;
        this.nameSuffix = Integer.toHexString(System.identityHashCode(compilerStrategy));
        this.constructorResolverStrategy = constructorResolverStrategy;
    }
    
    public GeneratedObjectFactory build(Type<?> type) {
        
        final String className = type.getSimpleName() + "_ObjectFactory" + nameSuffix;
        
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
        
        // TODO: can this condition be reached?
        // if object factory generation failed, we should not create the factory
        // which is unable to construct an instance of anything.
        out.append("throw new %s(s.getClass().getCanonicalName() + \" is an unsupported source class : \"+s.getClass().getCanonicalName());",
                IllegalArgumentException.class.getCanonicalName());
        out.append("\n}");
        
        context.addMethod(out.toString());
    }
    
    private void addSourceClassConstructor(CodeSourceBuilder out, Type<?> type, Type<?> sourceClass, StringBuilder logDetails) {
        ClassMap<Object, Object> classMap = mapperFactory.getClassMap(new MapperKey(type,sourceClass)); 
        if (classMap==null) {
        	classMap = mapperFactory.getClassMap(new MapperKey(sourceClass,type));
        }
        
        ConstructorMapping<?> constructorMapping = (ConstructorMapping<?>) constructorResolverStrategy.resolve(classMap, type);
        Constructor<?> constructor = constructorMapping.getConstructor();
        
        if (constructor == null) {
            throw new IllegalArgumentException("no constructors found for " + type);
        } else if (logDetails != null) {
        	logDetails.append("\n\tUsing constructor: " + constructor);
        }
        
        List<FieldMap> properties = constructorMapping.getMappedFields();
        Class<?>[] constructorArguments = constructor.getParameterTypes();

        int argIndex = 0;
        
        out.ifInstanceOf("s", sourceClass).then();
        out.append("%s source = (%s) s;", sourceClass.getCanonicalName(), sourceClass.getCanonicalName());
        out.append("\ntry {\n");
        argIndex = 0;
        for (FieldMap fieldMap : properties) {
        	
            Class<?> targetClass = constructorArguments[argIndex];
            VariableRef v = new VariableRef(TypeFactory.resolveValueOf(targetClass, type), "arg" + argIndex++);
            VariableRef s = new VariableRef(fieldMap.getSource(), "source");
           
            out.statement(v.declare());
            
            if (generateConverterCode(out, v, fieldMap)) {
                continue;
            }
                        
            out.mapFields(fieldMap, s, v, fieldMap.getDestination().getType(), logDetails);
        }
        
        out.append("return new %s", type.getCanonicalName()).append("(");
        for (int i = 0; i < properties.size(); i++) {
            out.append("arg%d", i);
            if (i < properties.size() - 1) {
                out.append(",");
            }
        }
        out.append(");");
        
        /*
         * Any exceptions thrown calling constructors should be propagated
         */
        out.append("\n} catch (java.lang.Exception e) {\n");
        out.append("throw new java.lang.RuntimeException(" + 
        		"\"Error while constructing new " + type.getSimpleName() + 
        		" instance\", e); \n}");
        out.end();
        
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
