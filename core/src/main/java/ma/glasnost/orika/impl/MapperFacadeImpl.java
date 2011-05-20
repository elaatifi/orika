package ma.glasnost.orika.impl;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ma.glasnost.orika.Converter;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingException;
import ma.glasnost.orika.ObjectFactory;
import ma.glasnost.orika.impl.util.ClassUtil;
import ma.glasnost.orika.metadata.MapperKey;
import ma.glasnost.orika.proxy.UnenhanceStrategy;

public class MapperFacadeImpl implements MapperFacade {
    
    private final MapperFactory mapperFactory;
    private final UnenhanceStrategy unenhanceStrategy;
    
    public MapperFacadeImpl(MapperFactory mapperFactory, UnenhanceStrategy unenhanceStrategy) {
        this.mapperFactory = mapperFactory;
        this.unenhanceStrategy = unenhanceStrategy;
    }
    
    public <S, D> D map(S sourceObject, Class<D> destinationClass) {
        return map(sourceObject, destinationClass, new MappingContext());
    }
    
    @SuppressWarnings("unchecked")
    public <S, D> D map(S sourceObject, Class<D> destinationClass, MappingContext context) {
        if (destinationClass == null)
            return null;
        if (sourceObject == null)
            throw new MappingException("Can not map a null object.");
        
        sourceObject = unenhanceStrategy.unenhanceObject(sourceObject);
        
        // XXX when it's immutable it's ok to copy by ref
        if (ClassUtil.isImmutable(sourceObject.getClass()) && sourceObject.getClass().equals(destinationClass)) {
            return (D) sourceObject;
        }
        
        if (context.isAlreadyMapped(sourceObject)) {
            return (D) context.getMappedObject(sourceObject);
        }
        
        if (Modifier.isAbstract(destinationClass.getModifiers())) {
            destinationClass = (Class<D>) mapperFactory.lookupConcreteDestinationClass(sourceObject.getClass(), destinationClass, context);
        }
        
        D destinationObject = newObject(destinationClass);
        
        context.cacheMappedObject(sourceObject, destinationObject);
        
        map(sourceObject, destinationObject, context);
        return destinationObject;
    }
    
    public <S, D> void map(S sourceObject, D destinationObject, MappingContext context) {
        sourceObject = unenhanceStrategy.unenhanceObject(sourceObject);
        Class<?> sourceClass = sourceObject.getClass();
        Class<?> destinationClass = destinationObject.getClass();
        while (!destinationClass.equals(Object.class)) {
            mapDeclaredProperties(sourceObject, destinationObject, sourceClass, destinationClass, context);
            destinationClass = destinationClass.getSuperclass();
            sourceClass = sourceClass.getSuperclass();
        }
    }
    
    public <S, D> void map(S sourceObject, D destinationObject) {
        map(sourceObject, destinationObject, new MappingContext());
    }
    
    public final <S, D> Set<D> mapAsSet(Iterable<S> source, Class<D> destinationClass) {
        return mapAsSet(source, destinationClass, new MappingContext());
    }
    
    public final <S, D> Set<D> mapAsSet(Iterable<S> source, Class<D> destinationClass, MappingContext context) {
        return (Set<D>) mapAsCollection(source, destinationClass, new HashSet<D>(), context);
    }
    
    public final <S, D> List<D> mapAsList(Iterable<S> source, Class<D> destinationClass) {
        return (List<D>) mapAsCollection(source, destinationClass, new ArrayList<D>(), new MappingContext());
    }
    
    public final <S, D> List<D> mapAsList(Iterable<S> source, Class<D> destinationClass, MappingContext context) {
        return (List<D>) mapAsCollection(source, destinationClass, new ArrayList<D>(), context);
    }
    
    public <S, D> D[] mapAsArray(D[] destination, Iterable<S> source, Class<D> destinationClass) {
        return mapAsArray(destination, source, destinationClass, new MappingContext());
    }
    
    public <S, D> D[] mapAsArray(D[] destination, S[] source, Class<D> destinationClass) {
        return mapAsArray(destination, source, destinationClass, new MappingContext());
    }
    
    public <S, D> D[] mapAsArray(D[] destination, Iterable<S> source, Class<D> destinationClass, MappingContext context) {
        int i = 0;
        for (S s : source) {
            destination[i++] = map(s, destinationClass);
        }
        return destination;
    }
    
    public <S, D> D[] mapAsArray(D[] destination, S[] source, Class<D> destinationClass, MappingContext context) {
        int i = 0;
        for (S s : source) {
            destination[i++] = map(s, destinationClass);
        }
        return destination;
    }
    
    public <S, D> List<D> mapAsList(S[] source, Class<D> destinationClass) {
        return mapAsList(source, destinationClass, new MappingContext());
    }
    
    public <S, D> List<D> mapAsList(S[] source, Class<D> destinationClass, MappingContext context) {
        List<D> destination = new ArrayList<D>(source.length);
        for (S s : source) {
            destination.add(map(s, destinationClass, context));
        }
        return destination;
    }
    
    public <S, D> Set<D> mapAsSet(S[] source, Class<D> destinationClass) {
        return mapAsSet(source, destinationClass, new MappingContext());
    }
    
    public <S, D> Set<D> mapAsSet(S[] source, Class<D> destinationClass, MappingContext context) {
        Set<D> destination = new HashSet<D>(source.length);
        for (S s : source) {
            destination.add(map(s, destinationClass));
        }
        return destination;
    }
    
    protected void mapDeclaredProperties(Object sourceObject, Object destinationObject, Class<?> sourceClass, Class<?> destinationClass,
            MappingContext context) {
        MapperKey mapperKey = new MapperKey(sourceClass, destinationClass);
        GeneratedMapperBase mapper = mapperFactory.get(mapperKey);
        
        if (mapper == null) {
            throw new IllegalStateException(String.format("Can not create a mapper for classes : %s, %s", destinationClass, sourceObject
                    .getClass()));
        }
        
        if (mapper.getAType().equals(sourceClass)) {
            mapper.mapAtoB(sourceObject, destinationObject, context);
        } else if (mapper.getAType().equals(destinationClass)) {
            mapper.mapBtoA(sourceObject, destinationObject, context);
        } else {
            throw new IllegalStateException(String.format("Source object type's must be one of '%s' or '%s'.", mapper.getAType(), mapper
                    .getBType()));
        }
    }
    
    protected <D> D newObject(Class<D> destinationClass) {
        
        try {
            ObjectFactory<D> objectFactory = mapperFactory.lookupObjectFactory(destinationClass);
            if (objectFactory != null) {
                return objectFactory.create();
            } else {
                return destinationClass.newInstance();
            }
        } catch (InstantiationException e) {
            throw new MappingException(e);
        } catch (IllegalAccessException e) {
            throw new MappingException(e);
        }
    }
    
    protected <S, D> Collection<D> mapAsCollection(Iterable<S> source, Class<D> destinationClass, Collection<D> destination,
            MappingContext context) {
        for (S item : source) {
            destination.add(map(item, destinationClass, context));
        }
        return destination;
    }
    
    @SuppressWarnings("unchecked")
    public <S, D> D convert(S source, Class<D> destinationClass) {
        Converter<S, D> converter = (Converter<S, D>) mapperFactory.lookupConverter(source.getClass(), destinationClass);
        return converter.convert(source);
    }
    
}
