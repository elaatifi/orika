package ma.glasnost.orika;

import java.lang.reflect.ParameterizedType;

import ma.glasnost.orika.metadata.Property;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.metadata.TypeFactory;

/**
 * @author mattdeboer
 * 
 * @param <A>
 * @param <B>
 */
public abstract class CustomFilter<A, B> implements Filter<A, B> {
    
    private final Type<A> sourceType;
    private final Type<B> destinationType;
    
    /**
     * 
     */
    public CustomFilter() {
        java.lang.reflect.Type genericSuperclass = getClass().getGenericSuperclass();
        if (genericSuperclass != null && genericSuperclass instanceof ParameterizedType) {
            ParameterizedType superType = (ParameterizedType) genericSuperclass;
            sourceType = TypeFactory.valueOf(superType.getActualTypeArguments()[0]);
            destinationType = TypeFactory.valueOf(superType.getActualTypeArguments()[1]);
        } else {
            throw new IllegalStateException("When you subclass the ConverterBase S and D type-parameters are required.");
        }
    }
    
    public Type<A> getAType() {
        return sourceType;
    }
    
    public Type<B> getBType() {
        return destinationType;
    }
    
    public boolean appliesTo(Property source, Property destination) {
        return sourceType.isAssignableFrom(source.getType()) && destinationType.isAssignableFrom(destination.getType());
    }
    
}
