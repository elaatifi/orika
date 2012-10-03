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

import ma.glasnost.orika.property.PropertyResolver;

public class NestedProperty extends Property {
    
    private final Property[] path;
    
    public NestedProperty(String expression, Property property, Property[] path) {
        super(expression,property.getName(),property.getGetter(),property.getSetter(),property.getType(),property.getElementType());
        this.path = path;
    }
    
    @Override
    public NestedProperty copy() {
    	
    	Property[] copyPath = new Property[path.length];
    	for (int i=0, count = path.length; i < count; ++i) {
    		copyPath[i] = path[i].copy();
    	}
    	NestedProperty copy = new NestedProperty(this.getExpression(), super.copy(), copyPath);
        return copy;
    }
    
    @Override
    public Property[] getPath() {
        return path;
    }
    
    @Override
    public boolean hasPath() {
        return true;
    }
    
    public boolean equals(Object other) {
    	return super.equals(other);
    }
    
    public int hashCode() {
    	return super.hashCode();
    }
    
    static class Builder extends Property.Builder {

        private Property.Builder parent;
        
        /**
         * @param owningType
         * @param name
         */
        Builder(Property.Builder parent, String name) {
            super(null, name);
            this.parent = parent;
        }
        
        public Property build(PropertyResolver propertyResolver) {
            Property parentProperty = parent.build(propertyResolver);
            
            Property[] path;
            if (parentProperty instanceof NestedProperty) {
                path = ((NestedProperty)parentProperty).getPath();
                System.arraycopy(path, 0, path, 0, path.length + 1);
                path[path.length-1] = parentProperty;
            } else {
                path = new Property[]{parentProperty};
            }
            this.owningType = parentProperty.getType();
            
            Property p = super.build(propertyResolver);
            return new NestedProperty("", p, path);
        }
        
    }
    
}
