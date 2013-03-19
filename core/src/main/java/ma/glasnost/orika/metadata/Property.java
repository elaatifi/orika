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

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ma.glasnost.orika.MapEntry;
import ma.glasnost.orika.property.PropertyResolver;

/**
 * Property is an immutable representation of an accessor/mutator pair (either
 * of which may be null) that is used to generate code needed to copy data from
 * one type to another.
 * 
 */
public class Property {
    
    private static final Property[] EMPTY_PATH = new Property[0];
    
    private final String expression;
    private final String name;
    private final String getter;
    private final String setter;
    private final Type<?> type;
    private final Type<?> elementType;
    private final Property container;
    
    protected Property(final String expression, final String name, final String getter, final String setter, final Type<?> type,
            final Type<?> elementType, final Property container) {
        super();
        this.expression = expression;
        this.name = name;
        this.getter = getter;
        this.setter = setter;
        this.type = type;
        
        this.elementType = getElementType(type, elementType);
        
        this.container = container;
    }
    
    private Type<?> getElementType(final Type<?> type, final Type<?> elementType) {
        
        if (elementType != null) {
            return elementType;
        }
        
        if (type.getActualTypeArguments().length > 0 && elementType == null) {
            return (Type<?>) type.getActualTypeArguments()[0];
        } else if (type.isCollection()) {
            Type<?> collectionElementType = elementType;
            Type<?> collection = type.findAncestor(Collection.class);
            if (collection != null) {
                collectionElementType = (Type<?>) collection.getActualTypeArguments()[0];
            }
            return collectionElementType;
            
        } else if (type.isMap()) {
            
            Type<?> mapElementType = elementType;
            Type<?> map = type.findAncestor(Map.class);
            if (map != null) {
                @SuppressWarnings("unchecked")
                Type<? extends Map<Object, Object>> mapType = (Type<? extends Map<Object, Object>>) map;
                mapElementType = MapEntry.entryType(mapType);
            }
            return mapElementType;
            
        } else {
            return elementType;
        }
    }
    
    public Property copy() {
        return copy(this.type);
    }
    
    public Property copy(final Type<?> newType) {
        return new Property.Builder().name(this.name)
                .getter(this.getter)
                .setter(this.setter)
                .type(newType)
                .elementType(this.elementType)
                .build(null);
    }
    
    /**
     * @return the expression describing this property
     */
    public String getExpression() {
        return expression;
    }
    
    /**
     * @return the name of this property
     */
    public String getName() {
        return name;
    }
    
    /**
     * @return the type of this property
     */
    public Type<?> getType() {
        return type;
    }
    
    /**
     * @return the string description of the accessor for this property
     */
    public String getGetter() {
        return getter;
    }
    
    /**
     * @return the string description of the mutator for this property
     */
    public String getSetter() {
        return setter;
    }
    
    /**
     * @return the name of the setter method for this property
     */
    public String getSetterName() {
        return setter.split("[\\( \\=]")[0];
    }
    
    /**
     * @return the name of the getter method for this property
     */
    public String getGetterName() {
        return getter.split("[\\( \\=]")[0];
    }
    
    /**
     * @return the element type for this property
     */
    public Type<?> getElementType() {
        return elementType;
    }
    
    /**
     * @return the raw type of this property
     */
    public Class<?> getRawType() {
        return getType().getRawType();
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        
        final Property property = (Property) o;
        
        if (!expression.equals(property.expression)) {
            return false;
        }
        if (getter != null ? !getter.equals(property.getter) : property.getter != null) {
            return false;
        }
        if (setter != null ? !setter.equals(property.setter) : property.setter != null) {
            return false;
        }
        return !(type != null && !type.equals(property.type));
    }
    
    public boolean isPrimitive() {
        return type.getRawType().isPrimitive();
    }
    
    public boolean isArray() {
        return type.getRawType().isArray();
    }
    
    public boolean isAssignableFrom(final Property p) {
        return type.isAssignableFrom(p.type);
    }
    
    public boolean isCollection() {
        return Collection.class.isAssignableFrom(type.getRawType());
    }
    
    public boolean isSet() {
        return Set.class.isAssignableFrom(type.getRawType());
    }
    
    public boolean isList() {
        return List.class.isAssignableFrom(type.getRawType());
    }
    
    public boolean isMap() {
        return Map.class.isAssignableFrom(type.getRawType());
    }
    
    public boolean isMapKey() {
        return false;
    }
    
