package ma.glasnost.orika.constructor;

import java.lang.reflect.Constructor;

import ma.glasnost.orika.metadata.ClassMap;

public interface PickingConstructorStrategy {
    
    <T, A, B> Constructor<T> pick(ClassMap<A, B> classMap, Class<T> sourceClass);
}
