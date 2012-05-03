package ma.glasnost.orika.impl.mapping.strategy;

import ma.glasnost.orika.MappingContext;

public interface MappingStrategy {
    
    public Object map(Object sourceObject, Object destinationObject, MappingContext context);
 
}
