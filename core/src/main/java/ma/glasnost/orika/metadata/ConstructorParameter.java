/*
 * Orika - simpler, better and faster Java bean mapping
 *
 * Copyright (C) 2011-2013 Orika authors
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

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * ConstructorParameter proxies a constructor parameter of a given name and type
 * which appears in one or more constructors as a Property
 * 
 * @author mattdeboer
 * 
 */
public class ConstructorParameter extends Property {
    
    private final Map<Constructor<?>, Integer> owningConstructors;
    
    /**
     * @param name
     * @param type
     * @param constructors
     */
    public ConstructorParameter(String name, Type<?> type, Map<Constructor<?>, Integer> constructors) {
        super(name, name, null, null, type, null, null);
        
        if (constructors == null) {
            this.owningConstructors = Collections.emptyMap();
        } else {
            this.owningConstructors = new HashMap<Constructor<?>, Integer>(constructors);
        }
    }
    
    /**
     * Get the index within the specified constructor at which this parameter
     * occurs.
     * 
     * @param constructor
     * @return the argument index of this parameter within the specified
     *         constructor, or <code>null</code> if this parameter is not found
     *         within the specified constructor
     */
    public Integer getArgumentIndex(Constructor<?> constructor) {
        return owningConstructors.get(constructor);
    }
}
