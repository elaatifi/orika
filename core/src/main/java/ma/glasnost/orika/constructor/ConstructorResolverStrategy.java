package ma.glasnost.orika.constructor;

import java.lang.reflect.Constructor;

import ma.glasnost.orika.metadata.ClassMap;

public interface ConstructorResolverStrategy {
    
    <T, A, B> Constructor<T> resolve(ClassMap<A, B> classMap, Class<T> sourceClass);
}
