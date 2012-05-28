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

import java.util.Set;

import javassist.CannotCompileException;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.MappingException;
import ma.glasnost.orika.impl.GeneratedMapperBase;
import ma.glasnost.orika.metadata.ClassMap;
import ma.glasnost.orika.metadata.FieldMap;
import ma.glasnost.orika.metadata.MapperKey;
import ma.glasnost.orika.metadata.Type;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MapperGenerator {
    
    private static Logger LOG = LoggerFactory.getLogger(MapperGenerator.class);
    
    private final MapperFactory mapperFactory;
    private final CompilerStrategy compilerStrategy;
    private final UsedTypesContext usedTypes;
    
    public MapperGenerator(MapperFactory mapperFactory, CompilerStrategy compilerStrategy) {
        this.mapperFactory = mapperFactory;
        this.compilerStrategy = compilerStrategy;
        this.usedTypes = new UsedTypesContext();
    }
    
    public GeneratedMapperBase build(ClassMap<?, ?> classMap) {
        
        try {
            compilerStrategy.assureTypeIsAccessible(classMap.getAType().getRawType());
            compilerStrategy.assureTypeIsAccessible(classMap.getBType().getRawType());
            
            final GeneratedSourceCode mapperCode = new GeneratedSourceCode(
                    classMap.getMapperClassName(), GeneratedMapperBase.class,
                    compilerStrategy);
            
            addMapMethod(mapperCode, true, classMap);
            addMapMethod(mapperCode, false, classMap);
            
            GeneratedMapperBase instance = mapperCode.getInstance();
            instance.setAType(classMap.getAType());
            instance.setBType(classMap.getBType());
            instance.setUsedTypes(usedTypes.getUsedTypesArray());
            
            return instance;
            
        } catch (final Exception e) {
            throw new MappingException(e);
        }
    }
    
    private void addMapMethod(GeneratedSourceCode context, boolean aToB, ClassMap<?, ?> classMap) throws CannotCompileException {
        
        final CodeSourceBuilder out = new CodeSourceBuilder(usedTypes, mapperFactory);
        final String mapMethod = "map" + (aToB ? "AtoB" : "BtoA");
        out.append("\tpublic void ")
                .append(mapMethod)
                .append("(java.lang.Object a, java.lang.Object b, %s mappingContext) {\n\n", MappingContext.class.getCanonicalName());
        
        // out.assertType("a", sourceClass);
        // out.assertType("b", destinationClass);
        VariableRef source;
        VariableRef destination;
        if (aToB) {
            source = new VariableRef(classMap.getAType(), "source");
            destination = new VariableRef(classMap.getBType(), "destination"); 
        } else {
            source = new VariableRef(classMap.getBType(), "source");
            destination = new VariableRef(classMap.getAType(), "destination");
        }
         
        out.statement("super.%s(a, b, mappingContext);", mapMethod);
        out.statement(source.declare("a"));
        out.statement(destination.declare("b"));
        
        for (FieldMap fieldMap : classMap.getFieldsMapping()) {
            
            if (fieldMap.isExcluded()) {
                continue;
            }
            
            if (isAlreadyExistsInUsedMappers(fieldMap, classMap)) {
                continue;
            }
            
            if (!aToB) {
                fieldMap = fieldMap.flip();
            }
            if (!fieldMap.isIgnored()) {
                try {
                    generateFieldMapCode(out, fieldMap, destination.type());
                } catch (final Exception e) {
                    throw new MappingException(e);
                }
            }
        }
        out.append("\n\t\tif(customMapper != null) { \n\t\t\t customMapper.")
                .append(mapMethod)
                .append("(source, destination, mappingContext);\n\t\t}");
        
        out.append("\n\t}");
        
        context.addMethod(out.toString());
    }
    
    private boolean isAlreadyExistsInUsedMappers(FieldMap fieldMap, ClassMap<?, ?> classMap) {
        
        Set<ClassMap<Object, Object>> usedClassMapSet = mapperFactory.lookupUsedClassMap(new MapperKey(classMap.getAType(),
                classMap.getBType()));
        
        boolean exists = false;
        
        for (ClassMap<Object, Object> usedClassMap : usedClassMapSet) {
            if (usedClassMap.getFieldsMapping().contains(fieldMap))
                exists = true;
            break;
        }
        
        return exists;
    }
    
    private void generateFieldMapCode(CodeSourceBuilder code, FieldMap fieldMap, Type<?> destinationType) throws Exception {
        
        final VariableRef sourceProperty = new VariableRef(fieldMap.getSource(), "source");
        final VariableRef destinationProperty = new VariableRef(fieldMap.getDestination(), "destination");
        
        if (!sourceProperty.isReadable() || ((!destinationProperty.isAssignable()) && !destinationProperty.isCollection())) {
            return;
        }
        
        // Make sure the source and destination types are accessible to the
        // builder
        
        compilerStrategy.assureTypeIsAccessible(sourceProperty.rawType());
        compilerStrategy.assureTypeIsAccessible(destinationProperty.rawType());
        
        try {
            //
            // Ensure that there we will not cause a NPE
            //
            if (sourceProperty.isNestedProperty()) {
                code.ifPathNotNull(sourceProperty).then();
            }
            
            if (destinationProperty.isNestedProperty()) {
                code.assureInstanceExists(destinationProperty);
            }
            
            code.mapFields(fieldMap, sourceProperty, destinationProperty, destinationType);
                        
            // Close up, and set null to destination
            if (sourceProperty.isNestedProperty()) {
                code.end();
            }
            
        } catch (final Exception e) {
            if (fieldMap.isConfigured()) {
                throw e;
                // elsewise ignore
            }
        }
    }
    
}
