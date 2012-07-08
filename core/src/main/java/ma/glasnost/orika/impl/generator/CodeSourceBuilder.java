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

import static java.lang.String.format;
import static ma.glasnost.orika.impl.Specifications.*;

import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;

import ma.glasnost.orika.Converter;
import ma.glasnost.orika.MapEntry;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingException;
import ma.glasnost.orika.converter.ConverterFactory;
import ma.glasnost.orika.impl.generator.MapEntryRef.EntryPart;
import ma.glasnost.orika.impl.util.ClassUtil;
import ma.glasnost.orika.metadata.FieldMap;
import ma.glasnost.orika.metadata.FieldMapBuilder;
import ma.glasnost.orika.metadata.MapperKey;
import ma.glasnost.orika.metadata.Property;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.metadata.TypeFactory;

public class CodeSourceBuilder {
    
    private final StringBuilder out = new StringBuilder();
    private final UsedTypesContext usedTypes;
    private final UsedConvertersContext usedConverters;
    private final MapperFactory mapperFactory;
    private final Logger logger;
    
    public CodeSourceBuilder(UsedTypesContext usedTypes, UsedConvertersContext usedConverters, MapperFactory mapperFactory, Logger logger) {
        this.usedTypes = usedTypes;
        this.usedConverters = usedConverters;
        this.mapperFactory = mapperFactory;
        this.logger = logger;
    }
    
    private String usedConverter(Converter<?,?> converter) {
    	int index = usedConverters.getIndex(converter);
        return "(("+Converter.class.getCanonicalName()+")usedConverters[" + index + "])";
    }
    
    private String usedType(Type<?> type) {
        int index = usedTypes.getIndex(type);
        return "(("+Type.class.getCanonicalName()+")usedTypes[" + index + "])";
    }
    
    private String usedType(VariableRef r) {
        return usedType(r.type());
    }
    
    public CodeSourceBuilder convert(VariableRef d, VariableRef s, Converter converter) {
          
        //converterId = getConverterId(converterId);  
        //String statement = d.assign("mapperFacade.convert(%s, %s, %s, %s)", s.asWrapper(), usedType(s), usedType(d), converterId);
        String statement = d.assign("%s.convert(%s, %s)", usedConverter(converter), s.asWrapper(), usedType(d));
        
        		
        //D convert(S source, Type<? extends D> destinationType)
        if (s.isPrimitive()) {
            statement(statement);
        } else {
            statement(s.ifNotNull() + statement);
        }
        return this;
    }
    
    private String getConverterId(String converterId) {
        converterId = converterId == null ? "null" : ("\"" + converterId + "\"");
        return converterId;
    }
    
    public CodeSourceBuilder copyByReference(VariableRef d, VariableRef s) {
        return statement(d.assign(s));
    }
    
    public CodeSourceBuilder fromArrayOrCollectionToCollection(VariableRef d, VariableRef s, Property ip, Type<?> destinationType) {
        
        final Class<?> dc = destinationType.getRawType();
        final Class<?> destinationElementClass = d.elementType().getRawType();
        
        if (destinationElementClass == null) {
            throw new MappingException("cannot determine runtime type of destination collection " + dc.getName() + "." + d.name());
        }
        
       
        if (d.isAssignable()) {
            statement("if (%s == null) %s", d, d.assign(d.newCollection()));
        }
        
        // Start check if source property ! = null
        ifNotNull(s).then();
        if(s.isArray()) {
        	if(s.elementType().isPrimitive())
        		newLine().append("%s.addAll(asList(%s));", d, s);
        	else 
        		newLine().append("%s.addAll(mapperFacade.mapAsList(asList(%s), %s.class));", d, s, d.typeName());
        } else {
        	/*
        	 * 
        	 */
	        newLine().append("%s.clear();", d);
	        newLine().append("%s.addAll(mapperFacade.mapAs%s(%s, %s, %s, mappingContext));", d, d.collectionType(),
	                s, usedType(s.elementType()), usedType(d.elementType()));
	        
	        /*
	         * Instead, create a new element
	         */
	        VariableRef collection = new VariableRef(d.type(), "destinationCollection");
	        newLine().append("%s.clear();", d);
	        newLine().append("%s.addAll(mapperFacade.mapAs%s(%s, %s, %s, mappingContext));", d, d.collectionType(),
	                s, usedType(s.elementType()), usedType(d.elementType()));
	        
        }
        if (ip != null) {
            final VariableRef inverse = new VariableRef(ip, "orikaCollectionItem");
            
            if (ip.isCollection()) {
                append(
                        format("for (java.util.Iterator orikaIterator = %s.iterator(); orikaIterator.hasNext();) { ", d) +
                        format("    %s orikaCollectionItem = (%s) orikaIterator.next();", d.elementTypeName(), d.elementTypeName()) +
                        format("    %s { %s; }", inverse.ifNull(), inverse.assign(inverse.newCollection())) +
                        format("    %s.add(%s);", inverse, d.owner()) +
                        format("}")
                        );

            } else if (ip.isArray()) {
                append(" // TODO support array");
            } else {
                append(
                        format("for (java.util.Iterator orikaIterator = %s.iterator(); orikaIterator.hasNext();) { ", d) +
                        format("    %s orikaCollectionItem = (%s) orikaIterator.next();", d.elementTypeName(), d.elementTypeName()) +
                        format("    %s;", inverse.assign(d.owner())) +
                        format("}")
                        );

            }
        }
        // End check if source property ! = null
        _else().statement(d.assignIfPossible("null")).end();
        
        return this;
    }
    
