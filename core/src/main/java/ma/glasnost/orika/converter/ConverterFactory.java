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
package ma.glasnost.orika.converter;

public interface ConverterFactory {
    
    /**
     * Lookup for an anonymous converter that can handle the conversion between
     * this two types
     * 
     * @param sourceClass
     * @param destinationClass
     * @return converter
     */
    Converter<Object, Object> getConverter(Class<Object> sourceClass, Class<Object> destinationClass);
    
    /**
     * Lookup a converter by id
     * 
     * @param converterId
     * @return
     */
    Converter<Object, Object> getConverter(String converterId);
    
    /**
     * Register a generic and (anonymous) converter
     * 
     * @param converter
     */
    <S, D> void registerConverter(Converter<S, D> converter);
    
    /**
     * Register an identified converter
     * 
     * @param converterId
     * @param converter
     */
    <S, D> void registerConverter(String converterId, Converter<S, D> converter);
    
    /**
     * Check if an identified converter exists
     * 
     * @param converterId
     * @return
     */
    boolean hasConverter(String converterId);
    
    /**
     * Verify if there is an anonymous converter can handle the conversion
     * between two types
     * 
     * @param sourceClass
     * @param destinationClass
     * @return true / false
     */
    boolean canConvert(Class<Object> sourceClass, Class<Object> destinationClass);
    
}