package ma.glasnost.orika.impl.converter;

import ma.glasnost.orika.Converter;

abstract class AbstractConverter<S, D> implements Converter<S, D> {

	private final Class<S> sourceClass;

	private final Class<D> destinationClass;

	public AbstractConverter(Class<S> sourceClass, Class<D> destinationClass) {
		this.sourceClass = sourceClass;
		this.destinationClass = destinationClass;
	}

	public Class<S> getSourceClass() {
		return sourceClass;
	}

	public Class<D> getDestinationClass() {
		return destinationClass;
	}

}
