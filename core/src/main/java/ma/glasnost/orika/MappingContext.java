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

import java.util.HashMap;
import java.util.Map;

public class MappingContext {

	private final Map<Class<?>, Class<?>> mapping;
	private final Map<Object, Object> cache;

	public MappingContext() {
		mapping = new HashMap<Class<?>, Class<?>>();
		cache = new HashMap<Object, Object>();
	}

	@SuppressWarnings("unchecked")
	public <S, D> Class<? extends D> getConcreteClass(Class<S> sourceClass, Class<D> destinationClass) {

		Class<?> clazz = mapping.get(sourceClass);
		if (clazz != null && destinationClass.isAssignableFrom(clazz)) {
			return (Class<? extends D>) clazz;
		}
		return null;
	}

	public <S, D> void cacheMappedObject(S source, D destination) {
		cache.put(source, destination);
	}

	public <S> boolean isAlreadyMapped(S source) {
		return cache.containsKey(source);
	}

	public Object getMappedObject(Object source) {
		return cache.get(source);
	}
}
