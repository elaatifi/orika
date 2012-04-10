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

package ma.glasnost.orika.metadata;

import java.lang.reflect.Type;

public class ConverterKey {
    
    private final Type sourceClass;
    private final Type destinationClass;
    private final String id;
    
    public ConverterKey(Type sourceClass, Type destinationClass) {
        
        assert sourceClass != null;
        assert destinationClass != null;
        
        this.sourceClass = sourceClass;
        this.destinationClass = destinationClass;
        this.id = null;
    }
    
    public ConverterKey(String id) {
        assert id != null;
        
        this.id = id;
        
        this.sourceClass = null;
        this.destinationClass = null;
    }
    
    public Type getSourceClass() {
        return sourceClass;
    }
    
    public Type getDestinationClass() {
        return destinationClass;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        
        if (id == null) {
            result = prime * result + destinationClass.hashCode();
            result = prime * result + sourceClass.hashCode();
        } else {
            result = prime * result + id.hashCode();
        }
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
        ConverterKey other = (ConverterKey) obj;
        
        if (id != null) {
            return id.equals(other.id);
        } else {
            return destinationClass.equals(other.destinationClass) && sourceClass.equals(other.sourceClass);
        }
    }
    
}
