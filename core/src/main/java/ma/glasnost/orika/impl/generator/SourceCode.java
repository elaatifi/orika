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
import static ma.glasnost.orika.impl.Specifications.aMultiOccurrenceElementMap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javassist.CannotCompileException;
import ma.glasnost.orika.BoundMapperFacade;
import ma.glasnost.orika.Converter;
import ma.glasnost.orika.MapEntry;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingException;
import ma.glasnost.orika.converter.ConverterFactory;
import ma.glasnost.orika.impl.GeneratedObjectBase;
import ma.glasnost.orika.impl.generator.CompilerStrategy.SourceCodeGenerationException;
import ma.glasnost.orika.impl.generator.Node.NodeList;
import ma.glasnost.orika.impl.generator.UsedMapperFacadesContext.UsedMapperFacadesIndex;
import ma.glasnost.orika.impl.generator.specification.AnyTypeToString;
import ma.glasnost.orika.impl.generator.specification.ApplyRegisteredMapper;
import ma.glasnost.orika.impl.generator.specification.ArrayOrCollectionToArray;
import ma.glasnost.orika.impl.generator.specification.ArrayOrCollectionToCollection;
import ma.glasnost.orika.impl.generator.specification.ArrayOrCollectionToMap;
import ma.glasnost.orika.impl.generator.specification.Convert;
import ma.glasnost.orika.impl.generator.specification.CopyByReference;
import ma.glasnost.orika.impl.generator.specification.EnumToEnum;
import ma.glasnost.orika.impl.generator.specification.MapToArray;
import ma.glasnost.orika.impl.generator.specification.MapToCollection;
import ma.glasnost.orika.impl.generator.specification.MapToMap;
import ma.glasnost.orika.impl.generator.specification.MultiOccurrenceElementToObject;
import ma.glasnost.orika.impl.generator.specification.MultiOccurrenceToMultiOccurrence;
import ma.glasnost.orika.impl.generator.specification.ObjectToMultiOccurrenceElement;
import ma.glasnost.orika.impl.generator.specification.ObjectToObject;
import ma.glasnost.orika.impl.generator.specification.PrimitiveAndObject;
import ma.glasnost.orika.impl.generator.specification.PrimitiveOrWrapperToPrimitive;
import ma.glasnost.orika.impl.generator.specification.PrimitiveToWrapper;
import ma.glasnost.orika.impl.generator.specification.StringToEnum;
import ma.glasnost.orika.impl.generator.specification.StringToStringConvertible;
import ma.glasnost.orika.impl.generator.specification.UnmappableEnum;
import ma.glasnost.orika.impl.util.ClassUtil;
import ma.glasnost.orika.metadata.ClassMap;
import ma.glasnost.orika.metadata.FieldMap;
import ma.glasnost.orika.metadata.MapperKey;
import ma.glasnost.orika.metadata.Property;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.metadata.TypeFactory;
import ma.glasnost.orika.property.PropertyResolverStrategy;

/**
 * SourceCode is a utility class used to generate the various source code
 * snippets needed to generate the Orika mapping objects
 * 
 */
public class SourceCode {
    
    /**
     * @deprecated use <code>OrikaSystemProperties.WRITE_CLASS_FILES</code> instead
     */
    @Deprecated
    public static final String PROPERTY_WRITE_CLASS_FILES = "ma.glasnost.orika.GeneratedSourceCode.writeClassFiles";

    /**
     * @deprecated use <code>OrikaSystemProperties.WRITE_SOURCE_FILES</code> instead
     */
    @Deprecated
    public static final String PROPERTY_WRITE_SOURCE_FILES = "ma.glasnost.orika.GeneratedSourceCode.writeSourceFiles";

    private StringBuilder sourceBuilder;
    private String classSimpleName;
    private String packageName;
    private String className;
    private CompilerStrategy compilerStrategy;
    private List<String> methods;
    private List<String> fields;
    private Class<?> superClass;
    
