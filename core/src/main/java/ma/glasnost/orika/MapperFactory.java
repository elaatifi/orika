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

import ma.glasnost.orika.impl.GeneratedMapperBase;
import ma.glasnost.orika.impl.MappingContext;
import ma.glasnost.orika.metadata.ClassMap;
import ma.glasnost.orika.metadata.MapperKey;

public interface MapperFactory {

	GeneratedMapperBase get(MapperKey mapperKey);

	<S, D> void registerClassMap(ClassMap<S, D> classMap);

	<S, D> void registerConverter(Converter<S, D> converter);

	<S, D> Converter<S, D> lookupConverter(Class<S> source, Class<D> destination);

	<T> void registerObjectFactory(ObjectFactory<T> objectFactory, Class<T> targetClass);

	<T> ObjectFactory<T> lookupObjectFactory(Class<T> targetClass);

	<S, D> Class<? extends D> lookupConcreteDestinationClass(Class<S> sourceClass, Class<D> destinationClass,
			MappingContext context);

	MapperFacade getMapperFacade();
}
