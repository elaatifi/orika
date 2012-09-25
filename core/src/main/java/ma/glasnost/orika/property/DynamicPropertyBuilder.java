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
class DynamicPropertyBuilder {
    
    private Method readMethod;
    private Method writeMethod;
    private Class<?> propertyType;
    private Type<?> owningType;
    private String name;
    
    public DynamicPropertyBuilder(Type<?> owningType) {
        this.owningType = owningType;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setReadMethod(Method readMethod) {
        this.readMethod = readMethod;
        if (this.propertyType == null) {
            this.propertyType = readMethod.getReturnType();
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

    public void setWriteMethod(Method writeMethod) {
        this.writeMethod = writeMethod;
        if (writeMethod.getParameterTypes().length == 1) {
            this.propertyType = writeMethod.getParameterTypes()[0];
        } 
    }
 
    public Property toProperty() {
        validate();
        Property property = new Property();
        property.setName(name);
        property.setType(TypeFactory.valueOf(propertyType));
        property.setExpression(name);
        if (readMethod != null) {
            property.setGetter(readMethod.getName() + "()");
        }
        if (writeMethod != null) {
            property.setSetter(writeMethod.getName() + "(%s)");
        }
        return property;
    }
    
    private void validate() {
        
        if (readMethod == null && writeMethod == null) {
            throw new IllegalArgumentException("property " + 
                    owningType.getCanonicalName() + "["+ name +"]" + 
                   " cannot be read or written");
        } else {
            if (writeMethod != null && writeMethod.getParameterTypes().length != 1) {
                throw new IllegalArgumentException("writeMethod (" + writeMethod.getName() + ") for " + 
                        owningType.getCanonicalName() + "["+ name +"] does not have exactly 1 input argument ");
            }
            
            if (readMethod != null && (readMethod.getReturnType() == null || readMethod.getReturnType().equals(Void.TYPE)) || readMethod.getReturnType().equals(Void.class)) {
                throw new IllegalArgumentException("readMethod (" + readMethod.getName() + ") for " + 
                        owningType.getCanonicalName() + "["+ name +"] does not return a value ");
            }
            
            if (readMethod != null && writeMethod != null) { 
        
                if(!readMethod.getReturnType().isAssignableFrom(propertyType)) {
                    /*
                     * If we've already parsed the write method, and the read method type is not
                     * a sub-type of the write method's type, the two should not be considered to form
                     * a 'property'.
                     */
                    throw new IllegalArgumentException("write method ("+writeMethod.getName()+") for " + 
                            owningType.getCanonicalName() + "["+ name +"]" + 
                           " has type (" + writeMethod.getParameterTypes()[0].getCanonicalName() + ") " +
                           "is not assignable from the type (" + readMethod.getReturnType().getCanonicalName() + ") for " +
                           "the corresponding read method (" + readMethod.getName() + ")");
                }
            }   
        }  
    }
    
}
