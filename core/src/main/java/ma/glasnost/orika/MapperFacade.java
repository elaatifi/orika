/*
 * Orika - simpler, better and faster Java bean mapping
 * 
 * Copyright (C) 2011 Orika authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ma.glasnost.orika;

import java.util.List;
import java.util.Map;
import java.util.Set;

import ma.glasnost.orika.metadata.Type;

/**
 * The main runtime interface between a Java application and Orika. This is the
 * central interface abstracting the service of a Java bean mapping. <br>
 * <br>
 * 
 * The main operation of <code>MapperFacade</code> is <code>map()</code> that
 * copy from a deeply structured one object to an other.<br>
 * <br>
 * 
 * MapperFacade manages the state of a mapping operation through MappingContext.<br>
 * The operation of mapping may include : <br>
 * <ul>
 * <li>Creation of new objects : <code>newObject()</code></li>
 * <li>Conversion object to another type: <code>convert()</code></li>
 * <li>Mapping recursively an object to an other class : <code>map()</code></li>
 * </ul>
 * <br>
 * 
 * Example of code to map an instance of <code>Entity</code>(<code>entity</code>
 * ) to <code>DTO</code> class:<br>
 * 
 * <pre>
 * ...
 * DTO newDTO = mapperFacade.map(entity, DTO.class);
 * ...
 * </pre>
 * 
 * @author S.M. El Aatifi
 * 
 */
public interface MapperFacade {
    
    /**
     * Create and return a new instance of type D mapped with the properties
     * of <code>sourceObject</code>.
     * 
     * @param sourceObject the object to map from
     * @param destinationClass the type of the new object to return
     * @return a new instance of type D mapped with the properties of <code>sourceObject</code>
     */
    <S, D> D map(S sourceObject, Class<D> destinationClass);
    
    /**
     * Create and return a new instance of type D mapped with the properties
     * of <code>sourceObject</code>.
     * 
     * @param sourceObject the object to map from
     * @param destinationClass the type of the new object to return
     * @param context the context from the current mapping request
     * @return a new instance of type D mapped with the properties of <code>sourceObject</code>
     */
    <S, D> D map(S sourceObject, Class<D> destinationClass, MappingContext context);
    
    /**
     * Maps the properties of <code>sourceObject</code> onto <code>destinationObject</code>.
     * 
     * @param sourceObject the object from which to read the properties
     * @param destinationObject the object onto which the properties should be mapped
     */
    <S, D> void map(S sourceObject, D destinationObject);
    
    /**
     * Maps the properties of <code>sourceObject</code> onto <code>destinationObject</code>.
     * 
     * @param sourceObject the object from which to read the properties
     * @param destinationObject the object onto which the properties should be mapped
     * @param context the context from the current mapping request
     */
    <S, D> void map(S sourceObject, D destinationObject, MappingContext context);
    
    /**
     * Maps the properties of <code>sourceObject</code> onto <code>destinationObject</code>, 
     * using <code>sourceType</code> and <code>destinationType</code> to specify the parameterized
     * types of the source and destination object.
     * 
     * @param sourceObject the object from which to read the properties
     * @param destinationObject the object onto which the properties should be mapped
     * @param sourceType the parameterized type of the source object
     * @param destinationType the parameterized type of the destination object
     */
    <S, D> void map(S sourceObject, D destinationObject, Type<S> sourceType, Type<D> destinationType);   

    /**
     * Maps the properties of <code>sourceObject</code> onto <code>destinationObject</code>, 
     * using <code>sourceType</code> and <code>destinationType</code> to specify the parameterized
     * types of the source and destination object.
     * 
     * @param sourceObject the object from which to read the properties
     * @param destinationObject the object onto which the properties should be mapped
     * @param sourceType the parameterized type of the source object
     * @param destinationType the parameterized type of the destination object
     * @param context the context from the current mapping request
     */
    <S, D> void map(S sourceObject, D destinationObject, Type<S> sourceType, Type<D> destinationType, MappingContext context);   
    
    /**
     * Maps the source iterable into a new Set parameterized by <code>destinationClass</code>.
     * 
     * @param source the Iterable from which to map
     * @param destinationClass the type of elements to be contained in the returned Set.
     * @return a new Set containing elements of type <code>destinationClass</code> mapped from 
     * the elements of <code>source</code>.
     */
    <S, D> Set<D> mapAsSet(Iterable<S> source, Class<D> destinationClass);
    
