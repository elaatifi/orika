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
import static ma.glasnost.orika.impl.Specifications.aBeanToArrayOrList;
import static ma.glasnost.orika.impl.Specifications.aBeanToMap;
import static ma.glasnost.orika.impl.Specifications.aCollection;
import static ma.glasnost.orika.impl.Specifications.aConversionToString;
import static ma.glasnost.orika.impl.Specifications.aMapToArray;
import static ma.glasnost.orika.impl.Specifications.aMapToBean;
import static ma.glasnost.orika.impl.Specifications.aMapToCollection;
import static ma.glasnost.orika.impl.Specifications.aMapToMap;
import static ma.glasnost.orika.impl.Specifications.aPrimitiveToWrapper;
import static ma.glasnost.orika.impl.Specifications.aStringToPrimitiveOrWrapper;
import static ma.glasnost.orika.impl.Specifications.aWrapperToPrimitive;
import static ma.glasnost.orika.impl.Specifications.anArray;
import static ma.glasnost.orika.impl.Specifications.anArrayOrCollectionToMap;
import static ma.glasnost.orika.impl.Specifications.anArrayOrListToBean;
import static ma.glasnost.orika.impl.Specifications.immutable;
import static ma.glasnost.orika.impl.Specifications.toAnEnumeration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import ma.glasnost.orika.BoundMapperFacade;
import ma.glasnost.orika.Converter;
import ma.glasnost.orika.MapEntry;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingException;
import ma.glasnost.orika.converter.ConverterFactory;
import ma.glasnost.orika.impl.generator.MapEntryRef.EntryPart;
import ma.glasnost.orika.impl.generator.UsedMapperFacadesContext.UsedMapperFacadesIndex;
import ma.glasnost.orika.impl.util.ClassUtil;
import ma.glasnost.orika.metadata.ClassMap;
import ma.glasnost.orika.metadata.ClassMapBuilder;
import ma.glasnost.orika.metadata.FieldMap;
import ma.glasnost.orika.metadata.FieldMapBuilder;
import ma.glasnost.orika.metadata.MapperKey;
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
    private final UsedMapperFacadesContext usedMapperFacades;
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
    public CodeSourceBuilder(UsedTypesContext usedTypes, UsedConvertersContext usedConverters, UsedMapperFacadesContext usedMappers, MapperFactory mapperFactory) {
        this.usedTypes = usedTypes;
        this.usedConverters = usedConverters;
        this.mapperFactory = mapperFactory;
        this.usedMapperFacades = usedMappers;
    }
    
    private String usedConverter(Converter<?, ?> converter) {
        int index = usedConverters.getIndex(converter);
        return "((" + Converter.class.getCanonicalName() + ")usedConverters[" + index + "])";
    }
    
    private String usedType(Type<?> type) {
        int index = usedTypes.getIndex(type);
        return "((" + Type.class.getCanonicalName() + ")usedTypes[" + index + "])";
    }
    
    private String usedMapperFacadeCall(VariableRef source, VariableRef destination) {
        return usedMapperFacadeCall(source.type(), destination.type());
    }
    
    private String usedMapperFacadeCall(Type<?> sourceType, Type<?> destinationType) {
        UsedMapperFacadesIndex usedFacade = usedMapperFacades.getIndex(sourceType, destinationType, mapperFactory);
        String mapInDirection = usedFacade.isReversed ? "mapReverse" : "map";
        return "((" + BoundMapperFacade.class.getCanonicalName() + ")usedMapperFacades[" + usedFacade.index + "])." + mapInDirection + "";
    }
    
    private String callMapper(Type<?> sourceType, Type<?> destinationType, String sourceExpression, String destExpression) {
        return usedMapperFacadeCall(sourceType, destinationType) + "(" + sourceExpression + ", " + destExpression + ", mappingContext)";
    }
    
    private String callMapper(Type<?> sourceType, Type<?> destinationType, String sourceExpression) {
        return usedMapperFacadeCall(sourceType, destinationType) + "(" + sourceExpression + ", mappingContext)";
    }
    
    private String callMapper(VariableRef source, VariableRef destination) {
        return callMapper(source.type(), destination.type(), ""+source, ""+destination);
    }
    
    private String callMapper(VariableRef source, Type<?> destination) {
        return callMapper(source.type(), destination, ""+source);
    }
    
    private String usedMapperFacadeNewObjectCall(VariableRef source, VariableRef destination) {
        return usedMapperFacadeNewObjectCall(source.type(), destination.type());
    }
    
    private String usedMapperFacadeNewObjectCall(Type<?> sourceType, Type<?> destinationType) {
        UsedMapperFacadesIndex usedFacade = usedMapperFacades.getIndex(sourceType, destinationType, mapperFactory);
        String instantiateMethod = usedFacade.isReversed ? "newObject" : "newObjectReverse";
        return "((" + BoundMapperFacade.class.getCanonicalName() + ")usedMapperFacades[" + usedFacade.index + "])." + instantiateMethod + "";
    }
    
    private String newObjectFromMapper(Type<?> sourceType, Type<?> destinationType, String sourceExpr) {
        return usedMapperFacadeNewObjectCall(sourceType, destinationType) + "(" + sourceExpr + ", mappingContext)";
    }
    
    private String newObjectFromMapper(VariableRef source, Type<?> destinationType) {
        return usedMapperFacadeNewObjectCall(source.type(), destinationType) + "(" + source + ", mappingContext)";
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
    public CodeSourceBuilder fromArrayOrCollectionToCollection(VariableRef dest, VariableRef src, Property ip) {
        
        MultiOccurrenceVariableRef s = MultiOccurrenceVariableRef.from(src);
        MultiOccurrenceVariableRef d = MultiOccurrenceVariableRef.from(dest);
        
        final Class<?> dc = dest.getOwner().rawType();
        final Class<?> destinationElementClass = d.elementType().getRawType();
        
        if (destinationElementClass == null) {
            throw new MappingException("cannot determine runtime type of destination collection " + dc.getName() + "." + d.name());
        }
        
        // Start check if source property ! = null
        ifNotNull(s).then();
        
        if (d.isAssignable()) {
            statement("if (%s == null) %s", d, d.assign(d.newInstance(src.size())));
        }
        
        if (s.isArray()) {
            if (s.elementType().isPrimitive())
                newLine().append("%s.addAll(asList(%s));", d, s);
            else
                newLine().append("%s.addAll(mapperFacade.mapAsList(asList(%s), %s.class, mappingContext));", d, s, d.typeName());
        } else {
            
            newLine().append("%s.clear();", d);
            newLine().append("%s.addAll(mapperFacade.mapAs%s(%s, %s, %s, mappingContext));", d, d.collectionType(), s,
                    usedType(s.elementType()), usedType(d.elementType()));
        }
        if (ip != null) {
            final MultiOccurrenceVariableRef inverse = new MultiOccurrenceVariableRef(ip, "orikaCollectionItem");
            
            if (ip.isCollection()) {
                append(format("for (java.util.Iterator orikaIterator = %s.iterator(); orikaIterator.hasNext();) { ", d)
                        + format("    %s orikaCollectionItem = (%s) orikaIterator.next();", d.elementTypeName(), d.elementTypeName())
                        + format("    %s { %s; }", inverse.ifNull(), inverse.assignIfPossible(inverse.newCollection()))
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
    
    public CodeSourceBuilder insert(int position, String str) {
        out.insert(position, str);
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
            String expr = format(str, args);
            if (!this.out.toString().endsWith("\n") && !expr.startsWith("\n")) {
                append("\n");
            }
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
    public CodeSourceBuilder fromStringToEnum(VariableRef d, VariableRef s) {
        
        String assignEnum = d.assign("Enum.valueOf(%s.class, \"\"+%s)", d.typeName(), s);
        statement("%s { %s; } else { %s; }", s.ifNotNull(), assignEnum, d.assign("null"));
        
        return this;
    }
    
    /**
     * Generate code to map from an enum to another enum
     * 
     * @param d
     *            the destination variable
     * @param s
     *            the source variable
     * @return a reference to <code>this</code> SourceCodeBuilder
     */
    public CodeSourceBuilder fromEnumToEnum(VariableRef d, VariableRef s) {
        
        String assignEnum = d.assign("Enum.valueOf(%s.class, %s.name())", d.typeName(), s);
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
        
        String mapNewObject = d.assign(format("(%s)%s(%s, mappingContext)", d.typeName(), usedMapperFacadeCall(s, d), s));
        //String mapNewObject = d.assign(format("(%s)mapperFacade.map(%s, %s, %s, mappingContext)", d.typeName(), s, usedType(s), usedType(d)));
        //String mapExistingObject = format("mapperFacade.map(%s, %s, %s, %s, mappingContext)", s, d, usedType(s), usedType(d));
        String mapExistingObject = format("%s(%s, %s, mappingContext)", usedMapperFacadeCall(s, d), s, d);
        String mapStmt = format(" %s { %s; } else { %s; }", d.ifNull(), mapNewObject, mapExistingObject);
        
        String ipStmt = "";
        if (ip != null) {
            VariableRef inverse = new VariableRef(ip, d);
            
            if (inverse.isCollection()) {
                MultiOccurrenceVariableRef inverseCollection = MultiOccurrenceVariableRef.from(inverse);
                ipStmt += inverse.ifNull() + inverse.assign(inverseCollection.newCollection()) + ";";
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
     * @param ref
     *            the variable reference for which to check for non-null
     * @return a reference to <code>this</code> SourceCodeBuilder
     */
    public CodeSourceBuilder ifNotNull(VariableRef ref) {
        return newLine().append(ref.ifNotNull());
    }
    
    /**
     * Append an 'if' statement checking whether the provided variable is null
     * 
     * @param ref
     *            the variable reference for which to check for null
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
     *            the nested variable reference for which to check for non-null
     *            path
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
    public CodeSourceBuilder assureInstanceExists(VariableRef propertyRef, VariableRef source) {
        
        for (final VariableRef ref : propertyRef.getPath()) {
            
            if (!ClassUtil.isConcrete(ref.type())) {
                throw new MappingException("Abstract types are unsupported for nested properties. \n" + ref.name());
            }
            
            statement("if(%s == null) %s", ref,
                    ref.assign("(%s)%s(source, mappingContext)", ref.typeName(), usedMapperFacadeNewObjectCall(ref,source)));
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
    public CodeSourceBuilder fromMapToMap(VariableRef dest, VariableRef src) {
        
        MultiOccurrenceVariableRef d = MultiOccurrenceVariableRef.from(dest);
        MultiOccurrenceVariableRef s = MultiOccurrenceVariableRef.from(src);
        
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
            mapFields(FieldMapBuilder.mapKeys(s.mapKeyType(), d.mapKeyType()), sourceKey, newKey, null);
            mapFields(FieldMapBuilder.mapValues(s.mapValueType(), d.mapValueType()), sourceVal, newVal, null);
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
    public CodeSourceBuilder fromArrayOrCollectionToMap(VariableRef dest, VariableRef src) {
        
        MultiOccurrenceVariableRef d = MultiOccurrenceVariableRef.from(dest);
        MultiOccurrenceVariableRef s = MultiOccurrenceVariableRef.from(src);
        
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
        public IterableRef parent;
        
        public IterableRef() {}
        public IterableRef(IterableRef parent) {
            this.parent = parent;
        }
    }
    
    
    
    
    private static class Node {
        
        public Property property;
        public MultiOccurrenceVariableRef multiOccurrenceVar;
        public MultiOccurrenceVariableRef newDestination;
        public VariableRef elementRef;
        public FieldMap value;
        public List<Node> children = new ArrayList<Node>();
        public Node parent;
        public Set<Node> mapped = new HashSet<Node>();
        
        private Node(Property property, FieldMap fieldMap, Node parent, List<Node> nodes, boolean isSource) {
            
            String name = isSource ? "source" : "destination";
            this.value = fieldMap;
            this.parent = parent;
            this.property = property;
            
            if (property.isMultiOccurrence()) {
                Type<?> elementType = null;
                if (property.isMap()) {
                    elementType = property.getType().getNestedType(1);
                } else if (property.isCollection()) {
                    elementType = property.getElementType();
                } else if (property.isArray()) {
                    elementType = property.getType().getComponentType();
                } 
                
                this.newDestination = new MultiOccurrenceVariableRef(property.getType(), "new_" + name);  
                String multiOccurrenceName;
                if (parent != null) {
                    multiOccurrenceName = name(parent.elementRef.name(),name);
                } else /*if (isSource)*/ {
                    multiOccurrenceName = name;
                } /*else {
                    multiOccurrenceName = newDestination.name();
                }*/
                this.multiOccurrenceVar = new MultiOccurrenceVariableRef(property, multiOccurrenceName);
                this.elementRef = new VariableRef(elementType, property.getName() + "_" + name+ "Element");
            } 
            
            if (nodes !=null) {
                nodes.add(this);
            } else if (parent != null) {
                parent.children.add(this);
            }
        }
        
        public Node(Property property, Node parent, boolean isSource) {
            this(property, null, parent, null, isSource);
        }
        
        public Node(Property property, FieldMap fieldMap, Node parent, boolean isSource) {
            this(property, fieldMap, parent, null, isSource);
        }
        
        public Node(Property property, FieldMap fieldMap, List<Node> nodes, boolean isSource) {
            this(property, fieldMap, null, nodes, isSource);
        }
        
        private String name(String value1, String defaultValue) {
            if (value1 != null && !"".equals(value1)) {
                return value1;
            } else {
                return defaultValue;
            }
        }
        
        public boolean isLeaf() {
            return children.isEmpty();
        }
        
        /**
         * @param type
         * @param isSource
         * @return
         */
        public FieldMap getMap() {
            Node node = null;
            TreeMap<Integer, FieldMap> nodes = new TreeMap<Integer, FieldMap>();
            
            for (Node child: children) {
                if (child.value != null) {
                    boolean isSource = false;
                    if (this.multiOccurrenceVar.type().equals(child.value.getSource().getContainer().getType())) {
                        isSource = true;
                    }
                    
                    int depth = 0;
                    FieldMap value = child.value;
                    Property prop = isSource ? value.getSource() : value.getDestination();
                    while (prop.getContainer() != null) {
                        ++depth;
                        prop = prop.getContainer();
                    }
                    
                    if (!nodes.containsKey(Integer.valueOf(depth))) {
                        nodes.put(Integer.valueOf(depth), value);
                    }
                }
            }
            if (!nodes.isEmpty()) {
                return nodes.firstEntry().getValue();
            } else {
                return null;
            }
        }
        
        public boolean isMapped(Node node) {
            return mapped.contains(node);
        }
        
        public void mapped(Node node) {
            mapped.add(node);
        }
        public static Node findFieldMap(final FieldMap map, final List<Node> nodes, boolean useSource) {
            LinkedList<Property> path = new LinkedList<Property>();
            Property root = useSource ? map.getSource() : map.getDestination();
            Property container = root;
            while (container.getContainer() != null) {
                path.addFirst(container.getContainer());
                container = container.getContainer();
            }
            Node currentNode = null;
            List<Node> children = nodes;
            
            for(int p = 0, len=path.size(); p < len; ++p) {
                Property pathElement = path.get(p);
                currentNode = null;
                for (Node node: children) {
                    if (node.property.equals(pathElement)) {
                       currentNode = node;
                       children = currentNode.children;
                       break;
                    }
                }
                if (currentNode == null) {
                    return null;
                }
            }
            
            for (Node node: children) {
                if (map.equals(node.value)) {
                    return node;
                }
            }
            return null;
        }
        
        public static Node addFieldMap(final FieldMap map, final List<Node> nodes, boolean useSource) {
            LinkedList<Property> path = new LinkedList<Property>();
            Property root = useSource ? map.getSource() : map.getDestination();
            Property container = root;
            while (container.getContainer() != null) {
                path.addFirst(container.getContainer());
                container = container.getContainer();
            }
            /*
             * Attempt to locate the path within the tree of nodes
             * under which this fieldMap should be placed
             */
            Node currentNode = null;
            Node parentNode = null;
            List<Node> children = nodes;
            
            for(int p = 0, len=path.size(); p < len; ++p) {
                Property pathElement = path.get(p);
                
                for (Node node: children) {
                    if (node.property.equals(pathElement)) {
                       currentNode = node;
                       children = currentNode.children;
                       break;
                    }
                }
                if (currentNode == null) {
                    
                    currentNode = new Node(pathElement, parentNode, useSource);
                    if (parentNode == null) {
                        nodes.add(currentNode);
                    }
                    parentNode = currentNode;
                    for (p+=1; p < len; ++p) {
                        currentNode = new Node(path.get(p), parentNode, useSource);
                        parentNode = currentNode;
                    }
                } else {
                    parentNode = currentNode;
                    currentNode = null;
                }
            }
            /*
             * Finally add a node for the fieldMap at the end
             */
            if (parentNode == null) {
                currentNode = new Node(root, map, nodes, useSource);
            } else {
                currentNode = new Node(root, map, parentNode, useSource);
            }
                
            return currentNode;
        }
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
    public Set<FieldMap> fromMultiOccurrenceToMultiOccurrence(Set<FieldMap> fieldMappings, StringBuilder logDetails) {
        
        List<Node> sourceNodes = new ArrayList<Node>();
        List<Node> destNodes = new ArrayList<Node>();
        
        for (FieldMap map : fieldMappings) {

            Node.addFieldMap(map, sourceNodes, true);
            Node.addFieldMap(map, destNodes, false);
        }    
          
        return generateMultiOccurrenceMapping(sourceNodes, destNodes, fieldMappings, logDetails);
    }
    
    private String join(List<?> list, String separator) {
        StringBuilder result = new StringBuilder();
        for (Object item : list) {
            result.append(item + separator);
        }
        return result.length() > 0 ? result.substring(0, result.length() - separator.length()) : "";
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
    public Set<FieldMap> generateMultiOccurrenceMapping(List<Node> sourceNodes, List<Node> destNodes,
            Set<FieldMap> subFields, StringBuilder logDetails) {
        
        Map<MapperKey, ClassMapBuilder<?,?>> builders = new HashMap<MapperKey, ClassMapBuilder<?,?>>();
        List<String> sourceSizes = new ArrayList<String>();
        for (Node ref : sourceNodes) {
            if (!ref.isLeaf()) {
                sourceSizes.add(ref.multiOccurrenceVar.size());
            }
        }
        
        String sizeExpr = "min(" + join(sourceSizes, ",") + ")";
        
        for (Node destRef : destNodes) {
            
            statement(destRef.newDestination.declare(destRef.newDestination.newInstance(sizeExpr)));
            if (destRef.newDestination.isArray()) {
                statement(destRef.newDestination.declareIterator());
            }
            List<Node> children = new ArrayList<Node>();
            children.add(destRef);
            while (!children.isEmpty()) {
                Node child = children.remove(0);
                children.addAll(child.children);
                if (child.elementRef != null) {
                    statement(child.elementRef.declare());
                }
            }
        }
        
        StringBuilder endWhiles = new StringBuilder();
        iterateSources(sourceNodes, destNodes, endWhiles);
        
        LinkedList<Node> stack = new LinkedList<Node>(destNodes);
        while (!stack.isEmpty()) {
            
            Node currentNode = stack.removeFirst();
            stack.addAll(0, currentNode.children);
            Node srcNode = null;
            if (currentNode.value != null) {
                srcNode = Node.findFieldMap(currentNode.value, sourceNodes, true);
            } else {
                srcNode = Node.findFieldMap(currentNode.getMap(), sourceNodes, true).parent;
            }
            
            if (!currentNode.isLeaf()) { 
                
                append("if ( " + currentNode.elementRef.isNull() + orCurrentElementComparator(srcNode, currentNode) + ")").begin();
                statement(currentNode.elementRef.assign(newObjectFromMapper(currentNode.elementRef, currentNode.elementRef.type())));
                statement(currentNode.multiOccurrenceVar.add(currentNode.elementRef));
                end();
            }
            
            if (currentNode.value != null) {
                if (!currentNode.parent.isMapped(srcNode.parent) && srcNode.parent != null) {
                    statement(callMapper(srcNode.parent.elementRef, currentNode.parent.elementRef)).newLine();;
                    currentNode.parent.mapped(srcNode.parent);
                }
                if (srcNode.parent != null 
                        && srcNode.parent.elementRef != null 
                        && currentNode.parent != null 
                        && currentNode.parent.elementRef != null) {
                    
                    MapperKey key = new MapperKey(srcNode.parent.elementRef.type(), currentNode.parent.elementRef.type());
                    if (!mapperFactory.existsRegisteredMapper(key.getAType(), key.getBType(), true)) {
                        ClassMapBuilder<?,?> builder = builders.get(key);
                        if (builder == null) {
                            builder = mapperFactory.classMap(key.getAType(), key.getBType());
                            builders.put(key, builder);
                        }
                        builder.fieldMap(currentNode.value.getSource().getName(), currentNode.value.getDestination().getName()).add();
                    }
                }
            }
        }  
        
        append(endWhiles.toString());
        
        for (Node destRef : destNodes) {
            if ("".equals(destRef.multiOccurrenceVar.name())) {
                newLine();
                statement("%s.addAll(%s)",destRef.multiOccurrenceVar, destRef.newDestination);
                newLine();
            }
        }
        
        for (ClassMapBuilder<?,?> builder: builders.values()) {
            builder.register();
        }
        
        return subFields;
    }
    
    private void iterateSources(List<Node> sourceNodes, List<Node> destNodes, StringBuilder endWhiles) {
        
        if (!sourceNodes.isEmpty()) {
            for (Node srcRef : sourceNodes) {
                if (!srcRef.isLeaf()) {
                    statement(srcRef.multiOccurrenceVar.declareIterator());
                }
            }
            
            StringBuilder loopSource = new StringBuilder();
            /*
             * Create while loop for the top level multi-occurrence objects
             */
            loopSource.append("while (");
            Iterator<Node> sourcesIter = sourceNodes.iterator();
            boolean atLeastOneIter = false;
            while (sourcesIter.hasNext()) {
                Node ref = sourcesIter.next();
                if (!ref.isLeaf()) {
                    if (atLeastOneIter) {
                        loopSource.append(" && ");
                    }
                    loopSource.append(ref.multiOccurrenceVar.iteratorHasNext());
                    atLeastOneIter = true;
                }
            }
            loopSource.append(") {");
            
            if (atLeastOneIter) {
                newLine().append(loopSource.toString());
            }
            for (Node srcRef : sourceNodes) {
                
                if (!srcRef.isLeaf()) {
                    statement(srcRef.elementRef.declare(srcRef.multiOccurrenceVar.nextElement()));
//                    Node destRef = Node.findFieldMap(srcRef.getMap(), destNodes, false).parent;
//                    if (!destRef.isMapped(srcRef)) {    
//                        append("if ( " + destRef.elementRef.isNull() + orCurrentElementComparator(srcRef, destRef) + ")").begin();
//                        statement(destRef.elementRef.assign(newObjectFromMapper(destRef.elementRef, destRef.elementRef.type())));
//                        statement(destRef.multiOccurrenceVar.add(destRef.elementRef));
//                        end();
//                        destRef.mapped(srcRef);
//                    }
                    iterateSources(srcRef.children, destNodes, endWhiles);
                }
            }
            if (atLeastOneIter) {
                endWhiles.append("}\n");
            }
        }
    }
    
    
    private String orCurrentElementComparator(Node source, Node dest) {
        // TODO:
        if (source == null) {
            return  " || " + "true";
        } else {
            return "";
        }
    }
    
    /**
     * Generates source code to compare the fields of the source and
     * destination variables.
     * 
     * @param source
     * @param dest
     * @return
     */
    private String testEquality(VariableRef source, VariableRef dest) {
        ClassMap<?,?> classMap = mapperFactory.getClassMap(new MapperKey(source.type(), dest.type()));
        
        
        
        return null;
    }
    
    /**
     * @param d
     * @param src
     * @return
     */
    public CodeSourceBuilder applyToMultiOccurrence(VariableRef d, VariableRef src, VariableRef destElement) {
        
        MultiOccurrenceVariableRef dest = MultiOccurrenceVariableRef.from(d);
        append(dest.ifNull());
        statement(dest.assignIfPossible(dest.newCollection()));
        if (destElement != null) {
            statement(callMapper(src, destElement));
        } else {
            statement(dest.add(callMapper(src, dest.type())));
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
            return multiOccurrenceType;
            //throw new IllegalArgumentException(multiOccurrenceType + " is not a supported multi-occurrence type");
        }
    }
    
    private Property root(Property prop) {
        Property root = prop;
        while (root.getContainer() != null) {
            root = root.getContainer();
        }
        return root;
    }
    
    /**
     * Finds all field maps out of the provided set which are associated with
     * the map passed in ( including that map itself); by "associated", we mean
     * any mappings which are connected to the original FieldMap by having a
     * matching source or destination, including transitive associations.
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
        Set<String> thisRoundSources = Collections.singleton(root(map.getSource()).getExpression());
        Set<String> thisRoundDestinations = Collections.singleton(root(map.getDestination()).getExpression());
        
        while (!unprocessed.isEmpty() && !(thisRoundSources.isEmpty() && thisRoundDestinations.isEmpty())) {
            
            Iterator<FieldMap> iter = unprocessed.iterator();
            while (iter.hasNext()) {
                FieldMap f = iter.next();
                boolean containsSource = thisRoundSources.contains(root(f.getSource()).getExpression());
                boolean containsDestination = thisRoundDestinations.contains(root(f.getDestination()).getExpression());
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
    private CodeSourceBuilder fromMapToArray(VariableRef d, VariableRef s, Property inverse) {
        
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
    private CodeSourceBuilder fromMapToCollection(VariableRef d, VariableRef s, Property inverse) {
        
        return fromArrayOrCollectionToCollection(d, entrySetRef(s), inverse);
    }
    
    private VariableRef entrySetRef(VariableRef s) {
        @SuppressWarnings("unchecked")
        Type<?> sourceEntryType = TypeFactory.valueOf(Set.class, MapEntry.entryType((Type<? extends Map<Object, Object>>) s.type()));
        return new VariableRef(sourceEntryType, s + ".entrySet()");
    }
       
    public CodeSourceBuilder fromMapElementToObject(VariableRef d, VariableRef s) {
        statement(d.assign(d.cast(s)));
        return this;
    }
    
    public CodeSourceBuilder fromObjectToMapElement(VariableRef d, VariableRef s) {
        statement(d.assign(s));
        return this;
    } 
    
 
    public CodeSourceBuilder fromArrayOrListElementToObject(VariableRef d, VariableRef s) {
        statement(d.assign(d.cast(s)));
        return this;
    }
    
    public CodeSourceBuilder fromObjectToArrayOrListElement(VariableRef d, VariableRef s) {
        statement(d.assign(s));
        return this;
    }
    
    /**
     * Generate the code necessary to process the provided FieldMap.
     * 
     * @param fieldMap
     *            the FieldMap describing fields to be mapped
     * @param sourceProperty
     *            a variable reference to the source property
     * @param destinationProperty
     *            a variable reference to the destination property
     * @param destinationType
     *            the destination's type
     * @param logDetails
     *            a StringBuilder to contain the debug output
     * @return a reference to <code>this</code> CodeSourceBuilder
     */
    public FieldMap mapFields(FieldMap fieldMap, VariableRef sourceProperty, VariableRef destinationProperty,
            StringBuilder logDetails) {
        
        FieldMap processedFieldMap = fieldMap;
        if (sourceProperty.isNestedProperty()) {
            ifPathNotNull(sourceProperty).then();
        }
        
        if (destinationProperty.isNestedProperty()) {
            if (!sourceProperty.isPrimitive()) {
                ifNotNull(sourceProperty).then();
            }
            assureInstanceExists(destinationProperty, sourceProperty);
        }
        
        Converter<Object, Object> converter = getConverter(fieldMap, fieldMap.getConverterId());
        
        // Generate mapping code for every case
        if (fieldMap.is(immutable())) {
            if (logDetails != null) {
                logDetails.append("treating as immutable (using copy-by-reference)");
            }
            copyByReference(destinationProperty, sourceProperty);
        } else if (fieldMap.is(aWrapperToPrimitive())) {
            if (logDetails != null) {
                logDetails.append("mapping primitive wrapper to primitive");
            }
            fromPrimitiveOrWrapperToPrimitive(destinationProperty, sourceProperty);
        } else if (fieldMap.is(aPrimitiveToWrapper())) {
            if (logDetails != null) {
                logDetails.append("mapping primitive to primitive wrapper");
            }
            fromPrimitiveToWrapper(destinationProperty, sourceProperty);
        } else if (converter != null) {
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
            if (TypeFactory.valueOf(Enum.class).isAssignableFrom(sourceProperty.type()))
                fromEnumToEnum(destinationProperty, sourceProperty);
            else if (sourceProperty.type().equals(TypeFactory.valueOf(String.class)))
                fromStringToEnum(destinationProperty, sourceProperty);
            else {
                if (logDetails != null) {
                    logDetails.append("mapping to enumaration has been skipped.");
                }
            }
        } else if (fieldMap.is(anArray())) {
            if (logDetails != null) {
                logDetails.append("mapping Array or Collection to Array");
            }
            fromArrayOrCollectionToArray(destinationProperty, sourceProperty);
        } else if (fieldMap.is(aCollection())) {
            if (logDetails != null) {
                logDetails.append("mapping Array or Collection to Collection");
            }
            fromArrayOrCollectionToCollection(destinationProperty, sourceProperty, fieldMap.getInverse());
        } else if (fieldMap.is(aMapToMap())) {
            if (logDetails != null) {
                logDetails.append("mapping Map to Map");
            }
            fromMapToMap(destinationProperty, sourceProperty);
        } else if (fieldMap.is(aMapToArray())) {
            if (logDetails != null) {
                logDetails.append("mapping Map to Array");
            }
            fromMapToArray(destinationProperty, sourceProperty, fieldMap.getInverse());
        } else if (fieldMap.is(aMapToCollection())) {
            if (logDetails != null) {
                logDetails.append("mapping Map to Collection");
            }
            fromMapToCollection(destinationProperty, sourceProperty, fieldMap.getInverse());
        } else if (fieldMap.is(anArrayOrCollectionToMap())) {
            if (logDetails != null) {
                logDetails.append("mapping Map to Array");
            }
            fromArrayOrCollectionToMap(destinationProperty, sourceProperty);
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
            if (fieldMap.is(aMapToBean())) {
                fromMapElementToObject(destinationProperty, sourceProperty);
            } else if (fieldMap.is(aBeanToMap())) {
                fromObjectToMapElement(destinationProperty, sourceProperty);
            } else if (fieldMap.is(anArrayOrListToBean())) {
                fromArrayOrListElementToObject(destinationProperty, sourceProperty);
            } else if (fieldMap.is(aBeanToArrayOrList())) {
                fromObjectToArrayOrListElement(destinationProperty, sourceProperty);
            } else if (sourceProperty.isPrimitive() || destinationProperty.isPrimitive()) {
                if (logDetails != null) {
                    logDetails.append("ignoring ( Object to primitive or primitive to Object )");
                }
                // TODO: should we throw an exception here instead?
                newLine().append("/* Ignore field map : %s -> %s */", sourceProperty.property(), destinationProperty.property());
                processedFieldMap = null;
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
        
        return processedFieldMap;
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
