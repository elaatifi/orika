package ma.glasnost.orika.impl;

import ma.glasnost.orika.ObjectFactory;

public abstract class AbstractObjectFactory<T> implements ObjectFactory<T> {

	private final Class<T> targetClass;

	public AbstractObjectFactory(Class<T> targetClass) {
		this.targetClass = targetClass;
	}

	public Class<T> getTargetClass() {
		return targetClass;
	}
}