    public CodeSourceBuilder newLine() {
        return append("\n");
    }
    
    public CodeSourceBuilder append(String str, Object... args) {
        out.append(String.format(str, args));
        return this;
    }
    
    public CodeSourceBuilder append(String str) {
        out.append(str);
        return this;
    }
    
    /**
     * Appends the provided string as a source code statement
     * 
     * @param str
     * @param args
     * @return
     */
    public CodeSourceBuilder statement(String str, Object...args) {
        if (str !=null && !"".equals(str.trim())) {
	    	String expr = "\n" + format(str, args);
	        append(expr);
	        if (!expr.endsWith(";") && !expr.endsWith("}")) {
	        	append(";");
	        }
        }
        return this;
    }
    
    public CodeSourceBuilder then() {
        return append("{").newLine();
    }
    
    public CodeSourceBuilder begin() {
        return then();
    }
    
    public CodeSourceBuilder end() {
        return newLine().append("}").newLine();
    }
    
    public CodeSourceBuilder _else() {
        return newLine().append("} else {").newLine();
    }
    
    @Override
    public String toString() {
        return out.toString();
    }
    
    public CodeSourceBuilder fromStringToStringConvertable(VariableRef d, VariableRef s) {

        if (d.isPrimitive()) {
            statement(d.assign("%s.valueOf(%s)", d.wrapperTypeName(), s));
        } else {
            statement(s.ifNotNull() + d.assign("%s.valueOf(%s)", d.typeName(), s));
        }
        
        return this;
        
    }
    
    public CodeSourceBuilder fromAnyTypeToString(VariableRef d, VariableRef s) {

        if (s.isPrimitive()) {
            statement(d.assign("\"\"+ %s", s));
        } else {
            statement(s.ifNotNull() + d.assign("%s.toString()",s));
        }
        
        return this;
        
    }
    
    public CodeSourceBuilder fromPrimitiveToWrapper(VariableRef d, VariableRef s) {
 
        statement(d.assign("%s.valueOf(%s)", d.typeName(), s));
        
        return this;
    }
    
    public CodeSourceBuilder setPrimitive(VariableRef d, VariableRef s) {

        if (s.isPrimitive()) {
            statement(d.assign(s)); 
        } else {
            statement(s.ifNotNull() + d.assign(s));
        }        
        
        return this;
    }
    
    /**
     * Generates code to convert from an array or collection to an array
     * 
     * @param d
     * @param s
     * @return
     */
    public CodeSourceBuilder fromArrayOrCollectionToArray(VariableRef d, VariableRef s) {
    
        final VariableRef arrayVar = d.elementRef(d.name());
        String newArray = format("%s[] %s = new %s[%s]", d.elementTypeName(), d.name(), d.elementTypeName(), s.size());
        String mapArray;
        if (d.elementType().isPrimitive()) {
            mapArray = format("mapArray(%s, asList(%s), %s.class, mappingContext)", arrayVar, s, arrayVar.typeName());
        } else {
            mapArray = format("mapperFacade.mapAsArray(%s, asList(%s), %s, %s, mappingContext)", d.name(), s, usedType(s.elementType()),
                    usedType(d.elementType()));
        }
        statement(" %s { %s; %s; %s; } else { %s; }", s.ifNotNull(), newArray, mapArray, d.assign(arrayVar), d.assign("null"));
        
        return this;
    }
    