    public boolean isListElement() {
        return false;
    }
    
    public boolean isArrayElement() {
        return false;
    }
    
    /**
     * @return true if this property is a Map, Collection or Array
     */
    public boolean isMultiOccurrence() {
        return isMap() || isCollection() || isArray();
    }
    
    public boolean hasPath() {
        return false;
    }
    
    public Property[] getPath() {
        return EMPTY_PATH;
    }
    
    public Property getContainer() {
        return container;
    }
    
    public Property getElement() {
        return null;
    }
    
    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + (getter != null ? getter.hashCode() : 0);
        result = 31 * result + (setter != null ? setter.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }
    
    @Override
    public String toString() {
        return expression + "(" + type + ")";
    }
    
    public boolean isEnum() {
        return type.getRawType().isEnum();
    }
    
    /**
     * Builder is used to construct immutable Property instances
     * 
     */
    public static class Builder {
        
        private Method getterMethod;
        private Method setterMethod;
        private String getter;
        private String setter;
        private String propertyTypeName;
        private Type<?> elementType;
        private Type<?> propertyType;
        protected Type<?> owningType;
        private String name;
        private String expression;
        private Property container;
        private Property[] path;
        
        public Builder(final Type<?> owningType, final String name) {
            this.owningType = owningType;
            this.name = name;
        }
        
        public Builder() {
            
        }
        
        public Builder merge(final Property property) {
            
            if (property.getter != null) {
                getter = property.getter;
                getterMethod = null;
            }
            if (property.setter != null) {
                setter = property.setter;
                setterMethod = null;
            }
            if (elementType == null || (property.elementType != null && elementType.isAssignableFrom(property.elementType))) {
                elementType = property.elementType;
            }
            if (propertyType == null || (property.type != null && propertyType.isAssignableFrom(property.type))) {
                propertyType = property.type;
            }
            if (container == null || property.container != null) {
                container = property.container;
            }
            if (property.getPath().length > 0) {
                path = property.getPath();
            }
            name = property.name;
            expression = property.expression;
            
            return this;
        }
        
        public static Builder propertyFor(final Type<?> owningType, final String name) {
            return new Builder(owningType, name);
        }
        
        public static Builder propertyFor(final Class<?> owningType, final String name) {
            return new Builder(TypeFactory.valueOf(owningType), name);
        }
        
        public static Builder propertyFor(final String owningTypeDescriptor, final String name) {
            return new Builder(TypeFactory.valueOf(owningTypeDescriptor), name);
        }
        
        public Builder nestedProperty(final String name) {
            return new NestedProperty.Builder(this, name);
        }
        
        private String fixQuotes(final String methodExpression) {
            String expression = methodExpression;
            StringBuilder output = new StringBuilder();
            while (expression.length() > 0) {
                int currentDouble = expression.indexOf('"');
                int currentSingle = expression.indexOf("'");
                if (currentSingle > 0 && (currentSingle < currentDouble || currentDouble == -1)) {
                    output.append(expression.subSequence(0, currentSingle));
                    expression = expression.substring(currentSingle + 1);
                    
                    int nextSingle = expression.indexOf("'");
                    if (nextSingle == 1) {
                        output.append("'" + expression.substring(0, 2));
                    } else {
                        output.append("\"" + expression.substring(0, nextSingle) + "\"");
                    }
                    expression = expression.substring(nextSingle + 1);
                } else if (currentDouble > 0) {
                    output.append(expression.subSequence(0, currentDouble));
                    expression = expression.substring(currentDouble + 1);
                    
                    int nextDouble = expression.indexOf('"');
                    output.append("\"" + expression.substring(0, nextDouble) + "\"");
                    expression = expression.substring(nextDouble + 1);
                } else {
                    output.append(expression);
                    expression = "";
                }
            }
            return output.toString();
        }
        
        /**
         * @param container
         *            the container to set
         */
        public Builder container(final Property container) {
            this.container = container;
            return this;
        }
        
        /**
         * @param path
         *            the path to set
         * @return
         */
        public Builder path(final Property[] path) {
            this.path = path;
            return this;
        }
        
        /**
         * @param getter
         *            the getter to set
         */
        public Builder getter(final String getter) {
            this.getter = fixQuotes(getter);
            return this;
        }
        
        /**
         * @param setter
         *            the setter to set
         */
        public Builder setter(final String setter) {
            this.setter = fixQuotes(setter);
            return this;
        }
        
