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

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Map;

import ma.glasnost.orika.MappingException;
import ma.glasnost.orika.metadata.Property;
import ma.glasnost.orika.metadata.Type;

/**
 * IntrospectionPropertyResolver leverages JavaBeans introspector to resolve
 * properties for provided types.<br>
 * 
 * @author
 * 
 */
public class IntrospectorPropertyResolver extends PropertyResolver {
    
    /**
     * Constructs a new IntrospectorPropertyResolver
     * 
     * @param includePublicFields
     *            whether properties for public fields should be processed as
     *            properties
     */
    public IntrospectorPropertyResolver(boolean includePublicFields) {
        super(includePublicFields);
    }
    
    /**
     * Constructs a new IntrospectorPropertyResolver which includes public
     * fields as properties
     */
    public IntrospectorPropertyResolver() {
        super(true);
    }
    
    /**
     * Collects all properties for the specified type.
     * 
     * @param type
     *            the type for which to collect properties
     * @param referenceType
     *            the reference type for use in resolving generic parameters as
     *            needed
     * @param properties
     *            the properties collected for the current type
     */
    protected void collectProperties(Class<?> type, Type<?> referenceType, Map<String, Property> properties) {
        
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(type);
            PropertyDescriptor[] descriptors = beanInfo.getPropertyDescriptors();
            
            for (final PropertyDescriptor pd : descriptors) {
                
                try {
                    Method readMethod = getReadMethod(pd, type);
                    Method writeMethod = getWriteMethod(pd, type);
                    
                    processProperty(pd.getName(), pd.getPropertyType(), readMethod, writeMethod, type, referenceType, properties);
                    
                } catch (final Exception e) {
                    /*
                     * Wrap with info for the property we were trying to
                     * introspect
                     */
                    throw new RuntimeException("Unexpected error while trying to resolve property " + referenceType.getCanonicalName()
                            + ", [" + pd.getName() + "]", e);
                }
            }
        } catch (IntrospectionException e) {
            throw new MappingException(e);
        }
    }
    
    /**
     * Get the read method for the particular property descriptor
     * 
     * @param pd the property descriptor
     * @return the property's read method
     */
    private Method getReadMethod(PropertyDescriptor pd, Class<?> type) {
        final String capitalName = capitalize(pd.getName());
        Method readMethod;
        if (pd.getReadMethod() == null && Boolean.class.equals(pd.getPropertyType())) {
            /*
             * Special handling for Boolean "is" read method; not strictly
             * compliant with the JavaBeans specification, but still very common
             */
            try {
                readMethod = type.getMethod("is" + capitalName);
            } catch (NoSuchMethodException e) {
                readMethod = null;
            }
        } else {
            readMethod = pd.getReadMethod();
        } 
        
        if (readMethod != null && readMethod.isBridge()) {
            /*
             * Special handling for a bug in sun jdk 1.6.0_u5
             * http://bugs.sun.com/view_bug.do?bug_id=6788525
             */
            readMethod = getNonBridgeAccessor(readMethod);
        }
        
        return readMethod;
    }
    
    /**
     * Gets the write method for the particular property descriptor
     * 
     * @param pd the property descriptor
     * @return the property's write method
     */
    private Method getWriteMethod(PropertyDescriptor pd, Class<?> type) {
        
        Method writeMethod = pd.getWriteMethod();
        if (writeMethod == null) {
            /*
             * Special handling for fluid APIs where setters return
             * a value
             */
            try {
                writeMethod = type.getMethod("set" + capitalize(pd.getName()), pd.getPropertyType());
            } catch (NoSuchMethodException e) {
                writeMethod = null;
            }
        }
        return writeMethod;
    }
    
    /**
     * Get a real accessor from a bridge method. work around to
     * http://bugs.sun.com/view_bug.do?bug_id=6788525
     * 
     * @param bridgeMethod
     *            any method that can potentially be a bridge method
     * @return if it is not a problematic method, it is returned back
     *         immediately if we can find a non-bridge method with the same name
     *         we return that if we cannot find a non-bridge method we return
     *         the bridge method back (to prevent any unintended breakage)
     */
    private static Method getNonBridgeAccessor(Method bridgeMethod) {
        
        Method realMethod = bridgeMethod;
        Method[] otherMethods = bridgeMethod.getDeclaringClass().getMethods();
        for (Method possibleRealMethod : otherMethods) {
            if (possibleRealMethod.getName().equals(bridgeMethod.getName()) && !possibleRealMethod.isBridge()
                    && possibleRealMethod.getParameterTypes().length == 0) {
                realMethod = possibleRealMethod;
                break;
            }
        }
        return realMethod;
    }
    
}
