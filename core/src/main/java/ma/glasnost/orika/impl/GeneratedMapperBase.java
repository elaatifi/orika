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

import ma.glasnost.orika.Mapper;
import ma.glasnost.orika.MapperBase;
import ma.glasnost.orika.MappingContext;

public abstract class GeneratedMapperBase extends MapperBase<Object, Object> {
    
    protected Mapper<Object, Object> customMapper;
    
    private Mapper<Object, Object>[] usedMappers;
    
    public void setCustomMapper(Mapper<Object, Object> customMapper) {
        this.customMapper = customMapper;
        this.customMapper.setMapperFacade(mapperFacade);
    }
    
    protected Mapper<Object, Object>[] getUsedMappers() {
        return usedMappers;
    }
    
    public void setUsedMappers(Mapper<Object, Object>[] usedMappers) {
        this.usedMappers = usedMappers;
    }
    
    @Override
    public void mapAtoB(Object a, Object b, MappingContext context) {
        if (usedMappers == null) {
            return;
        }
        for (Mapper<Object, Object> mapper : usedMappers) {
            mapper.mapAtoB(a, b, context);
        }
    }
    
    @Override
    public void mapBtoA(Object b, Object a, MappingContext context) {
        if (usedMappers == null) {
            return;
        }
        for (Mapper<Object, Object> mapper : usedMappers) {
            mapper.mapBtoA(a, b, context);
        }
    }
    
}