    /**
     * Maps the source iterable into a new Set parameterized by <code>destinationClass</code>.
     * 
     * @param source the Iterable from which to map
     * @param destinationClass the type of elements to be contained in the returned Set.
     * @param context the context from the current mapping request
     * @return a new Set containing elements of type <code>destinationClass</code> mapped from 
     * the elements of <code>source</code>.
     */
    <S, D> Set<D> mapAsSet(Iterable<S> source, Class<D> destinationClass, MappingContext context);
    
    /**
     * Maps the source Array into a new Set parameterized by <code>destinationClass</code>.
     * 
     * @param source the Array from which to map
     * @param destinationClass the type of elements to be contained in the returned Set.
     * @return a new Set containing elements of type <code>destinationClass</code> mapped from 
     * the elements of <code>source</code>.
     */
    <S, D> Set<D> mapAsSet(S[] source, Class<D> destinationClass);
    
    /**
     * Maps the source Array into a new Set parameterized by <code>destinationClass</code>.
     * 
     * @param source the Array from which to map
     * @param destinationClass the type of elements to be contained in the returned Set.
     * @param context the context from the current mapping request
     * @return a new Set containing elements of type <code>destinationClass</code> mapped from 
     * the elements of <code>source</code>.
     */
    <S, D> Set<D> mapAsSet(S[] source, Class<D> destinationClass, MappingContext context);
    
    /**
     * Maps the source Iterable into a new List parameterized by <code>destinationClass</code>.
     * 
     * @param source the Iterable from which to map
     * @param destinationClass the type of elements to be contained in the returned Set.
     * @return a new List containing elements of type <code>destinationClass</code> mapped from 
     * the elements of <code>source</code>.
     */
    <S, D> List<D> mapAsList(Iterable<S> source, Class<D> destinationClass);
    
    /**
     * Maps the source Iterable into a new List parameterized by <code>destinationClass</code>.
     * 
     * @param source the Iterable from which to map
     * @param destinationClass the type of elements to be contained in the returned Set.
     * @param context the context from the current mapping request
     * @return a new List containing elements of type <code>destinationClass</code> mapped from 
     * the elements of <code>source</code>.
     */
    <S, D> List<D> mapAsList(Iterable<S> source, Class<D> destinationClass, MappingContext context);
    
    /**
     * Maps the source Array into a new List parameterized by <code>destinationClass</code>.
     * 
     * @param source the Array from which to map
     * @param destinationClass the type of elements to be contained in the returned Set.
     * @return a new List containing elements of type <code>destinationClass</code> mapped from 
     * the elements of <code>source</code>.
     */
    <S, D> List<D> mapAsList(S[] source, Class<D> destinationClass);
    
    /**
     * Maps the source Array into a new List parameterized by <code>destinationClass</code>.
     * 
     * @param source the Array from which to map
     * @param destinationClass the type of elements to be contained in the returned Set.
     * @param context the context from the current mapping request
     * @return a new List containing elements of type <code>destinationClass</code> mapped from 
     * the elements of <code>source</code>.
     */
    <S, D> List<D> mapAsList(S[] source, Class<D> destinationClass, MappingContext context);
    
    /**
     * Maps the source Array into a new Array of type<code>D</code>.
     * 
     * @param source the Array from which to map
     * @param destinationClass the type of elements to be contained in the returned Set.
     * @param context the context from the current mapping request
     * @return a new Array containing elements of type <code>destinationClass</code> mapped from 
     * the elements of <code>source</code>.
     */
    <S, D> D[] mapAsArray(D[] destination, Iterable<S> source, Class<D> destinationClass);
    
    <S, D> D[] mapAsArray(D[] destination, S[] source, Class<D> destinationClass);
    
    <S, D> D[] mapAsArray(D[] destination, Iterable<S> source, Class<D> destinationClass, MappingContext context);
    
    <S, D> D[] mapAsArray(D[] destination, S[] source, Class<D> destinationClass, MappingContext context);
    
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // New method sigantures to support generics mapping
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    <S, D> D map(S sourceObject, Type<S> sourceType, Type<D> destinationType);
    
    <S, D> D map(S sourceObject, Type<S> sourceType, Type<D> destinationType, MappingContext context);
    
    <S, D> Set<D> mapAsSet(Iterable<S> source, Type<S> sourceType, Type<D> destinationType);
    
    <S, D> Set<D> mapAsSet(Iterable<S> source, Type<S> sourceType, Type<D> destinationType, MappingContext context);
    
