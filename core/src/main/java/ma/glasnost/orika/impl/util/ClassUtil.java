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

package ma.glasnost.orika.impl.util;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public final class ClassUtil {
    
    private static final String CGLIB_ID = "$$EnhancerByCGLIB$$";
    private static final String JAVASSIST_PACKAGE = "org.javassist.tmp.";
    private static final String JAVASSIST_NAME = "_$$_javassist_";
    private static final Set<Class<?>> IMMUTABLES_TYPES = getImmutablesTypes();
    
    private ClassUtil() {
        
    }
    
    @SuppressWarnings("unchecked")
    private static Set<Class<?>> getImmutablesTypes() {
        return new HashSet<Class<?>>(Arrays.asList(String.class, Integer.class, Long.class, Boolean.class, Character.class, Byte.class,
                Double.class, Float.class, BigDecimal.class, Byte.TYPE, Short.TYPE, Integer.TYPE, Boolean.TYPE, Long.TYPE, Float.TYPE,
                Double.TYPE, Character.TYPE, Date.class, java.sql.Date.class));
        
    }
    
    public static boolean isImmutable(Class<?> clazz) {
        return IMMUTABLES_TYPES.contains(clazz) || clazz.isEnum();
    }
    
    public static boolean isProxy(Class<?> clazz) {
        if (clazz.isInterface()) {
            return false;
        }
        final String className = clazz.getName();
        return className.contains(CGLIB_ID) || className.startsWith(JAVASSIST_PACKAGE) || className.contains(JAVASSIST_NAME);
    }
}
