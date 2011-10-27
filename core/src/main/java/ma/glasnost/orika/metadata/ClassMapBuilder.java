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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ma.glasnost.orika.Mapper;
import ma.glasnost.orika.MappingException;
import ma.glasnost.orika.impl.util.PropertyUtil;

public final class ClassMapBuilder<A, B> {
    
    private final Map<String, Property> aProperties;
    private final Map<String, Property> bProperties;
    private final Set<String> propertiesCache;
    final private Class<A> aType;
    final private Class<B> bType;
    final private Set<FieldMap> fieldsMapping;
    final private Set<MapperKey> usedMappers;
    private Mapper<A, B> customizedMapper;
    
    private ClassMapBuilder(Class<A> aType, Class<B> bType) {
        
        if (aType == null) {
            throw new MappingException("[aType] is required");
        }
        
        if (bType == null) {
            throw new MappingException("[bType] is required");
        }
        
        aProperties = PropertyUtil.getProperties(aType);
        bProperties = PropertyUtil.getProperties(bType);
        propertiesCache = new HashSet<String>();
        
        this.aType = aType;
        this.bType = bType;
        this.fieldsMapping = new HashSet<FieldMap>();
        this.usedMappers = new HashSet<MapperKey>();
    }
    
    /**
     * Map a field two way
     * 
     * @param a
     *            property name in type A
     * @param b
     *            property name in type B
     * @return
     */
    public ClassMapBuilder<A, B> field(String a, String b) {
        return fieldMap(a, b).add();
    }
    
    public FieldMapBuilder<A, B> fieldMap(String a) {
        return fieldMap(a, a);
    }
    
    public FieldMapBuilder<A, B> fieldMap(String a, String b) {
        final FieldMapBuilder<A, B> fieldMapBuilder = new FieldMapBuilder<A, B>(this, a, b);
        
        return fieldMapBuilder;
    }
    
    public ClassMapBuilder<A, B> customize(Mapper<A, B> customizedMapper) {
        this.customizedMapper = customizedMapper;
        return this;
    }
    
    public ClassMapBuilder<A, B> use(Class<?> aParentClass, Class<?> bParentClass) {
        if (aType.isAssignableFrom(aParentClass)) {
            throw new MappingException(aType.getSimpleName() + " is not a subclass of " + aParentClass.getSimpleName());
        }
        
        if (bType.isAssignableFrom(bParentClass)) {
            throw new MappingException(bType.getSimpleName() + " is not a subclass of " + bParentClass.getSimpleName());
        }
        
        usedMappers.add(new MapperKey(aParentClass, bParentClass));
        
        return this;
    }
    
    public ClassMapBuilder<A, B> byDefault() {
        
        for (final String propertyName : aProperties.keySet()) {
            if (bProperties.containsKey(propertyName) && !propertiesCache.contains(propertyName)) {
                fieldMap(propertyName).add();
            }
        }
        
        return this;
    }
    
    public ClassMap<A, B> toClassMap() {
        return new ClassMap<A, B>(aType, bType, fieldsMapping, customizedMapper, usedMappers);
    }
    
    public static <A, B> ClassMapBuilder<A, B> map(Class<A> aType, Class<B> bType) {
        return new ClassMapBuilder<A, B>(aType, bType);
    }
    
    Property resolveProperty(Class<?> type, String expr) {
        Property property;
        if (PropertyUtil.isExpression(expr)) {
            property = PropertyUtil.getNestedProperty(type, expr);
        } else {
            final Map<String, Property> properties = PropertyUtil.getProperties(type);
            if (properties.containsKey(expr)) {
                property = properties.get(expr);
            } else {
                throw new MappingException(expr + " does not belong to " + type);
            }
        }
        
        return property;
    }
    
    Property resolveAProperty(String expr) {
        Property property;
        if (PropertyUtil.isExpression(expr)) {
            property = PropertyUtil.getNestedProperty(aType, expr);
        } else if (aProperties.containsKey(expr)) {
            property = aProperties.get(expr);
        } else {
            throw new MappingException(expr + " does not belong to " + aType.getSimpleName());
        }
        
        return property;
    }
    
    Property resolveBProperty(String expr) {
        Property property;
        if (PropertyUtil.isExpression(expr)) {
            property = PropertyUtil.getNestedProperty(bType, expr);
        } else if (bProperties.containsKey(expr)) {
            property = bProperties.get(expr);
        } else {
            throw new MappingException(expr + " does not belong to " + bType.getSimpleName());
        }
        
        return property;
    }
    
    void addFieldMap(FieldMap fieldMap) {
        this.fieldsMapping.add(fieldMap);
        propertiesCache.add(fieldMap.getSource().getExpression());
    }
    
}
