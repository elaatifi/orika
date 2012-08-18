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

package ma.glasnost.orika.metadata;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import ma.glasnost.orika.DefaultFieldMapper;
import ma.glasnost.orika.Mapper;
import ma.glasnost.orika.MappingException;
import ma.glasnost.orika.impl.UtilityResolver;
import ma.glasnost.orika.property.PropertyResolverStrategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ClassMapBuilder provides a fluent API which can be used to define 
 * a mapping from one class to another.
 *
 * @param <A>
 * @param <B>
 */
public class ClassMapBuilder<A, B> {
    
    private final Map<String, Property> aProperties;
    private final Map<String, Property> bProperties;
    private final Set<String> propertiesCacheA;
    private final Set<String> propertiesCacheB;
    final private Type<A> aType;
    final private Type<B> bType;
    final private Set<FieldMap> fieldsMapping;
    
    final private Set<MapperKey> usedMappers;
    private Mapper<A, B> customizedMapper;
    private String[] constructorA;
    private String[] constructorB;
    private final PropertyResolverStrategy propertyResolver;
    private DefaultFieldMapper[] defaults;
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ClassMapBuilder.class);
    
    /**
     * Note: this static member variable exists to support the deprecated static map methods;
     * it can be removed as soon as they are removed
     */
    private static volatile WeakReference<PropertyResolverStrategy> defaultPropertyResolver;
    
    /**
     * @param aType
     * @param bType
     * @param propertyResolver
     * @param defaults
     */
    protected ClassMapBuilder(Type<A> aType, Type<B> bType, PropertyResolverStrategy propertyResolver, DefaultFieldMapper... defaults) {
	    
    	if (aType == null) {
	        throw new MappingException("[aType] is required");
	    }
	    
	    if (bType == null) {
	        throw new MappingException("[bType] is required");
	    }
	    
	    this.propertyResolver = propertyResolver;
	    this.defaults = defaults;
	    
	    aProperties = propertyResolver.getProperties(aType);
	    bProperties = propertyResolver.getProperties(bType);
	    propertiesCacheA = new LinkedHashSet<String>();
	    propertiesCacheB = new LinkedHashSet<String>();
	    
	    this.aType = aType;
	    this.bType = bType;
	    this.fieldsMapping = new LinkedHashSet<FieldMap>();
	    this.usedMappers = new LinkedHashSet<MapperKey>();
	    
	}
	
    /**
     * @param aType
     * @param bType
     * @deprecated Use of this method instantiates a new PropertyResolverStrategy instance
     * each time; instead, {@link ma.glasnost.orika.MapperFactory#classMap(Type, Type)} should
     * be used which leverages the PropertyResolverStrategy instance associated with the factory.
     */
    @Deprecated
	private ClassMapBuilder(Type<A> aType, Type<B> bType) {
        
    	this(aType, bType, getDefaultPropertyResolver());
    }
    
    private static PropertyResolverStrategy getDefaultPropertyResolver() {
    	if (defaultPropertyResolver == null || defaultPropertyResolver.get() == null) {
        	synchronized(ClassMapBuilder.class) {
        		if (defaultPropertyResolver == null || defaultPropertyResolver.get() == null) {
        			defaultPropertyResolver = new WeakReference<PropertyResolverStrategy>(
        					UtilityResolver.getDefaultPropertyResolverStrategy());
        		}
        	}
        }
    	return defaultPropertyResolver.get();
    }
    
    /**
     * Map a field in both directions
     * 
     * @param fieldNameA
     *            property name in type A
     * @param fieldNameB
     *            property name in type B
     * @return
     */
    public ClassMapBuilder<A, B> field(String fieldNameA, String fieldNameB) {
        return fieldMap(fieldNameA, fieldNameB).add();
    }
    
    
    /**
     * Map a field in one direction only (from fieldNameA to fieldNameB)
     * 
     * @param fieldNameA the (source) fieldName from type A
     * @param fieldNameB the (destination) fieldName from type B
     * @return
     */
    public ClassMapBuilder<A, B> fieldAToB(String fieldNameA, String fieldNameB) {
        return fieldMap(fieldNameA, fieldNameB).aToB().add();
    }
    
    /**
     * Map a field in one direction only (from fieldNameB to fieldNameA)
     * 
     * @param fieldNameB the (source) fieldName from type B
     * @param fieldNameA the (destination) fieldName from type A
     * @return
     */
    public ClassMapBuilder<A, B> fieldBToA(String fieldNameB, String fieldNameA) {
        return fieldMap(fieldNameA, fieldNameB).bToA().add();
    }
    
    /**
     * Create a fieldMap for the particular field (same property name used in both types)
     * 
     * @param a
     * @return
     */
    public FieldMapBuilder<A, B> fieldMap(String a) {
        return fieldMap(a, a);
    }
    
    /**
     * Create a fieldMap for the particular field mapping 
     * 
     * @param fieldNameA the name of the field in type A
     * @param fieldNameB the name of the field in type B
     * @return
     */
    public FieldMapBuilder<A, B> fieldMap(String fieldNameA, String fieldNameB) {
    
    	try {
	    	final FieldMapBuilder<A, B> fieldMapBuilder = new FieldMapBuilder<A, B>(this, fieldNameA, fieldNameB);
	        
	        return fieldMapBuilder;
	    } catch (MappingException e) {
	    	/*
	    	 * Add more information to the message to help with debugging
	    	 */
	    	String msg = getClass().getSimpleName() + ".map(" + aType + ", " + bType + ")" +
	    			".field('" + fieldNameA + "', '" + fieldNameB + "'): Error: " + e.getLocalizedMessage();
	    	throw new MappingException(msg, e);
	    }
    }
    
    /**
     * Exclude the specified field from mapping
     * 
     * @param fieldName the name of the field/property to exclude
     * @return
     */
    public ClassMapBuilder<A, B> exclude(String fieldName) {
        return fieldMap(fieldName).exclude().add();
    }
    
    /**
     * Set the custom mapper to use for this mapping.
     * 
     * @param legacyCustomizedMapper
     * @return
     * @deprecated use {@link #customize(Mapper)} instead
     */
    @Deprecated
    public final ClassMapBuilder<A, B> customize(ma.glasnost.orika.MapperBase<A, B> legacyCustomizedMapper) {
        customize(new ma.glasnost.orika.MapperBase.MapperBaseAdapter<A, B>(legacyCustomizedMapper));
        return this;
    }
    
    /**
     * Set the custom mapper to use for this mapping.
     * 
     * @param customizedMapper
     * @return
     */
    public ClassMapBuilder<A, B> customize(Mapper<A, B> customizedMapper) {
        this.customizedMapper = customizedMapper;
        return this;
    }
    
    /**
     * Configure this ClassMapBuilder to use an existing mapping (for parent classes)
     * defined from <code>aParentClass</code> to <code>bParentClass</code>.
     * 
     * @param aParentClass the source class of the parent mapping
     * @param bParentClass the destination class of the parent mapping
     * @return this ClassMapBuilder
     */
    public <X, Y> ClassMapBuilder<A, B> use(Class<?> aParentClass, Class<?> bParentClass) {
        
        @SuppressWarnings("unchecked")
        Type<Object> aParentType = TypeFactory.valueOf((Class<Object>) aParentClass);
        @SuppressWarnings("unchecked")
        Type<Object> bParentType = TypeFactory.valueOf((Class<Object>) bParentClass);
        
        return use(aParentType, bParentType);
    }
    
    /**
     * Configure this ClassMapBuilder to use an existing mapping (for parent classes)
     * defined from <code>aParentClass</code> to <code>bParentClass</code>.
     * 
     * @param aParentClass the source type of the parent mapping
     * @param bParentClass the destination type of the parent mapping
     * @return this ClassMapBuilder
     */
    public <X, Y> ClassMapBuilder<A, B> use(Type<?> aParentType, Type<?> bParentType) {
        
        if (!aParentType.isAssignableFrom(aType)) {
            throw new MappingException(aType.getSimpleName() + " is not a subclass of " + aParentType.getSimpleName());
        }
        
        if (!bParentType.isAssignableFrom(bType)) {
            throw new MappingException(bType.getSimpleName() + " is not a subclass of " + bParentType.getSimpleName());
        }
        
        usedMappers.add(new MapperKey(aParentType, bParentType));
        
        return this;
    }
    
    /**
     * Configures this class-map builder to employ the default property mapping
     * behavior to any properties that have not already been mapped or excluded; 
     * if any DefaultFieldMapper instances are passed, they will be used to attempt a
     * property name match if a direct match is not found.
     * 
     * @param defaults zero or more DefaultFieldMapper instances to apply during the default mapping
     * @return this ClassMapBuilder instance
     */
    public ClassMapBuilder<A, B> byDefault(DefaultFieldMapper... withDefaults) {
        
    	DefaultFieldMapper[] defaults;
    	if (withDefaults.length == 0) {
    		defaults = this.defaults;
    	} else {
    		defaults = withDefaults;
    	}
    	
        for (final String propertyName : getPropertiesForTypeA()) {
            if (!getMappedPropertiesForTypeA().contains(propertyName)) {
                if (getPropertiesForTypeB().contains(propertyName)) {
                    if (!getMappedPropertiesForTypeB().contains(propertyName)) {
                        /*
                         * Don't include the default mapping of Class to Class; this
                         * property is resolved for all types, but can't be mapped 
                         * in either direction.
                         */
                        if (!propertyName.equals("class")) {
                            fieldMap(propertyName).add();
                        }
                    }
                } else {
                    Property prop = resolvePropertyForA(propertyName);
                    for (DefaultFieldMapper defaulter : defaults) {
                        String suggestion = defaulter.suggestMappedField(propertyName, prop.getType());
                        if (suggestion != null && getPropertiesForTypeB().contains(suggestion)/*bProperties.containsKey(suggestion)*/) {
                            if (!getMappedPropertiesForTypeB().contains(suggestion)) {
                                fieldMap(propertyName, suggestion).add();
                            }
                        }
                    }
                }
            }
        }
        
        return this;
    }
    
    /**
     * @deprecated use {@link #byDefault(DefaultFieldMapper...)} instead
     * 
     * @param hint0
     *            first hint
     * @param mappingHints
     *            remaining hints
     * @return
     */
    @Deprecated
    public final ClassMapBuilder<A, B> byDefault(ma.glasnost.orika.MappingHint hint0, ma.glasnost.orika.MappingHint... mappingHints) {
        ma.glasnost.orika.MappingHint[] hints = new ma.glasnost.orika.MappingHint[mappingHints.length + 1];
        hints[0] = hint0;
        if (mappingHints.length > 0) {
            System.arraycopy(mappingHints, 0, hints, 1, mappingHints.length);
        }
        return byDefault(hints);
    }
    
    /**
     * @deprecated use {@link #byDefault(DefaultFieldMapper...)} instead
     * 
     * @param mappingHints
     * @return
     */
    @Deprecated
    public final ClassMapBuilder<A, B> byDefault(ma.glasnost.orika.MappingHint[] mappingHints) {
        
        for (final String propertyName : aProperties.keySet()) {
            if (!propertiesCacheA.contains(propertyName)) {
                if (bProperties.containsKey(propertyName)) {
                    if (!propertiesCacheB.contains(propertyName)) {
                        /*
                         * Don't include the default mapping of Class to Class; this
                         * property is resolved for all types, but can't be mapped 
                         * in either direction.
                         */
                        if (!propertyName.equals("class")) {
                            fieldMap(propertyName).add();
                        }
                    }
                } else {
                    Property prop = aProperties.get(propertyName);
                    for (ma.glasnost.orika.MappingHint hint : mappingHints) {
                        String suggestion = hint.suggestMappedField(propertyName, prop.getType().getRawType());
                        if (suggestion != null && bProperties.containsKey(suggestion)) {
                            if (!propertiesCacheB.contains(suggestion)) {
                                fieldMap(propertyName, suggestion).add();
                            }
                        }
                    }
                }
            }
        }
        
        return this;
    }
    
    /**
     * Produces a ClassMap instance based on the configurations defined on this
     * ClassMapBuilder. A ClassMap is used by Orika as a runtime descriptor for
     * the details of a mapping between one type and another.
     * 
     * @return a ClassMap as configured by this ClassMapBuilder
     */
    public ClassMap<A, B> toClassMap() {
    	
    	if(LOGGER.isDebugEnabled()) {
    		StringBuilder output = new StringBuilder();
        	output.append("ClassMap created:\n\t"+ getClass().getSimpleName() + ".map(" + aType + ", " + bType + ")");
        	for (FieldMap f: fieldsMapping) {
        		if (f.isExcluded()) {
        			output.append("\n\t .exclude('" + f.getSourceName() + "')");
        		} else if (f.getElementMap() == null){
        			output.append("\n\t .field( " + f.getSource() + ", " + f.getDestination() + " )");
        		} else {
        			StringBuilder source = new StringBuilder(""+f.getSource());
        			StringBuilder dest = new StringBuilder(""+f.getDestination());
        			StringBuilder suffix = new StringBuilder();
        			FieldMap elementMap = f.getElementMap();
        			while (elementMap != null) {
        				source.append("[" + elementMap.getSource());
        				dest.append("[" + elementMap.getDestination());
        				suffix.append("]");
        				elementMap = elementMap.getElementMap();
        			}
        			
        			output.append("\n\t .field( " + source + suffix + ", " + dest + suffix + " )");
        		}
        	}	
        	if (constructorA != null) {
        		output.append("\n\t .constructorA(" + Arrays.toString(constructorA) + ")");
        	}
        	if (constructorB != null) {
        		output.append("\n\t .constructorB(" + Arrays.toString(constructorB) + ")");
        	}
        	LOGGER.debug(output.toString());
        }
    	
        return new ClassMap<A, B>(aType, bType, fieldsMapping, customizedMapper, usedMappers, constructorA, constructorB);
    }
    
    /**
     * Creates a new ClassMapBuilder configuration for mapping between <code>aType</code>
     * and <code>bType</code>.
     * 
     * @param aType
     * @param bType
     * @return
     * @deprecated use {@link ma.glasnost.orika.MapperFactory#classMap(Class, Class)} instead
     */
    public static final <A, B> ClassMapBuilder<A, B> map(Class<A> aType, Class<B> bType) {
        return new ClassMapBuilder<A, B>(TypeFactory.<A> valueOf(aType), TypeFactory.<B> valueOf(bType));
    }
    
    /**
     * @param aType
     * @param bType
     * @return
     * @deprecated use {@link ma.glasnost.orika.MapperFactory#classMap(Type, Type)} instead
     */
    public static final <A, B> ClassMapBuilder<A, B> map(Type<A> aType, Type<B> bType) {
        return new ClassMapBuilder<A, B>(aType, bType);
    }
    
    /**
     * @param aType
     * @param bType
     * @return
     * @deprecated use {@link ma.glasnost.orika.MapperFactory#classMap(Class, Type)} instead
     */
    public static final <A, B> ClassMapBuilder<A, B> map(Class<A> aType, Type<B> bType) {
        return new ClassMapBuilder<A, B>(TypeFactory.<A> valueOf(aType), bType);
    }
    
    /**
     * @param aType
     * @param bType
     * @return
     * @deprecated use {@link ma.glasnost.orika.MapperFactory#classMap(Type, Class)} instead
     */
    public static final <A, B> ClassMapBuilder<A, B> map(Type<A> aType, Class<B> bType) {
        return new ClassMapBuilder<A, B>(aType, TypeFactory.<B> valueOf(bType));
    }
    
    /**
     * Determines whether the provided string is a valid property expression
     * 
     * @param expression the expression to evaluate
     * @return
     */
    protected boolean isNestedPropertyExpression(String expression) {
        return expression.indexOf('.') != -1;
    }
    
    /**
     * Resolves a property for the particular type, based on the provided property expression
     * 
     * @param type the type to resolve
     * @param expr the property expression to resolve
     * @return
     */
    protected Property resolveProperty(java.lang.reflect.Type type, String expr) {
        Property property;
        if (isSelfReferenceExpression(expr)) {
        	property = new Property();
        	property.setName("");
        	property.setGetter("");
        	property.setExpression("");
        	property.setType(TypeFactory.valueOf(type));
        } else if (isNestedPropertyExpression(expr)) {
            property = propertyResolver.getNestedProperty(type, expr);
        } else {
            final Map<String, Property> properties = propertyResolver.getProperties(type);
            if (properties.containsKey(expr)) {
                property = properties.get(expr);
            } else {
                throw new MappingException(expr + " does not belong to " + type);
            }
        }
        
        return property;
    }
    
    /**
     * Determines if the provided property expression is a element self-reference.
     * 
	 * @param expr the expression to evaluate
	 * @return
	 */
	private boolean isSelfReferenceExpression(String expr) {
		return "".equals(expr);
	}

	/**
     * Resolves a property expression for this builder's 'A' type
     * 
     * @param expr the property expression
     * @return
     */
    protected Property resolvePropertyForA(String expr) {
        return resolveProperty(aType, expr);
    }
    
    /**
     * Resolves a property expression for this builder's 'B' type
     * 
     * @param expr the property expression
     * @return
     */
    protected Property resolvePropertyForB(String expr) {
        return resolveProperty(bType, expr);
    }
    
    /**
     * @return the 'A' type for this builder
     */
    protected Type<?> getAType() {
    	return aType;
    }
    
    /**
     * @return the 'B' type for this builder
     */
    protected Type<?> getBType() {
    	return bType;
    }
    
    protected void addFieldMap(FieldMap fieldMap) {
    	getMappedFields().add(fieldMap);
        getMappedPropertiesForTypeA().add(fieldMap.getSource().getExpression());
        getMappedPropertiesForTypeB().add(fieldMap.getDestination().getExpression());
    }
    
    /**
     * @return the mapped properties for type A
     */
    protected Set<String> getMappedPropertiesForTypeA() {
    	return propertiesCacheA;
    }
    
    /**
     * @return the mapped properties for type B
     */
    protected Set<String> getMappedPropertiesForTypeB() {
    	return propertiesCacheB;
    }
    
    /**
     * @return the mapped fields for this builder
     */
    protected Set<FieldMap> getMappedFields() {
    	return fieldsMapping;
    }
    
    /**
     * @return the known properties for type A
     */
    protected Set<String> getPropertiesForTypeA() {
    	return aProperties.keySet();
    }
    
    /**
     * @return the known properties for type B
     */
    protected Set<String> getPropertiesForTypeB() {
    	return bProperties.keySet();
    }
    
    
    /**
     * Declares a constructor to be used for the A type 
     * with the specified arguments.
     * 
     * @param args the arguments identifying the constructor to be used
     * @return
     */
    public ClassMapBuilder<A, B> constructorA(String... args) {
        this.constructorA = args.clone();
        return this;
    }
    
    /**
     * Declares a constructor to be used for the B type
     * with the specified arguments.
     * 
     * @param args the arguments identifying the constructor to be used
     * @return
     */
    public ClassMapBuilder<A, B> constructorB(String... args) {
        this.constructorB = args.clone();
        return this;
    }
    
}
