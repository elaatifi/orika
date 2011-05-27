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

import ma.glasnost.orika.impl.MappingContext;

/**
 * 
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

	<S, D> D convert(S source, Class<D> destinationClass);

	/**
	 * Create new instance of a destination class. Abstract types are
	 * unsupported.
	 * 
	 * @param destinationClass
	 * @param mappingContext
	 * @return
	 */
	<D> D newObject(Class<? extends D> destinationClass, MappingContext mappingContext);

}