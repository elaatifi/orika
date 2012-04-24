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

import java.util.List;
import java.util.Set;

import ma.glasnost.orika.MappingException;
import ma.glasnost.orika.impl.util.ClassUtil;
import ma.glasnost.orika.metadata.Property;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.metadata.TypeFactory;

public class CodeSourceBuilder {
    
    private final StringBuilder out = new StringBuilder();
    private int currentIndent = 1;
    private final UsedTypesContext usedTypes;
    
    public CodeSourceBuilder(int indent, UsedTypesContext usedTypes) {
        this.currentIndent = indent;
        this.usedTypes = usedTypes;
    }
    
    /**
     * Returns a fully type-cast getter for the property which has no reliance
     * on java generics.
     * 
     * @param property
     *            the Property for which to return the getter
     * @param variableExpression
     *            the String value to use for the variable on which the getter
     *            is called
     * @return
     */
    private String getGetter(final Property property, String variableExpression) {
        String var = variableExpression;
        if (property.hasPath()) {
            for (final Property p : property.getPath()) {
                var = getGetter(p, var);
            }
        }
        return "((" + property.getType().getCanonicalName() + ")" + var + "." + property.getGetter() + ")";
    }
    
    /**
     * Returns a fully type-cast setter for the property which has no reliance
     * on java generics.
     * 
     * @param property
     *            the Property for which to return the getter
     * @param variableExpression
     *            the String value to use for the variable on which the getter
     *            is called
     * @return
     */
    private String getSetter(final Property property, final String variableExpression) {
        String var = variableExpression;
        if (property.hasPath()) {
            for (final Property p : property.getPath()) {
                var = getGetter(p, var);
            }
        }
        return var + "." + property.getSetter();
        
    }
    
    private String getUsedType(Type<?> type) {
        int index = usedTypes.getUsedTypeIndex(type);
        return "usedTypes[" + index + "]";
    }
    
    private String getUsedType(Property prop) {
        return getUsedType(prop.getType());
    }
    
    private String getUsedComponentType(Property prop) {
        return getUsedType(prop.getType().getComponentType());
    }
    
    private String getUsedElementType(Property prop) {
        return getUsedType(prop.isArray() ? prop.getType().getComponentType() :prop.getElementType());
    }
    
    public CodeSourceBuilder assertType(String var, Class<?> clazz) {
        newLine();
        append("if(!(" + var + " instanceof ").append(clazz.getCanonicalName()).append(")) ");
        begin();
        append("throw new IllegalStateException(\"[" + var + "] is not an instance of " + clazz.getCanonicalName() + " \");");
        end();
        return this;
    }
    
    public CodeSourceBuilder convert(Property destination, Property source, String converterId) {
        
        final String typeCastGetter = getGetter(source, "source");
        final String typeCastSetter = getSetter(destination, "destination");
        final String sourceType = getUsedType(source);
        final String targetType = getUsedType(destination);
        final Class<?> destinationClass = destination.getRawType();
        converterId = getConverterId(converterId);
        
        String exprValue = String.format("mapperFacade.convert(%s, %s, %s, %s)", typeCastGetter, sourceType, targetType, converterId);
        
        if (destination.isPrimitive()) {
            exprValue = String.format("((%s)%s).%sValue()", ClassUtil.getWrapperType(destinationClass).getCanonicalName(), exprValue,
                    getPrimitiveType(destinationClass));
        }
        
        String value = String.format("(%s) %s", destinationClass.getCanonicalName(), exprValue);
        
        return newLine().ifSourceNotNull(source).then().append(String.format(typeCastSetter + ";", value)).newLine().end();
    }
    
    private String getConverterId(String converterId) {
        converterId = converterId == null ? "null" : ("\"" + converterId + "\"");
        return converterId;
    }
    
    public CodeSourceBuilder set(Property d, Property s) {
        final String typeCastGetter = getGetter(s, "source");
        final String typeCastSetter = getSetter(d, "destination");
        
        return newLine().append(typeCastSetter + ";", typeCastGetter);
    }
    
