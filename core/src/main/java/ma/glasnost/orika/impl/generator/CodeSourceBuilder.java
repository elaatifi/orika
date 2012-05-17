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

import static java.lang.String.format;
import ma.glasnost.orika.MappingException;
import ma.glasnost.orika.impl.util.ClassUtil;
import ma.glasnost.orika.metadata.Property;
import ma.glasnost.orika.metadata.Type;

public class CodeSourceBuilder {
    
    private final StringBuilder out = new StringBuilder();
//    private int currentIndent = 1;
    private final UsedTypesContext usedTypes;
    
    public CodeSourceBuilder(int indent, UsedTypesContext usedTypes) {
//        this.currentIndent = indent;
        this.usedTypes = usedTypes;
    }
    
//    /**
//     * Returns a fully type-cast getter for the property which has no reliance
//     * on java generics.
//     * 
//     * @param property
//     *            the Property for which to return the getter
//     * @param variableExpression
//     *            the String value to use for the variable on which the getter
//     *            is called
//     * @return
//     */
//    private String getGetter(final Property property, String variableExpression) {
//        String var = variableExpression;
//        if (property.hasPath()) {
//            for (final Property p : property.getPath()) {
//                var = getGetter(p, var);
//            }
//        }
//        return "((" + property.getType().getCanonicalName() + ")" + var + "." + property.getGetter() + ")";
//    }
    
//    /**
//     * Returns a fully type-cast setter for the property which has no reliance
//     * on java generics.
//     * 
//     * @param property
//     *            the Property for which to return the getter
//     * @param variableExpression
//     *            the String value to use for the variable on which the getter
//     *            is called
//     * @return
//     */
//    private String getSetter(final Property property, final String variableExpression) {
//        String var = variableExpression;
//        if (property.hasPath()) {
//            for (final Property p : property.getPath()) {
//                var = getGetter(p, var);
//            }
//        }
//        return var + "." + property.getSetter();
//        
//    }
    
    private String usedType(Type<?> type) {
        int index = usedTypes.getUsedTypeIndex(type);
        return "usedTypes[" + index + "]";
    }
    
//    private String usedType(Property prop) {
//        return usedType(prop.getType());
//    }
    
    private String usedType(VariableRef r) {
        return usedType(r.type());
    }
    
    public CodeSourceBuilder convert(VariableRef d, VariableRef s, String converterId) {
          
        converterId = getConverterId(converterId);  
        String statement = d.assign("mapperFacade.convert(%s, %s, %s, %s)", s.asWrapper(), usedType(s), usedType(d), converterId);
        
        if (s.isPrimitive()) {
            statement(statement);
        } else {
            statement(s.ifNotNull() + statement);
        }
        return this;
    }
    
    private String getConverterId(String converterId) {
        converterId = converterId == null ? "null" : ("\"" + converterId + "\"");
        return converterId;
    }
    
    public CodeSourceBuilder set(VariableRef d, VariableRef s) {
        return statement(d.assign(s));
    }
    
    public CodeSourceBuilder setCollection(VariableRef d, VariableRef s, Property ip, Type<?> destinationType) {
        
        final Class<?> dc = destinationType.getRawType();
        final Class<?> destinationElementClass = d.elementType().getRawType();
        
        if (destinationElementClass == null) {
            throw new MappingException("cannot determine runtime type of destination collection " + dc.getName() + "." + d.name());
        }
       
        if (d.isAssignable()) {
            statement("if (%s == null) %s", d, d.assign(d.newCollection()));
        }
        
        // Start check if source property ! = null
        ifNotNull(s).then();
        if(s.isArray()) {
        	if(s.elementType().isPrimitive())
        		newLine().append("%s.addAll(asList(%s));", d, s);
        	else 
        		newLine().append("%s.addAll(mapperFacade.mapAsList(asList(%s), %s.class));", d, s, d.typeName());
        } else {
	        newLine().append("%s.clear();", d);
	        newLine().append("%s.addAll(mapperFacade.mapAs%s(%s, %s, %s, mappingContext));", d, d.collectionType(),
	                s, usedType(s.elementType()), usedType(d.elementType()));
        }
        if (ip != null) {
            final VariableRef inverse = new VariableRef(ip, "orikaCollectionItem");
            
            if (ip.isCollection()) {
                append(
                        format("for (java.util.Iterator orikaIterator = %s.iterator(); orikaIterator.hasNext();) { ", d) +
                        format("    %s orikaCollectionItem = (%s) orikaIterator.next();", d.elementTypeName(), d.elementTypeName()) +
                        format("    %s { %s; }", inverse.ifNull(), inverse.assign(inverse.newCollection())) +
                        format("    %s.add(%s);", inverse, d.owner()) +
                        format("}")
                        );

            } else if (ip.isArray()) {
                append(" // TODO support array");
            } else {
                append(
                        format("for (java.util.Iterator orikaIterator = %s.iterator(); orikaIterator.hasNext();) { ", d) +
                        format("    %s orikaCollectionItem = (%s) orikaIterator.next();", d.elementTypeName(), d.elementTypeName()) +
                        format("    %s;", inverse.assign(d.owner())) +
                        format("}")
                        );

            }
        }
        // End check if source property ! = null
        _else().statement(d.assignIfPossible("null")).end();
        
        return this;
    }
    
