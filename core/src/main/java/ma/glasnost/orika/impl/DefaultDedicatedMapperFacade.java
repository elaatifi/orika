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

import ma.glasnost.orika.DedicatedMapperFacade;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.impl.mapping.strategy.MappingStrategy;

/**
 * @author matt.deboer@gmail.com
 *
 */
class DefaultDedicatedMapperFacade<S, D> implements DedicatedMapperFacade<S, D> {
    
   protected volatile MappingStrategy aToB;
   protected volatile MappingStrategy bToA;
   protected volatile MappingStrategy aToBInPlace;
   protected volatile MappingStrategy bToAInPlace;
   
   protected final java.lang.reflect.Type sourceType;
   protected final java.lang.reflect.Type destinationType;
   protected final MapperFacadeImpl mapperFacade;
   
   DefaultDedicatedMapperFacade(MapperFacadeImpl mapperFacade, java.lang.reflect.Type sourceType, java.lang.reflect.Type destinationType) {
       this.mapperFacade = mapperFacade;
       this.sourceType = sourceType;
       this.destinationType = destinationType;
   }
   
   @SuppressWarnings("unchecked")
   public D mapAtoB(S source) {
       MappingContext context = new MappingContext();
       if (aToB == null) {
           synchronized(this) {
               if (aToB == null) {
                   aToB = mapperFacade.resolveMappingStrategy(source, sourceType, destinationType, false, context);
               }
           }
       }
       return (D) aToB.map(source, null, context);
   }
    
   @SuppressWarnings("unchecked")
   public S mapBtoA(D source) {
       MappingContext context = new MappingContext();
       if (bToA == null) {
           synchronized(this) {
               if (bToA == null) {
                   bToA = mapperFacade.resolveMappingStrategy(source, destinationType, sourceType, false, context);
               }
           }
       }
       return (S) bToA.map(source, null, context);
   }
    
   public void mapAtoB(S source, D destination) {
       MappingContext context = new MappingContext();
       if (aToBInPlace == null) {
           synchronized(this) {
               if (aToBInPlace == null) {
                   aToBInPlace = mapperFacade.resolveMappingStrategy(source, sourceType, destinationType, true, context);
               }
           }
       }
       aToBInPlace.map(source, destination, context);
   }
   
   public void mapBtoA(D destination, S source) {
       MappingContext context = new MappingContext();
       if (bToAInPlace == null) {
           synchronized(this) {
               if (bToAInPlace == null) {
                   bToAInPlace = mapperFacade.resolveMappingStrategy(source, destinationType, sourceType, false, context);
               }
           }
       }
       bToAInPlace.map(destination, source, context);
   }
}