    public CodeSourceBuilder setCollection(Property dp, Property sp, Property ip, Type<?> destinationType) {
        
        final Class<?> dc = destinationType.getRawType();
        final Class<?> destinationElementClass = dp.getElementType().getRawType();
        
        if (destinationElementClass == null) {
            throw new MappingException("cannot determine runtime type of destination collection " + dc.getName() + "." + dp.getName());
        }
        
        String destinationCollection = "List";
        String newStatement = "new java.util.ArrayList()";
        if (List.class.isAssignableFrom(dp.getRawType())) {
            destinationCollection = "List";
            newStatement = "new java.util.ArrayList()";
        } else if (Set.class.isAssignableFrom(dp.getRawType())) {
            destinationCollection = "Set";
            newStatement = "new java.util.HashSet()";
        }
        
        final String sourceGetter = getGetter(sp, "source");
        final String destinationGetter = getGetter(dp, "destination");
        final String destinationSetter = getSetter(dp, "destination");
        
        final String sourceType = getUsedElementType(sp);
        final String destinationElementType = getUsedElementType(dp);
        
        boolean destinationHasSetter = false;
        try {
            destinationHasSetter = (dc.getMethod(dp.getSetterName(), dp.getRawType()) != null);
            
        } catch (Exception e) {
            /* ignored: no destination setter available */
        }
        
        if (destinationHasSetter) {
            newLine().append("if (%s == null) ", destinationGetter).begin().append(destinationSetter + ";", newStatement).end();
        }
        // Start check if source property ! = null
        ifSourceNotNull(sp).then();
        if(sp.isArray()) {
        	if(sp.getType().getComponentType().isPrimitive())
        		newLine().append("%s.addAll(asList(%s));", destinationGetter, sourceGetter, dp.getType().getCanonicalName());
        	else 
        		newLine().append("%s.addAll(mapperFacade.mapAsList(asList(%s), %s.class));", destinationGetter, sourceGetter, dp.getType().getCanonicalName());
        } else {
	        newLine().append("%s.clear();", destinationGetter);
	        newLine().append("%s.addAll(mapperFacade.mapAs%s(%s, %s, %s, mappingContext));", destinationGetter, destinationCollection,
	                sourceGetter, sourceType, destinationElementType);
        }
        if (ip != null) {
            final String ipGetter = getGetter(ip, "orikaCollectionItem");
            final String ipSetter = getSetter(ip, "orikaCollectionItem");
            
            if (ip.isCollection()) {
                newLine().append("for (java.util.Iterator orikaIterator = %s.iterator(); orikaIterator.hasNext();) ", destinationGetter);
                begin().append("%s orikaCollectionItem = (%s) orikaIterator.next();", dp.getElementType().getCanonicalName(),
                        dp.getElementType().getCanonicalName());
                newLine().append("if (%s == null) ", ipGetter);
                begin();
                if (ip.isSet()) {
                    append(ipSetter + ";", "new java.util.HashSet()");
                    newLine();
                } else if (ip.isList()) {
                    append(ipSetter + ";", "new java.util.ArrayList()");
                    newLine();
                } else {
                    throw new MappingException("Unsupported collection type: " + ip.getType());
                }
                end();
                append("%s.add(destination);", ipGetter);
                end();
            } else if (ip.isArray()) {
                append(" // TODO support array");
            } else {
                newLine().append("for (java.util.Iterator orikaIterator = %s.iterator(); orikaIterator.hasNext();)", destinationGetter);
                begin().append("%s orikaCollectionItem = (%s) orikaIterator.next();", dp.getElementType().getCanonicalName(),
                        dp.getElementType().getCanonicalName());
                newLine().append(ipSetter + ";", "destination");
                end();
            }
        }
        // End check if source property ! = null
        elze().setDestinationNull(dp).end();
        
        return this;
    }
    
    public CodeSourceBuilder newLine() {
        out.append("\n");
        for (int i = 0; i < currentIndent; ++i) {
            out.append("\t");
        }
        return this;
    }
    
    public CodeSourceBuilder append(String str, Object... args) {
        out.append(String.format(str, args));
        return this;
    }
    
    public CodeSourceBuilder append(String str) {
        out.append(str);
        return this;
    }
    
    public CodeSourceBuilder then() {
        append("{");
        ++currentIndent;
        return newLine();
    }
    
    public CodeSourceBuilder begin() {
        return then();
    }
    
    public CodeSourceBuilder end() {
        --currentIndent;
        newLine();
        append("}");
        return newLine();
    }
    
