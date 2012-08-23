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
import static ma.glasnost.orika.impl.Specifications.aCollection;
import static ma.glasnost.orika.impl.Specifications.aConversionToString;
import static ma.glasnost.orika.impl.Specifications.aMapToArray;
import static ma.glasnost.orika.impl.Specifications.aMapToCollection;
import static ma.glasnost.orika.impl.Specifications.aMapToMap;
import static ma.glasnost.orika.impl.Specifications.aPrimitiveToWrapper;
import static ma.glasnost.orika.impl.Specifications.aStringToPrimitiveOrWrapper;
import static ma.glasnost.orika.impl.Specifications.aWrapperToPrimitive;
import static ma.glasnost.orika.impl.Specifications.anArray;
import static ma.glasnost.orika.impl.Specifications.anArrayOrCollectionToMap;
import static ma.glasnost.orika.impl.Specifications.immutable;
import static ma.glasnost.orika.impl.Specifications.toAnEnumeration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ma.glasnost.orika.Converter;
import ma.glasnost.orika.MapEntry;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingException;
import ma.glasnost.orika.converter.ConverterFactory;
import ma.glasnost.orika.impl.generator.MapEntryRef.EntryPart;
import ma.glasnost.orika.impl.util.ClassUtil;
import ma.glasnost.orika.metadata.ClassMapBuilder;
import ma.glasnost.orika.metadata.FieldMap;
import ma.glasnost.orika.metadata.FieldMapBuilder;
import ma.glasnost.orika.metadata.Property;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.metadata.TypeFactory;

/**
 * CodeSourceBuilder is a utility class used to generate the various source code
 * snippets needed to generate the Orika mapping objects
 * 
 */
public class CodeSourceBuilder {
    
    private final StringBuilder out = new StringBuilder();
    private final UsedTypesContext usedTypes;
    private final UsedConvertersContext usedConverters;
    private final MapperFactory mapperFactory;
    
    /**
     * Constructs a new instance of SourceCodeBuilder
     * 
     * @param usedTypes
     *            a context for tracking the types used in the generated mapper
     * @param usedConverters
     *            a context for tracking the converters used in the generated
     *            mapper
     * @param mapperFactory
     *            the mapper factory for which the mapper is being generated
     */
    public CodeSourceBuilder(UsedTypesContext usedTypes, UsedConvertersContext usedConverters, MapperFactory mapperFactory) {
        this.usedTypes = usedTypes;
        this.usedConverters = usedConverters;
        this.mapperFactory = mapperFactory;
    }
    
    private String usedConverter(Converter<?, ?> converter) {
        int index = usedConverters.getIndex(converter);
        return "((" + Converter.class.getCanonicalName() + ")usedConverters[" + index + "])";
    }
    
    private String usedType(Type<?> type) {
        int index = usedTypes.getIndex(type);
        return "((" + Type.class.getCanonicalName() + ")usedTypes[" + index + "])";
    }
    
    private String usedType(VariableRef r) {
        return usedType(r.type());
    }
    
    /**
     * Generate the code to use a specific converter from one type to another
     * 
     * @param d
     *            the destination variable
     * @param s
     *            the source variable
     * @param converter
     * @return a reference to <code>this</code> SourceCodeBuilder
     */
    public CodeSourceBuilder convert(VariableRef d, VariableRef s, Converter<Object, Object> converter) {
        
        String statement = d.assign("%s.convert(%s, %s)", usedConverter(converter), s.asWrapper(), usedType(d));
        
        if (s.isPrimitive()) {
            statement(statement);
        } else {
            statement(s.ifNotNull() + statement);
        }
        return this;
    }
    
    /**
     * Generate the code used to copy properties by reference
     * 
     * @param d
     *            the destination variable
     * @param s
     *            the source variable
     * @return a reference to <code>this</code> SourceCodeBuilder
     */
    public CodeSourceBuilder copyByReference(VariableRef d, VariableRef s) {
        return statement(d.assign(s));
    }
    
