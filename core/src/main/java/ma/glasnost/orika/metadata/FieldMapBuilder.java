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


public class FieldMapBuilder<A, B> {
    
    private final ClassMapBuilder<A, B> classMapBuilder;
    
    private final Property aProperty;
    
    private final Property bProperty;
    
    private Property aInverseProperty;
    
    private Property bInverseProperty;
    
    private String converterId;
    
    private MappingDirection mappingDirection = MappingDirection.BIDIRECTIONAL;
    private boolean excluded;
    
    FieldMapBuilder(ClassMapBuilder<A, B> classMapBuilder, String a, String b) {
        this.classMapBuilder = classMapBuilder;
        
        this.aProperty = classMapBuilder.resolveAProperty(a);
        this.bProperty = classMapBuilder.resolveBProperty(b);
        
    }
    
    public ClassMapBuilder<A, B> add() {
        final FieldMap fieldMap = new FieldMap(aProperty, bProperty, aInverseProperty, bInverseProperty, mappingDirection, true, excluded,
                converterId);
        classMapBuilder.addFieldMap(fieldMap);
        
        return classMapBuilder;
    }
    
    public FieldMapBuilder<A, B> aInverse(String aInverse) {
        final Type<?> type = aProperty.isCollection() ? aProperty.getElementType() : aProperty.getType();
        aInverseProperty = classMapBuilder.resolveProperty(type, aInverse);
        
        return this;
    }
    
    public FieldMapBuilder<A, B> bInverse(String bInverse) {
        final Type<?> type = bProperty.isCollection() ? bProperty.getElementType() : bProperty.getType();
        bInverseProperty = classMapBuilder.resolveProperty(type, bInverse);
        
        return this;
    }
    
    /**
     * Specify that the configured field mapping (property) should only be used
     * when mapping in the direction from A to B
     * 
     * @return
     */
    public FieldMapBuilder<A, B> aToB() {
        
        mappingDirection = MappingDirection.A_TO_B;
        
        return this;
    }
    
    /**
     * Specify that the configured field mapping (property) should only be used
     * when mapping in the direction from B to A
     * 
     * @return
     */
    public FieldMapBuilder<A, B> bToA() {
        mappingDirection = MappingDirection.B_TO_A;
        
        return this;
    }
    
    /**
     * Specify that the converter (which was previously registered with the specified id)
     * should be applied to this specific field mapping.
     * 
     * @param id the id with which the converter to use was registered
     * @return
     */
    public FieldMapBuilder<A, B> converter(String id) {
        this.converterId = id;
        return this;
    }
    
    /**
     * Specify that the property should be excluded from mapping
     */
    public FieldMapBuilder<A, B> exclude() {
        excluded = true;
        return this;
    }
    
    
    public static FieldMap mapKeys(Type<?> aType, Type<?> bType) {
    	
    	Property aProperty = new Property();
    	aProperty.setName("key");
    	aProperty.setExpression("key");
    	aProperty.setGetter("getKey()");
    	aProperty.setSetter("setKey(%s)");
    	aProperty.setType(aType);
    	Property bProperty = aProperty.copy();
    	bProperty.setType(bType);
    	
    	return new FieldMap(aProperty, bProperty, null, null, MappingDirection.A_TO_B, true, false, null);
    }
    
    public static FieldMap mapValues(Type<?> aType, Type<?> bType) {
    	
    	Property aProperty = new Property();
    	aProperty.setName("value");
    	aProperty.setExpression("value");
    	aProperty.setGetter("getValue()");
    	aProperty.setSetter("setValue(%s)");
    	aProperty.setType(aType);
    	Property bProperty = aProperty.copy();
    	bProperty.setType(bType);
    	
    	return new FieldMap(aProperty, bProperty, null, null, MappingDirection.A_TO_B, true, false, null);
    }
}
