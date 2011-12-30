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
import java.util.Set;

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
    <S, D> D newObject(S source, Class<? extends D> destinationClass, MappingContext context);
    
}