    /**
     * Generate the code to map from an array or collection to another
     * collection
     * 
     * @param d
     *            the destination variable
     * @param s
     *            the source variable
     * @param ip
     * @param destinationType
     * @return a reference to <code>this</code> SourceCodeBuilder
     */
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
        if (s.isArray()) {
            if (s.elementType().isPrimitive())
                newLine().append("%s.addAll(asList(%s));", d, s);
            else
                newLine().append("%s.addAll(mapperFacade.mapAsList(asList(%s), %s.class));", d, s, d.typeName());
        } else {
            
            newLine().append("%s.clear();", d);
            newLine().append("%s.addAll(mapperFacade.mapAs%s(%s, %s, %s, mappingContext));", d, d.collectionType(), s,
                    usedType(s.elementType()), usedType(d.elementType()));
        }
        if (ip != null) {
            final VariableRef inverse = new VariableRef(ip, "orikaCollectionItem");
            
            if (ip.isCollection()) {
                append(format("for (java.util.Iterator orikaIterator = %s.iterator(); orikaIterator.hasNext();) { ", d)
                        + format("    %s orikaCollectionItem = (%s) orikaIterator.next();", d.elementTypeName(), d.elementTypeName())
                        + format("    %s { %s; }", inverse.ifNull(), inverse.assign(inverse.newCollection()))
                        + format("    %s.add(%s);", inverse, d.owner()) + format("}"));
                
            } else if (ip.isArray()) {
                append(" // TODO support array");
            } else {
                append(format("for (java.util.Iterator orikaIterator = %s.iterator(); orikaIterator.hasNext();) { ", d)
                        + format("    %s orikaCollectionItem = (%s) orikaIterator.next();", d.elementTypeName(), d.elementTypeName())
                        + format("    %s;", inverse.assign(d.owner())) + format("}"));
                
            }
        }
        // End check if source property ! = null
        _else().statement(d.assignIfPossible("null")).end();
        
