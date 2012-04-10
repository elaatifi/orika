package ma.glasnost.orika.impl;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.ObjectFactory;
import ma.glasnost.orika.metadata.Type;

public abstract class GeneratedObjectFactory implements ObjectFactory<Object> {
    protected MapperFacade mapperFacade;
    protected Type<Object>[] usedTypes;
    
    public void setMapperFacade(MapperFacade mapperFacade) {
        this.mapperFacade = mapperFacade;
    }
    
    public void setUsedTypes(Type<Object>[] usedTypes) {
        this.usedTypes = usedTypes;
    }
    
}
