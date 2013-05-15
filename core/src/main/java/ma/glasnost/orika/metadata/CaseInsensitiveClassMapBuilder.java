package ma.glasnost.orika.metadata;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import ma.glasnost.orika.DefaultFieldMapper;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.property.PropertyResolverStrategy;

/**
 * CaseInsensitiveClassMapBuilder is an extension of ClassMapBuilder which performs
 * case-insensitive matching of property names in the 'byDefault()' method.
 * 
 * @author mattdeboer
 *
 * @param <A>
 * @param <B>
 */
public class CaseInsensitiveClassMapBuilder<A,B> extends ClassMapBuilder<A,B> {

    /**
     *
     */
    public static class Factory extends ClassMapBuilderFactory {

        /* (non-Javadoc)
         * @see ma.glasnost.orika.metadata.ClassMapBuilderFactory#newClassMapBuilder(ma.glasnost.orika.metadata.Type, ma.glasnost.orika.metadata.Type, ma.glasnost.orika.property.PropertyResolverStrategy, ma.glasnost.orika.DefaultFieldMapper[])
         */
        @Override
        protected <A, B> ClassMapBuilder<A, B> newClassMapBuilder(
                Type<A> aType, Type<B> bType,
                MapperFactory mapperFactory,
                PropertyResolverStrategy propertyResolver,
                DefaultFieldMapper[] defaults) {
            
            return new CaseInsensitiveClassMapBuilder<A,B>(aType, bType, mapperFactory, propertyResolver, defaults);
        }
    }
    
    /**
     * @param aType
     * @param bType
     * @param mapperFactory
     * @param propertyResolver
     * @param defaults
     */
    protected CaseInsensitiveClassMapBuilder(Type<A> aType, Type<B> bType, MapperFactory mapperFactory,
            PropertyResolverStrategy propertyResolver, DefaultFieldMapper[] defaults) {
        super(aType, bType, mapperFactory, propertyResolver, defaults);
    }

    /* (non-Javadoc)
     * @see ma.glasnost.orika.metadata.ClassMapBuilder#byDefault(ma.glasnost.orika.DefaultFieldMapper[])
     * 
     * Applies default mapping allowing for case-insensitive matching of property names
     * 
     */
    @Override
    public ClassMapBuilder<A, B> byDefault(DefaultFieldMapper... withDefaults) {
        
        super.byDefault(withDefaults);
        
        
        DefaultFieldMapper[] defaults;
        if (withDefaults.length == 0) {
            defaults = getDefaultFieldMappers();
        } else {
            defaults = withDefaults;
        }
        
        
        Map<String, String> propertiesForA = new HashMap<String, String>();
        for (String prop: getPropertiesForTypeA()) {
            propertiesForA.put(prop.toLowerCase(), prop);
        }
        
        Map<String, String> propertiesForB = new HashMap<String, String>();
        for (String prop: getPropertiesForTypeB()) {
            propertiesForB.put(prop.toLowerCase(), prop);
        }
        
        for (final Entry<String, String> entry : propertiesForA.entrySet()) {
            String propertyNameA = entry.getValue();
            String lowercaseName = entry.getKey();
            if (!getMappedPropertiesForTypeA().contains(propertyNameA)) {
                if (propertiesForB.containsKey(lowercaseName)) {
                    String propertyNameB = propertiesForB.get(lowercaseName);
                    if (!getMappedPropertiesForTypeB().contains(propertyNameB)) {
                        /*
                         * Don't include the default mapping of Class to Class; this
                         * property is resolved for all types, but can't be mapped 
                         * in either direction.
                         */
                        if (!propertyNameA.equals("class")) {
                            fieldMap(propertyNameA, propertyNameB, true).add();
                        }
                    }
                } else {
                    Property prop = resolvePropertyForA(propertyNameA);
                    for (DefaultFieldMapper defaulter : defaults) {
                        String suggestion = defaulter.suggestMappedField(propertyNameA, prop.getType());
                        if (suggestion != null && getPropertiesForTypeB().contains(suggestion)) {
                            if (!getMappedPropertiesForTypeB().contains(suggestion)) {
                                fieldMap(propertyNameA, suggestion, true).add();
                            }
                        }
                    }
                }
            }
        }
        
        return this;
    }
    
}
