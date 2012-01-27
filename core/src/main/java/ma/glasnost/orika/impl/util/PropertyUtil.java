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

package ma.glasnost.orika.impl.util;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ma.glasnost.orika.metadata.NestedProperty;
import ma.glasnost.orika.metadata.Property;

import org.apache.commons.lang.StringUtils;

public final class PropertyUtil {
    
    private static final Map<Class<?>, Map<String, Property>> PROPERTIES_CACHE = new ConcurrentHashMap<Class<?>, Map<String, Property>>();
    
    private PropertyUtil() {
        
    }
    
    public static Map<String, Property> getProperties(Class<?> clazz) {
        final Map<String, Property> properties = new HashMap<String, Property>();
        if (PROPERTIES_CACHE.containsKey(clazz)) {
            return PROPERTIES_CACHE.get(clazz);
        }
        BeanInfo beanInfo;
        try {
            beanInfo = Introspector.getBeanInfo(clazz);
            for (final PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
                try {
                    final Property property = new Property();
                    property.setExpression(pd.getName());
                    property.setName(pd.getName());
                    if (pd.getReadMethod() != null) {
                        property.setGetter(pd.getReadMethod().getName());
                    }
                    
                    if (pd.getWriteMethod() != null) {
                        
                        property.setSetter(pd.getWriteMethod().getName());
                    } else {
                        for (Method method : clazz.getDeclaredMethods()) {
                            if (method.getName().equals("set" + StringUtils.capitalize(pd.getName()))
                                    && method.getParameterTypes().length == 1 && method.getReturnType() != Void.class) {
                                property.setSetter(method.getName());
                                break;
                            }
                        }
                    }
                    
                    if (pd.getReadMethod() == null && pd.getWriteMethod() == null) {
                        continue;
                    }
                    
                    try {
                        property.setType(pd.getReadMethod()
                                .getDeclaringClass()
                                .getDeclaredMethod(property.getGetter(), new Class[0])
                                .getReturnType());
                    } catch (final Exception e) {
                        property.setType(pd.getPropertyType());
                    }
                    properties.put(pd.getName(), property);
                    
                    if (pd.getReadMethod() != null) {
                        final Method method = pd.getReadMethod();
                        if (property.getType() != null && Collection.class.isAssignableFrom(property.getType())) {
                            if (method.getGenericReturnType() instanceof ParameterizedType) {
                                property.setParameterizedType((Class<?>) ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments()[0]);
                            }
                        }
                    } else if (pd.getWriteMethod() != null) {
                        final Method method = pd.getWriteMethod();
                        
                        if (Collection.class.isAssignableFrom(property.getType()) && method.getGenericParameterTypes().length > 0) {
                            property.setParameterizedType((Class<?>) ((ParameterizedType) method.getGenericParameterTypes()[0]).getActualTypeArguments()[0]);
                        }
                    } else {
                        
                    }
                } catch (final Throwable e) {
                    e.printStackTrace();
                }
            }
        } catch (final IntrospectionException e) {
            e.printStackTrace();
            /* Ignore */
        }
        
        PROPERTIES_CACHE.put(clazz, Collections.unmodifiableMap(properties));
        return properties;
    }
    
    public static NestedProperty getNestedProperty(Class<?> clazz, String p) {
        Map<String, Property> properties = getProperties(clazz);
        Property property = null;
        final List<Property> path = new ArrayList<Property>();
        if (p.indexOf('.') != -1) {
            final String[] ps = p.split("\\.");
            int i = 0;
            while (i < ps.length) {
                if (!properties.containsKey(ps[i])) {
                    throw new RuntimeException(clazz.getName() + " do not contains [" + ps[i] + "] property.");
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
            throw new RuntimeException(clazz.getName() + " do not contains [" + p + "] property.");
        }
        
        return new NestedProperty(p, property, path.toArray(new Property[path.size()]));
    }
    
    public static boolean isExpression(String a) {
        return a.indexOf('.') != -1;
    }
}
