package ma.glasnost.orika.impl.generator;

import ma.glasnost.orika.metadata.Property;
import ma.glasnost.orika.metadata.Type;

public class MapEntryRef extends VariableRef {

	public enum EntryPart {
		KEY {{
			this.prototype = new Property();
			prototype.setName("key");
			prototype.setExpression("key");
			prototype.setGetter("getKey()");
			prototype.setSetter("setKey(%s)");
	    	
		}},
		VALUE {{
			this.prototype = new Property();
			prototype.setName("value");
			prototype.setExpression("value");
			prototype.setGetter("getValue()");
			prototype.setSetter("setValue(%s)");
		}};
		
		protected Property prototype;
		
		Property newProperty(Type<?> type) {
			Property p = prototype.copy();
			p.setType(type);
			return p;
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