    public CodeSourceBuilder elze() {
        --currentIndent;
        newLine();
        append("} else {");
        ++currentIndent;
        return newLine();
    }
    
    @Override
    public String toString() {
        return out.toString();
    }
    
    public CodeSourceBuilder setFromStringConversion(Property dp, Property sp) {
        final String getter = getGetter(sp, "source");
        final String setter = getSetter(dp, "destination");
        
        newLine();
        if (dp.getType().isPrimitive()) {
            final String wrapperTypeName = ClassUtil.getWrapperType(dp.getRawType()).getCanonicalName();
            final String value = String.format("%s.valueOf(%s).%sValue()", wrapperTypeName, getter, getPrimitiveType(dp.getRawType()));
            append(setter + ";", value);
        } else {
            final String wrapperTypeName = dp.getType().getCanonicalName();
            final String value = String.format("%s.valueOf(%s)", wrapperTypeName, getter);
            ifSourceNotNull(sp).then();
            append(setter + ";", value);
            end();
        }
        
        return this;
        
    }
    
    public CodeSourceBuilder setToStringConversion(Property dp, Property sp) {
        final String getter = getGetter(sp, "source");
        final String setter = getSetter(dp, "destination");
        
        newLine();
        
        if (!sp.getType().isPrimitive()) {
            final String value = String.format("%s.toString()", getter);
            ifSourceNotNull(sp).then();
            append(setter + ";", value);
            end();
        } else {
            final String value = String.format("\"\"+%s", getter);
            append(setter + ";", value);
        }
        
        return this;
        
    }
    
    public CodeSourceBuilder setWrapper(Property dp, Property sp) {
        final String getter = getGetter(sp, "source");
        final String setter = getSetter(dp, "destination");
        final String value = String.format("%s.valueOf((%s)%s)", dp.getType().getCanonicalName(), getPrimitiveType(dp.getRawType()), getter);
        newLine().append(setter + ";", value);
        return this;
    }
    
    public CodeSourceBuilder setPrimitive(Property dp, Property sp) {
        final String getter = getGetter(sp, "source");
        final String setter = getSetter(dp, "destination");
        
        if (!sp.getType().isPrimitive()) {
            ifSourceNotNull(sp).then();
        }
        final String value = String.format("%s.%sValue()", getter, getPrimitiveType(dp.getRawType()));
        newLine().append(setter + ";", value);
        
        if (!sp.getType().isPrimitive()) {
            end();
        }
        
        return this;
    }
    
    public CodeSourceBuilder setArray(Property dp, Property sp) {
        final String getSizeCode = sp.getRawType().isArray() ? "length" : "size()";
        
        final String paramType = dp.getRawType().getComponentType().getCanonicalName();
        final String getter = getGetter(sp, "source");
        final String setter = getSetter(dp, "destination");
        final String sourceType = getUsedComponentType(sp);
        final String destinationType = getUsedComponentType(dp);
        
        ifSourceNotNull(sp).then();
        
        newLine().append("%s[] %s = new %s[%s.%s];", paramType, dp.getName(), paramType, getter, getSizeCode);
        newLine();
        String convertArrayToList = "asList";
        if (dp.getRawType().getComponentType().isPrimitive()) {
            append("mapArray(%s,%s(%s), %s.class, mappingContext);", dp.getName(), convertArrayToList, getter, paramType);
        } else {
            append("mapperFacade.mapAsArray(%s, %s(%s), %s, %s, mappingContext);", dp.getName(), convertArrayToList, getter, sourceType,
                    destinationType);
        }
        newLine().append(setter + ";", dp.getName());
        
        elze().setDestinationNull(dp).end();
        
        return this;
    }
    
    public CodeSourceBuilder setToEnumeration(Property dp, Property sp) {
        
        final String typeCastGetter = getGetter(sp, "source");
        final String typeCastSetter = getSetter(dp, "destination");
        final String expressionGetter = sp.isEnum() ? typeCastGetter + ".name()" : "\"\"+" + typeCastGetter;
        
        ifSourceNotNull(sp).then();
        
        final String value = String.format("(%s)Enum.valueOf(%s.class, %s)", dp.getType().getCanonicalName(), dp.getType()
                .getCanonicalName(), expressionGetter);
        append(typeCastSetter + ";", value);
        elze();
        setDestinationNull(dp);
        end();
        return this;
    }
    
