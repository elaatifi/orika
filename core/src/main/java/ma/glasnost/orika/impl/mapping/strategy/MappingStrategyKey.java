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

import ma.glasnost.orika.metadata.Type;

/**
 * MappingStrategyKey defines the minimum information necessary to cache a
 * particular mapping strategy
 * 
 * @author matt.deboer@gmail.com
 *
 */
public class MappingStrategyKey {
    
    private static final ThreadLocal<Object> strategyKey = new ThreadLocal<Object>() {
        protected Object initialValue() {
            return new MappingStrategyKey();
        }
    };
    
    public static MappingStrategyKey getCurrent() {
        return (MappingStrategyKey)strategyKey.get();
    }
    
    private Class<?> rawSourceType;
    private Type<?> sourceType;
    private Type<?> destinationType;
    
    private MappingStrategyKey() {
        
    }
    
    protected Class<?> getRawSourceType() {
        return rawSourceType;
    }

    protected Type<?> getSourceType() {
        return sourceType;
    }

    protected Type<?> getDestinationType() {
        return destinationType;
    }

    public void initialize(Class<?> rawSourceType, Type<?> sourceType, Type<?> destinationType) {
        this.rawSourceType = rawSourceType;
        this.sourceType = sourceType;
        this.destinationType = destinationType;
    }
    
    public void clear() {
        this.rawSourceType = null;
        this.sourceType = null;
        this.destinationType = null;
    }
    
    public MappingStrategyKey toImmutableCopy() {
        MappingStrategyKey copy = new ImmutableMappingStrategyKey(this.rawSourceType, this.sourceType, this.destinationType);
        clear();
        return copy;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getDestinationType() == null) ? 0 : getDestinationType().hashCode());
        result = prime * result + ((getRawSourceType() == null) ? 0 : getRawSourceType().hashCode());
        result = prime * result + ((getSourceType() == null) ? 0 : getSourceType().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass() && ImmutableMappingStrategyKey.class != obj.getClass())
            return false;
        MappingStrategyKey other = (MappingStrategyKey) obj;
        if (getDestinationType() == null) {
            if (other.getDestinationType() != null)
                return false;
        } else if (!getDestinationType().equals(other.getDestinationType()))
            return false;
        if (getRawSourceType() == null) {
            if (other.getRawSourceType() != null)
                return false;
        } else if (!getRawSourceType().equals(other.getRawSourceType()))
            return false;
        if (getSourceType() == null) {
            if (other.getSourceType() != null)
                return false;
        } else if (!getSourceType().equals(other.getSourceType()))
            return false;
        return true;
    }
    
    public static class ImmutableMappingStrategyKey extends MappingStrategyKey {
        
        protected final Class<?> immutableRawSourceType;
        protected final Type<?> immutableSourceType;
        protected final Type<?> immutableDestinationType;
        
        public ImmutableMappingStrategyKey(Class<?> rawSourceType, Type<?> sourceType, Type<?> destinationType) {
            this.immutableRawSourceType = rawSourceType;
            this.immutableSourceType = sourceType;
            this.immutableDestinationType = destinationType;
        }
        
        protected Class<?> getRawSourceType() {
            return immutableRawSourceType;
        }

        protected Type<?> getSourceType() {
            return immutableSourceType;
        }

        protected Type<?> getDestinationType() {
            return immutableDestinationType;
        }
    }
    
}
