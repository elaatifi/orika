package ma.glasnost.orika.impl;

import ma.glasnost.orika.Converter;
import ma.glasnost.orika.BoundMapperFacade;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.ObjectFactory;
import ma.glasnost.orika.metadata.Type;

public abstract class GeneratedObjectFactory implements ObjectFactory<Object> {
    protected MapperFacade mapperFacade;
    protected Type<Object>[] usedTypes;
    protected Converter<Object,Object>[] usedConverters;
    protected BoundMapperFacade<Object, Object>[] usedMapperFacades;
    
    public void setMapperFacade(MapperFacade mapperFacade) {
        this.mapperFacade = mapperFacade;
    }
    
    public void setUsedTypes(Type<Object>[] usedTypes) {
        this.usedTypes = usedTypes;
    }
    
    public void setUsedConverters(Converter<Object,Object>[] usedConverters) {
    	this.usedConverters = usedConverters;
    }
    
    public void setUsedMapperFacades(BoundMapperFacade<Object, Object>[] usedMapperFacades) {
        this.usedMapperFacades = usedMapperFacades;
    }
}