    private final UsedTypesContext usedTypes;
    private final UsedConvertersContext usedConverters;
    private final UsedMapperFacadesContext usedMapperFacades;
    private final MapperFactory mapperFactory;
    private final LinkedHashSet<Specification> specifications;
    private final MultiOccurrenceToMultiOccurrence multiOccurrenceSpec;
    private final StringBuilder logDetails;
    private final PropertyResolverStrategy propertyResolver;
    
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
    public SourceCode(final String baseClassName, Class<?> superClass,
            CompilerStrategy compilerStrategy, PropertyResolverStrategy propertyResolver, 
            MapperFactory mapperFactory, StringBuilder logDetails) {
        
        String safeBaseClassName = baseClassName.replace("[]", "$Array");
        this.compilerStrategy = compilerStrategy;
        this.sourceBuilder = new StringBuilder();
        this.classSimpleName = safeBaseClassName + System.identityHashCode(this);
        this.superClass = superClass;
        this.propertyResolver = propertyResolver;

        int namePos = safeBaseClassName.lastIndexOf(".");
        if (namePos > 0) {
            this.packageName = safeBaseClassName.substring(0, namePos - 1);
            this.classSimpleName = safeBaseClassName.substring(namePos + 1);
        } else {
            this.packageName = "ma.glasnost.orika.generated";
        }
        this.className = this.packageName + "." + this.classSimpleName;
        this.methods = new ArrayList<String>();
        this.fields = new ArrayList<String>();
        
        
        sourceBuilder.append("package " + packageName + ";\n\n");
        sourceBuilder.append("public class " + classSimpleName + " extends "
                + superClass.getCanonicalName() + " {\n");
        
        this.usedTypes = new UsedTypesContext();
        this.usedConverters = new UsedConvertersContext();
        this.mapperFactory = mapperFactory;
        this.usedMapperFacades = new UsedMapperFacadesContext();
        this.specifications = new LinkedHashSet<Specification>();
        this.logDetails = logDetails;
        
        // TODO: allow customization of this ordering
        specifications.add(new CopyByReference(mapperFactory));
        specifications.add(new PrimitiveOrWrapperToPrimitive(mapperFactory));
        specifications.add(new PrimitiveToWrapper(mapperFactory));
        specifications.add(new Convert(mapperFactory));
        specifications.add(new ApplyRegisteredMapper(mapperFactory));
        specifications.add(new EnumToEnum(mapperFactory));
        specifications.add(new StringToEnum(mapperFactory));
        specifications.add(new UnmappableEnum(mapperFactory));
        specifications.add(new ArrayOrCollectionToArray(mapperFactory));
        specifications.add(new ArrayOrCollectionToCollection(mapperFactory));
        specifications.add(new MapToMap(mapperFactory));
        specifications.add(new MapToArray(mapperFactory));
        specifications.add(new MapToCollection(mapperFactory));
        specifications.add(new ArrayOrCollectionToMap(mapperFactory));
        specifications.add(new StringToStringConvertible(mapperFactory));
        specifications.add(new AnyTypeToString(mapperFactory));
        specifications.add(new MultiOccurrenceElementToObject(mapperFactory));
        specifications.add(new ObjectToMultiOccurrenceElement(mapperFactory));
        specifications.add(new PrimitiveAndObject(mapperFactory));
        specifications.add(new ObjectToObject(mapperFactory));
        
        this.multiOccurrenceSpec = new MultiOccurrenceToMultiOccurrence(mapperFactory);
    }
    
    
    /**
     * @return the StringBuilder containing the current accumulated source.
     */
    protected StringBuilder getSourceBuilder() {
        return sourceBuilder;
    }

    public Class<?> getSuperClass() {
        return superClass;
    }

