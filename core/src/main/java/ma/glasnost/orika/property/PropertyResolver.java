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
package ma.glasnost.orika.property;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ma.glasnost.orika.MappingException;
import ma.glasnost.orika.metadata.NestedProperty;
import ma.glasnost.orika.metadata.Property;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.metadata.TypeFactory;

/**
 * PropertyResolver defines the core functionality for resolving properties;
 * specific PropertyResolverStrategy implementations should extend from this
 * class, implementing the 'collectProperties' method.
 * 
 * @author matt.deboer@gmail.com
 * 
 */
public abstract class PropertyResolver implements PropertyResolverStrategy {
    
    private final boolean includePublicFields;
    
    private final Map<java.lang.reflect.Type, Map<String, Property>> propertiesCache = new ConcurrentHashMap<java.lang.reflect.Type, Map<String, Property>>();
    private final Map<java.lang.reflect.Type, Map<String, Property>> dynamicPropertiesCache = new ConcurrentHashMap<java.lang.reflect.Type, Map<String, Property>>();
    
    public PropertyResolver(boolean includePublicFields) {
        this.includePublicFields = includePublicFields;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * ma.glasnost.orika.property.PropertyResolverStrategy#getProperties(java
     * .lang.reflect.Type)
     */
    public Map<String, Property> getProperties(java.lang.reflect.Type theType) {
        
        Map<String, Property> properties = propertiesCache.get(theType);
        if (properties == null) {
            synchronized (theType) {
                properties = propertiesCache.get(theType);
                if (properties == null) {
                    
                    properties = new LinkedHashMap<String, Property>();
                    Type<?> referenceType;
                    
                    if (theType instanceof Type) {
                        referenceType = (Type<?>) theType;
                    } else if (theType instanceof Class) {
                        referenceType = TypeFactory.valueOf((Class<?>) theType);
                    } else {
                        throw new IllegalArgumentException("type " + theType + " not supported.");
                    }
                    
                    /*
                     * We process the properties for a type by starting with the
                     * type itself, and then continue by processing the classes
                     * and interfaces in the type's hierarchy. Property
                     * definitions from an ancestor should not override those
                     * already defined.
                     */
                    LinkedList<Class<? extends Object>> types = new LinkedList<Class<? extends Object>>();
                    types.addFirst((Class<? extends Object>) referenceType.getRawType());
                    while (!types.isEmpty()) {
                        Class<? extends Object> type = types.removeFirst();
                        
                        collectProperties(type, referenceType, properties);
                        
                        if (type.getSuperclass() != null && !Object.class.equals(type.getSuperclass())) {
                            types.add(type.getSuperclass());
                        }
                        
                        @SuppressWarnings("unchecked")
                        List<? extends Class<? extends Object>> interfaces = Arrays.<Class<? extends Object>> asList(type.getInterfaces());
                        types.addAll(interfaces);
                    }
                    
                    if (includePublicFields) {
                        /*
                         * Call this outside of the loop because the fields
                         * returned are already inclusive of ancestors.
                         */
                        collectPublicFieldProperties(referenceType, properties);
                    }
                    
                    propertiesCache.put(theType, Collections.unmodifiableMap(properties));
                }
            }
        }
        return properties;
    }
    
    /**
     * Attempt to resolve the generic type, using refereceType to resolve
     * TypeVariables
     * 
     * @param genericType
     *            the type to resolve
     * @param referenceType
     *            the reference type to use for lookup of type variables
     * @return
     */
    private Type<?> resolveGenericType(java.lang.reflect.Type genericType, Type<?> referenceType) {
        Type<?> resolvedType = null;
        Type<?> reference = referenceType;
        do {
            if (genericType instanceof TypeVariable && reference.isParameterized()) {
                java.lang.reflect.Type t = reference.getTypeByVariable((TypeVariable<?>) genericType);
                if (t != null) {
                    resolvedType = TypeFactory.valueOf(t);
                }
            } else if (genericType instanceof ParameterizedType) {
                if (reference.isParameterized()) {
                    resolvedType = TypeFactory.resolveValueOf((ParameterizedType) genericType, reference);
                } else {
                    resolvedType = TypeFactory.valueOf((ParameterizedType) genericType);
                }
            }
            reference = reference.getSuperType();
        } while (resolvedType == null && reference != TypeFactory.TYPE_OF_OBJECT);
        return resolvedType;
    }
    
    protected String capitalize(String string) {
        return string.substring(0, 1).toUpperCase() + string.substring(1);
    }
    
