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

import ma.glasnost.orika.impl.MappingContext;

/**
 * Defines a contract bewteen internal parts of Orikas such as
 * <code>MapperFacade</code>, <code>MapperGenerator</code>,
 * <code>MapperFactory</code> and generated mappers
 * 
 * @author S.M. El Aatifi
 * 
 * @see MapperFacade
 * @see ma.glasnost.orika.impl.MapperGenerator
 */
public interface Mapper<A, B> {

	void mapAtoB(A a, B b, MappingContext context);

	void mapBtoA(B b, A a, MappingContext context);

	void setMapperFacade(MapperFacade mapper);

}
