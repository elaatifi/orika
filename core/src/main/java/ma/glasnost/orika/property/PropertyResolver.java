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
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
    
    public PropertyResolver(boolean includePublicFields) {
        this.includePublicFields = includePublicFields;
    }
    
    /* (non-Javadoc)
     * @see ma.glasnost.orika.property.PropertyResolverStrategy#getProperties(java.lang.reflect.Type)
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
                        
                        List<? extends Class<? extends Object>> interfaces = Arrays.<Class<? extends Object>> asList(type.getInterfaces());
                        types.addAll(interfaces);
                    }
                    
                    if (includePublicFields) {
                        /* 
                         * Call this outside of the loop because the fields returned 
                         * are already inclusive of ancestors.
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
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * ma.glasnost.orika.property.PropertyResolverStrategy#getNestedProperty
     * (java.lang.reflect.Type, java.lang.String)
     */
    public NestedProperty getNestedProperty(java.lang.reflect.Type type, String p) {
        
        String typeName = type.toString();
        Map<String, Property> properties = getProperties(type);
        Property property = null;
        final List<Property> path = new ArrayList<Property>();
        if (p.indexOf('.') != -1) {
            final String[] ps = p.split("\\.");
            int i = 0;
            while (i < ps.length) {
                if (!properties.containsKey(ps[i])) {
                    throw new RuntimeException("could not resolve nested property [" + p + "] on " + type + ", because " + type
                            + " does not contain property [" + ps[i] + "]");
                }
                property = properties.get(ps[i]);
                properties = getProperties(property.getType());
                i++;
                if (i < ps.length) {
                    path.add(property);
                }
            }
        }
        
        if (property == null) {
            throw new RuntimeException(typeName + " does not contain property [" + p + "]");
        }
        
        return new NestedProperty(p, property, path.toArray(new Property[path.size()]));
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
     * @param referenceType the type for which to collect public field properties
     * @param properties the collected properties for this type
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
