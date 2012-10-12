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

package ma.glasnost.orika;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import ma.glasnost.orika.impl.mapping.strategy.MappingStrategy;
import ma.glasnost.orika.metadata.ClassMap;
import ma.glasnost.orika.metadata.MapperKey;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.metadata.TypeFactory;

public class MappingContext {
	
	private final Map<Type<?>, Type<?>> mapping;
	private final Map<java.lang.reflect.Type, Map<Object, Object>> cache;
	private List<Map<MapperKey, ClassMap<?,?>>> mappersSeen;
	private MappingStrategy strategy;
	private boolean isNew = true;
	private int depth;
	
	public static class Factory implements MappingContextFactory {

	    LinkedBlockingQueue<MappingContext> contextQueue = new LinkedBlockingQueue<MappingContext>();
	    
        public MappingContext getContext() {
            MappingContext context = contextQueue.poll();
            return context != null ? context : new MappingContext();
        }
        
        public void release(MappingContext context) {
            context.reset();
            contextQueue.offer(context);
        }
	}
	

	private MappingContext() {
		mapping = new HashMap<Type<?>, Type<?>>();
		cache = new HashMap<java.lang.reflect.Type, Map<Object, Object>>();
	}
	
	public void setResolvedMappingStrategy(MappingStrategy strategy) {
	    this.strategy = strategy;
	}
	
	public MappingStrategy getResolvedMappingStrategy() {
	    return this.strategy;
	}
	
	@SuppressWarnings("unchecked")
	public <S, D> Type<? extends D> getConcreteClass(Type<S> sourceType,
			Type<D> destinationType) {
	    if (isNew) {
	        return null;
	    }
		final Type<?> type = mapping.get(sourceType);
		if (type != null && destinationType.isAssignableFrom(type)) {
			return (Type<? extends D>) type;
		}
		return null;
	}

	public void registerConcreteClass(Type<?> subjectClass,
			Type<?> concreteClass) {
		mapping.put(subjectClass, concreteClass);
		isNew = false;
	}

	@Deprecated
	public <S, D> void cacheMappedObject(S source, D destination) {
		cacheMappedObject(source, TypeFactory.typeOf(destination), destination);
	}

	public <S, D> void cacheMappedObject(S source, java.lang.reflect.Type destinationType,
			D destination) {
		Map<Object, Object> localCache = cache.get(destinationType);
		if (localCache == null) {
			localCache = new IdentityHashMap<Object, Object>(2);
			cache.put(destinationType, localCache);
		}
		localCache.put(source, destination);
		isNew = false;
	}

	/**
	 * @param source
	 * @param destinationType
	 * @return
	 * @deprecated use {@link #getMappedObject(Object, Type)} instead
	 */
	@Deprecated
	public <S, D> boolean isAlreadyMapped(S source, java.lang.reflect.Type destinationType) {
		if (isNew) {
		    return false;
		}
		Map<Object, Object> localCache = cache.get(destinationType);
		return (localCache != null && localCache.get(source) != null);
	}

	@SuppressWarnings("unchecked")
	public <D> D getMappedObject(Object source, java.lang.reflect.Type destinationType) {
		
	    if (isNew) {
	        return null;
	    }
	    Map<Object, Object> localCache = cache.get(destinationType);
		return (D) (localCache == null ? null : localCache.get(source));
	}

	/**
	 * Registers a ClassMap marking it as mapped within the current context;
	 * 
	 * @param classMap
	 */
	public void registerMapperGeneration(ClassMap<?,?> classMap) {
	    if (mappersSeen == null) {
	        mappersSeen = new ArrayList<Map<MapperKey, ClassMap<?,?>>>();
	    }
	    Map<MapperKey, ClassMap<?,?>> list = mappersSeen.isEmpty() ? null : this.mappersSeen.get(depth-1);
	    if (list == null) {
	        list = new HashMap<MapperKey, ClassMap<?,?>>();
	    }
	    list.put(classMap.getMapperKey(), classMap);
	}
	
	public ClassMap<?,?> getMapperGeneration(MapperKey mapperKey) {
	    ClassMap<?,?> result = null;
	    Map<MapperKey, ClassMap<?,?>> map = (mappersSeen == null || mappersSeen.isEmpty()) ? null : this.mappersSeen.get(depth-1);
	    if (map != null) {
	        result = map.get(mapperKey);
	    }
	    return result;
	}
	
	public void beginMapping() {
	    ++depth;
	}
	
	public void endMapping() {
	    --depth;
	}
	
    public void reset() {
        cache.clear();
        mapping.clear();
        if (mappersSeen != null) {
            mappersSeen.clear();
        }
        strategy = null;
        isNew = true;
        depth = 0;
    }
    
    public static final class NonCyclicMappingContext extends MappingContext {
        
        public <S, D> void cacheMappedObject(S source, D destination) {
            // No-op
        }
        
        public <S, D> void cacheMappedObject(S source, java.lang.reflect.Type destinationType, D destination) {
            // No-op
        }
        
        public <S, D> boolean isAlreadyMapped(S source, java.lang.reflect.Type destinationType) {
            return false;
        }
        
        public <D> D getMappedObject(Object source, java.lang.reflect.Type destinationType) {
            return null;
        }
    }
	
}