package ma.glasnost.orika;

import java.lang.reflect.ParameterizedType;

public abstract class ConverterBase<S, D> implements Converter<S, D> {
    
    private final Class<S> sourceClass;
    
    private final Class<D> destinationClass;
    
    @SuppressWarnings("unchecked")
    public ConverterBase() {
        sourceClass = (Class<S>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        destinationClass = (Class<D>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[1];
        
    }
    
    public Class<D> getDestination() {
        return destinationClass;
    }
    
    public Class<S> getSource() {
        return sourceClass;
    }
    
}
