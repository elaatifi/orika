package ma.glasnost.orika.metadata;

public class ConverterKey {

	private final Class<?> sourceClass;
	private final Class<?> destinationClass;

	public ConverterKey(Class<?> sourceClass, Class<?> destinationClass) {
		this.sourceClass = sourceClass;
		this.destinationClass = destinationClass;
	}

	public Class<?> getSourceClass() {
		return sourceClass;
	}

	public Class<?> getDestinationClass() {
		return destinationClass;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((destinationClass == null) ? 0 : destinationClass.hashCode());
		result = prime * result + ((sourceClass == null) ? 0 : sourceClass.hashCode());
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
		if (destinationClass == null) {
			if (other.destinationClass != null)
				return false;
		} else if (!destinationClass.equals(other.destinationClass))
			return false;
		if (sourceClass == null) {
			if (other.sourceClass != null)
				return false;
		} else if (!sourceClass.equals(other.sourceClass))
			return false;
		return true;
	}

}
