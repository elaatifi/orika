package ma.glasnost.orika.impl.mapping.strategy;

import ma.glasnost.orika.Mapper;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.ObjectFactory;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.unenhance.UnenhanceStrategy;

public class InstantiateAndMapReverseStrategy implements MappingStrategy {
    
    private final ObjectFactory<Object> objectFactory;
    private final Mapper<Object, Object> customMapper;
    private final Type<Object> sourceType;
    private final Type<Object> destinationType;
    private final UnenhanceStrategy unenhancer;
    
    public InstantiateAndMapReverseStrategy(Type<Object> sourceType, Type<Object> destinationType, Mapper<Object,Object> customMapper, ObjectFactory<Object> objectFactory, UnenhanceStrategy unenhancer) {
        this.sourceType = sourceType;
        this.destinationType = destinationType;
        this.objectFactory = objectFactory;
        this.customMapper = customMapper;
        this.unenhancer = unenhancer;
    }

    public Object map(Object sourceObject, Object destinationObject, MappingContext context) {
        
        sourceObject = unenhancer.unenhanceObject(sourceObject, sourceType);
        
        Object newInstance = objectFactory.create(sourceObject, context);
        customMapper.mapBtoA(sourceObject, newInstance, context);
        
        context.cacheMappedObject(sourceObject, destinationType, newInstance);
        
        return newInstance;
    }
    
}