        public Builder expression(final String expression) {
            this.expression = expression;
            return this;
        }
        
        public Builder name(final String name) {
            this.name = name;
            return this;
        }
        
        public Builder type(final java.lang.reflect.Type type) {
            this.propertyType = TypeFactory.valueOf(type);
            return this;
        }
        
        public Builder type(final String typeName) {
            this.propertyTypeName = typeName;
            return this;
        }
        
        public Builder elementType(final Type<?> elementType) {
            this.elementType = elementType;
            return this;
        }
        
        public Builder getter(final Method readMethod) {
            this.getterMethod = readMethod;
            return this;
        }
        
        /**
         * @return the readMethod
         */
        public Method getReadMethod() {
            return getterMethod;
        }
        
        /**
         * @return the writeMethod
         */
        public Method getWriteMethod() {
            return setterMethod;
        }
        
        public Builder setter(final Method writeMethod) {
            this.setterMethod = writeMethod;
            return this;
        }
        
        public Property build() {
            return build(null);
        }
        
        public Property build(final PropertyResolver propertyResolver) {
            validate(propertyResolver);
            
            if (propertyType == null && propertyTypeName != null) {
                propertyType = TypeFactory.valueOf(propertyTypeName);
            } else if (propertyType == null) {
                propertyType = TypeFactory.TYPE_OF_OBJECT;
            }
            if (getterMethod != null) {
                getter = getterMethod.getName() + "()";
            }
            if (setterMethod != null) {
                setter = setterMethod.getName() + "(%s)";
            }
            Property p = new Property(expression != null ? expression : name, name, getter, setter, propertyType, elementType, container);
            if (path != null) {
                p = new NestedProperty(expression, p, path);
            }
            return p;
        }
        
        private void validate(final PropertyResolver propertyResolver) {
            
            if (getterMethod == null && setterMethod == null && getter == null && setter == null) {
                throw new IllegalArgumentException("property " + (owningType != null ? owningType.getCanonicalName() : "") + "[" + name
                        + "]" + " cannot be read or written");
            } else {
                
                if (owningType != null && (!"".equals(getter) || !"".equals(setter))) {
                    for (Method m : owningType.getRawType().getMethods()) {
                        if (getter != null && m.getName().equals(getter) && m.getParameterTypes().length == 0) {
                            getter(m);
                        } else if (setter != null && m.getName().endsWith(setter) && m.getParameterTypes().length == 1) {
                            setter(m);
                        }
                    }
                }
                
                if (propertyResolver != null && getterMethod != null && this.propertyType == null) {
                    this.propertyType = propertyResolver.resolvePropertyType(getterMethod, null, owningType.getRawType(), owningType);
                }
                
                if (propertyResolver != null && setterMethod != null && setterMethod.getParameterTypes().length == 1) {
                    this.propertyType = propertyResolver.resolvePropertyType(getterMethod, setterMethod.getParameterTypes()[0],
                            owningType.getRawType(), owningType);
                }
                
                if (setterMethod != null && setterMethod.getParameterTypes().length != 1) {
                    throw new IllegalArgumentException("writeMethod (" + setterMethod.getName() + ") for " + owningType.getCanonicalName()
                            + "[" + name + "] does not have exactly 1 input argument ");
                }
                
                if (getterMethod != null
                        && (getterMethod.getReturnType() == null || getterMethod.getReturnType().equals(Void.TYPE) || getterMethod.getReturnType()
                                .equals(Void.class))) {
                    throw new IllegalArgumentException("readMethod (" + getterMethod.getName() + ") for " + owningType.getCanonicalName()
                            + "[" + name + "] does not return a value ");
                }
                
                if (getterMethod != null && setterMethod != null && propertyType != null) {
                    
                    if (!getterMethod.getReturnType().isAssignableFrom(propertyType.getRawType())) {
                        /*
                         * If we've already parsed the write method, and the
                         * read method type is not a sub-type of the write
                         * method's type, the two should not be considered to
                         * form a 'property'.
                         */
                        throw new IllegalArgumentException("write method (" + setterMethod.getName() + ") for "
                                + owningType.getCanonicalName() + "[" + name + "]" + " has type ("
                                + setterMethod.getParameterTypes()[0].getCanonicalName() + ") " + "is not assignable from the type ("
                                + getterMethod.getReturnType().getCanonicalName() + ") for " + "the corresponding read method ("
                                + getterMethod.getName() + ")");
                    }
                }
            }
        }
        
    }
    
}
