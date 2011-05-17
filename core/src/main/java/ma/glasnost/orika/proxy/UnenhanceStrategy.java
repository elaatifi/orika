package ma.glasnost.orika.proxy;

public interface UnenhanceStrategy {

	public <T> T unenhanceObject(T object);

	public <T> Class<T> unenhanceClass(T object);
}
