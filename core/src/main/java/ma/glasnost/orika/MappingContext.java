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

import java.util.IdentityHashMap;
import java.util.Map;

import javolution.util.FastMap;
import ma.glasnost.orika.impl.mapping.strategy.MappingStrategy;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.metadata.TypeFactory;

public class MappingContext {
	
	private final Map<Type<?>, Type<?>> mapping;
	private final Map<java.lang.reflect.Type, Map<Object, Object>> cache;
	private MappingStrategy strategy;

	public MappingContext() {
		mapping = new FastMap<Type<?>, Type<?>>();
		cache = new FastMap<java.lang.reflect.Type, Map<Object, Object>>();
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

		final Type<?> type = mapping.get(sourceType);
		if (type != null && destinationType.isAssignableFrom(type)) {
			return (Type<? extends D>) type;
		}
		return null;
	}

	public void registerConcreteClass(Type<?> subjectClass,
			Type<?> concreteClass) {
		mapping.put(subjectClass, concreteClass);
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
	}

	/**
	 * @param source
	 * @param destinationType
	 * @return
	 * @deprecated use {@link #getMappedObject(Object, Type)} instead
	 */
	@Deprecated
	public <S, D> boolean isAlreadyMapped(S source, java.lang.reflect.Type destinationType) {
		
		Map<Object, Object> localCache = cache.get(destinationType);
		return (localCache != null && localCache.get(source) != null);
	}

	@SuppressWarnings("unchecked")
	public <D> D getMappedObject(Object source, java.lang.reflect.Type destinationType) {
		Map<Object, Object> localCache = cache.get(destinationType);
		return (D) (localCache == null ? null : localCache.get(source));
	}
	
}