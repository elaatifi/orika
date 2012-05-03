package ma.glasnost.orika.impl.mapping.strategy;

import ma.glasnost.orika.Mapper;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.unenhance.UnenhanceStrategy;

public class InstantiateByDefaultAndMapForwardStrategy implements MappingStrategy {
    
    private final Mapper<Object, Object> customMapper;
    private final Type<Object> sourceType;
    private final Type<Object> destinationType;
    private final UnenhanceStrategy unenhancer;
    
    public InstantiateByDefaultAndMapForwardStrategy(Type<Object> sourceType, Type<Object> destinationType, Mapper<Object,Object> customMapper, UnenhanceStrategy unenhancer) {
        this.sourceType = sourceType;
        this.destinationType = destinationType;
        this.customMapper = customMapper;
        this.unenhancer = unenhancer;
    }

    public Object map(Object sourceObject, Object destinationObject, MappingContext context) {
        
        Object newInstance;
        try {
            newInstance = destinationType.getRawType().newInstance();
            
            sourceObject = unenhancer.unenhanceObject(sourceObject, sourceType);
            
            customMapper.mapAtoB(sourceObject, newInstance, context);
            
            context.cacheMappedObject(sourceObject, destinationType, newInstance);
            
            return newInstance;
        
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        
    }
    
}
