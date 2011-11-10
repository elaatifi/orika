package ma.glasnost.orika.impl;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.ObjectFactory;

public abstract class GeneratedObjectFactory implements ObjectFactory<Object> {
    protected MapperFacade mapperFacade;
    
    public void setMapperFacade(MapperFacade mapperFacade) {
        this.mapperFacade = mapperFacade;
    }
    
}
