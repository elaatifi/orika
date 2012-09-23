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
import ma.glasnost.orika.MappingContextFactory;
import ma.glasnost.orika.impl.mapping.strategy.MappingStrategy;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.metadata.TypeFactory;

/**
 * @author matt.deboer@gmail.com
 * 
 */
class DefaultDedicatedMapperFacade<A, B> implements DedicatedMapperFacade<A, B> {
    
    protected volatile MappingStrategy aToB;
    protected volatile MappingStrategy bToA;
    protected volatile MappingStrategy aToBInPlace;
    protected volatile MappingStrategy bToAInPlace;
    
    
    protected final java.lang.reflect.Type rawAType;
    protected final java.lang.reflect.Type rawBType;
    protected final Type<A> aType;
    protected final Type<B> bType;
    protected final MapperFacadeImpl mapperFacade;
    protected final MappingContextFactory contextFactory;
    
    DefaultDedicatedMapperFacade(MapperFacadeImpl mapperFacade, MappingContextFactory contextFactory,  java.lang.reflect.Type typeOfA, java.lang.reflect.Type typeOfB) {
        this.mapperFacade = mapperFacade;
        this.contextFactory = contextFactory;
        this.rawAType = typeOfA;
        this.rawBType = typeOfB;
        this.aType = TypeFactory.valueOf(typeOfA);
        this.bType = TypeFactory.valueOf(typeOfB);
    }
    
    public Type<A> getAType() {
        return aType;
    }
    
    public Type<B> getBType() {
        return bType;
    }
    
    public B mapAtoB(A instanceA) {
        MappingContext context = contextFactory.getContext();
        try {
            return mapAtoB(instanceA, context);
        } finally {
            contextFactory.release(context);
        }
    }
    
    public A mapBtoA(B source) {
        MappingContext context = contextFactory.getContext();
        try {
            return mapBtoA(source, context);
        } finally {
            contextFactory.release(context);
        }
    }
    
    public void mapAtoB(A instanceA, B instanceB) {
        MappingContext context = contextFactory.getContext();
        try {
            mapAtoB(instanceA, instanceB, context);
        } finally {
            contextFactory.release(context);
        }
    }
    
    public void mapBtoA(B instanceB, A instanceA) {
        MappingContext context = contextFactory.getContext();
        try {
            mapBtoA(instanceB, instanceA, context);
        } finally {
            contextFactory.release(context);
        }
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see ma.glasnost.orika.DedicatedMapperFacade#mapAtoB(java.lang.Object,
     * ma.glasnost.orika.MappingContext)
     */
    @SuppressWarnings("unchecked")
    public B mapAtoB(A instanceA, MappingContext context) {
        B result = (B) context.getMappedObject(instanceA, bType);
        if (result == null) {
            if (aToB == null) {
                synchronized (this) {
                    if (aToB == null) {
                        aToB = mapperFacade.resolveMappingStrategy(instanceA, rawAType, rawBType, false, context);
                    }
                }
            }
            result = (B) aToB.map(instanceA, null, context);
        }
        return result;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see ma.glasnost.orika.DedicatedMapperFacade#mapBtoA(java.lang.Object,
     * ma.glasnost.orika.MappingContext)
     */
    @SuppressWarnings("unchecked")
    public A mapBtoA(B instanceB, MappingContext context) {
        A result = (A) context.getMappedObject(instanceB, aType);
        if (result == null) {
            if (bToA == null) {
                synchronized (this) {
                    if (bToA == null) {
                        bToA = mapperFacade.resolveMappingStrategy(instanceB, rawBType, rawAType, false, context);
                    }
                }
            }
            result = (A) bToA.map(instanceB, null, context);
        }
        return result;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see ma.glasnost.orika.DedicatedMapperFacade#mapAtoB(java.lang.Object,
     * java.lang.Object, ma.glasnost.orika.MappingContext)
     */
    public void mapAtoB(A instanceA, B instanceB, MappingContext context) {
        if (context.getMappedObject(instanceA, bType) == null) {
            if (aToBInPlace == null) {
                synchronized (this) {
                    if (aToBInPlace == null) {
                        aToBInPlace = mapperFacade.resolveMappingStrategy(instanceA, rawAType, rawBType, true, context);
                    }
                }
            }
            aToBInPlace.map(instanceA, instanceB, context);
        }
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see ma.glasnost.orika.DedicatedMapperFacade#mapBtoA(java.lang.Object,
     * java.lang.Object, ma.glasnost.orika.MappingContext)
     */
    public void mapBtoA(B instanceB, A instanceA, MappingContext context) {
        if (context.getMappedObject(instanceB, aType) == null) {
            if (bToAInPlace == null) {
                synchronized (this) {
                    if (bToAInPlace == null) {
                        bToAInPlace = mapperFacade.resolveMappingStrategy(instanceB, rawBType, rawAType, false, context);
                    }
                }
            }
            bToAInPlace.map(instanceB, instanceA, context);
        }
    }
    
    public String toString() {
        return getClass().getSimpleName() + "(" + aType +", " + bType + ")";
    }
}
