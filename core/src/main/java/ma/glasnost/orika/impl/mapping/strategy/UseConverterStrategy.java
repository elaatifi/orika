package ma.glasnost.orika.impl.mapping.strategy;

import ma.glasnost.orika.Converter;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.unenhance.UnenhanceStrategy;

public class UseConverterStrategy implements MappingStrategy {
    
    private final Converter<Object,Object> converter;
    private final Type<Object> sourceType;
    private final Type<?> destinationType;
    private final UnenhanceStrategy unenhancer;
    
    @SuppressWarnings("unchecked")
    public UseConverterStrategy(Type<?> sourceType, Type<?> destinationType, Converter<Object,Object> converter, UnenhanceStrategy unenhancer) {
        this.sourceType = (Type<Object>) sourceType;
        this.destinationType = destinationType;
        this.converter = converter;
        this.unenhancer = unenhancer;
    }

    public Object map(Object sourceObject, Object destinationObject, MappingContext context) {
        return converter.convert(unenhancer.unenhanceObject(sourceObject, sourceType), destinationType);
    }
}
