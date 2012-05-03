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

package ma.glasnost.orika.impl.mapping.strategy;

import ma.glasnost.orika.Converter;
import ma.glasnost.orika.Mapper;
import ma.glasnost.orika.ObjectFactory;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.unenhance.UnenhanceStrategy;

/**
 * MappingStrategyRecorder is used to record the important details regarding
 * the branch logic and component lookups performed for a given set of input
 * types.<br>
 * After recording these details, it can be used to generate an appropriate
 * MappingStrategy instance which can be cached and reused for that particular
 * set of inputs.
 * 
 * @author matt.deboer@gmail.com
 *
 */
public class MappingStrategyRecorder {
    
    private boolean copyByReference;
    private boolean mapReverse;
    private boolean unenhance;
    
    private Mapper<Object, Object> resolvedMapper;
    private ObjectFactory<Object> resolvedObjectFactory;
    private Converter<Object, Object> resolvedConverter;
    private Type<Object> resolvedSourceType;
    private Type<Object> resolvedDestinationType;
    
    private UnenhanceStrategy unenhanceStrategy;
    
    public MappingStrategyRecorder(UnenhanceStrategy unenhanceStrategy) {
        this.unenhanceStrategy = unenhanceStrategy;
    }
    
    public boolean isUnenhance() {
        return unenhance;
    }

    public void setUnenhance(boolean unenhance) {
        this.unenhance = unenhance;
    }
    
    public Converter<Object, Object> getResolvedConverter() {
        return resolvedConverter;
    }

    public void setResolvedConverter(Converter<Object, Object> resolvedConverter) {
        this.resolvedConverter = resolvedConverter;
    }

    
    public Type<?> getResolvedSourceType() {
        return resolvedSourceType;
    }

    @SuppressWarnings("unchecked")
    public void setResolvedSourceType(Type<?> resolvedSourceType) {
        this.resolvedSourceType = (Type<Object>) resolvedSourceType;
    }

    public Type<?> getResolvedDestinationType() {
        return resolvedDestinationType;
    }

    @SuppressWarnings("unchecked")
    public void setResolvedDestinationType(Type<?> resolvedDestinationType) {
        this.resolvedDestinationType = (Type<Object>) resolvedDestinationType;
    }

    public boolean isCopyByReference() {
        return copyByReference;
    }

    public void setCopyByReference(boolean copyByReference) {
        this.copyByReference = copyByReference;
    }

    public boolean isMapReverse() {
        return mapReverse;
    }

    public void setMapReverse(boolean mapReverse) {
        this.mapReverse = mapReverse;
    }

    public Mapper<Object, Object> getResolvedMapper() {
        return resolvedMapper;
    }

    public void setResolvedMapper(Mapper<Object, Object> resolvedMapper) {
        this.resolvedMapper = resolvedMapper;
    }

    public ObjectFactory<Object> getResolvedObjectFactory() {
        return resolvedObjectFactory;
    }

    @SuppressWarnings("unchecked")
    public void setResolvedObjectFactory(ObjectFactory<?> resolvedObjectFactory) {
        this.resolvedObjectFactory = (ObjectFactory<Object>) resolvedObjectFactory;
    }

    public MappingStrategy playback() {
        
        UnenhanceStrategy unenhanceStrategy; 
        if (unenhance) {
            unenhanceStrategy = this.unenhanceStrategy;
        } else {
            unenhanceStrategy = DefaultUnenhancer.getInstance();
        }
        
        if (copyByReference) {
            return CopyByReferenceStrategy.getInstance();
        } else if (resolvedConverter != null) {
            return new UseConverterStrategy(resolvedSourceType, resolvedDestinationType, resolvedConverter, unenhanceStrategy);
        } else if (resolvedObjectFactory != null) {
            if (mapReverse) {
                return new InstantiateAndMapReverseStrategy(resolvedSourceType, resolvedDestinationType, resolvedMapper, resolvedObjectFactory, unenhanceStrategy);
            } else {
                return new InstantiateAndMapForwardStrategy(resolvedSourceType, resolvedDestinationType, resolvedMapper, resolvedObjectFactory, unenhanceStrategy);
            }
        } else {
            if (mapReverse) {
                return new InstantiateByDefaultAndMapReverseStrategy(resolvedSourceType, resolvedDestinationType, resolvedMapper, unenhanceStrategy);
            } else {
                return new InstantiateByDefaultAndMapForwardStrategy(resolvedSourceType, resolvedDestinationType, resolvedMapper, unenhanceStrategy);
            }
        }
    }
    
    static class DefaultUnenhancer implements UnenhanceStrategy {

        private static final DefaultUnenhancer INSTANCE = new DefaultUnenhancer(); 
        
        public static UnenhanceStrategy getInstance() {
            return INSTANCE;
        }

        public <T> Type<T> unenhanceType(T object, Type<T> type) {
            throw new UnsupportedOperationException();
        }

        public <T> T unenhanceObject(T object, Type<T> type) {
            return object;
        }
    }
}
