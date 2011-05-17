package ma.glasnost.orika;

public interface Converter<S, D> {

	D convert(S source) throws ConverterException;

	Class<S> getSource();

	Class<D> getDestination();
}
