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
class DefaultDedicatedMapperFacade<A, B> implements DedicatedMapperFacade<A, B> {
    
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
    
    public B mapAtoB(A instanceA) {
        return mapAtoB(instanceA, new MappingContext());
    }
    
    public A mapBtoA(B source) {
        return mapBtoA(source, new MappingContext());
    }
    
    public void mapAtoB(A instanceA, B instanceB) {
        mapAtoB(instanceA, instanceB, new MappingContext());
    }
    
    public void mapBtoA(B instanceB, A instanceA) {
        mapBtoA(instanceB, instanceA, new MappingContext());
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see ma.glasnost.orika.DedicatedMapperFacade#mapAtoB(java.lang.Object,
     * ma.glasnost.orika.MappingContext)
     */
    @SuppressWarnings("unchecked")
    public B mapAtoB(A instanceA, MappingContext context) {
        
        if (aToB == null) {
            synchronized (this) {
                if (aToB == null) {
                    aToB = mapperFacade.resolveMappingStrategy(instanceA, sourceType, destinationType, false, context);
                }
            }
        }
        return (B) aToB.map(instanceA, null, context);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see ma.glasnost.orika.DedicatedMapperFacade#mapBtoA(java.lang.Object,
     * ma.glasnost.orika.MappingContext)
     */
    @SuppressWarnings("unchecked")
    public A mapBtoA(B instanceB, MappingContext context) {
        if (bToA == null) {
            synchronized (this) {
                if (bToA == null) {
                    bToA = mapperFacade.resolveMappingStrategy(instanceB, destinationType, sourceType, false, context);
                }
            }
        }
        return (A) bToA.map(instanceB, null, context);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see ma.glasnost.orika.DedicatedMapperFacade#mapAtoB(java.lang.Object,
     * java.lang.Object, ma.glasnost.orika.MappingContext)
     */
    public void mapAtoB(A instanceA, B instanceB, MappingContext context) {
        if (aToBInPlace == null) {
            synchronized (this) {
                if (aToBInPlace == null) {
                    aToBInPlace = mapperFacade.resolveMappingStrategy(instanceA, sourceType, destinationType, true, context);
                }
            }
        }
        aToBInPlace.map(instanceA, instanceB, context);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see ma.glasnost.orika.DedicatedMapperFacade#mapBtoA(java.lang.Object,
     * java.lang.Object, ma.glasnost.orika.MappingContext)
     */
    public void mapBtoA(B instanceB, A instanceA, MappingContext context) {
        if (bToAInPlace == null) {
            synchronized (this) {
                if (bToAInPlace == null) {
                    bToAInPlace = mapperFacade.resolveMappingStrategy(instanceB, destinationType, sourceType, false, context);
                }
            }
        }
        bToAInPlace.map(instanceB, instanceA, context);
    }
}