    public CodeSourceBuilder fromStringOrEnumToEnum(VariableRef d, VariableRef s) {
        
        String assignEnum = d.assign("Enum.valueOf(%s.class, \"\"+%s)", /*d.typeName(),*/ d.typeName(), s);
        statement( "%s { %s; } else { %s; }", s.ifNotNull(), assignEnum, d.assign("null"));
        
        return this;
    }
    
    public CodeSourceBuilder fromObjectToObject(VariableRef d, VariableRef s, Property ip) {
        
        String mapNewObject = d.assign(format("(%s)mapperFacade.map(%s, %s, %s, mappingContext)", d.typeName(), s, usedType(s), usedType(d)));
        String mapExistingObject = format("mapperFacade.map(%s, %s, %s, %s, mappingContext)", s, d, usedType(s), usedType(d));
        String mapStmt = format(" %s { %s; } else { %s; }", d.ifNull(), mapNewObject, mapExistingObject);
        
        String ipStmt = "";
        if (ip != null) {
            VariableRef inverse = new VariableRef(ip, d);
            
            if (inverse.isCollection()) {
                ipStmt += inverse.ifNull() + inverse.assign(inverse.newCollection()) + ";";
                ipStmt += format("%s.add(%s);", inverse, d.owner());
            } else if (inverse.isArray()) {
                ipStmt += "/* TODO Orika CodeSourceBuilder.setObject does not support Arrays */";
            } else {
                ipStmt += inverse.assign(d.owner()) + ";";
            }   
        }
        
        statement("%s { %s;  %s } else { %s; }", s.ifNotNull(), mapStmt, ipStmt, d.assign("null"));  
        
        return this;
    }

    /**
     * Generate code for testing that the given variable reference is not null
     * 
     * @param p
     * @return
     */
    public CodeSourceBuilder ifNotNull(VariableRef p) {
        return newLine().append(p.ifNotNull());
    }
    
    public CodeSourceBuilder ifNull(VariableRef p) {
        return newLine().append(p.ifNull());
    }
    
    public CodeSourceBuilder ifPathNotNull(VariableRef p) {
    	return append(p.ifPathNotNull());
    }
 
    public CodeSourceBuilder assureInstanceExists(VariableRef pRef) {
        
        for (final VariableRef ref : pRef.getPath()) {
            
            if (!ClassUtil.isConcrete(ref.type())) {
                throw new MappingException("Abstract types are unsupported for nested properties. \n" + ref.name());
            }
            statement("if(%s == null) %s", ref, 
            		ref.assign("(%s)mapperFacade.newObject(source, %s, mappingContext)", ref.typeName(), usedType(ref)));
        }
                
        return this;
    }
    
    public CodeSourceBuilder ifInstanceOf(String expression, Type<?> sourceClass) {
        append("if(%s instanceof %s)", expression, sourceClass.getCanonicalName());
        return this;
    }

