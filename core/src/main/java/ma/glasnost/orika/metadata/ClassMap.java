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
import java.util.Set;

import ma.glasnost.orika.Mapper;

public class ClassMap<A, B> {
    
    final Class<A> aType;
    final Class<B> bType;
    final Set<FieldMap> fieldsMapping;
    final Set<Property> aExcludedFields;
    final Set<Property> bExcludedFields;
    Mapper<A, B> customizedMapper;
    
    public ClassMap(Class<A> aType, Class<B> bType) {
        this.aType = aType;
        this.bType = bType;
        
        fieldsMapping = new HashSet<FieldMap>();
        aExcludedFields = new HashSet<Property>();
        bExcludedFields = new HashSet<Property>();
    }
    
    public void addFieldMap(FieldMap fieldMap) {
        fieldsMapping.add(fieldMap);
    }
    
    public void addAExcludedField(Property aProperty) {
        aExcludedFields.add(aProperty);
    }
    
    public void addBExcludedField(Property bProperty) {
        bExcludedFields.add(bProperty);
    }
    
    public boolean isAExcludedField(Property aProperty) {
        return aExcludedFields.contains(aProperty);
    }
    
    public boolean isBExcludedField(Property bProperty) {
        return bExcludedFields.contains(bProperty);
    }
    
    public Class<?> getAType() {
        return aType;
    }
    
    public Class<?> getBType() {
        return bType;
    }
    
    public Set<FieldMap> getFieldsMapping() {
        return fieldsMapping;
    }
    
    public String getATypeName() {
        return aType.getSimpleName();
    }
    
    public String getBTypeName() {
        return bType.getSimpleName();
    }
    
    public Mapper<A, B> getCustomizedMapper() {
        return customizedMapper;
    }
    
    public void setCustomizedMapper(Mapper<A, B> customizedMapper) {
        this.customizedMapper = customizedMapper;
    }
    
    @Override
    public int hashCode() {
        int result = 31;
        result = result + ((aType == null) ? 0 : aType.hashCode());
        result = result + ((bType == null) ? 0 : bType.hashCode());
        return result;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ClassMap<?, ?> other = (ClassMap<?, ?>) obj;
        if (aType == null) {
            if (other.aType != null)
                return false;
        } else if (!aType.equals(other.aType))
            return false;
        if (bType == null) {
            if (other.bType != null)
                return false;
        } else if (!bType.equals(other.bType))
            return false;
        return true;
    }
    
}
