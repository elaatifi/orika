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
package ma.glasnost.orika.impl.generator;

import java.util.Map;
import java.util.Set;

import ma.glasnost.orika.MapEntry;
import ma.glasnost.orika.metadata.Property;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.metadata.TypeFactory;

/**
 * VariableRef represents a reference to a given variable or property;
 * it contains various helper methods to properly set it's value and
 * interrogate it's underlying property or type. It also returns a properly 
 * type-safe cast of it as the toString() method, so it can safely be
 * used directly as a replacement parameter for source code statements.
 * 
 * 
 * @author matt.deboer@gmail.com
 *
 */
public class MultiOccurrenceVariableRef extends VariableRef {
    
    private String iteratorName;
    private boolean iteratorDeclared;
	
    public static MultiOccurrenceVariableRef from(VariableRef r) {
    	if (r.property() != null) {
    		return new MultiOccurrenceVariableRef(r.property(), r.name());
    	} else {
    		return new MultiOccurrenceVariableRef(r.type(), r.name());
    	}
    }
    
    public MultiOccurrenceVariableRef(Property property, String name) {
        super(property, name);
    }
    
    public MultiOccurrenceVariableRef(Property property, MultiOccurrenceVariableRef anchor) {
        super(property, anchor);
    }
    
    public MultiOccurrenceVariableRef(Type<?> type, String name) {
        super(type, name);
    }

    private String getIteratorName() {
    	if (iteratorName == null) {
    		if (isArray()) {
    			iteratorName = name() + "_$_index";
        	} else {
        		iteratorName = name() + "_$_iter";
        	}
    	}
    	return iteratorName;
    }
    
    public String declareIterator() {
    	if (iteratorDeclared) {
    		throw new IllegalStateException("Iterator has already been declared");
    	}
    	String iterator;
    	if (isArray()) {
    		iterator = "int " + getIteratorName() + " = -1";
    	} else if (isMap()) {
    		iterator = new EntrySetRef(this, name()).declareIterator();
    	} else {
    		iterator = "java.util.Iterator " + getIteratorName() + " = " + getter() + ".iterator()";
    	}
    	iteratorDeclared = true;
    	return iterator;
    }
    
    public String nextElement() {
    	if (!iteratorDeclared) {
    		throw new IllegalStateException("Iterator has not been declared"); 
    	}
    	String next;
    	if (isArray()) {
    		next = getter() + "[++" + getIteratorName()+"]";
    	} else {
    		next = getIteratorName() + ".next()";
    	}
    	return next;
    }
    
    public String iteratorHasNext() {
    	if (!iteratorDeclared) {
    		throw new IllegalStateException("Iterator has not been declared"); 
    	}
    	String hasNext;
    	if (isArray()) {
    		hasNext = getIteratorName() +" < (" + getter() + ".length - 1)";
    	} else {
    		hasNext = getIteratorName() + ".hasNext()";
    	}
    	return hasNext;
    }
    
    public String add(VariableRef value) {
    	
    	if (isArray()) {
    		if (!iteratorDeclared) {
    			throw new IllegalStateException("Iterator must be declared in order to add elements to destination array");
    		}
    		return getter() + "[++" + getIteratorName() + "] = " + value;
    	} else if (isMap() && value.isMapEntry()) {
    		return getter() + ".put(" + value + ".getKey(), " + value + ".getValue())"; 
    	} else if (isCollection()) {
    		return getter() + ".add(" + value + ")";
    	} else {
    		throw new IllegalArgumentException(type() + " does not support adding elements of type " + value.type());
    	}
    }
    
    public String collectionType() {
        String collection;
        if (isList()) {
            collection ="List";
        } else if (isSet()) {
            collection = "Set";
        } else if (isCollection()){
        	collection = "Collection";
        } else {
            throw new IllegalStateException(type() + " is not a collection type");
        }
        return collection;
    }
    
    public String newCollection() {
        return newInstance("");
    }
    
    public String newInstance(String sizeExpr) {
    	if (isArray()) {
    		return "new " + rawType().getComponentType().getCanonicalName() + "[" + sizeExpr + "]"; 
    	} else if (isMap()) {
    		return "new java.util.LinkedHashMap(" + sizeExpr + ")";
    	} else if ("Set".equals(collectionType())) {
            return "new java.util.HashSet(" + sizeExpr + ")";
        } else {
            return "new java.util.ArrayList(" + sizeExpr + ")";
        }
    }
    
    public String newMap(String sizeExpr) {
    	return "new java.util.LinkedHashMap(" + sizeExpr + ")";
    }
    
    public String newMap() {
    	return newMap("");
    }
    
    /**
     * Generates java code for a reference to the "size" of this VariableRef
     * @return
     */
    public String size() {
        return getter() + "." + (rawType().isArray() ? "length" : "size()");
    }
    
    
    public static class EntrySetRef extends MultiOccurrenceVariableRef {
    	
    	private String name;
    	
    	public EntrySetRef(VariableRef sourceMap) {
    		this(sourceMap, null);
    	}  
    	
    	public EntrySetRef(VariableRef sourceMap, String variableName) {
    		super(getSourceEntryType(sourceMap), sourceMap+ ".entrySet()");
    		this.name = variableName;
    	}
    	
		@SuppressWarnings("unchecked")
		private static Type<?> getSourceEntryType(VariableRef sourceMap) {
			return TypeFactory.valueOf(Set.class, MapEntry.entryType((Type<? extends Map<Object, Object>>) sourceMap.type()));
		}
		
		public String name() {
			if (this.name != null) {
				return this.name;
			} else {
				return super.name();
			}
		}
    }
    
    
    
    
    
}
