package ma.glasnost.orika;

import java.util.List;
import java.util.Set;

import ma.glasnost.orika.impl.MappingContext;

public interface MapperFacade {
    
    <S, D> D map(S sourceObject, Class<D> destinationClass);
    
    <S, D> D map(S sourceObject, Class<D> destinationClass, MappingContext context);
    
    <S, D> void map(S sourceObject, D destinationObject);
    
    <S, D> void map(S sourceObject, D destinationObject, MappingContext context);
    
    <S, D> Set<D> mapAsSet(Iterable<S> source, Class<D> destinationClass);
    
    <S, D> Set<D> mapAsSet(Iterable<S> source, Class<D> destinationClass, MappingContext context);
    
    <S, D> Set<D> mapAsSet(S[] source, Class<D> destinationClass);
    
    <S, D> Set<D> mapAsSet(S[] source, Class<D> destinationClass, MappingContext context);
    
    <S, D> List<D> mapAsList(Iterable<S> source, Class<D> destinationClass);
    
    <S, D> List<D> mapAsList(Iterable<S> source, Class<D> destinationClass, MappingContext context);
    
    <S, D> List<D> mapAsList(S[] source, Class<D> destinationClass);
    
    <S, D> List<D> mapAsList(S[] source, Class<D> destinationClass, MappingContext context);
    
    <S, D> D[] mapAsArray(D[] destination, Iterable<S> source, Class<D> destinationClass);
    
    <S, D> D[] mapAsArray(D[] destination, S[] source, Class<D> destinationClass);
    
    <S, D> D[] mapAsArray(D[] destination, Iterable<S> source, Class<D> destinationClass, MappingContext context);
    
    <S, D> D[] mapAsArray(D[] destination, S[] source, Class<D> destinationClass, MappingContext context);
    
    <S, D> D convert(S source, Class<D> destinationClass);
    
}