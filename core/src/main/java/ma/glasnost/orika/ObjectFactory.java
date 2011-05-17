package ma.glasnost.orika;

public interface ObjectFactory<T> {

	T create();

	Class<T> getTargetClass();
}