    public CodeSourceBuilder newLine() {
        return append("\n");
    }
    
    public CodeSourceBuilder append(String str, Object... args) {
        out.append(String.format(str, args));
        return this;
    }
    
    public CodeSourceBuilder append(String str) {
        out.append(str);
        return this;
    }
    
    /**
     * Appends the provided string as a source code statement
     * 
     * @param str
     * @param args
     * @return
     */
    public CodeSourceBuilder statement(String str, Object...args) {
        if (str !=null && !"".equals(str.trim())) {
	    	String expr = "\n" + format(str, args);
	        append(expr);
	        if (!expr.endsWith(";") && !expr.endsWith("}")) {
	        	append(";");
	        }
        }
        return this;
    }
    
    public CodeSourceBuilder then() {
        return append("{").newLine();
    }
    
    public CodeSourceBuilder begin() {
        return then();
    }
    
    public CodeSourceBuilder end() {
        return newLine().append("}").newLine();
    }
    
    public CodeSourceBuilder _else() {
        return newLine().append("} else {").newLine();
    }
    
    @Override
    public String toString() {
        return out.toString();
    }
    
    public CodeSourceBuilder setFromStringConversion(VariableRef d, VariableRef s) {

        if (d.isPrimitive()) {
            statement(d.assign("%s.valueOf(%s)", d.wrapperTypeName(), s));
        } else {
            statement(s.ifNotNull() + d.assign("%s.valueOf(%s)", d.typeName(), s));
        }
        
        return this;
        
    }
    
    public CodeSourceBuilder setToStringConversion(VariableRef d, VariableRef s) {

        if (s.isPrimitive()) {
            statement(d.assign("\"\"+ %s", s));
        } else {
            statement(s.ifNotNull() + d.assign("%s.toString()",s));
        }
        
        return this;
        
    }
    
    public CodeSourceBuilder setWrapper(VariableRef d, VariableRef s) {
 
        statement(d.assign("%s.valueOf(%s)", d.typeName(), s));
        
        return this;
    }
    
    public CodeSourceBuilder setPrimitive(VariableRef d, VariableRef s) {

        if (s.isPrimitive()) {
            statement(d.assign(s)); 
        } else {
            statement(s.ifNotNull() + d.assign(s));
        }        
        
        return this;
    }
    
    public CodeSourceBuilder setArray(VariableRef d, VariableRef s) {
    
        final VariableRef arrayVar = d.elementRef(d.name());
        String newArray = format("%s[] %s = new %s[%s]", d.elementTypeName(), d.name(), d.elementTypeName(), s.size());
        String mapArray;
        if (d.elementType().isPrimitive()) {
            mapArray = format("mapArray(%s, asList(%s), %s.class, mappingContext)", arrayVar, s, arrayVar.typeName());
        } else {
            mapArray = format("mapperFacade.mapAsArray(%s, asList(%s), %s, %s, mappingContext)", d.name(), s, usedType(s.elementType()),
                    usedType(d.elementType()));
        }
        statement(" %s { %s; %s; %s; } else { %s; }", s.ifNotNull(), newArray, mapArray, d.assign(arrayVar), d.assign("null"));
        
        return this;
    }
    
    public CodeSourceBuilder setToEnumeration(VariableRef d, VariableRef s) {
        
        String assignEnum = d.assign("Enum.valueOf(%s.class, \"\"+%s)", /*d.typeName(),*/ d.typeName(), s);
        statement( "%s { %s; } else { %s; }", s.ifNotNull(), assignEnum, d.assign("null"));
        
        return this;
    }
    
