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

import java.lang.reflect.ParameterizedType;

/**
 * Abstract class for all converter built in hierarchy
 * 
 * @author S.M. Elaatifi
 */
public abstract class ConverterBase<S, D> implements Converter<S, D> {

	private final Class<S> sourceClass;

	private final Class<D> destinationClass;

	@Deprecated
	public ConverterBase() {
		sourceClass = (Class<S>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
		destinationClass = (Class<D>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[1];

	}

	public ConverterBase(Class<S> sourceClass, Class<D> destinationClass) {
		this.sourceClass = sourceClass;
		this.destinationClass = destinationClass;
	}

	public Class<D> getDestination() {
		return destinationClass;
	}

	public Class<S> getSource() {
		return sourceClass;
	}

}
