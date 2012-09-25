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

import ma.glasnost.orika.BoundMapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.MappingContextFactory;
import ma.glasnost.orika.ObjectFactory;
import ma.glasnost.orika.impl.mapping.strategy.MappingStrategy;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.metadata.TypeFactory;

/**
 * DefaultBoundMapperFacade is the base implementation of BoundMapperFacade
 * 
 * @author matt.deboer@gmail.com
 * 
 */
class DefaultBoundMapperFacade<A, B> implements BoundMapperFacade<A, B> {
    
    protected volatile MappingStrategy aToB;
    protected volatile MappingStrategy bToA;
    protected volatile MappingStrategy aToBInPlace;
    protected volatile MappingStrategy bToAInPlace;
    protected volatile ObjectFactory<A> objectFactoryA;
    protected volatile ObjectFactory<B> objectFactoryB;
    
    protected final java.lang.reflect.Type rawAType;
    protected final java.lang.reflect.Type rawBType;
    protected final Type<A> aType;
    protected final Type<B> bType;
    protected final MapperFactory mapperFactory;
    protected final MappingContextFactory contextFactory;
    
    /**
     * Constructs a new instance of DefaultBoundMapperFacade
     * 
     * @param mapperFactory
     * @param contextFactory
     * @param typeOfA
     * @param typeOfB
     */
    DefaultBoundMapperFacade(MapperFactory mapperFactory, MappingContextFactory contextFactory,  java.lang.reflect.Type typeOfA, java.lang.reflect.Type typeOfB) {
        this.mapperFactory = mapperFactory;
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
    
    public B map(A instanceA) {
        MappingContext context = contextFactory.getContext();
        try {
            return map(instanceA, context);
        } finally {
            contextFactory.release(context);
        }
    }
    
    public A mapReverse(B source) {
        MappingContext context = contextFactory.getContext();
        try {
            return mapReverse(source, context);
        } finally {
            contextFactory.release(context);
        }
    }
    
    public void map(A instanceA, B instanceB) {
        MappingContext context = contextFactory.getContext();
        try {
            map(instanceA, instanceB, context);
        } finally {
            contextFactory.release(context);
        }
    }
    
    public void mapReverse(B instanceB, A instanceA) {
        MappingContext context = contextFactory.getContext();
        try {
            mapReverse(instanceB, instanceA, context);
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
    public B map(A instanceA, MappingContext context) {
        B result = (B) context.getMappedObject(instanceA, bType);
        if (result == null) {
            if (aToB == null) {
                synchronized (this) {
                    if (aToB == null) {
                        aToB = mapperFactory.getMapperFacade().resolveMappingStrategy(instanceA, rawAType, rawBType, false, context);
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
    public A mapReverse(B instanceB, MappingContext context) {
        A result = (A) context.getMappedObject(instanceB, aType);
        if (result == null) {
            if (bToA == null) {
                synchronized (this) {
                    if (bToA == null) {
                        bToA = mapperFactory.getMapperFacade().resolveMappingStrategy(instanceB, rawBType, rawAType, false, context);
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
    public void map(A instanceA, B instanceB, MappingContext context) {
        if (context.getMappedObject(instanceA, bType) == null) {
            if (aToBInPlace == null) {
                synchronized (this) {
                    if (aToBInPlace == null) {
                        aToBInPlace = mapperFactory.getMapperFacade().resolveMappingStrategy(instanceA, rawAType, rawBType, true, context);
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
    public void mapReverse(B instanceB, A instanceA, MappingContext context) {
        if (context.getMappedObject(instanceB, aType) == null) {
            if (bToAInPlace == null) {
                synchronized (this) {
                    if (bToAInPlace == null) {
                        bToAInPlace = mapperFactory.getMapperFacade().resolveMappingStrategy(instanceB, rawBType, rawAType, true, context);
                    }
                }
            }
            bToAInPlace.map(instanceB, instanceA, context);
        }
    }
    
    public String toString() {
        return getClass().getSimpleName() + "(" + aType +", " + bType + ")";
    }

    /* (non-Javadoc)
     * @see ma.glasnost.orika.DedicatedMapperFacade#newObjectB(java.lang.Object, ma.glasnost.orika.MappingContext)
     */
    public B newObject(A source, MappingContext context) {
        if (objectFactoryB == null) {
            synchronized(this) {
                if (objectFactoryB == null) {
                    objectFactoryB = mapperFactory.lookupObjectFactory(bType);
                }
            }
        }
        return objectFactoryB.create(source, context);
    }

    /* (non-Javadoc)
     * @see ma.glasnost.orika.DedicatedMapperFacade#newObjectA(java.lang.Object, ma.glasnost.orika.MappingContext)
     */
    public A newObjectReverse(B source, MappingContext context) {
        if (objectFactoryA == null) {
            synchronized(this) {
                if (objectFactoryA == null) {
                    objectFactoryA = mapperFactory.lookupObjectFactory(aType);
                }
            }
        }
        return objectFactoryA.create(source, context);
    }
}