    <S, D> Set<D> mapAsSet(S[] source, Type<S> sourceType, Type<D> destinationType);
    
    <S, D> Set<D> mapAsSet(S[] source, Type<S> sourceType, Type<D> destinationType, MappingContext context);
    
    <S, D> List<D> mapAsList(Iterable<S> source, Type<S> sourceType, Type<D> destinationType);
    
    <S, D> List<D> mapAsList(Iterable<S> source, Type<S> sourceType, Type<D> destinationType, MappingContext context);
    
    <S, D> List<D> mapAsList(S[] source, Type<S> sourceType, Type<D> destinationType);
    
    <S, D> List<D> mapAsList(S[] source, Type<S> sourceType, Type<D> destinationType, MappingContext context);
    
    <S, D> D[] mapAsArray(D[] destination, Iterable<S> source, Type<S> sourceType, Type<D> destinationType);
    
    <S, D> D[] mapAsArray(D[] destination, S[] source, Type<S> sourceType, Type<D> destinationType);
    
    <S, D> D[] mapAsArray(D[] destination, Iterable<S> source, Type<S> sourceType, Type<D> destinationType, MappingContext context);
    
    <S, D> D[] mapAsArray(D[] destination, S[] source, Type<S> sourceType, Type<D> destinationType, MappingContext context);
    
    <S, D> D convert(S source, Type<S> sourceType, Type<D> destinationType, String converterId);

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // New method signatures to support java.util.Map types
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    /*
     * New mapping type: Map to Map
     */
    <Sk, Sv, Dk, Dv> Map<Dk,Dv> mapAsMap(Map<Sk,Sv> source, Type<? extends Map<Sk,Sv>> sourceType, Type<? extends Map<Dk,Dv>> destinationType);
    
    <Sk, Sv, Dk, Dv> Map<Dk,Dv> mapAsMap(Map<Sk,Sv> source, Type<? extends Map<Sk,Sv>> sourceType, Type<? extends Map<Dk,Dv>> destinationType, MappingContext context);
    
    /*
     * New mapping type: Iterable or Array to Map
     */
    <S, Dk, Dv> Map<Dk, Dv> mapAsMap(Iterable<S> source, Type<S> sourceType, Type<? extends Map<Dk, Dv>> destinationType);
    
    <S, Dk, Dv> Map<Dk, Dv> mapAsMap(Iterable<S> source, Type<S> sourceType, Type<? extends Map<Dk, Dv>> destinationType, MappingContext context);
    
    <S, Dk, Dv> Map<Dk, Dv> mapAsMap(S[] source, Type<S> sourceType, Type<? extends Map<Dk, Dv>> destinationType);
    
    <S, Dk, Dv> Map<Dk, Dv> mapAsMap(S[] source, Type<S> sourceType, Type<? extends Map<Dk, Dv>> destinationType, MappingContext context);
    
    /*
     * New mapping type: Map to List, Set or Array
     */
    <Sk, Sv, D> List<D> mapAsList(Map<Sk, Sv> source, Type<? extends Map<Sk, Sv>> sourceType, Type<D> destinationType);
    
    <Sk, Sv, D> List<D> mapAsList(Map<Sk, Sv> source, Type<? extends Map<Sk, Sv>> sourceType, Type<D> destinationType, MappingContext context);
    
    <Sk, Sv, D> Set<D> mapAsSet(Map<Sk, Sv> source, Type<? extends Map<Sk, Sv>> sourceType, Type<D> destinationType);
    
    <Sk, Sv, D> Set<D> mapAsSet(Map<Sk, Sv> source, Type<? extends Map<Sk, Sv>> sourceType, Type<D> destinationType, MappingContext context);
    
    <Sk, Sv, D> D[] mapAsArray(D[] destination, Map<Sk, Sv> source, Type<? extends Map<Sk, Sv>> sourceType, Type<D> destinationType);
    
    <Sk, Sv, D> D[] mapAsArray(D[] destination, Map<Sk, Sv> source, Type<? extends Map<Sk, Sv>> sourceType, Type<D> destinationType, MappingContext context);
     
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    <S, D> D convert(S source, Class<D> destinationClass, String converterId);
    
    /**
     * Create new instance of a destination class. <strong>Abstract types are
     * unsupported</code>.
     * 
     * @param source
     * @param destinationClass
     * @return new instance of <code>destinationClass</code>
     */
    // TODO Utilité d'avoir cette méthode publique?
    <S, D> D newObject(S source, Type<? extends D> destinationClass, MappingContext context);
    
}