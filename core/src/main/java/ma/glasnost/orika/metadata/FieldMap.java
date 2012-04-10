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

import ma.glasnost.orika.impl.Specifications.Specification;

public class FieldMap {
    
    private final Property source;
    private final Property destination;
    private final Property aInverse;
    private final Property bInverse;
    private final MappingDirection mappingDirection;
    private boolean configured;
    private final boolean excluded;
    private final String converterId;
    
    public FieldMap(Property a, Property b, Property aInverse, Property bInverse, MappingDirection mappingDirection, boolean configured,
            boolean excluded, String converterId) {
        this.source = a;
        this.destination = b;
        this.aInverse = aInverse;
        this.bInverse = bInverse;
        this.mappingDirection = mappingDirection;
        this.configured = configured;
        this.converterId = converterId;
        this.excluded = excluded;
    }
    
    public FieldMap copy() {
        
        return new FieldMap(copy(source), copy(destination), copy(aInverse), copy(bInverse), mappingDirection, configured, excluded,
                converterId);
    }
    
    private Property copy(Property property) {
        return property != null ? property.copy() : null;
    }
    
    public Property getSource() {
        return source;
    }
    
    public Property getDestination() {
        return destination;
    }
    
    String getSourceName() {
        return source.getExpression();
    }
    
    String getDestinationName() {
        return destination.getExpression();
    }
    
    public boolean isConfigured() {
        return configured;
    }
    
    public void setConfigured(boolean configured) {
        this.configured = configured;
    }
    
    public Property getInverse() {
        return bInverse;
    }
    
    public boolean isIgnored() {
        return MappingDirection.B_TO_A == mappingDirection;
    }
    
    public FieldMap flip() {
        return new FieldMap(destination, source, bInverse, aInverse, mappingDirection.flip(), configured, excluded, converterId);
    }
    
    public boolean is(Specification specification) {
        return specification.apply(this);
    }
    
    public boolean have(Specification specification) {
        return specification.apply(this);
    }
    
    public String getConverterId() {
        return converterId;
    }
    
    public boolean isExcluded() {
        return excluded;
    }
    
    @Override
    public String toString() {
        return "FieldMap [destination=" + getDestination().toString() + ", source=" + getSource().toString() + "]";
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * ((aInverse == null) ? 0 : aInverse.hashCode());
        result = prime * ((bInverse == null) ? 0 : bInverse.hashCode());
        result = prime * ((destination == null) ? 0 : destination.hashCode());
        result = prime * ((source == null) ? 0 : source.hashCode());
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
        FieldMap other = (FieldMap) obj;
        if (aInverse == null) {
            if (other.aInverse != null)
                return false;
        } else if (!aInverse.equals(other.aInverse))
            return false;
        if (bInverse == null) {
            if (other.bInverse != null)
                return false;
        } else if (!bInverse.equals(other.bInverse))
            return false;
        if (destination == null) {
            if (other.destination != null)
                return false;
        } else if (!destination.equals(other.destination))
            return false;
        if (source == null) {
            if (other.source != null)
                return false;
        } else if (!source.equals(other.source))
            return false;
        return true;
    }
    
}
