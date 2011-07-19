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
    
    public FieldMap(Property a, Property b, Property aInverse, Property bInverse, MappingDirection mappingDirection, boolean configured) {
        this.source = a;
        this.destination = b;
        this.aInverse = aInverse;
        this.bInverse = bInverse;
        this.mappingDirection = mappingDirection;
        this.configured = configured;
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
        return new FieldMap(destination, source, bInverse, aInverse, mappingDirection.flip(), configured);
    }
    
    public boolean is(Specification specification) {
        return specification.apply(this);
    }
    
    public boolean have(Specification specification) {
        return specification.apply(this);
    }
    
    @Override
    public String toString() {
        return "FieldMap [destination=" + getDestinationName() + ", source=" + getSourceName() + "]";
    }
    
}
