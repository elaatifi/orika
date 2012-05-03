package ma.glasnost.orika.impl.mapping.strategy;

import ma.glasnost.orika.MappingContext;

public class CopyByReferenceStrategy implements MappingStrategy {
    
    private static class Singleton {
        private static CopyByReferenceStrategy INSTANCE = new CopyByReferenceStrategy();
    }
    
    public static CopyByReferenceStrategy getInstance() {
        return Singleton.INSTANCE;
    }
    
    private CopyByReferenceStrategy() {
       
    }

    public Object map(Object sourceObject, Object destinationObject, MappingContext context) {
        return sourceObject;
    }
    
}