	public CodeSourceBuilder fromMapToMap(VariableRef d, VariableRef s, Type<?> destinationType) {
        
        ifNotNull(s).then();
        
        if (d.isAssignable()) {
            statement("if (%s == null) %s", d, d.assign(d.newMap()));
        }
        
        statement("%s.clear()", d);
        if (d.mapKeyType().equals(s.mapKeyType()) 
        	&& d.mapValueType().equals(s.mapValueType())) {
        	/*
        	 * Simple map-to-map case: both key and value types are identical
        	 */
        	statement("%s.putAll(mapperFacade.mapAsMap(%s, %s, %s, mappingContext));", d, 
                s, usedType(s.type()), usedType(d.type()));
        } else {
        	VariableRef newKey = new VariableRef(d.mapKeyType(), "_$_key");
        	VariableRef newVal = new VariableRef(d.mapValueType(), "_$_val");
        	VariableRef entry = new VariableRef(TypeFactory.valueOf(Map.Entry.class), "_$_entry");
        	VariableRef sourceKey = new MapEntryRef(s.mapKeyType(), "_$_entry", EntryPart.KEY);
        	VariableRef sourceVal = new MapEntryRef(s.mapValueType(), "_$_entry", EntryPart.VALUE);
        	/*
        	 * Loop through the individual entries, map key/value and then put them into the destination
        	 */
        	append("for( Object _$_o: %s.entrySet())", s).begin();
        	statement(entry.declare("_$_o"));
        	statement(newKey.declare());
        	statement(newVal.declare());
        	mapFields(FieldMapBuilder.mapKeys(s.mapKeyType(), d.mapKeyType()), sourceKey, newKey, null, null);
        	mapFields(FieldMapBuilder.mapValues(s.mapValueType(), d.mapValueType()), sourceVal, newVal, null, null);
        	statement("%s.put(%s, %s)", d, newKey, newVal);
        	end();
        }
        _else().statement(d.assignIfPossible("null")).end();
        
        return this;
	}
	
	
	public CodeSourceBuilder fromArrayOrCollectionToMap(VariableRef d, VariableRef s) {
        
        ifNotNull(s).then();
        
        if (d.isAssignable()) {
            statement("if (%s == null) %s", d, d.assign(d.newMap()));
        }
        statement("%s.clear()", d);
        
    	//VariableRef newKey = new VariableRef(d.mapKeyType(), "_$_key");
    	//VariableRef newVal = new VariableRef(d.mapValueType(), "_$_val");
    	VariableRef element = new VariableRef(s.elementType(), "_$_element");
    	
    	@SuppressWarnings("unchecked")
    	Type<MapEntry<Object, Object>> entryType = MapEntry.concreteEntryType((Type<? extends Map<Object, Object>>) d.type());
    			
    	VariableRef newEntry = new VariableRef(entryType, "_$_entry");
    	VariableRef newKey = new MapEntryRef(newEntry.type(), newEntry.name(), EntryPart.KEY);
    	VariableRef newVal = new MapEntryRef(newEntry.type(), newEntry.name(), EntryPart.VALUE);
    	/*
    	 * Loop through the individual entries, map key/value and then put them into the destination
    	 */
    	append("for( Object _o : %s)", s).begin();
    	statement(element.declare("_o"));
    	statement(newEntry.declare("mapperFacade.map(%s, %s, %s, mappingContext)", element, usedType(element), usedType(newEntry)));
    	//statement(newKey.declare());
    	//statement(newVal.declare());
    	
    	statement("%s.put(%s, %s)", d, newKey, newVal);
    	end();
        
        _else().statement(d.assignIfPossible("null")).end();
        
        return this;
	}
	
	
	
	/**
	 * Generates the source code to convert from a Map type to an Array type
	 * 
	 * @param d a reference to the destination 
	 * @param s a reference to the source
	 * @param inverse the destination's inverse property
	 * @param destinationType
	 * @return
	 */
	private CodeSourceBuilder fromMapToArray(VariableRef d,
			VariableRef s, Property inverse, Type<?> destinationType) {
      
		return fromArrayOrCollectionToArray(d, entrySetRef(s));
	}
	
	
	private CodeSourceBuilder fromMapToCollection(VariableRef d, VariableRef s, Property inverse, Type<?> destinationType) {
		
		return fromArrayOrCollectionToCollection(d, entrySetRef(s) , inverse, destinationType);
	}
	
	
	private VariableRef entrySetRef(VariableRef s) {
		@SuppressWarnings("unchecked")
		Type<Set<Map.Entry<Object, Object>>> sourceEntryType = TypeFactory.valueOf(Set.class, MapEntry.entryType((Type<? extends Map<Object, Object>>) s.type()));
		return new VariableRef(sourceEntryType, s + ".entrySet()");
	}
	
