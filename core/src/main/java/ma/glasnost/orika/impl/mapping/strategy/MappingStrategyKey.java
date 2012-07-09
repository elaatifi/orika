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
    
    protected final Class<?> immutableRawSourceType;
    protected final Type<?> immutableSourceType;
    protected final Type<?> immutableDestinationType;
    protected final boolean immutableDestinationProvided;
    
    public MappingStrategyKey(Class<?> rawSourceType, Type<?> sourceType, Type<?> destinationType, boolean destinationProvided) {
        this.immutableRawSourceType = rawSourceType;
        this.immutableSourceType = sourceType;
        this.immutableDestinationType = destinationType;
        this.immutableDestinationProvided = destinationProvided;
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
    
    protected boolean isDestinationProvided() {
    	return immutableDestinationProvided;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (isDestinationProvided() ? 1231 : 1237);
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
        if (getClass() != obj.getClass())
            return false;
        MappingStrategyKey other = (MappingStrategyKey) obj;
        if (isDestinationProvided() != other.isDestinationProvided())
        	return false;
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
    
    public String toString() {
    	return "[" + getRawSourceType().getSimpleName() + ", " + getSourceType() + ", " + getDestinationType() +"]";
    }
}