    public CodeSourceBuilder setObject(Property dp, Property sp, Property ip) {
        
        final String spGetter = getGetter(sp, "source");
        final String spSetter = getSetter(dp, "destination");
        final String dpGetter = getGetter(dp, "destination");
        final String sourceType = getUsedType(sp);
        final String destinationType = getUsedType(dp);
        
        ifSourceNotNull(sp).then();
        
        final String value = String.format("(%s)mapperFacade.map(%s, %s, %s, mappingContext)", dp.getType().getCanonicalName(), spGetter,
                sourceType, destinationType);
        newLine().append("if (%s == null) ", dpGetter);
        begin().append(spSetter + ";", value);
        elze();
        append("mapperFacade.map(%s, %s, %s, %s, mappingContext);", spGetter, dpGetter, sourceType, destinationType);
        end();
        if (ip != null) {
            final String ipSetter = getSetter(ip, dpGetter);
            final String ipGetter = getGetter(ip, dpGetter);
            
            if (ip.isCollection()) {
                append("if (%s == null) ", ipGetter);
                begin();
                if (ip.isSet()) {
                    append(ipSetter + ";", "new java.util.HashSet()");
                } else if (ip.isList()) {
                    append(ipSetter + ";", "new java.util.ArrayList()");
                } else {
                    throw new MappingException("Unsupported collection type: " + ip.getType());
                }
                end();
                append("%s.add(destination);", ipGetter);
            } else if (ip.isArray()) {
                // TODO To implement
                newLine().append("/* TODO Orika CodeSourceBuilder.setObject does not support Arrays */").newLine();
            } else {
                append(ipSetter + ";", "destination");
            }
        }
        
        elze().setDestinationNull(dp).end();
        
        return this;
    }
    
    public CodeSourceBuilder ifSourceNotNull(Property sp) {
        final String typeCastGetter = getGetter(sp, "source");
        append("if(%s != null)", typeCastGetter);
        return this;
    }
    
    public CodeSourceBuilder avoidSourceNPE(Property sp) {
        newLine();
        if (sp.hasPath()) {
            boolean first = true;
            append("if(");
            String expression = "source";
            
            for (final Property p : sp.getPath()) {
                if (!first) {
                    append(" && ");
                } else {
                    first = false;
                }
                expression = getGetter(p, expression);
                append("%s != null", expression);
            }
            append(")");
        }
        return this;
    }
    
    /**
     * Generate code setting new property when its value is null
     * 
     * @param property
     *            Property
     * @see ma.glasnost.orika.MapperFacade#newObject
     * @return CodeSourceBuilder
     */
    public CodeSourceBuilder ifDestinationNull(Property property) {
        
        String getterExpression = "destination";
        String setterExpression;
        for (final Property p : property.getPath()) {
            
            if (!ClassUtil.isConcrete(p.getType())) {
                throw new MappingException("Abstract types are unsupported for nested properties. \n" + property.toString());
            }
            setterExpression = getSetter(p, getterExpression);
            getterExpression = getGetter(p, getterExpression);
            
            append("if(%s == null) ", getterExpression);
            newLine();
            final String value = String.format("(%s)mapperFacade.newObject(source, %s, mappingContext)", p.getType().getCanonicalName(),
                    getUsedType(p));
            append("\t" + setterExpression + ";", value);
        }
        return this;
    }
    
    public CodeSourceBuilder ifSourceInstanceOf(Type<?> sourceClass) {
        append("if(s instanceof %s)", sourceClass.getCanonicalName());
        return this;
    }
    
    public CodeSourceBuilder setDestinationNull(Property dp) {
        if (dp.getSetter() != null)
            append(getSetter(dp, "destination") + ";", "null" );
        return this;
    }
    
    private String getPrimitiveType(Class<?> clazz) {
        String type = clazz.getSimpleName().toLowerCase();
        if ("integer".equals(type)) {
            type = "int";
        } else if ("character".equals(type)) {
            type = "char";
        }
        return type;
    }
    
    public CodeSourceBuilder declareVar(Class<?> clazz, String var) {
        append("\n%s %s = %s;", clazz.getCanonicalName(), var, clazz.isPrimitive() ? getDefaultPrimtiveValue(clazz) : "null");
        return this;
    }
    
