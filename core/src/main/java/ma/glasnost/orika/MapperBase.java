package ma.glasnost.orika;

import ma.glasnost.orika.impl.MappingContext;

public abstract class MapperBase<A, B> implements Mapper<A, B> {
    
    protected MapperFacade mapperFacade;
    
    public MapperBase() {
        
    }
    
    public void mapAtoB(A a, B b, MappingContext context) {
        /* */
    }
    
    public void mapBtoA(B b, A a, MappingContext context) {
        /* */
    }
    
    public void setMapperFacade(MapperFacade mapper) {
        this.mapperFacade = mapper;
    }
    
}
