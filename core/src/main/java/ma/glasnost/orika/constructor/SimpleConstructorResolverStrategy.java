package ma.glasnost.orika.constructor;

import java.lang.reflect.Constructor;

import ma.glasnost.orika.metadata.ClassMap;

public class SimpleConstructorResolverStrategy implements ConstructorResolverStrategy {
    
    @SuppressWarnings({ "unchecked" })
    public <T, A, B> Constructor<T> resolve(ClassMap<A, B> classMap, Class<T> sourceClass) {
        boolean aToB = classMap.getBType().equals(sourceClass);
        // String[] argumentNames = aToB ? classMap.getConstructorB() :
        // classMap.getConstructorA();
        Class<?> targetClass = aToB ? classMap.getBType() : classMap.getAType();
        
        // TODO to specify
        return (Constructor<T>) targetClass.getConstructors()[0];
        
    }
}