    public CodeSourceBuilder assignImmutableVar(String var, Property sp) {
        append("%s = %s;", var, getGetter(sp, "source"));
        return this;
    }
    
    public CodeSourceBuilder assignStringConvertedVar(String var, Property sp) {
        append("%s = \"\" + %s;", var, getGetter(sp, "source"));
        return this;
    }
    
    public CodeSourceBuilder assignVarConvertedFromString(String var, Property sp, Property dp) {
        append("%s = \"\" + %s;", var, getGetter(sp, "source"));
        
        final String getter = getGetter(sp, "source");
        
        newLine();
        if (dp.getType().isPrimitive()) {
            final String wrapperTypeName = ClassUtil.getWrapperType(dp.getRawType()).getCanonicalName();
            append("%s = %s.valueOf(%s).%sValue();", var, wrapperTypeName, getter, getPrimitiveType(dp.getRawType()));
        } else {
            final String wrapperTypeName = dp.getType().getCanonicalName();
            ifSourceNotNull(sp).then();
            append("%s = %s.valueOf(%s);", var, wrapperTypeName, getter);
            end();
        }
        
        return this;
    }
    
    public CodeSourceBuilder assignObjectVar(String var, Property sp, Class<?> targetClass) {
        String sourceType = getUsedType(sp);
        String targetType = getUsedType(TypeFactory.valueOf(targetClass));
        append("%s = (%s) mapperFacade.map(%s, %s, %s);", var, targetClass.getCanonicalName(), getGetter(sp, "source"), sourceType,
                targetType);
        return this;
    }
    
    public CodeSourceBuilder assignCollectionVar(String var, Property sp, Property dp) {
        
        String destinationCollection = "List";
        String sourceType = getUsedElementType(sp);
        String destinationType = getUsedElementType(dp);
        
        if (dp.isList()) {
            destinationCollection = "List";
        } else if (dp.isSet()) {
            destinationCollection = "Set";
        }
        
        final String sourceGetter = getGetter(sp, "source");
        
        append("%s = mapperFacade.mapAs%s(%s, %s, %s, mappingContext);", var, destinationCollection, sourceGetter, sourceType,
                destinationType);
        return this;
    }
    
    public CodeSourceBuilder assignArrayVar(String var, Property sp, Class<?> targetClass) {
        String getter = getGetter(sp, "source");
        final String getSizeCode = sp.getRawType().isArray() ? "length" : "size()";
        final String castSource = sp.getRawType().isArray() ? "Object[]" : "";
        final String sourceType = getUsedComponentType(sp);
        final String targetType = getUsedType(TypeFactory.valueOf(targetClass));
        
        append("%s[] %s = new %s[%s.%s];", targetClass, var, targetClass.getCanonicalName(), getter, getSizeCode).append(
                "mapperFacade.mapAsArray((Object[])%s, (%s)%s, %s, %s, mappingContext);", var, castSource, getter, sourceType, targetType);
        return this;
    }
    
    public CodeSourceBuilder assignPrimitiveToWrapperVar(String var, Property sp, Class<?> targetClass) {
        final String getter = getGetter(sp, "source");
        
        append("%s = %s.valueOf((%s) %s);\n", var, targetClass.getCanonicalName(), getPrimitiveType(targetClass), getter);
        return this;
    }
    
    public CodeSourceBuilder assignWrapperToPrimitiveVar(String var, Property sp, Class<?> targetClass) {
        String getter = getGetter(sp, "source");
        append("%s = %s.%sValue();\n", var, getter, getPrimitiveType(targetClass));
        return this;
    }
    
    public CodeSourceBuilder assignConvertedVar(String var, Property source, Class<?> targetClass, String converterId) {
        final String getter = getGetter(source, "source");
        converterId = getConverterId(converterId);
        append("%s = ((%s)mapperFacade.convert(%s, %s.class, %s)); \n", var, targetClass.getCanonicalName(), getter,
                targetClass.getCanonicalName(), converterId);
        return this;
        
    }
    
    private String getDefaultPrimtiveValue(Class<?> clazz) {
        if (Boolean.TYPE.equals(clazz))
            return "false";
        else if (Character.TYPE.equals(clazz))
            return "'\\u0000'";
        else
            return "0";
    }
    
}
