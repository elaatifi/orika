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
import ma.glasnost.orika.BoundMapperFacade;
import ma.glasnost.orika.Converter;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.MappingException;
import ma.glasnost.orika.constructor.ConstructorResolverStrategy;
import ma.glasnost.orika.constructor.ConstructorResolverStrategy.ConstructorMapping;
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
    
    public GeneratedObjectFactory build(Type<?> type, MappingContext context) {
        
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
            UsedMapperFacadesContext usedMapperFacades = new UsedMapperFacadesContext();
            
            addCreateMethod(factoryCode, usedTypes, usedConverters, usedMapperFacades, type, context, logDetails);
            
            GeneratedObjectFactory objectFactory = (GeneratedObjectFactory) factoryCode.getInstance();
            objectFactory.setMapperFacade(mapperFactory.getMapperFacade());
            
            Type<Object>[] usedTypesArray = usedTypes.toArray();
            Converter<Object,Object>[] usedConvertersArray = usedConverters.toArray();
            BoundMapperFacade<Object, Object>[] usedMapperFacadesArray = usedMapperFacades.toArray();
            if (logDetails != null) {
                if (usedTypesArray.length > 0) {
                    logDetails.append("\n\t" + Type.class.getSimpleName() + "s used: " + Arrays.toString(usedTypesArray));
                }
                if (usedConvertersArray.length > 0) {
                    logDetails.append("\n\t" + Converter.class.getSimpleName() + "s used: " + Arrays.toString(usedConvertersArray));
                }
                if (usedMapperFacadesArray.length > 0) {
                    logDetails.append("\n\t" + BoundMapperFacade.class.getSimpleName() + "s used: " + Arrays.toString(usedMapperFacadesArray));
                }
            } 
            objectFactory.setUsedTypes(usedTypesArray);
            objectFactory.setUsedConverters(usedConvertersArray);
            objectFactory.setUsedMapperFacades(usedMapperFacadesArray);
            
            if (logDetails != null) {
            	LOGGER.debug(logDetails.toString());
            }
            
            return objectFactory;
            
        } catch (final Exception e) {
            throw new MappingException("exception while creating object factory for " + type.getName(), e);
        } 
    }
    
    private void addCreateMethod(GeneratedSourceCode code, UsedTypesContext usedTypes, 
    		UsedConvertersContext usedConverters, UsedMapperFacadesContext usedMappers, 
    		Type<?> type, MappingContext mappingContext, StringBuilder logDetails) throws CannotCompileException {
    	
        final CodeSourceBuilder out = new CodeSourceBuilder(usedTypes, usedConverters, usedMappers, mapperFactory);
        out.append("public Object create(Object s, " + MappingContext.class.getCanonicalName() + " mappingContext) {");
        out.append("if(s == null) throw new %s(\"source object must be not null\");", IllegalArgumentException.class.getCanonicalName());
        
        Set<Type<? extends Object>> sourceClasses = mapperFactory.lookupMappedClasses(type);
        
        if (sourceClasses != null && !sourceClasses.isEmpty()) {
            for (Type<? extends Object> sourceType : sourceClasses) {
                addSourceClassConstructor(out, type, sourceType, mappingContext, logDetails);
            }
        } else {
            throw new MappingException("Cannot generate ObjectFactory for " + type);
        }
        
        // TODO: can this condition be reached?
        // if object factory generation failed, we should not create the factory
        // which is unable to construct an instance of anything.
        out.append("throw new %s(s.getClass().getCanonicalName() + \" is an unsupported source class : \"+s.getClass().getCanonicalName());",
                IllegalArgumentException.class.getCanonicalName());
        out.append("\n}");
        
        code.addMethod(out.toString());
    }
    
    private void addSourceClassConstructor(CodeSourceBuilder out, Type<?> type, Type<?> sourceType, MappingContext mappingContext, StringBuilder logDetails) {
        MapperKey mapperKey = new MapperKey(type,sourceType);
        ClassMap<Object, Object>  classMap = mapperFactory.getClassMap(mapperKey); 
        
        if (classMap==null) {
        	classMap = mapperFactory.getClassMap(new MapperKey(sourceType,type));
        }
        
        if (type.isArray()) {
            addArrayClassConstructor(out, type, sourceType, classMap.getFieldsMapping().size());
        } else {
        
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
            
            out.ifInstanceOf("s", sourceType).then();
            out.append("%s source = (%s) s;", sourceType.getCanonicalName(), sourceType.getCanonicalName());
            out.append("\ntry {\n");
            argIndex = 0;
            
            for (FieldMap fieldMap : properties) {
            	
                Class<?> targetClass = constructorArguments[argIndex];
                VariableRef v = new VariableRef(TypeFactory.resolveValueOf(targetClass, type), "arg" + argIndex++);
                VariableRef s = new VariableRef(fieldMap.getSource(), "source");
               
                out.statement(v.declare());
                            
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
    }

    /**
     * @param type
     * @param size
     */
    private void addArrayClassConstructor(CodeSourceBuilder out, Type<?> type, Type<?> sourceType, int size) {
        out.ifInstanceOf("s", sourceType).then();
        out.append("return new " + type.getRawType().getComponentType().getCanonicalName() + "[" + size + "];");
        out.end();
    }
}
