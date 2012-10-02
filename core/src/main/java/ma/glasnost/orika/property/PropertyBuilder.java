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

import java.lang.reflect.Method;

import ma.glasnost.orika.metadata.Property;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.metadata.TypeFactory;

/**
 * @author matt.deboer@gmail.com
 * 
 */
class PropertyBuilder {
    
    private Method readMethod;
    private Method writeMethod;
    private String getter;
    private String setter;
    private String propertyTypeName;
    private Type<?> propertyType;
    private Type<?> owningType;
    private String name;
    private PropertyResolver propertyResolver;
    
    public PropertyBuilder(Type<?> owningType, PropertyResolver propertyResolver) {
        this.owningType = owningType;
        this.propertyResolver = propertyResolver;
    }
    
    /**
     * @param getter
     *            the getter to set
     */
    public void getter(String getter) {
        this.getter = getter;
    }
    
    /**
     * @param setter
     *            the setter to set
     */
    public void setter(String setter) {
        this.setter = setter;
    }
    
    public void name(String name) {
        this.name = name;
    }
    
    public void typeName(String typeName) {
        this.propertyTypeName = typeName;
    }
    
    public void getterMethod(Method readMethod) {
        this.readMethod = readMethod;
        if (this.propertyType == null) {
            this.propertyType = propertyResolver.resolvePropertyType(readMethod, null, owningType.getRawType(), owningType);
        }
    }
    
    /**
     * @return the readMethod
     */
    public Method getReadMethod() {
        return readMethod;
    }
    
    /**
     * @return the writeMethod
     */
    public Method getWriteMethod() {
        return writeMethod;
    }
    
    public void setterMethod(Method writeMethod) {
        this.writeMethod = writeMethod;
        if (writeMethod.getParameterTypes().length == 1) {
            this.propertyType = propertyResolver.resolvePropertyType(readMethod, writeMethod.getParameterTypes()[0],
                    owningType.getRawType(), owningType);
        }
    }
    
    public Property build() {
        validate();
        Property property = new Property();
        property.setName(name);
        if (propertyType == null && propertyTypeName != null) {
            property.setType(TypeFactory.valueOf(propertyTypeName));
        } else if (propertyType != null) {
            property.setType(TypeFactory.valueOf(propertyType));
        } else {
            throw new IllegalStateException("no type specified for property '" + name + "{" + getter + (setter != null ? "|" + setter : "")
                    + "}'");
        }
        property.setExpression(name);
        if (readMethod != null) {
            property.setGetter(readMethod.getName() + "()");
        } else {
            property.setGetter(getter);
        }
        if (writeMethod != null) {
            property.setSetter(writeMethod.getName() + "(%s)");
        } else {
            property.setSetter(setter);
        }
        
        return property;
    }
    
    private void validate() {
        
        if (readMethod == null && writeMethod == null && getter == null && setter == null) {
            throw new IllegalArgumentException("property " + owningType.getCanonicalName() + "[" + name + "]"
                    + " cannot be read or written");
        } else {
            
            if (!"".equals(getter) || !"".equals(setter)) {
                for (Method m : owningType.getRawType().getMethods()) {
                    if (getter != null && m.getName().equals(getter) && m.getParameterTypes().length == 0) {
                        getterMethod(m);
                    } else if (setter != null && m.getName().endsWith(setter) && m.getParameterTypes().length == 1) {
                        setterMethod(m);
                    }
                }
            }
            
            if (writeMethod != null && writeMethod.getParameterTypes().length != 1) {
                throw new IllegalArgumentException("writeMethod (" + writeMethod.getName() + ") for " + owningType.getCanonicalName() + "["
                        + name + "] does not have exactly 1 input argument ");
            }
            
            if (readMethod != null
                    && (readMethod.getReturnType() == null || readMethod.getReturnType().equals(Void.TYPE) || readMethod.getReturnType()
                            .equals(Void.class))) {
                throw new IllegalArgumentException("readMethod (" + readMethod.getName() + ") for " + owningType.getCanonicalName() + "["
                        + name + "] does not return a value ");
            }
            
            if (readMethod != null && writeMethod != null && propertyType != null) {
                
                if (!readMethod.getReturnType().isAssignableFrom(propertyType.getRawType())) {
                    /*
                     * If we've already parsed the write method, and the read
                     * method type is not a sub-type of the write method's type,
                     * the two should not be considered to form a 'property'.
                     */
                    throw new IllegalArgumentException("write method (" + writeMethod.getName() + ") for " + owningType.getCanonicalName()
                            + "[" + name + "]" + " has type (" + writeMethod.getParameterTypes()[0].getCanonicalName() + ") "
                            + "is not assignable from the type (" + readMethod.getReturnType().getCanonicalName() + ") for "
                            + "the corresponding read method (" + readMethod.getName() + ")");
                }
            }
        }
    }
    
}
