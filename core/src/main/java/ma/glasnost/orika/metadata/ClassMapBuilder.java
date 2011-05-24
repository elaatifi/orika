package ma.glasnost.orika.metadata;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ma.glasnost.orika.Mapper;
import ma.glasnost.orika.MappingException;
import ma.glasnost.orika.impl.util.PropertyUtil;

public final class ClassMapBuilder<A, B> {
    
    private final ClassMap<A, B> classMap;
    private final Map<String, Property> aProperties;
    private final Map<String, Property> bProperties;
    private final Set<String> propertiesCache;
    
    private ClassMapBuilder(ClassMap<A, B> classMap) {
        this.classMap = classMap;
        aProperties = PropertyUtil.getProperties(classMap.aType);
        bProperties = PropertyUtil.getProperties(classMap.bType);
        propertiesCache = new HashSet<String>();
    }
    
    /**
     * Map a field two way
     * 
     * @param a
     *            property name in type A
     * @param b
     *            property name in type B
     * @return
     */
    public ClassMapBuilder<A, B> field(String a, String b) {
        Property aProperty = resolveAProperty(a), bProperty = resolveBProperty(b);
        classMap.addFieldMapping(new FieldMap(aProperty, bProperty, true, false));
        propertiesCache.add(a);
        return this;
    }
    
    /**
     * Exclude a field two way
     * 
     * @param a
     *            property name in type A
     * @param b
     *            property name in type B
     * @return
     */
    public ClassMapBuilder<A, B> exclude(String a, String b) {
        Property aProperty = resolveAProperty(a), bProperty = resolveBProperty(b);
        classMap.addFieldMapping(new FieldMap(aProperty, bProperty, true, true));
        propertiesCache.add(a);
        return this;
    }
    
    public ClassMapBuilder<A, B> customize(Mapper<A, B> customizedMapper) {
        classMap.setCustomizedMapper(customizedMapper);
        return this;
    }
    
    public ClassMapBuilder<A, B> byDefault() {
        
        for (String propertyName : aProperties.keySet()) {
            if (bProperties.containsKey(propertyName) && !propertiesCache.contains(propertyName)) {
                Property a = aProperties.get(propertyName);
                Property b = bProperties.get(propertyName);
                classMap.fieldsMapping.add(new FieldMap(a, b));
            }
        }
        
        return this;
    }
    
    public ClassMap<A, B> toClassMap() {
        return classMap;
    }
    
    public static <A, B> ClassMapBuilder<A, B> map(Class<A> aType, Class<B> bType) {
        return new ClassMapBuilder<A, B>(new ClassMap<A, B>(aType, bType));
    }
    
    Property resolveAProperty(String expr) {
        Property property;
        if (PropertyUtil.isExpression(expr)) {
            property = PropertyUtil.getNestedProperty(classMap.getAType(), expr);
        } else if (aProperties.containsKey(expr)) {
            property = aProperties.get(expr);
        } else {
            throw new MappingException(expr + " do not belongs to " + classMap.getATypeName());
        }
        
        return property;
    }
    
    Property resolveBProperty(String expr) {
        Property property;
        if (PropertyUtil.isExpression(expr)) {
            property = PropertyUtil.getNestedProperty(classMap.getBType(), expr);
        } else if (bProperties.containsKey(expr)) {
            property = bProperties.get(expr);
        } else {
            throw new MappingException(expr + " do not belongs to " + classMap.getATypeName());
        }
        
        return property;
    }
    
}
