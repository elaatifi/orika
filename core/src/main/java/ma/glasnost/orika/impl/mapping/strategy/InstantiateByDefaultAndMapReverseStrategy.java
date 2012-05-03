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

import ma.glasnost.orika.Mapper;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.unenhance.UnenhanceStrategy;

public class InstantiateByDefaultAndMapReverseStrategy implements MappingStrategy {
    
    private final Mapper<Object, Object> customMapper;
    private final Type<Object> sourceType;
    private final Type<Object> destinationType;
    private final UnenhanceStrategy unenhancer;
    
    public InstantiateByDefaultAndMapReverseStrategy(Type<Object> sourceType, Type<Object> destinationType, Mapper<Object,Object> customMapper, UnenhanceStrategy unenhancer) {
        this.sourceType = sourceType;
        this.destinationType = destinationType;
        this.customMapper = customMapper;
        this.unenhancer = unenhancer;
    }

    public Object map(Object sourceObject, Object destinationObject, MappingContext context) {
        
        Object newInstance;
        try {
            newInstance = destinationType.getRawType().newInstance();
            
            sourceObject = unenhancer.unenhanceObject(sourceObject, sourceType);
            
            customMapper.mapBtoA(sourceObject, newInstance, context);
            
            context.cacheMappedObject(sourceObject, destinationType, newInstance);
            
            return newInstance;
        
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    
}
