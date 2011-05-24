package ma.glasnost.orika.impl;

import java.util.HashMap;
import java.util.Map;

public class MappingContext {

	private final Map<Class<?>, Class<?>> mapping;
	private final Map<Object, Object> cache;

	public MappingContext() {
		mapping = new HashMap<Class<?>, Class<?>>();
		cache = new HashMap<Object, Object>();
	}

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
