/*
 * Orika - simpler, better and faster Java bean mapping
 *
 * Copyright (C) 2011-2013 Orika authors
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

package ma.glasnost.orika.impl.generator;

import ma.glasnost.orika.metadata.Property;
import ma.glasnost.orika.metadata.Type;

public class MapEntryRef extends VariableRef {

	public enum EntryPart {
		KEY {{
		    this.prototype = new Property.Builder()
                .name("key")
                .expression("key")
                .getter("getKey()")
                .setter("setKey(%s)")
                .build();
	    	
		}},
		VALUE {{
			this.prototype = new Property.Builder()
			    .name("value")
			    .expression("value")
			    .getter("getValue()")
			    .setter("setValue(%s)")
			    .build();
		}};
		
		protected Property prototype;
		
		Property newProperty(Type<?> type) {
			return prototype.copy(type);
		}
	}
	
	public MapEntryRef(Type<?> type, String name, EntryPart entryPart) {
		super(entryPart.newProperty(type), name);
	}
	
	protected String getter() {
		return VariableRef.getGetter(property(), name);
	}
	
	protected String setter() {
		return VariableRef.getSetter(property(), name);
	}

}