	public CodeSourceBuilder mapFields(FieldMap fieldMap, VariableRef sourceProperty, VariableRef destinationProperty, Type<?> destinationType, StringBuilder logDetails) {
		
		// Generate mapping code for every case     
	    Converter<Object, Object> converter = getConverter(fieldMap, fieldMap.getConverterId());
	    if (converter != null) {
	    	if (logDetails != null) {
	    		logDetails.append("using converter " + converter);
	    	}
	    	convert(destinationProperty, sourceProperty, converter);
	    } else if (mapperFactory.existsRegisteredMapper(fieldMap.getSource().getType(), fieldMap.getDestination().getType())) {	
	    	if (logDetails != null) {
	    		logDetails.append("using registered mapper");
	    	}
	    	fromObjectToObject(destinationProperty, sourceProperty, fieldMap.getInverse());
	    } else if (fieldMap.is(toAnEnumeration())) {
	    	if (logDetails != null) {
	    		logDetails.append("mapping from String or enum to enum");
	    	}
	    	fromStringOrEnumToEnum(destinationProperty, sourceProperty);
	    } else if (fieldMap.is(immutable())) {
	    	if (logDetails != null) {
	    		logDetails.append("treating as immutable (using copy-by-reference)");
	    	}
	    	copyByReference(destinationProperty, sourceProperty);
	    } else if (fieldMap.is(anArray())) {
	    	if (logDetails != null) {
	    		logDetails.append("mapping Array or Collection to Array");
	    	}
	    	fromArrayOrCollectionToArray(destinationProperty, sourceProperty);
	    } else if (fieldMap.is(aCollection())) {
	    	if (logDetails != null) {
	    		logDetails.append("mapping Array or Collection to Collection");
	    	}
	        fromArrayOrCollectionToCollection(destinationProperty, sourceProperty, fieldMap.getInverse(), destinationType);
	    } else if (fieldMap.is(aWrapperToPrimitive())) {
	    	if (logDetails != null) {
	    		logDetails.append("mapping primitive wrapper to primitive");
	    	}
	    	setPrimitive(destinationProperty, sourceProperty);
	    } else if (fieldMap.is(aMapToMap())) {
	    	if (logDetails != null) {
	    		logDetails.append("mapping Map to Map");
	    	}
	    	fromMapToMap(destinationProperty, sourceProperty, destinationType);
	    } else if (fieldMap.is(aMapToArray())) {
	    	if (logDetails != null) {
	    		logDetails.append("mapping Map to Array");
	    	}
	    	fromMapToArray(destinationProperty, sourceProperty, fieldMap.getInverse(), destinationType);
	    } else if (fieldMap.is(aMapToCollection())) {
	    	if (logDetails != null) {
	    		logDetails.append("mapping Map to Collection");
	    	}
	    	fromMapToCollection(destinationProperty, sourceProperty, fieldMap.getInverse(), destinationType);
	    } else if (fieldMap.is(anArrayOrCollectionToMap())) {
	    	if (logDetails != null) {
	    		logDetails.append("mapping Map to Array");
	    	}
	    	fromArrayOrCollectionToMap(destinationProperty, sourceProperty);
	    } else if (fieldMap.is(aPrimitiveToWrapper())) {
	    	if (logDetails != null) {
	    		logDetails.append("mapping primitive to primitive wrapper");
	    	}
	    	fromPrimitiveToWrapper(destinationProperty, sourceProperty);
	    } else if (fieldMap.is(aStringToPrimitiveOrWrapper())) {
	    	if (logDetails != null) {
	    		logDetails.append("mapping String to \"String-convertable\"");
	    	}
	    	fromStringToStringConvertable(destinationProperty, sourceProperty);
	    } else if (fieldMap.is(aConversionToString())) {
	    	if (logDetails != null) {
	    		logDetails.append("mapping Object to String");
	    	}
	    	fromAnyTypeToString(destinationProperty, sourceProperty);
	    } else {
	        /**/
	        
	        if (sourceProperty.isPrimitive() || destinationProperty.isPrimitive()) {
	        	if (logDetails != null) {
		    		logDetails.append("ignoring { Object to primitive or primitive to Object}");
		    	}
	        	newLine().append("/* Ignore field map : %s -> %s */", sourceProperty.property(), destinationProperty.property());
	        
	    	} else {
	    		if (logDetails != null) {
		    		logDetails.append("mapping Object to Object");
		    	}
	            fromObjectToObject(destinationProperty, sourceProperty, fieldMap.getInverse());
	        }
	    }
	    return this;
	}

	

	private Converter<Object, Object> getConverter(FieldMap fieldMap, String converterId) {
        Converter<Object, Object> converter = null;
        ConverterFactory converterFactory = mapperFactory.getConverterFactory();
        if (converterId != null) {
            converter = converterFactory.getConverter(converterId);
        } else {
            converter = converterFactory.getConverter(fieldMap.getSource().getType(), fieldMap.getDestination().getType());
        }
        return converter;
    }
	
}
