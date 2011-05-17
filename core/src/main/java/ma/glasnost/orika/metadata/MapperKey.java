package ma.glasnost.orika.metadata;

public class MapperKey {

	private Class<?> aType;
	private Class<?> bType;

	public MapperKey(Class<?> aClass, Class<?> bClass) {
		this.aType = aClass;
		this.bType = bClass;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		MapperKey mapperKey = (MapperKey) o;

		return (equals(aType, mapperKey.aType) && equals(bType, mapperKey.bType))
				|| (equals(aType, mapperKey.bType) || equals(bType, mapperKey.aType));

	}

	private boolean equals(Class<?> a, Class<?> b) {
		return (a == null ? b == null : a.equals(b));
	}

	@Override
	public int hashCode() {
		int result = aType != null ? aType.hashCode() : 0;
		result = result + (bType != null ? bType.hashCode() : 0);
		return result;
	}

	public Class<?> getAType() {
		return aType;
	}

	public void setAType(Class<?> aType) {
		this.aType = aType;
	}

	public Class<?> getBType() {
		return bType;
	}

	public void setBType(Class<?> bType) {
		this.bType = bType;
	}
}