    public String getClassSimpleName() {
        return classSimpleName;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getClassName() {
        return className;
    }

    List<String> getFields() {
        return fields;
    }

    List<String> getMethods() {
        return methods;
    }

    
    /**
     * Adds a method definition to the class based on the provided source.
     * 
     * @param methodSource
     */
    public void addMethod(String methodSource) {
        sourceBuilder.append("\n" + methodSource + "\n");
        this.methods.add(methodSource);
    }

    /**
     * Adds a field definition to the class based on the provided source.
     * 
     * @param fieldSource
     *            the source from which to compile the field
     */
    public void addField(String fieldSource) {
        sourceBuilder.append("\n" + fieldSource + "\n");
        this.fields.add(fieldSource);
    }

    /**
     * @return the completed generated java source for the class.
     */
    public String toSourceFile() {
        return sourceBuilder.toString() + "\n}";
    }

    /**
     * Compile and return the (generated) class; this will also cause the
     * generated class to be detached from the class-pool, and any (optional)
     * source and/or class files to be written.
     * 
     * @return the (generated) compiled class
     * @throws CannotCompileException
     * @throws IOException
     */
    protected Class<?> compileClass() throws SourceCodeGenerationException {
        return compilerStrategy.compileClass(this);
    }

    /**
     * @return a new instance of the (generated) compiled class
     * @throws CannotCompileException
     * @throws IOException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    @SuppressWarnings("unchecked")
    public <T extends GeneratedObjectBase> T getInstance() throws SourceCodeGenerationException,
            InstantiationException, IllegalAccessException {

        T instance = (T) compileClass().newInstance();
        
        
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
        instance.setUsedTypes(usedTypesArray);
        instance.setUsedConverters(usedConvertersArray);
        instance.setUsedMapperFacades(usedMapperFacadesArray);
        
        return instance;
    }
    
    public String usedConverter(Converter<?, ?> converter) {
        int index = usedConverters.getIndex(converter);
        return "((" + Converter.class.getCanonicalName() + ")usedConverters[" + index + "])";
    }
    
    public String usedType(Type<?> type) {
        int index = usedTypes.getIndex(type);
        return "((" + Type.class.getCanonicalName() + ")usedTypes[" + index + "])";
    }
    
    public String usedMapperFacadeCall(VariableRef source, VariableRef destination) {
        return usedMapperFacadeCall(source.type(), destination.type());
    }
    
    public String usedMapperFacadeCall(Type<?> sourceType, Type<?> destinationType) {
        UsedMapperFacadesIndex usedFacade = usedMapperFacades.getIndex(sourceType, destinationType, mapperFactory);
        String mapInDirection = usedFacade.isReversed ? "mapReverse" : "map";
        return "((" + BoundMapperFacade.class.getCanonicalName() + ")usedMapperFacades[" + usedFacade.index + "])." + mapInDirection + "";
    }
    
    public String callMapper(Type<?> sourceType, Type<?> destinationType, String sourceExpression, String destExpression) {
        return usedMapperFacadeCall(sourceType, destinationType) + "(" + sourceExpression + ", " + destExpression + ", mappingContext)";
    }
    
    public String callMapper(Type<?> sourceType, Type<?> destinationType, String sourceExpression) {
        return usedMapperFacadeCall(sourceType, destinationType) + "(" + sourceExpression + ", mappingContext)";
    }
    
    public String callMapper(VariableRef source, VariableRef destination) {
        return callMapper(source.type(), destination.type(), ""+source, ""+destination);
    }
    
    public String callMapper(VariableRef source, Type<?> destination) {
        return callMapper(source.type(), destination, ""+source);
    }
    
    public String usedMapperFacadeNewObjectCall(VariableRef source, VariableRef destination) {
        return newObjectFromMapper(source.type(), destination.type());
    }
    
    public String newObjectFromMapper(Type<?> sourceType, Type<?> destinationType) {
        UsedMapperFacadesIndex usedFacade = usedMapperFacades.getIndex(sourceType, destinationType, mapperFactory);
        String instantiateMethod = usedFacade.isReversed ? "newObject" : "newObjectReverse";
        return "((" + BoundMapperFacade.class.getCanonicalName() + ")usedMapperFacades[" + usedFacade.index + "])." + instantiateMethod + "";
    }
    
    /**
     * Generates a code snippet to generate a new instance of the destination type from a mapper
     * 
     * @param source
     * @param destinationType
     * @return a code snippet to generate a new instance of the destination type from a mapper
     */
    public String newObjectFromMapper(VariableRef source, Type<?> destinationType) {
        return newObjectFromMapper(source.type(), destinationType) + "(" + source + ", mappingContext)";
    }
    
    /**
     * Generate usedType array index code for the provided variable
     * 
     * @param r
     * @return the code snippet for referencing a used type by it's array index
     */
    public String usedType(VariableRef r) {
        return usedType(r.type());
    }
    
    /**
     * @param source
     * @param destinationType
     * @return the code snippet for generating a new instance, or assigning the default value in cases of primitive types
     */
    public String newObject(VariableRef source, Type<?> destinationType) {
        if (destinationType.isPrimitive()) {
            return VariableRef.getDefaultValue(destinationType.getRawType());
        } else {
            return newObjectFromMapper(source, destinationType);
        }
    }
    
    
    /**
     * Append a statement which assures that the variable reference has an
     * existing instance; if it does not, a new object is generated using
     * MapperFacade.newObject
     * 
     * @param propertyRef
     *            the property or variable reference on which to check for an
     *            instance
     * @param source 
     * @return a reference to <code>this</code> SourceCodeBuilder
     */
    public String assureInstanceExists(VariableRef propertyRef, VariableRef source) {
        
        StringBuilder out = new StringBuilder();
        for (final VariableRef ref : propertyRef.getPath()) {
            
            if (!ClassUtil.isConcrete(ref.type())) {
                throw new MappingException("Abstract types are unsupported for nested properties. \n" + ref.name());
            }
            
            out.append(statement("if(%s == null) %s", ref,
                    ref.assign("(%s)%s(source, mappingContext)", ref.typeName(), usedMapperFacadeNewObjectCall(ref,source))));
        }
        return out.toString();
    }
    
    /**
     * Appends the provided string as a source code statement, ending it with a
     * statement terminator as appropriate.
     * 
     * @param str
     * @param args
     * @return a reference to <code>this</code> SourceCodeBuilder
     */
    public static String statement(String str, Object... args) {
        if (str != null && !"".equals(str.trim())) {
            String expr = format(str, args);
            String prefix = "";
            String suffix = "";
            if (!expr.startsWith("\n")) {
                prefix = "\n";
            }
            String trimmed = expr.trim();
            if (!"".equals(trimmed) && !trimmed.endsWith(";") && !trimmed.endsWith("}") && !trimmed.endsWith("{") && !trimmed.endsWith("(")) {
                suffix = ";";
            }
            return prefix + expr + suffix;
        }
        return "";
    }
    
    /**
     * Appends all of the String values provided to the StringBuilder in order,
     * as "statements"
     * 
     * @param out
     * @param statements
     */
    public static void append(StringBuilder out, String...statements) {
        for (String statement: statements) {
            out.append(statement(statement));
        }
    }
    
    /**
     * Join the items in the list together in a String, separated by the 
     * provided separator
     * 
     * @param list
     * @param separator
     * @return a String which joins the items of the list
     */
    public static String join(List<?> list, String separator) {
        StringBuilder result = new StringBuilder();
        for (Object item : list) {
            result.append(item + separator);
        }
        return result.length() > 0 ? result.substring(0, result.length() - separator.length()) : "";
    }
    
    /**
     * Creates a VariableRef representing a Set<Map.Entry> for the provided
     * VariableRef (which should be a Map)
     * 
     * @param s the Map type variable ref
     * @return a new VariableRef corresponding to an EntrySet for the provided variable ref, 
     * which should be a Map type
     */
    public static VariableRef entrySetRef(VariableRef s) {
        @SuppressWarnings("unchecked")
        Type<?> sourceEntryType = TypeFactory.valueOf(Set.class, MapEntry.entryType((Type<? extends Map<Object, Object>>) s.type()));
        return new VariableRef(sourceEntryType, s + ".entrySet()");
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
    public String fromMultiOccurrenceToMultiOccurrence(Set<FieldMap> fieldMappings, StringBuilder logDetails) {
        
        return multiOccurrenceSpec.fromMultiOccurrenceToMultiOccurrence(fieldMappings, this, logDetails);
    }
    
    
    /**
     * @param source
     * @param dest
     * @param srcNodes
     * @param destNodes
     * @return a code snippet suitable to use as an equality comparison test for the provided 
     * source and destination nodes
     */
    public String currentElementComparator(Node source, Node dest, NodeList srcNodes, NodeList destNodes) {
        
        StringBuilder comparator = new StringBuilder();
        
        MapperKey key = new MapperKey(source.elementRef.type(), dest.elementRef.type());
        ClassMap<?,?> classMap = mapperFactory.getClassMap(key);
        if (classMap == null) {
            classMap = mapperFactory.classMap(key.getAType(), key.getBType()).byDefault().toClassMap();
        } 
        
        String or = "";
        Set<FieldMap> fieldMaps = new HashSet<FieldMap>(classMap.getFieldsMapping());
        for (Node node: source.children) {
            if (node.value != null) {
                fieldMaps.add(node.value);
            }
        }
        for (Node node: dest.children) {
            if (node.value != null) {
                fieldMaps.add(node.value);
            }
        }
        
        Set<String> comparisons = new HashSet<String>();
        
        for (FieldMap fieldMap: fieldMaps) {
            if (!(fieldMap.is(aMultiOccurrenceElementMap()) && fieldMap.isByDefault()) 
                    && !fieldMap.isExcluded()
                    && !fieldMap.isIgnored()) {
            
                Node srcNode = Node.findFieldMap(fieldMap, srcNodes, true);
                if (srcNode != null && srcNode.parent != null) {
                    srcNode = srcNode.parent;
                } else {
                    srcNode = source;
                }
                
                Node destNode = Node.findFieldMap(fieldMap, destNodes, false);
                if (destNode != null && destNode.parent != null) {
                    destNode = destNode.parent;
                } else {
                    destNode = dest;
                }
                
                Type<?> sourceType = source.elementRef.type();
                Type<?> destType = dest.elementRef.type();
                
                try {
                    propertyResolver.getProperty(sourceType, fieldMap.getSource().getName());
                    propertyResolver.getProperty(destType, fieldMap.getDestination().getName());
                    
                    VariableRef s = new VariableRef(fieldMap.getSource(), srcNode.elementRef);
                    VariableRef d = new VariableRef(fieldMap.getDestination(), destNode.elementRef);
                    String code = this.compareFields(fieldMap, s, d, key.getBType(), null);
                    if (!"".equals(code) && comparisons.add(code)) {
                        comparator.append(or + "!(" + code + ")"); 
                        or = " || ";
                    }
                } catch (Exception e) {
                    
                }
            }
        }
        return comparator.toString();
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
    public String mapFields(FieldMap fieldMap, VariableRef sourceProperty, VariableRef destinationProperty,
            Type<?> destinationType, StringBuilder logDetails) {
        
        StringBuilder out = new StringBuilder();
        StringBuilder closing = new StringBuilder();
        
        if (sourceProperty.isNestedProperty()) {
            out.append(sourceProperty.ifPathNotNull());
            out.append("{ \n");
            closing.append("\n}");
        }
        
        if (destinationProperty.isNestedProperty()) {
            if (!sourceProperty.isPrimitive()) {
                out.append(sourceProperty.ifNotNull());
                out.append("{ \n");
                closing.append("\n}");
            }
            out.append(assureInstanceExists(destinationProperty, sourceProperty));
        }
        
        Converter<Object, Object> converter = getConverter(fieldMap, fieldMap.getConverterId());
        sourceProperty.setConverter(converter);
        
        for (Specification spec: specifications) {
            if (spec.appliesTo(fieldMap)) {
                String code = spec.generateMappingCode(sourceProperty, destinationProperty, fieldMap.getInverse(), this);
                if (code == null || "".equals(code)) {
                    throw new IllegalStateException("empty code returned for spec " + spec + ", sourceProperty = " + sourceProperty + 
                            ", destinationProperty = " + destinationProperty);
                }
                out.append(code);
                break;
            }
        }
        
        out.append(closing.toString());
        
        return out.toString();
    }
    
    /**
     * Generates source code for an "equality" comparison of two variables, based on the FieldMap passed
     * 
     * @param fieldMap
     * @param sourceProperty
     * @param destinationProperty
     * @param destinationType
     * @param logDetails
     * @return the source code for equality test of the provided fields
     */
    public String compareFields(FieldMap fieldMap, VariableRef sourceProperty, VariableRef destinationProperty,
            Type<?> destinationType, StringBuilder logDetails) {
        
        StringBuilder out = new StringBuilder();
        
        out.append("(");
        if (sourceProperty.isNestedProperty()) {
            out.append(sourceProperty.ifPathNotNull());
            out.append(" && ");
        }
        
        if (destinationProperty.isNestedProperty()) {
            if (!sourceProperty.isPrimitive()) {
                out.append(sourceProperty.notNull());
                out.append(" && ");
            }
            out.append(assureInstanceExists(destinationProperty, sourceProperty));
        }
        
        Converter<Object, Object> converter = getConverter(fieldMap, fieldMap.getConverterId());
        sourceProperty.setConverter(converter);
        
        for (Specification spec: specifications) {
            if (spec.appliesTo(fieldMap)) {
                String code = spec.generateEqualityTestCode(sourceProperty, destinationProperty, fieldMap.getInverse(), this);
                if (code == null || "".equals(code)) {
                    throw new IllegalStateException("empty code returned for spec " + spec + ", sourceProperty = " + sourceProperty + 
                            ", destinationProperty = " + destinationProperty);
                }
                out.append(code);
                break;
            }
        }
        
        out.append(")");
        
        return out.toString();
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