    public CodeSourceBuilder setObject(VariableRef d, VariableRef s, Property ip) {
        
        String mapNewObject = d.assign(format("(%s)mapperFacade.map(%s, %s, %s, mappingContext)", d.typeName(), s, usedType(s), usedType(d)));
        String mapExistingObject = format("mapperFacade.map(%s, %s, %s, %s, mappingContext)", s, d, usedType(s), usedType(d));
        String mapStmt = format(" %s { %s; } else { %s; }", d.ifNull(), mapNewObject, mapExistingObject);
        
        String ipStmt = "";
        if (ip != null) {
            VariableRef inverse = new VariableRef(ip, d);
            
            if (inverse.isCollection()) {
                ipStmt += inverse.ifNull() + inverse.assign(inverse.newCollection()) + ";";
                ipStmt += format("%s.add(%s);", inverse, d.owner());
            } else if (inverse.isArray()) {
                ipStmt += "/* TODO Orika CodeSourceBuilder.setObject does not support Arrays */";
            } else {
                ipStmt += inverse.assign(d.owner()) + ";";
            }   
        }
        
        statement("%s { %s;  %s } else { %s; }", s.ifNotNull(), mapStmt, ipStmt, d.assign("null"));  
        
        return this;
    }
    
//    public CodeSourceBuilder ifSourceNotNull(Property sp) {
//        final String typeCastGetter = getGetter(sp, "source");
//        append("if(%s != null)", typeCastGetter);
//        return this;
//    }
    
    public CodeSourceBuilder ifNotNull(VariableRef p) {
        return append(p.ifNotNull());
    }
    
    public CodeSourceBuilder ifNull(VariableRef p) {
        return append(p.ifNull());
    }
    
    public CodeSourceBuilder ifPathNotNull(VariableRef p) {
    	return append(p.ifPathNotNull());
    }
    
//    public CodeSourceBuilder avoidSourceNPE(Property sp) {
//        newLine();
//        if (sp.hasPath()) {
//            boolean first = true;
//            append("if(");
//            String expression = "source";
//            
//            for (final Property p : sp.getPath()) {
//                if (!first) {
//                    append(" && ");
//                } else {
//                    first = false;
//                }
//                expression = getGetter(p, expression);
//                append("%s != null", expression);
//            }
//            append(")");
//        }
//        return this;
//    }
    
    public CodeSourceBuilder assureInstanceExists(VariableRef pRef) {
        
        for (final VariableRef ref : pRef.getPath()) {
            
            if (!ClassUtil.isConcrete(ref.type())) {
                throw new MappingException("Abstract types are unsupported for nested properties. \n" + ref.name());
            }
            statement("if(%s == null) %s", ref, 
            		ref.assign("(%s)mapperFacade.newObject(source, %s, mappingContext)", ref.typeName(), usedType(ref)));
        }
                
        return this;
    }
    
    
//    /**
//     * Generate code setting new property when its value is null
//     * 
//     * @param property
//     *            Property
//     * @see ma.glasnost.orika.MapperFacade#newObject
//     * @return CodeSourceBuilder
//     */
//    public CodeSourceBuilder ifDestinationNull(Property property) {
//        
//        String getterExpression = "destination";
//        String setterExpression;
//        for (final Property p : property.getPath()) {
//            
//            if (!ClassUtil.isConcrete(p.getType())) {
//                throw new MappingException("Abstract types are unsupported for nested properties. \n" + property.toString());
//            }
//            setterExpression = getSetter(p, getterExpression);
//            getterExpression = getGetter(p, getterExpression);
//            
//            append("if(%s == null) ", getterExpression);
//            newLine();
//            final String value = String.format("(%s)mapperFacade.newObject(source, %s, mappingContext)", p.getType().getCanonicalName(),
//                    usedType(p));
//            append("\t" + setterExpression + ";", value);
//        }
//        return this;
//    }
    
