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
package ma.glasnost.orika;

import ma.glasnost.orika.metadata.Type;

/**
 * DedicatedMapperFacade represents a caching mapper configuration
 * which is dedicated to mapping a particular pair of types.
 * 
 * @author matt.deboer@gmail.com
 * 
 */
public interface DedicatedMapperFacade<A, B> {
    
    /**
     * Generates a new instance of the 'B' type based on the specified
     * instance of 'A' 
     * 
     * @param source
     * @return
     */
    public B mapAtoB(A instanceA);
    
    /**
     * Generates a new instance of the 'B' type based on the specified
     * instance of 'A' 
     * 
     * @param source
     * @param context
     * @return
     */
    public B mapAtoB(A instanceA, MappingContext context);
    
    /**
     * Generates a new instance of the 'A' type based on the specified
     * instance of 'B' 
     * 
     * @param source
     * @return
     */
    public A mapBtoA(B instanceB);
    
    /**
     * Generates a new instance of the 'A' type based on the specified
     * instance of 'B' 
     * 
     * @param source
     * @param context
     * @return
     */
    public A mapBtoA(B instanceB, MappingContext context);
    
    /**
     * 
     * Maps properties (in place) from the instance of 'A' to the provided
     * instance of 'B' 
     * 
     * @param source
     * @param destination
     */
    public void mapAtoB(A instanceA, B instanceB);
    
    /**
     * 
     * Maps properties (in place) from the instance of 'A' to the provided
     * instance of 'B' 
     * 
     * @param source
     * @param context
     * @param destination
     */
    public void mapAtoB(A instanceA, B instanceB, MappingContext context);
    
    /**
     * Maps properties (in place) from the instance of 'B' to the provided
     * instance of 'A'
     * 
     * @param destination
     * @param source
     */
    public void mapBtoA(B instanceB, A instanceA);
    
    /**
     * Maps properties (in place) from the instance of 'B' to the provided
     * instance of 'A'
     * 
     * @param destination
     * @param context
     * @param source
     */
    public void mapBtoA(B instanceB, A instanceA, MappingContext context);
    
    
    /**
     * @return the 'A' type for this DedicatedMapperFacade
     */
    public Type<A> getAType();
    
    /**
     * @return the 'B' type for this DedicatedMapperFacade
     */
    public Type<B> getBType();
    
}
