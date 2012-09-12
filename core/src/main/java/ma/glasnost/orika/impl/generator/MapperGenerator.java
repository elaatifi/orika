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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Set;

import javassist.CannotCompileException;
import ma.glasnost.orika.Converter;
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
    
    private static Logger LOGGER = LoggerFactory.getLogger(MapperGenerator.class);
    
    private final MapperFactory mapperFactory;
    private final CompilerStrategy compilerStrategy;
    
    public MapperGenerator(MapperFactory mapperFactory, CompilerStrategy compilerStrategy) {
        this.mapperFactory = mapperFactory;
        this.compilerStrategy = compilerStrategy;
    }
    
    public GeneratedMapperBase build(ClassMap<?, ?> classMap) {
        
        try {
            compilerStrategy.assureTypeIsAccessible(classMap.getAType().getRawType());
            compilerStrategy.assureTypeIsAccessible(classMap.getBType().getRawType());
            
            final GeneratedSourceCode mapperCode = new GeneratedSourceCode(
                    classMap.getMapperClassName(), GeneratedMapperBase.class,
                    compilerStrategy);
            
            UsedTypesContext usedTypes = new UsedTypesContext();
            UsedConvertersContext usedConverters = new UsedConvertersContext();
            
            StringBuilder logDetails;
            if (LOGGER.isDebugEnabled()) {
            	logDetails = new StringBuilder();
            	logDetails.append("Generating new mapper for (" + classMap.getAType()+", " + classMap.getBTypeName() +")");
            } else {
            	logDetails = null;
            }
            
            
            addMapMethod(mapperCode, true, classMap, usedTypes, usedConverters, logDetails);
            addMapMethod(mapperCode, false, classMap, usedTypes, usedConverters, logDetails);
            
            GeneratedMapperBase instance = mapperCode.getInstance();
            instance.setAType(classMap.getAType());
            instance.setBType(classMap.getBType());
            
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
            instance.setUsedTypes(usedTypesArray);
            instance.setUsedConverters(usedConvertersArray);
            if (logDetails != null) {
            	LOGGER.debug(logDetails.toString());
            }
            
            return instance;
            
        } catch (final Exception e) {
            throw new MappingException(e);
        }
    }
    
    private String getFieldTag(FieldMap fieldMap) {
    	return "\n\t Field(" + fieldMap.getSource() + ", " + fieldMap.getDestination() + ") : ";
    }
    
    private void addMapMethod(GeneratedSourceCode context, boolean aToB, ClassMap<?, ?> classMap, UsedTypesContext usedTypes, UsedConvertersContext usedConverters, StringBuilder logDetails) throws CannotCompileException {
        
    	if (logDetails != null) {
        	if (aToB) {
        		logDetails.append("\n\t" +context.getClassSimpleName() + ".mapAToB("+ classMap.getAType()+", " + classMap.getBTypeName() +") {");
        	} else {
        		logDetails.append("\n\t" +context.getClassSimpleName() + ".mapBToA("+ classMap.getBType()+", " + classMap.getATypeName() +") {");
        	}
        }
    	
        final CodeSourceBuilder out = new CodeSourceBuilder(usedTypes, usedConverters, mapperFactory);
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
        LinkedList<FieldMap> nestedFieldMaps = new LinkedList<FieldMap>();
        
        for (FieldMap fieldMap : classMap.getFieldsMapping()) {
            
        	
            if (fieldMap.isExcluded()) {
            	if (logDetails != null) {
            		logDetails.append(getFieldTag(fieldMap) + "excuding (explicitly)");
            	}
                continue;
            }
            
            if (isAlreadyExistsInUsedMappers(fieldMap, classMap)) {
            	if (logDetails != null) {
            		logDetails.append(getFieldTag(fieldMap) + "excluding because it is already handled by another mapper in this hierarchy");
            		
            	}
            	continue;
            }
            
            if (!aToB) {
                fieldMap = fieldMap.flip();
            }
            
            if (fieldMap.getElementMap() != null) {
            	nestedFieldMaps.add(fieldMap);
            	continue;
            }
            
            if (logDetails != null) {
        		logDetails.append(getFieldTag(fieldMap));
        	}
            
            if (!fieldMap.isIgnored()) {
                try {
                    generateFieldMapCode(out, fieldMap, destination.type(), logDetails);
                } catch (final Exception e) {
                    MappingException me = new MappingException(e);
                    me.setSourceProperty(fieldMap.getSource());
                    me.setDestinationProperty(fieldMap.getDestination());
                    me.setSourceType(source.type());
                    me.setDestinationType(destination.type());
                    throw me;
                }
            } else if (logDetails !=null) {
            	logDetails.append("ignored for this mapping direction");
            }
        }
        
        while (!nestedFieldMaps.isEmpty()) {
        	Set<FieldMap> associated = out.getAssociatedMappings(nestedFieldMaps, nestedFieldMaps.getFirst());
        	nestedFieldMaps.removeAll(associated);
        	out.fromMultiOccurrenceToMultiOccurrence(associated, logDetails);
        }
        
        out.append("\n\t\tif(customMapper != null) { \n\t\t\t customMapper.")
                .append(mapMethod)
                .append("(source, destination, mappingContext);\n\t\t}");
        
        out.append("\n\t}");
        
        if (logDetails != null) {
        	logDetails.append("\n\t}");
        }
        
        context.addMethod(out.toString());
        
    }
    
    private boolean isAlreadyExistsInUsedMappers(FieldMap fieldMap, ClassMap<?, ?> classMap) {
        
        Set<ClassMap<Object, Object>> usedClassMapSet = mapperFactory.lookupUsedClassMap(new MapperKey(classMap.getAType(),
                classMap.getBType()));
        
        if (!fieldMap.isByDefault()) {
        	return false;
        }
        
        for (ClassMap<Object, Object> usedClassMap : usedClassMapSet) {
            for(FieldMap usedFieldMap: usedClassMap.getFieldsMapping()) {
            	if (usedFieldMap.getSource().equals(fieldMap.getSource())
            			&& usedFieldMap.getDestination().equals(fieldMap.getDestination())) {
            		return true;
            	}
            }
        }
        
        return false;
    }
    
    private void generateFieldMapCode(CodeSourceBuilder code, FieldMap fieldMap, Type<?> destinationType, StringBuilder logDetails) throws Exception {
        
        final VariableRef sourceProperty = new VariableRef(fieldMap.getSource(), "source");
        final VariableRef destinationProperty = new VariableRef(fieldMap.getDestination(), "destination");
        
        if (!sourceProperty.isReadable() || ((!destinationProperty.isAssignable()) && !destinationProperty.isCollection())) {
            if (logDetails != null) {
            	logDetails.append("excluding because ");
    			if (!sourceProperty.isReadable()) {
    				logDetails.append(fieldMap.getSource().getType() + "." + fieldMap.getSource().getName() + " is not readable");
    			} else {
    				// TODO: this brings up an important case: sometimes the destination is not assignable, 
    				// but it's properties can still be mapped in-place. Should we handle it?
    				logDetails.append(fieldMap.getDestination().getType() + "." + fieldMap.getDestination().getName() + " is neither assignable nor a collection");
    			}		
            }
        	return;
        }
        
        // Make sure the source and destination types are accessible to the builder
        compilerStrategy.assureTypeIsAccessible(sourceProperty.rawType());
        compilerStrategy.assureTypeIsAccessible(destinationProperty.rawType());

        code.mapFields(fieldMap, sourceProperty, destinationProperty, destinationType, logDetails);
    }
    
}