        return this;
    }
    
    /**
     * Generate a newline in the source code
     * 
     * @return a reference to <code>this</code> SourceCodeBuilder
     */
    public CodeSourceBuilder newLine() {
        return append("\n");
    }
    
    /**
     * Append the provided string to the current source code, replacing any
     * string formatting placeholders with the provided replacement arguments
     * 
     * @param str
     * @param args
     * @return a reference to <code>this</code> SourceCodeBuilder
     */
    public CodeSourceBuilder append(String str, Object... args) {
        out.append(String.format(str, args));
        return this;
    }
    
    /**
     * Append the provided string to the current source code
     * 
     * @param str
     * @return a reference to <code>this</code> SourceCodeBuilder
     */
    public CodeSourceBuilder append(String str) {
        out.append(str);
        return this;
    }
    
    /**
     * Appends the provided string as a source code statement, ending it with a
     * statement terminator as appropriate.
     * 
     * @param str
     * @param args
     * @return a reference to <code>this</code> SourceCodeBuilder
     */
    public CodeSourceBuilder statement(String str, Object... args) {
        if (str != null && !"".equals(str.trim())) {
            String expr = "\n" + format(str, args);
            append(expr);
            if (!expr.endsWith(";") && !expr.endsWith("}")) {
                append(";");
            }
        }
        return this;
    }
    
    /**
     * Append an open brace
     * 
     * @return a reference to <code>this</code> SourceCodeBuilder
     */
    public CodeSourceBuilder then() {
        return append("{").newLine();
    }
    
    /**
     * Append an open brace
     * 
     * @return a reference to <code>this</code> SourceCodeBuilder
     */
    public CodeSourceBuilder begin() {
        return then();
    }
    
    /**
     * Append a closing brace
     * 
     * @return a reference to <code>this</code> SourceCodeBuilder
     */
    public CodeSourceBuilder end() {
        return newLine().append("}").newLine();
    }
    
    /**
     * @return a reference to <code>this</code> SourceCodeBuilder
     */
    public CodeSourceBuilder _else() {
        return newLine().append("} else {").newLine();
    }
    
    @Override
    public String toString() {
        return out.toString();
    }
    
    /**
     * Generate mapping code to map from a string to a string-convertible type
     * 
     * @param d
     *            the destination variable
     * @param s
     *            the source variable
     * @return a reference to <code>this</code> SourceCodeBuilder
     */
    public CodeSourceBuilder fromStringToStringConvertable(final VariableRef d, final VariableRef s) {
        String value = s.toString();
        if (String.class.equals(s.rawType()) && (Character.class.equals(d.rawType()) || char.class.equals(d.rawType()))) {
            value = value + ".charAt(0)";
        }
        if (d.isPrimitive()) {
            statement(d.assign("%s.valueOf(%s)", d.wrapperTypeName(), value));
        } else {
            statement(s.ifNotNull() + d.assign("%s.valueOf(%s)", d.typeName(), value));
        }
        
        return this;
        
    }
    
    /**
     * Generate mapping code to map from any type to a string
     * 
     * @param d
     *            the destination variable
     * @param s
     *            the source variable
     * @return a reference to <code>this</code> SourceCodeBuilder
     */
    public CodeSourceBuilder fromAnyTypeToString(VariableRef d, VariableRef s) {
        
        if (s.isPrimitive()) {
            statement(d.assign("\"\"+ %s", s));
        } else {
            statement(s.ifNotNull() + d.assign("%s.toString()", s));
        }
        
        return this;
        
    }
    
    /**
     * Generate mapping code from a primitive to a primitive wrapper
     * 
     * @param d
     *            the destination variable
     * @param s
     *            the source variable
     * @return a reference to <code>this</code> SourceCodeBuilder
     */
    public CodeSourceBuilder fromPrimitiveToWrapper(VariableRef d, VariableRef s) {
        
        statement(d.assign("%s.valueOf(%s)", d.typeName(), s));
        
        return this;
    }
    
    /**
     * Generate mapping code from a primitive or primitive wrapper to primitive
     * 
     * @param d
     *            the destination variable
     * @param s
     *            the source variable
     * @return a reference to <code>this</code> SourceCodeBuilder
     */
    public CodeSourceBuilder fromPrimitiveOrWrapperToPrimitive(VariableRef d, VariableRef s) {
        
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
     *            the destination variable
     * @param s
     *            the source variable
     * @return a reference to <code>this</code> SourceCodeBuilder
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
    
    /**
     * Generate code to map from a string or enum to another enum
     * 
     * @param d
     *            the destination variable
     * @param s
     *            the source variable
     * @return a reference to <code>this</code> SourceCodeBuilder
     */
    public CodeSourceBuilder fromStringOrEnumToEnum(VariableRef d, VariableRef s) {
        
        String assignEnum = d.assign("Enum.valueOf(%s.class, \"\"+%s)", d.typeName(), s);
        statement("%s { %s; } else { %s; }", s.ifNotNull(), assignEnum, d.assign("null"));
        
        return this;
    }
    
    /**
     * Generate code to map from one object to another; this is a catch-all
     * mapping which can result in generating a new mapper for the objects which
     * have not yet been registered for mapping
     * 
     * @param d
     *            the destination variable
     * @param s
     *            the source variable
     * @param ip
     * @return a reference to <code>this</code> SourceCodeBuilder
     */
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
     * @param ref the variable reference for which to check for non-null
     * @return a reference to <code>this</code> SourceCodeBuilder
     */
    public CodeSourceBuilder ifNotNull(VariableRef ref) {
        return newLine().append(ref.ifNotNull());
    }
    
    /**
     * Append an 'if' statement checking whether the provided variable is null
     * 
     * @param ref the variable reference for which to check for null
     * @return a reference to <code>this</code> SourceCodeBuilder
     */
    public CodeSourceBuilder ifNull(VariableRef ref) {
        return newLine().append(ref.ifNull());
    }
    
    /**
     * Append an 'if' statement checking whether the path of parent variables
     * leading to this variable are all not null
     * 
     * @param ref
     *            the nested variable reference for which to check for non-null path
     * @return a reference to <code>this</code> SourceCodeBuilder
     */
    public CodeSourceBuilder ifPathNotNull(VariableRef ref) {
        return append(ref.ifPathNotNull());
    }
    
    /**
     * Append a statement which assures that the variable reference has an
     * existing instance; if it does not, a new object is generated using
     * MapperFacade.newObject
     * 
     * @param propertyRef
     *            the property or variable reference on which to check for an
     *            instance
     * @return a reference to <code>this</code> SourceCodeBuilder
     */
    public CodeSourceBuilder assureInstanceExists(VariableRef propertyRef) {
        
        for (final VariableRef ref : propertyRef.getPath()) {
            
            if (!ClassUtil.isConcrete(ref.type())) {
                throw new MappingException("Abstract types are unsupported for nested properties. \n" + ref.name());
            }
            statement("if(%s == null) %s", ref,
                    ref.assign("(%s)mapperFacade.newObject(source, %s, mappingContext)", ref.typeName(), usedType(ref)));
        }
        
        return this;
    }
    
    /**
     * Append an 'if' statement testing whether the expression is an instance of
     * the specified source type
     * 
     * @param expression
     *            the expression to test
     * @param sourceType
     *            the source type to check
     * @return a reference to <code>this</code> SourceCodeBuilder
     */
    public CodeSourceBuilder ifInstanceOf(String expression, Type<?> sourceType) {
        append("if(%s instanceof %s)", expression, sourceType.getCanonicalName());
        return this;
    }
    
    /**
     * Generate code to map from one java.util.Map to another
     * 
     * @param d
     *            the destination variable
     * @param s
     *            the source variable
     * @param destinationType
     *            the type of the destination variable
     * @return a reference to <code>this</code> SourceCodeBuilder
     */
    public CodeSourceBuilder fromMapToMap(VariableRef d, VariableRef s, Type<?> destinationType) {
        
        ifNotNull(s).then();
        
        if (d.isAssignable()) {
            statement("if (%s == null) %s", d, d.assign(d.newMap()));
        }
        
        statement("%s.clear()", d);
        if (d.mapKeyType().equals(s.mapKeyType()) && d.mapValueType().equals(s.mapValueType())) {
            /*
             * Simple map-to-map case: both key and value types are identical
             */
            statement("%s.putAll(mapperFacade.mapAsMap(%s, %s, %s, mappingContext));", d, s, usedType(s.type()), usedType(d.type()));
        } else {
            VariableRef newKey = new VariableRef(d.mapKeyType(), "_$_key");
            VariableRef newVal = new VariableRef(d.mapValueType(), "_$_val");
            VariableRef entry = new VariableRef(TypeFactory.valueOf(Map.Entry.class), "_$_entry");
            VariableRef sourceKey = new MapEntryRef(s.mapKeyType(), "_$_entry", EntryPart.KEY);
            VariableRef sourceVal = new MapEntryRef(s.mapValueType(), "_$_entry", EntryPart.VALUE);
            /*
             * Loop through the individual entries, map key/value and then put
             * them into the destination
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
    
    /**
     * Generate code to map from an Array or Collection to a Map
     * 
     * @param d
     *            the destination variable
     * @param s
     *            the source variable
     * @return a reference to <code>this</code> SourceCodeBuilder
     */
    public CodeSourceBuilder fromArrayOrCollectionToMap(VariableRef d, VariableRef s) {
        
        ifNotNull(s).then();
        
        if (d.isAssignable()) {
            statement("if (%s == null) %s", d, d.assign(d.newMap()));
        }
        statement("%s.clear()", d);
        
        VariableRef element = new VariableRef(s.elementType(), "_$_element");
        
        @SuppressWarnings("unchecked")
        Type<MapEntry<Object, Object>> entryType = MapEntry.concreteEntryType((Type<? extends Map<Object, Object>>) d.type());
        
        VariableRef newEntry = new VariableRef(entryType, "_$_entry");
        VariableRef newKey = new MapEntryRef(newEntry.type(), newEntry.name(), EntryPart.KEY);
        VariableRef newVal = new MapEntryRef(newEntry.type(), newEntry.name(), EntryPart.VALUE);
        /*
         * Loop through the individual entries, map key/value and then put them
         * into the destination
         */
        append("for( Object _o : %s)", s).begin();
        statement(element.declare("_o"));
        statement(newEntry.declare("mapperFacade.map(%s, %s, %s, mappingContext)", element, usedType(element), usedType(newEntry)));
        
        statement("%s.put(%s, %s)", d, newKey, newVal);
        end();
        
        _else().statement(d.assignIfPossible("null")).end();
        
        return this;
    }
    
    /**
     * IterableRef is a data structure to hold to variable references needed to
     * generate a parallel mapping
     * 
     */
    private static class IterableRef {
        public MultiOccurrenceVariableRef multiOccurrenceVar;
        public VariableRef elementRef;
        public MultiOccurrenceVariableRef newDestination;
        public Set<IterableRef> associations = new LinkedHashSet<IterableRef>();
    }
    
    /**
     * Generates the code to support a (potentially parallel) mapping from one
     * or more multi-occurrence fields in the source type to one or more
     * multi-occurrence fields in the destination type.
     * 
     * @param fieldMappings
     *            the field mappings to be applied
     * @param logDetails
     *            a StringBuilder to accept debug logging information
     * @return a reference to <code>this</code> SourceCodeBuilder
     */
    public CodeSourceBuilder fromMultiOccurrenceToMultiOccurrence(Set<FieldMap> fieldMappings, StringBuilder logDetails) {
        
        Map<String, IterableRef> sources = new HashMap<String, IterableRef>();
        Map<String, IterableRef> destinations = new HashMap<String, IterableRef>();
        Map<FieldMap, Set<FieldMap>> subFields = new HashMap<FieldMap, Set<FieldMap>>();
        
        for (FieldMap map : fieldMappings) {
            IterableRef srcRef = sources.get(map.getSource().getName());
            if (srcRef == null) {
                srcRef = new IterableRef();
                srcRef.multiOccurrenceVar = new MultiOccurrenceVariableRef(map.getSource(), "source");
                Type<?> elementType = srcRef.multiOccurrenceVar.elementType();
                if (MapEntry.class.equals(elementType.getRawType())) {
                    @SuppressWarnings("unchecked")
                    Type<Map<Object, Object>> mapType = (Type<Map<Object, Object>>) srcRef.multiOccurrenceVar.type();
                    elementType = MapEntry.entryType(mapType);
                }
                srcRef.elementRef = new VariableRef(elementType, srcRef.multiOccurrenceVar.name() + "_$_srcElement");
                sources.put(map.getSource().getName(), srcRef);
            }
            IterableRef destRef = destinations.get(map.getDestination().getName());
            if (destRef == null) {
                destRef = new IterableRef();
                destRef.multiOccurrenceVar = new MultiOccurrenceVariableRef(map.getDestination(), "destination");
                destRef.newDestination = new MultiOccurrenceVariableRef(map.getDestination().getType(), "new_$_"
                        + map.getDestination().getName());
                destRef.elementRef = destRef.multiOccurrenceVar.elementRef(destRef.multiOccurrenceVar.name() + "_$_dstElement");
                destinations.put(map.getDestination().getName(), destRef);
            }
            destRef.associations.add(srcRef);
            
            Set<FieldMap> elements = subFields.get(map.getBaseFieldMap());
            if (elements == null) {
                elements = new LinkedHashSet<FieldMap>();
                subFields.put(map.getBaseFieldMap(), elements);
            }
            elements.add(map.getElementMap());
        }
        
        /*
         * For any of the subField mappings which are between non-immutable
         * types
         */
        Iterator<Entry<FieldMap, Set<FieldMap>>> subfieldIter = subFields.entrySet().iterator();
        while (subfieldIter.hasNext()) {
            Entry<FieldMap, Set<FieldMap>> entry = subfieldIter.next();
            Type<?> srcType = elementType(entry.getKey().getSource().getType());
            Type<?> dstType = elementType(entry.getKey().getDestination().getType());
            if (!ClassUtil.isImmutable(dstType) && !ClassUtil.isImmutable(srcType)) {
                
                ClassMapBuilder<?, ?> builder = mapperFactory.classMap(srcType, dstType);
                
                for (FieldMap f : entry.getValue()) {
                    builder.field(f.getSource().getExpression(), f.getDestination().getExpression());
                }
                mapperFactory.registerClassMap(builder);
            }
        }
        
        return generateMultiOccurrenceMapping(sources, destinations, subFields, logDetails);
    }
    
    private String join(List<?> list, String separator) {
        StringBuilder result = new StringBuilder();
        for (Object item : list) {
            result.append(item + separator);
        }
        return result.substring(0, result.length() - separator.length());
    }
    
    /**
     * Generates the code to support a (potentially parallel) mapping from one
     * or more multi-occurrence fields in the source type to one or more
     * multi-occurrence fields in the destination type.
     * 
     * @param sources
     *            the associated source variables
     * @param destinations
     *            the associated destination variables
     * @param subFields
     *            the nested properties of the individual field maps
     * @param logDetails
     *            a StringBuilder to accept debug logging information
     * @return a reference to <code>this</code> CodeSourceBuilder
     */
    public CodeSourceBuilder generateMultiOccurrenceMapping(Map<String, IterableRef> sources, Map<String, IterableRef> destinations,
            Map<FieldMap, Set<FieldMap>> subFields, StringBuilder logDetails) {
        
        List<String> sourceSizes = new ArrayList<String>();
        for (IterableRef ref : sources.values()) {
            statement(ref.multiOccurrenceVar.declareIterator());
            sourceSizes.add(ref.multiOccurrenceVar.size());
        }
        String sizeExpr = "min(" + join(sourceSizes, ",") + ")";
        for (IterableRef destRef : destinations.values()) {
            statement(destRef.newDestination.declare(destRef.newDestination.newInstance(sizeExpr)));
            if (destRef.newDestination.isArray()) {
                statement(destRef.newDestination.declareIterator());
            }
        }
        
        append("while (");
        Iterator<IterableRef> sourcesIter = sources.values().iterator();
        while (sourcesIter.hasNext()) {
            IterableRef ref = sourcesIter.next();
            append(ref.multiOccurrenceVar.iteratorHasNext());
            if (sourcesIter.hasNext()) {
                append(" && ");
            }
            
        }
        append(") {");
        
        // get the next elements from the src iterators
        for (IterableRef srcRef : sources.values()) {
            statement(srcRef.elementRef.declare(srcRef.multiOccurrenceVar.nextElement()));
        }
        
        // apply the appropriate mappings onto the destination elements
        for (IterableRef destRef : destinations.values()) {
            
            if (ClassUtil.isImmutable(destRef.elementRef.type())) {
                statement(destRef.elementRef.declare());
            } else {
                statement(destRef.elementRef.declare("mapperFacade.newObject(%s, %s, mappingContext)", destRef.associations.iterator()
                        .next().elementRef, usedType(destRef.elementRef.type())));
            }
            
            for (IterableRef srcRef : destRef.associations) {
                
                /*
                 * check through the subFields mapped to find one with source
                 * and destination matching the types of the source and
                 * destination elements
                 */
                Iterator<Entry<FieldMap, Set<FieldMap>>> subfieldIter = subFields.entrySet().iterator();
                while (subfieldIter.hasNext()) {
                    Entry<FieldMap, Set<FieldMap>> entry = subfieldIter.next();
                    if (elementType(entry.getKey().getSource().getType()).isAssignableFrom(srcRef.elementRef.type())
                            && elementType(entry.getKey().getDestination().getType()).isAssignableFrom(destRef.elementRef.type())) {
                        
                        for (FieldMap subMap : entry.getValue()) {
                            VariableRef src = ("".equals(subMap.getSource().getExpression()) ? srcRef.elementRef : new VariableRef(
                                    subMap.getSource(), srcRef.elementRef));
                            VariableRef dest = ("".equals(subMap.getDestination().getExpression()) ? destRef.elementRef : new VariableRef(
                                    subMap.getDestination(), destRef.elementRef));
                            mapFields(subMap, src, dest, destRef.elementRef.type(), logDetails);
                            
                        }
                        
                        subfieldIter.remove();
                    }
                }
                
            }
            // add the new destination elements to their respective collections
            statement(destRef.newDestination.add(destRef.elementRef));
        }
        
        append("}");
        
        for (IterableRef destRef : destinations.values()) {
            statement(destRef.multiOccurrenceVar.assign(destRef.newDestination));
        }
        
        return this;
    }
    
    @SuppressWarnings("unchecked")
    private Type<?> elementType(Type<?> multiOccurrenceType) {
        if (multiOccurrenceType.isArray()) {
            return multiOccurrenceType.getComponentType();
        } else if (multiOccurrenceType.isMap()) {
            return MapEntry.entryType((Type<Map<Object, Object>>) multiOccurrenceType);
        } else if (multiOccurrenceType.isCollection()) {
            return multiOccurrenceType.getNestedType(0);
        } else {
            throw new IllegalArgumentException(multiOccurrenceType + " is not a supported multi-occurrence type");
        }
    }
    
    /**
     * Finds all field maps out of the provided set which are associated with
     * the map passed in ( including that map itself)
     * 
     * @param fieldMaps
     *            the set of all field maps
     * @param map
     *            the field map from which to start searching for reference
     * @return a Set of FieldMaps which are associated; they must be mapped in
     *         parallel
     */
    public Set<FieldMap> getAssociatedMappings(Collection<FieldMap> fieldMaps, FieldMap map) {
        
        Set<FieldMap> associated = new LinkedHashSet<FieldMap>();
        associated.add(map);
        Set<FieldMap> unprocessed = new LinkedHashSet<FieldMap>(fieldMaps);
        unprocessed.remove(map);
        
        Set<String> nextRoundSources = new LinkedHashSet<String>();
        Set<String> nextRoundDestinations = new LinkedHashSet<String>();
        Set<String> thisRoundSources = Collections.singleton(map.getSource().getName());
        Set<String> thisRoundDestinations = Collections.singleton(map.getDestination().getName());
        
        while (!unprocessed.isEmpty() && !(thisRoundSources.isEmpty() && thisRoundDestinations.isEmpty())) {
            
            Iterator<FieldMap> iter = unprocessed.iterator();
            while (iter.hasNext()) {
                FieldMap f = iter.next();
                boolean containsSource = thisRoundSources.contains(f.getSource().getName());
                boolean containsDestination = thisRoundDestinations.contains(f.getDestination().getName());
                if (containsSource && containsDestination) {
                    associated.add(f);
                    iter.remove();
                } else if (containsSource) {
                    associated.add(f);
                    iter.remove();
                    nextRoundDestinations.add(f.getDestination().getName());
                } else if (containsDestination) {
                    associated.add(f);
                    iter.remove();
                    nextRoundSources.add(f.getSource().getName());
                }
            }
            
            thisRoundSources = nextRoundSources;
            thisRoundDestinations = nextRoundDestinations;
            nextRoundSources = new LinkedHashSet<String>();
            nextRoundDestinations = new LinkedHashSet<String>();
        }
        
        return associated;
    }
    
    /**
     * Generates the source code to convert from a Map type to an Array type
     * 
     * @param d
     *            a reference to the destination
     * @param s
     *            a reference to the source
     * @param inverse
     *            the destination's inverse property
     * @param destinationType
     * @return a reference to <code>this</code> CodeSourceBuilder
     */
    private CodeSourceBuilder fromMapToArray(VariableRef d, VariableRef s, Property inverse, Type<?> destinationType) {
        
        return fromArrayOrCollectionToArray(d, entrySetRef(s));
    }
    
    /**
     * Generate the code to map from a java.util.Map to a Collection
     * 
     * @param d
     *            the destination variable
     * @param s
     *            the source variable
     * @param inverse
     *            the inverse property
     * @param destinationType
     * @return
     */
    private CodeSourceBuilder fromMapToCollection(VariableRef d, VariableRef s, Property inverse, Type<?> destinationType) {
        
        return fromArrayOrCollectionToCollection(d, entrySetRef(s), inverse, destinationType);
    }
    
    private VariableRef entrySetRef(VariableRef s) {
        @SuppressWarnings("unchecked")
        Type<?> sourceEntryType = TypeFactory.valueOf(Set.class, MapEntry.entryType((Type<? extends Map<Object, Object>>) s.type()));
        return new VariableRef(sourceEntryType, s + ".entrySet()");
    }
    
    /**
     * Generate the code necessary to process the provided FieldMap.
     * 
     * @param fieldMap
     *            the FieldMap describing fields to be mapped
     * @param sourceProperty
     *            a variable referece to the source property
     * @param destinationProperty
     *            a variable reference to the destination property
     * @param destinationType
     *            the destination's type
     * @param logDetails
     *            a StringBuilder to contain the debug output
     * @return a reference to <code>this</code> CodeSourceBuilder
     */
    public CodeSourceBuilder mapFields(FieldMap fieldMap, VariableRef sourceProperty, VariableRef destinationProperty,
            Type<?> destinationType, StringBuilder logDetails) {
        
        if (sourceProperty.isNestedProperty()) {
            ifPathNotNull(sourceProperty).then();
        }
        
        if (destinationProperty.isNestedProperty()) {
            if (!sourceProperty.isPrimitive()) {
                ifNotNull(sourceProperty).then();
            }
            assureInstanceExists(destinationProperty);
        }
        
        // Generate mapping code for every case
        Converter<Object, Object> converter = getConverter(fieldMap, fieldMap.getConverterId());
        if (converter != null) {
            if (logDetails != null) {
                logDetails.append("using converter " + converter);
            }
            convert(destinationProperty, sourceProperty, converter);
        } else if (mapperFactory.existsRegisteredMapper(fieldMap.getSource().getType(), fieldMap.getDestination().getType(), true)) {
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
            fromPrimitiveOrWrapperToPrimitive(destinationProperty, sourceProperty);
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
        if (destinationProperty.isNestedProperty()) {
            if (!sourceProperty.isPrimitive()) {
                end();
            }
        }
        // Close up, and set null to destination
        if (sourceProperty.isNestedProperty()) {
            end();
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
