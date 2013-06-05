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
package ma.glasnost.orika.util;

import ma.glasnost.orika.Converter;
import ma.glasnost.orika.Mapper;
import ma.glasnost.orika.metadata.MapperKey;
import ma.glasnost.orika.metadata.Type;

/**
 * Ordering
 * 
 * @param <T>
 */
public abstract class Ordering<T> {
    
    /**
     * Returns -1 if object1 should be ordered before object2; Returns 1 if
     * object2 should be ordered before object1; Returns 0 if there is no
     * ordering implied between the objects (they are either equal, or
     * unrelated).
     * 
     * 
     * @param object1
     * @param object2
     * @return -1 if object1 should be ordered before object2; 1 if object2
     *         should be ordered before object1; 0 if there is no ordering
     *         implied between the objects
     */
    public abstract int order(T object1, T object2);
    
    private static final int compare(Type<?> aType1, Type<?> bType1, Type<?> aType2, Type<?> bType2, boolean isBidirectional) {
        
        if ((aType1.equals(aType2) && bType1.equals(bType2)) || (aType1.equals(bType2) && aType2.equals(bType1))) {
            return 0;
        } else if ((aType1.isAssignableFrom(aType2) && bType1.isAssignableFrom(bType2))
                || (isBidirectional && aType1.isAssignableFrom(bType2) && bType1.isAssignableFrom(aType2))) {
            return 1;
        } else if ((aType2.isAssignableFrom(aType1) && bType2.isAssignableFrom(bType1))
                || (isBidirectional && aType2.isAssignableFrom(bType1) && bType2.isAssignableFrom(aType1))) {
            return -1;
        } else {
            return 0;
        }
    }
    
    /**
     * 
     */
    public static final Ordering<MapperKey> MAPPER_KEY = new Ordering<MapperKey>() {
        public int order(MapperKey mapper1, MapperKey mapper2) {
            return Ordering.compare(mapper1.getAType(), mapper1.getBType(), mapper2.getAType(), mapper2.getBType(), true);
        }
    };
    
    /**
     * 
     */
    public static final Ordering<Mapper<Object, Object>> MAPPER = new Ordering<Mapper<Object, Object>>() {
        public int order(Mapper<Object, Object> mapper1, Mapper<Object, Object> mapper2) {
            return Ordering.compare(mapper1.getAType(), mapper1.getBType(), mapper2.getAType(), mapper2.getBType(), true);
        }
    };
    
    /**
     * 
     */
    public static final Ordering<Converter<Object, Object>> CONVERTER = new Ordering<Converter<Object, Object>>() {
        public int order(Converter<Object, Object> mapper1, Converter<Object, Object> mapper2) {
            return Ordering.compare(mapper1.getAType(), mapper1.getBType(), mapper2.getAType(), mapper2.getBType(), false);
        }
    };
    
}
