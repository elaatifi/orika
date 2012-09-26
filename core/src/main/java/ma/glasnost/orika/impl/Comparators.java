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
package ma.glasnost.orika.impl;

import java.util.Comparator;

import ma.glasnost.orika.Converter;
import ma.glasnost.orika.Mapper;
import ma.glasnost.orika.metadata.MapperKey;
import ma.glasnost.orika.metadata.Type;

/**
 * @author matt.deboer@gmail.com
 *
 */
public abstract class Comparators {
    
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
            /*
             * Unrelated, thus they should be considered "equal" in regards to
             * sorting
             */
            return 0;
        }
    }
    
    public static final Comparator<MapperKey> MAPPER_KEY = new Comparator<MapperKey>() {
        public int compare(MapperKey mapper1, MapperKey mapper2) {
            return Comparators.compare(mapper1.getAType(), mapper1.getBType(), mapper2.getAType(), mapper2.getBType(), true);
        }
    };
    
    public static final Comparator<Mapper<Object, Object>> MAPPER = new Comparator<Mapper<Object, Object>>() {
        public int compare(Mapper<Object, Object> mapper1, Mapper<Object, Object> mapper2) {
            return Comparators.compare(mapper1.getAType(), mapper1.getBType(), mapper2.getAType(), mapper2.getBType(), true);
        }
    };
    
    public static final Comparator<Converter<Object, Object>> CONVERTER = new Comparator<Converter<Object, Object>>() {
        public int compare(Converter<Object, Object> mapper1, Converter<Object, Object> mapper2) {
            return Comparators.compare(mapper1.getAType(), mapper1.getBType(), mapper2.getAType(), mapper2.getBType(), false);
        }
    };
    
    
}
