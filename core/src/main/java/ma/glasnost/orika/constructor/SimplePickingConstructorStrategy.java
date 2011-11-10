package ma.glasnost.orika.constructor;

import java.lang.reflect.Constructor;

import ma.glasnost.orika.metadata.ClassMap;

public class SimplePickingConstructorStrategy implements PickingConstructorStrategy {
    
    @SuppressWarnings({ "unchecked" })
    public <T, A, B> Constructor<T> pick(ClassMap<A, B> classMap, Class<T> sourceClass) {
        boolean aToB = classMap.getBType().equals(sourceClass);
        // String[] argumentNames = aToB ? classMap.getConstructorB() :
        // classMap.getConstructorA();
        Class<?> targetClass = aToB ? classMap.getBType() : classMap.getAType();
        
        // TODO to specify
        return (Constructor<T>) targetClass.getConstructors()[0];
        
    }
}
