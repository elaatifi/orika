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
    
    private final ClassMap<A, B> classMap;
    private final Map<String, Property> aProperties;
    private final Map<String, Property> bProperties;
    private final Set<String> propertiesCache;
    
    private ClassMapBuilder(ClassMap<A, B> classMap) {
        this.classMap = classMap;
        aProperties = PropertyUtil.getProperties(classMap.aType);
        bProperties = PropertyUtil.getProperties(classMap.bType);
        propertiesCache = new HashSet<String>();
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
        FieldMapBuilder<A, B> fieldMapBuilder = new FieldMapBuilder<A, B>(this, a, b);
        
        return fieldMapBuilder;
    }
    
    public ClassMapBuilder<A, B> excludeAField(String a) {
        classMap.addAExcludedField(resolveAProperty(a));
        
        return this;
    }
    
    public ClassMapBuilder<A, B> excludeBField(String b) {
        classMap.addBExcludedField(resolveAProperty(b));
        
        return this;
    }
    
    public ClassMapBuilder<A, B> customize(Mapper<A, B> customizedMapper) {
        classMap.setCustomizedMapper(customizedMapper);
        return this;
    }
    
    public ClassMapBuilder<A, B> byDefault() {
        
        for (String propertyName : aProperties.keySet()) {
            if (bProperties.containsKey(propertyName) && !propertiesCache.contains(propertyName)) {
                fieldMap(propertyName).add();
            }
        }
        
        return this;
    }
    
    public ClassMap<A, B> toClassMap() {
        return classMap;
    }
    
    public static <A, B> ClassMapBuilder<A, B> map(Class<A> aType, Class<B> bType) {
        return new ClassMapBuilder<A, B>(new ClassMap<A, B>(aType, bType));
    }
    
    Property resolveProperty(Class<?> type, String expr) {
        Property property;
        if (PropertyUtil.isExpression(expr)) {
            property = PropertyUtil.getNestedProperty(type, expr);
        } else {
            Map<String, Property> properties = PropertyUtil.getProperties(type);
            if (properties.containsKey(expr)) {
                property = properties.get(expr);
            } else {
                throw new MappingException(expr + " do not belongs to " + type);
            }
        }
        
        return property;
    }
    
    Property resolveAProperty(String expr) {
        Property property;
        if (PropertyUtil.isExpression(expr)) {
            property = PropertyUtil.getNestedProperty(classMap.getAType(), expr);
        } else if (aProperties.containsKey(expr)) {
            property = aProperties.get(expr);
        } else {
            throw new MappingException(expr + " do not belongs to " + classMap.getATypeName());
        }
        
        return property;
    }
    
    Property resolveBProperty(String expr) {
        Property property;
        if (PropertyUtil.isExpression(expr)) {
            property = PropertyUtil.getNestedProperty(classMap.getBType(), expr);
        } else if (bProperties.containsKey(expr)) {
            property = bProperties.get(expr);
        } else {
            throw new MappingException(expr + " do not belongs to " + classMap.getATypeName());
        }
        
        return property;
    }
    
    void addFieldMap(FieldMap fieldMap) {
        classMap.addFieldMap(fieldMap);
        propertiesCache.add(fieldMap.getSource().getName());
    }
    
}
