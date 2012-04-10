package ma.glasnost.orika.property;

import ma.glasnost.orika.impl.UtilityResolver;

/**
 * 
 * 
 * @author matt.deboer@gmail.com
 *
 */
public abstract class PropertyResolver {
    
    private static final class Singleton {
        private static final PropertyResolverStrategy INSTANCE = UtilityResolver.getDefaultPropertyResolverStrategy();
    }
    
    public static PropertyResolverStrategy getInstance() {
        return Singleton.INSTANCE;
    }
}
