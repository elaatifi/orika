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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ma.glasnost.orika.metadata.Property;
import ma.glasnost.orika.metadata.Type;

/**
 * RegexPropertyResolver uses regular expressions to find properties based
 * on patterns configured for locating the read and write methods.<br><br>
 * 
 * The patterns provided should produce a match where group(1) returns the 
 * name of the property.<br>
 * <em>Note that the name will automatically be un-capitalized, so you need not worry
 * about defining your regular expression to handle this.</em><br><br>
 * Only no-argument getter methods returning a type are considered for a read method match, 
 * and only single-argument methods are considered for a write method match; the write method
 * need not have a void return type.<br><br>
 * The type of the setter method must be a sub-type (or matching type) of the getter method's type;
 * if the getter method is a strict sub-type, then the type of the setter method will define the 
 * type of the property.
 * <br>
 * <h3>Example</h3><br> <strong>"read([\w]+)Property"</strong> would match a method named <strong>'readMySpecialProperty'</strong>,
 * and define the name of the corresponding property as 'MySpecial', which will be automatically un-capitalized
 * to <strong>'mySpecial'</strong>.<br><br>
 * 
 * @author matt.deboer@gmail.com
 *
 */
public class RegexPropertyResolver extends IntrospectorPropertyResolver {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(RegexPropertyResolver.class);
    private final Pattern readPattern;
    private final Pattern writePattern;
    private final boolean includeJavaBeans;
    
    /**
     * @param readMethodRegex
     * @param writeMethodRegex
     * @param includeJavaBeans
     * @param includePublicFields
     */
    public RegexPropertyResolver(String readMethodRegex, String writeMethodRegex, boolean includeJavaBeans, boolean includePublicFields) {
        super(includePublicFields);
        this.includeJavaBeans = includeJavaBeans;
        this.readPattern = Pattern.compile(readMethodRegex);
        this.writePattern = Pattern.compile(writeMethodRegex);
    }
    
    protected String uncapitalize(String string) {
        return string.substring(0, 1).toLowerCase() + string.substring(1, string.length());
    }
    
    /**
     * Data structure for collecting resolved property info
     */
    private static final class PropertyInfo {
        /**
         * The read method for a property
         */
        private Method readMethod;
        /**
         * The write method for a property
         */
        private Method writeMethod;
        /**
         * The type for a property
         */
        private Class<?> type;
    }
    
    /**
     * Collects all properties for the specified type.
     * 
     * @param type the type for which to collect properties
     * @param referenceType the reference type for use in resolving generic parameters as needed
     * @param properties the properties collected for the current type
     */
    protected void collectProperties(Class<?> type, Type<?> referenceType, Map<String, Property> properties) {
        
        Map<String, PropertyInfo> collectedMethods = new LinkedHashMap<String, PropertyInfo>();
        for (Method m: type.getMethods()) {
            
            if (m.getParameterTypes().length == 0 && m.getReturnType() != null && m.getReturnType() != Void.TYPE) {
                Matcher readMatcher = readPattern.matcher(m.getName());
                if (readMatcher.matches()) {
                    String name = readMatcher.group(1);
                    if (name != null) {
                        name = uncapitalize(name);
                        PropertyInfo info = collectedMethods.get(name);
                        if (info != null && info.type != null && !m.getReturnType().isAssignableFrom(info.type)) {
                            /*
                             * If we've already parsed the write method, and the read method type is not
                             * a sub-type of the write method's type, the two should not be considered to form
                             * a 'property'.
                             */
                            LOGGER.warn("skipping write method for " + type.getCanonicalName() + "["+ name +"]" + 
                                   " because it's type (" + info.type.getCanonicalName() + ") " +
                                   "is not assignable from the corresponding read method's type (" + 
                                   m.getReturnType().getCanonicalName() + ")");
                            
                        } else {
                            if (info == null) {
                                info = new PropertyInfo();
                                collectedMethods.put(name, info);
                            }
                            if (info.type == null) {
                                info.type = m.getReturnType();
                            }
                            info.readMethod = m;
                        }
                    } else {
                        throw new IllegalStateException("the configured readMethod regex '" + readPattern + 
                                "' does not define group (1) containing the property's name");
                    }
    
                } 
            } else if (m.getParameterTypes().length == 1) {
            
                Matcher writeMatcher = writePattern.matcher(m.getName());
                if (writeMatcher.matches()) {
                    String name = writeMatcher.group(1);
                    if (name != null) {
                        name = uncapitalize(name);
                        PropertyInfo info = collectedMethods.get(name);
                        if (info != null && info.type != null && !info.type.isAssignableFrom(m.getParameterTypes()[0])) {
                            /*
                             * If we've already parsed the read method, and the read method type is not
                             * a sub-type of the write method's type, the two should not be considered to form
                             * a 'property'.
                             */
                            LOGGER.warn("skipping write method for " + type.getCanonicalName() + "["+ name +"]" + 
                                   " because it's type (" + m.getParameterTypes()[0].getCanonicalName() + ") " +
                                   "is not assignable from the corresponding read method's type (" + 
                                   info.type.getCanonicalName() + ")");
                            
                        } else {
                            if (info == null) {
                                info = new PropertyInfo();
                                collectedMethods.put(name, info);
                            }
                            
                            info.type = m.getParameterTypes()[0]; 
                            info.writeMethod = m;
                        }
                    } else {
                        throw new IllegalStateException("the configured writeMethod regex '" + writePattern + 
                                "' does not define group (1) containing the property's name");
                    }
                }
            }
        }
           
        for (Entry<String, PropertyInfo> entry: collectedMethods.entrySet()) {
            processProperty(entry.getKey(), entry.getValue().type, entry.getValue().readMethod, entry.getValue().writeMethod, type, referenceType, properties);
        }
        
        if (includeJavaBeans) {
            super.collectProperties(type, referenceType, properties);
        }
    }
}