    /**
     * Resolves the raw property type from a property descriptor; if a read
     * method is available, use it to refine the type. The results of
     * pd.getPropertyType() are sometimes inconsistent across platforms.
     * 
     * @param pd
     * @return
     */
    private Class<?> resolveRawPropertyType(Class<?> rawType, Method readMethod) {
        try {
            return (readMethod == null ? rawType : readMethod.getDeclaringClass()
                    .getDeclaredMethod(readMethod.getName(), new Class[0])
                    .getReturnType());
        } catch (Exception e) {
            return rawType;
        }
    }
    
    /**
     * Processes a property, adding it to the map of properties for the owning
     * type being processed
     * 
     * @param propertyName
     *            the name of the property
     * @param propertyType
     *            the Class of the property
     * @param readMethod
     *            the read method for the property
     * @param writeMethod
     *            the write method for the property
     * @param owningType
     *            the owning type for which the properties are being resolved
     * @param referenceType
     *            a reference type to be used for resolving generic parameters
     * @param properties
     */
    protected void processProperty(String propertyName, Class<?> propertyType, Method readMethod, Method writeMethod, Class<?> owningType,
            Type<?> referenceType, Map<String, Property> properties) {
        final Property property = new Property();
        
        property.setExpression(propertyName);
        property.setName(propertyName);
        
        if (readMethod != null) {
            property.setGetter(readMethod.getName() + "()");
        }
        if (writeMethod != null) {
            property.setSetter(writeMethod.getName() + "(%s)");
        }
        
        if (readMethod != null || writeMethod != null) {
            
            Class<?> rawType = resolveRawPropertyType(propertyType, readMethod);
            
            if (referenceType.isParameterized() || owningType.getTypeParameters().length > 0 || rawType.getTypeParameters().length > 0) {
                /*
                 * Make attempts to determine the parameters
                 */
                Type<?> resolvedGenericType = null;
                if (readMethod != null) {
                    try {
                        resolvedGenericType = resolveGenericType(
                                readMethod.getDeclaringClass().getDeclaredMethod(readMethod.getName(), new Class[0]).getGenericReturnType(),
                                referenceType);
                    } catch (NoSuchMethodException e) {
                        throw new IllegalStateException("readMethod does not exist", e);
                    }
                }
                
                if (resolvedGenericType != null && !resolvedGenericType.isAssignableFrom(rawType)) {
                    property.setType(resolvedGenericType);
                } else {
                    property.setType(TypeFactory.valueOf(rawType));
                }
                
            } else {
                /*
                 * Neither the type nor it's parameter is generic; use the raw
                 * type
                 */
                property.setType(TypeFactory.valueOf(rawType));
            }
            
            Property existing = properties.get(propertyName);
            if (existing == null) {
                properties.put(propertyName, property);
            } else if (existing.getType().isAssignableFrom(property.getType()) && !existing.getType().equals(property.getType())) {
                /*
                 * The type has been refined by the generic information in a
                 * super-type
                 */
                existing.setType(property.getType());
            }
        }
    }
    
    /**
     * Add public non-static fields as properties
     * 
     * @param referenceType
     *            the type for which to collect public field properties
     * @param properties
     *            the collected properties for this type
     */
    protected void collectPublicFieldProperties(Type<?> referenceType, Map<String, Property> properties) {
        
        for (Field f : referenceType.getRawType().getFields()) {
            if (!Modifier.isStatic(f.getModifiers())) {
                final Property property = new Property();
                property.setExpression(f.getName());
                property.setName(f.getName());
                
                Class<?> rawType = f.getType();
                Type<?> genericType = resolveGenericType(f.getGenericType(), referenceType);
                if (genericType != null && !genericType.isAssignableFrom(rawType)) {
                    property.setType(genericType);
                } else {
                    property.setType(TypeFactory.valueOf(rawType));
                }
                
                Property existing = properties.get(property.getName());
                if (existing == null) {
                    property.setGetter(property.getName());
                    property.setSetter(property.getName() + " = %s");
                    properties.put(property.getName(), property);
                } else if (existing.getSetter() == null) {
                    existing.setSetter(property.getName() + " = %s");
                }
            }
        }
    }
    
