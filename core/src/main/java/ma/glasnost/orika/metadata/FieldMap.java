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
    private final boolean excluded;
    private final String converterId;
    private final boolean byDefault;
    private FieldMap elementMap;
    
    private FieldMap base;
    
    public FieldMap(Property a, Property b, Property aInverse, Property bInverse, MappingDirection mappingDirection,
            boolean excluded, String converterId, FieldMap elementMap, boolean byDefault) {
        this.source = a;
        this.destination = b;
        this.aInverse = aInverse;
        this.bInverse = bInverse;
        this.mappingDirection = mappingDirection;
        this.converterId = converterId;
        this.excluded = excluded;
        this.elementMap = elementMap;
        this.byDefault = byDefault;
    }
    
    public FieldMap copy() {
        
        return new FieldMap(copy(source), copy(destination), copy(aInverse), copy(bInverse), 
        		mappingDirection, excluded, converterId, copy(elementMap), byDefault);
    }
    
    private Property copy(Property property) {
        return property != null ? property.copy() : null;
    }
    
    private FieldMap copy(FieldMap fieldMap) {
    	return fieldMap != null ? fieldMap.copy() : null;
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
    
    public Property getInverse() {
        return bInverse;
    }
    
    public boolean isIgnored() {
        return MappingDirection.B_TO_A == mappingDirection;
    }
    
    public FieldMap flip() {
        return new FieldMap(destination, source, bInverse, aInverse, mappingDirection.flip(), excluded, converterId, 
        		elementMap != null ? elementMap.flip() : null, byDefault);
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
    
    public boolean isByDefault() {
    	return byDefault;
    }
    
    public boolean isExcluded() {
        return excluded;
    }
    
    public FieldMap getElementMap() {
    	return elementMap;
    }
    
    public FieldMap getBaseFieldMap() {
    	if (base == null) {
    		if (elementMap == null) {
    			base = this;
    		} else {
    			base = new FieldMap(source, destination, aInverse, bInverse, 
    	        		mappingDirection, excluded, converterId, null, byDefault);
    		}
    	}
    	return base;
    }
    
    @Override
    public String toString() {
        return "FieldMap [destination=" + getDestination().toString() + ", source=" + getSource().toString() + "]";
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((aInverse == null) ? 0 : aInverse.hashCode());
		result = prime * result
				+ ((bInverse == null) ? 0 : bInverse.hashCode());
		result = prime * result
				+ ((converterId == null) ? 0 : converterId.hashCode());
		result = prime * result
				+ ((destination == null) ? 0 : destination.hashCode());
		result = prime * result
				+ ((elementMap == null) ? 0 : elementMap.hashCode());
		result = prime * result + (excluded ? 1231 : 1237);
		result = prime
				* result
				+ ((mappingDirection == null) ? 0 : mappingDirection.hashCode());
		result = prime * result + ((source == null) ? 0 : source.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		FieldMap other = (FieldMap) obj;
		if (aInverse == null) {
			if (other.aInverse != null) {
				return false;
			}
		} else if (!aInverse.equals(other.aInverse)) {
			return false;
		}
		if (bInverse == null) {
			if (other.bInverse != null) {
				return false;
			}
		} else if (!bInverse.equals(other.bInverse)) {
			return false;
		}
		if (converterId == null) {
			if (other.converterId != null) {
				return false;
			}
		} else if (!converterId.equals(other.converterId)) {
			return false;
		}
		if (destination == null) {
			if (other.destination != null) {
				return false;
			}
		} else if (!destination.equals(other.destination)) {
			return false;
		}
		if (elementMap == null) {
			if (other.elementMap != null) {
				return false;
			}
		} else if (!elementMap.equals(other.elementMap)) {
			return false;
		}
		if (excluded != other.excluded) {
			return false;
		}
		if (mappingDirection != other.mappingDirection) {
			return false;
		}
		if (source == null) {
			if (other.source != null) {
				return false;
			}
		} else if (!source.equals(other.source)) {
			return false;
		}
		return true;
	}
    
  
    
}
