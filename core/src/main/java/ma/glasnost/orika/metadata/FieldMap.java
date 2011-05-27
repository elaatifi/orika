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

package ma.glasnost.orika.metadata;

import ma.glasnost.orika.impl.Specifications.Specification;

public class FieldMap {

	private final Property source;
	private final Property destination;
	private boolean excluded;
	private final boolean configured;

	public FieldMap(Property a, Property b) {
		this(a, b, false, false);
	}

	public FieldMap(Property a, Property b, boolean configured, boolean excluded) {
		this.source = a;
		this.destination = b;
		this.excluded = excluded;
		this.configured = configured;
	}

	public boolean isExcluded() {
		return excluded;
	}

	public void setExcluded(boolean exclude) {
		this.excluded = exclude;
	}

	public Property getSource() {
		return source;
	}

	public Property getDestination() {
		return destination;
	}

	String getSourceName() {
		return source.getName();
	}

	String getDestinationName() {
		return destination.getName();
	}

	public boolean isConfigured() {
		return configured;
	}

	public FieldMap flip() {
		return new FieldMap(destination, source, configured, excluded);
	}

	public boolean is(Specification specification) {
		return specification.apply(this);
	}

	public boolean have(Specification specification) {
		return specification.apply(this);
	}

	@Override
	public String toString() {
		return "FieldMap [destination=" + getDestinationName() + ", source=" + getSourceName() + "]";
	}

}