    /**
     * Determines whether the provided string is a valid nested property expression
     * 
     * @param expression
     *            the expression to evaluate
     * @return
     */
    protected boolean isNestedPropertyExpression(String expression) {
        return expression.indexOf('.') != -1;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * ma.glasnost.orika.property.PropertyResolverStrategy#getNestedProperty
     * (java.lang.reflect.Type, java.lang.String)
     */
    public NestedProperty getNestedProperty(java.lang.reflect.Type type, String p) {
        
        String typeName = type.toString();
        Property property = null;
        java.lang.reflect.Type propertyType = type;
        final List<Property> path = new ArrayList<Property>();
        final StringBuilder expression = new StringBuilder();
        if (p.indexOf('.') != -1) {
            final String[] ps = p.split("\\.");
            int i = 0;
            while (i < ps.length) {
                try {
                    property = getProperty(propertyType, ps[i]);
                    propertyType = property.getType();
                } catch (MappingException e) {
                    throw new MappingException("could not resolve nested property [" + p + "] on " + type + ", because " + e.getLocalizedMessage());
                }
                
                i++;
                if (i < ps.length) {
                    path.add(property);
                    expression.append(property.getName() + ".");
                } else {
                    expression.append(property.getExpression());
                }
            }
        }
        
        if (property == null) {
            throw new RuntimeException(typeName + " does not contain property [" + p + "]");
        }
        
        return new NestedProperty(expression.toString(), property, path.toArray(new Property[path.size()]));
    }
    
    public Property getProperty(java.lang.reflect.Type type, String expr) {
        
        return getProperty(type, expr, getProperties(type));
    }
    
    /**
     * Resolves the specified property expression
     * 
     * @param type the property's owning type
     * @param expr the property expression to resolve
     * @param properties the known properties for the type
     * @return the resolved Property
     * @throws MappingException if the expression cannot be resolved to a property for the type
     */
    protected Property getProperty(java.lang.reflect.Type type, String expr, Map<String, Property> properties) throws MappingException {
        Property property = null;
        
        if (isNestedPropertyExpression(expr)) {
            property = getNestedProperty(type, expr);
        } else {
            Map<String, Property> dynamicProperties = dynamicPropertiesCache.get(type);
            if (dynamicProperties != null) {
                property = dynamicProperties.get(expr);
            }
            if (property == null) {
                if (properties.containsKey(expr)) {
                    property = properties.get(expr);
                } else if (ADHOC_PROPERTY_PATTERN.matcher(expr).matches()) {
                    property = resolveAdHocProperty(type, expr);
                    if (property != null) {
                        synchronized(type) {
                            if (dynamicProperties == null) {
                                dynamicProperties = new HashMap<String, Property>(1);
                                dynamicPropertiesCache.put(type, dynamicProperties);
                            }
                            dynamicProperties.put(property.getName(), property);
                        }
                    }
                } else {
                    throw new MappingException(expr + " does not belong to " + type);
                }
            }
        }
        return property;
    }
    
    private static final Pattern ADHOC_PROPERTY_PATTERN = Pattern.compile("([\\w]+)\\(\\s*([\\w]*)\\s*,\\s*([\\w]*)\\s*\\)");
    
    /**
     * Determines whether the provided string is a valid ad-hoc property expression
     * 
     * @param expression
     *            the expression to evaluate
     * @return
     */
    protected boolean isAdHocPropertyExpression(String expression) {
        return expression.indexOf('.') != -1;
    }
    
    
    /**
     * Resolves ad-hoc properties, which are defined in-line with the following format:<br>
     * "name(getterName,setterName)"
     * 
     * @param type
     * @param expr
     * @return
     */
    public Property resolveAdHocProperty(java.lang.reflect.Type type, String expr) {
        Type<?> theType = TypeFactory.valueOf(type); 
        Matcher matcher = ADHOC_PROPERTY_PATTERN.matcher(expr);
        
        if (matcher.matches()) {
            DynamicPropertyBuilder builder = new DynamicPropertyBuilder(theType);
            builder.setName(matcher.group(1));
            String readMethod = matcher.group(2);
            String writeMethod = matcher.group(3);
            
            if (!"".equals(readMethod) || !"".equals(writeMethod.trim())) {
                for (Method m : theType.getRawType().getMethods()) {
                    if (m.getName().equals(readMethod) && m.getParameterTypes().length == 0) {
                        builder.setReadMethod(m);
                    } else if (m.getName().endsWith(writeMethod) && m.getParameterTypes().length == 1) {
                        builder.setWriteMethod(m);
                    }
                }
            }
            return builder.toProperty(); 
        } else {
            throw new IllegalArgumentException("'" + expr + "' is not a valid dynamic property expression");
        }
    }
    
    /**
     * Collects all properties for the specified type.
     * 
     * @param type
     *            the type for which to collect properties
     * @param referenceType
     *            the reference type for use in resolving generic parameters as
     *            needed
     * @param properties
     *            the properties collected for the current type
     */
    protected abstract void collectProperties(Class<?> type, Type<?> referenceType, Map<String, Property> properties);
    
}