    public CodeSourceBuilder ifInstanceOf(String expression, Type<?> sourceClass) {
        append("if(%s instanceof %s)", expression, sourceClass.getCanonicalName());
        return this;
    }
    
//    public CodeSourceBuilder setDestinationNull(Property dp) {
//        if (dp.getSetter() != null)
//            append(getSetter(dp, "destination") + ";", "null" );
//        return this;
//    }
    
//    private String getPrimitiveType(Class<?> clazz) {
//        String type = clazz.getSimpleName().toLowerCase();
//        if ("integer".equals(type)) {
//            type = "int";
//        } else if ("character".equals(type)) {
//            type = "char";
//        }
//        return type;
//    }
    
//    public CodeSourceBuilder declareVar(Class<?> clazz, String var) {
//        append("\n%s %s = %s;", clazz.getCanonicalName(), var, clazz.isPrimitive() ? getDefaultPrimitiveValue(clazz) : "null");
//        return this;
//    }
    
//    public CodeSourceBuilder assignImmutableVar(String var, Property sp) {
//        append("%s = %s;", var, getGetter(sp, "source"));
//        return this;
//    }
    
//    public CodeSourceBuilder assignStringConvertedVar(String var, Property sp) {
//        append("%s = \"\" + %s;", var, getGetter(sp, "source"));
//        return this;
//    }
    
//    public CodeSourceBuilder assignVarConvertedFromString(String var, Property sp, Property dp) {
//        append("%s = \"\" + %s;", var, getGetter(sp, "source"));
//        
//        final String getter = getGetter(sp, "source");
//        
//        newLine();
//        if (dp.getType().isPrimitive()) {
//            final String wrapperTypeName = ClassUtil.getWrapperType(dp.getRawType()).getCanonicalName();
//            append("%s = %s.valueOf(%s).%sValue();", var, wrapperTypeName, getter, getPrimitiveType(dp.getRawType()));
//        } else {
//            final String wrapperTypeName = dp.getType().getCanonicalName();
//            ifSourceNotNull(sp).then();
//            append("%s = %s.valueOf(%s);", var, wrapperTypeName, getter);
//            end();
//        }
//        
//        return this;
//    }
    
//    public CodeSourceBuilder assignObjectVar(String var, Property sp, Class<?> targetClass) {
//        String sourceType = usedType(sp);
//        String targetType = usedType(TypeFactory.valueOf(targetClass));
//        append("%s = (%s) mapperFacade.map(%s, %s, %s);", var, targetClass.getCanonicalName(), getGetter(sp, "source"), sourceType,
//                targetType);
//        return this;
//    }
    
//    public CodeSourceBuilder assignCollectionVar(String var, Property sp, Property dp) {
//        
//        String destinationCollection = "List";
//        String sourceType = usedElementType(sp);
//        String destinationType = usedElementType(dp);
//        
//        if (dp.isList()) {
//            destinationCollection = "List";
//        } else if (dp.isSet()) {
//            destinationCollection = "Set";
//        }
//        
//        final String sourceGetter = getGetter(sp, "source");
//        
//        append("%s = mapperFacade.mapAs%s(%s, %s, %s, mappingContext);", var, destinationCollection, sourceGetter, sourceType,
//                destinationType);
//        return this;
//    }
    
//    public CodeSourceBuilder assignArrayVar(String var, Property sp, Class<?> targetClass) {
//        String getter = getGetter(sp, "source");
//        final String getSizeCode = sp.getRawType().isArray() ? "length" : "size()";
//        final String castSource = sp.getRawType().isArray() ? "Object[]" : "";
//        final String sourceType = usedComponentType(sp);
//        final String targetType = usedType(TypeFactory.valueOf(targetClass));
//        
//        append("%s[] %s = new %s[%s.%s];", targetClass, var, targetClass.getCanonicalName(), getter, getSizeCode).append(
//                "mapperFacade.mapAsArray((Object[])%s, (%s)%s, %s, %s, mappingContext);", var, castSource, getter, sourceType, targetType);
//        return this;
//    }
    
//    public CodeSourceBuilder assignPrimitiveToWrapperVar(String var, Property sp, Class<?> targetClass) {
//        final String getter = getGetter(sp, "source");
//        
//        append("%s = %s.valueOf((%s) %s);\n", var, targetClass.getCanonicalName(), getPrimitiveType(targetClass), getter);
//        return this;
//    }
    
//    public CodeSourceBuilder assignWrapperToPrimitiveVar(String var, Property sp, Class<?> targetClass) {
//        String getter = getGetter(sp, "source");
//        append("%s = %s.%sValue();\n", var, getter, getPrimitiveType(targetClass));
//        return this;
//    }
    
//    public CodeSourceBuilder assignConvertedVar(String var, Property source, Class<?> targetClass, String converterId) {
//        final String getter = getGetter(source, "source");
//        converterId = getConverterId(converterId);
//        append("%s = ((%s)mapperFacade.convert(%s, %s.class, %s)); \n", var, targetClass.getCanonicalName(), getter,
//                targetClass.getCanonicalName(), converterId);
//        return this;
//        
//    }
    
//    private String getDefaultPrimitiveValue(Class<?> clazz) {
//        if (Boolean.TYPE.equals(clazz))
//            return "false";
//        else if (Character.TYPE.equals(clazz))
//            return "'\\u0000'";
//        else
//            return "0";
//    }
    
}
