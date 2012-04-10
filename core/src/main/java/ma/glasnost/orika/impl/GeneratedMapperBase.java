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

import java.util.ArrayList;
import java.util.List;

import ma.glasnost.orika.Mapper;
import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.metadata.Type;

public abstract class GeneratedMapperBase extends CustomMapper<Object, Object> {

    protected Mapper<Object, Object> customMapper;
    protected Type<Object>[] usedTypes;
    private Mapper<Object, Object>[] usedMappers;
    private Type<Object> aType;
    private Type<Object> bType;
    
    public Type<Object> getAType() {
    	return aType;
    }
    
    public Type<Object> getBType() {
    	return bType;
    }
    
    @SuppressWarnings("unchecked")
	public void setAType(Type<?> aType) {
    	this.aType = (Type<Object>)aType;
    }
    
    @SuppressWarnings("unchecked")
	public void setBType(Type<?> bType) {
    	this.bType = (Type<Object>)bType;
    }
    
    public void setCustomMapper(Mapper<Object, Object> customMapper) {
        this.customMapper = customMapper;
        this.customMapper.setMapperFacade(mapperFacade);
    }

    protected Mapper<Object, Object>[] getUsedMappers() {
        return usedMappers;
    }

    @Override
    public void setUsedMappers(Mapper<Object, Object>[] usedMappers) {
        this.usedMappers = usedMappers;
    }

    public void setUsedTypes(Type<Object>[] types) {
        this.usedTypes = types;
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


    protected static <T> List<T> asList(Iterable<T> iterable) {
        ArrayList<T> ts = new ArrayList<T>();
        for (T i : iterable) {
            ts.add(i);
        }
        return ts;
    }

    protected static List<Object> asList(Object[] iterable) {
        ArrayList<Object> ts = new ArrayList<Object>();
        for (Object i : iterable) {
            ts.add(i);
        }
        return ts;
    }
    
    protected static List<Object> asList(byte[] iterable) {
        ArrayList<Object> ts = new ArrayList<Object>();
        for (Object i : iterable) {
            ts.add(i);
        }
        return ts;
    }

    protected static List<Object> asList(int[] iterable) {
        ArrayList<Object> ts = new ArrayList<Object>();
        for (Object i : iterable) {
            ts.add(i);
        }
        return ts;
    }

    protected static List<Object> asList(char[] iterable) {
        ArrayList<Object> ts = new ArrayList<Object>();
        for (Object i : iterable) {
            ts.add(i);
        }
        return ts;
    }

    protected static List<Object> asList(long[] iterable) {
        ArrayList<Object> ts = new ArrayList<Object>();
        for (Object i : iterable) {
            ts.add(i);
        }
        return ts;
    }

    protected static List<Object> asList(float[] iterable) {
        ArrayList<Object> ts = new ArrayList<Object>();
        for (Object i : iterable) {
            ts.add(i);
        }
        return ts;
    }

    protected static List<Object> asList(double[] iterable) {
        ArrayList<Object> ts = new ArrayList<Object>();
        for (Object i : iterable) {
            ts.add(i);
        }
        return ts;
    }

    protected static List<Object> asList(boolean[] iterable) {
        ArrayList<Object> ts = new ArrayList<Object>();
        for (Object i : iterable) {
            ts.add(i);
        }
        return ts;
    }

    protected static List<Object> asList(short[] iterable) {
        ArrayList<Object> ts = new ArrayList<Object>();
        for (Object i : iterable) {
            ts.add(i);
        }
        return ts;
    }

    protected void mapArray(byte[] destination, List<Object> source, Class<?> clazz, MappingContext mappingContext) {
        if (source == null) {
            return;
        }

        int i = 0;
        for (final Object s : source) {
            destination[i++] = (Byte) mapperFacade.map(s, clazz, mappingContext);
        }

    }
    protected void mapArray(boolean[] destination, List<Object> source, Class<?> clazz, MappingContext mappingContext) {
        if (source == null) {
            return;
        }

        int i = 0;
        for (final Object s : source) {
            destination[i++] = (Boolean) mapperFacade.map(s, clazz, mappingContext);
        }

    }
    protected void mapArray(char[] destination, List<Object> source, Class<?> clazz, MappingContext mappingContext) {
        if (source == null) {
            return;
        }

        int i = 0;
        for (final Object s : source) {
            destination[i++] = (Character) mapperFacade.map(s, clazz, mappingContext);
        }

    }
    protected void mapArray(short[] destination, List<Object> source, Class<?> clazz, MappingContext mappingContext) {
        if (source == null) {
            return;
        }

        int i = 0;
        for (final Object s : source) {
            destination[i++] = (Short) mapperFacade.map(s, clazz, mappingContext);
        }

    }
    protected void mapArray(int[] destination, List<Object> source, Class<?> clazz, MappingContext mappingContext) {
        if (source == null) {
            return;
        }

        int i = 0;
        for (final Object s : source) {
            destination[i++] = (Integer) mapperFacade.map(s, clazz, mappingContext);
        }

    }
    protected void mapArray(long[] destination, List<Object> source, Class<?> clazz, MappingContext mappingContext) {
        if (source == null) {
            return;
        }

        int i = 0;
        for (final Object s : source) {
            destination[i++] = (Long) mapperFacade.map(s, clazz, mappingContext);
        }

    }
    protected void mapArray(float[] destination, List<Object> source, Class<?> clazz, MappingContext mappingContext) {
        if (source == null) {
            return;
        }

        int i = 0;
        for (final Object s : source) {
            destination[i++] = (Float) mapperFacade.map(s, clazz, mappingContext);
        }

    }
    protected void mapArray(double[] destination, List<Object> source, Class<?> clazz, MappingContext mappingContext) {
        if (source == null) {
            return;
        }

        int i = 0;
        for (final Object s : source) {
            destination[i++] = (Double) mapperFacade.map(s, clazz, mappingContext);
        }

    }
    
    
}