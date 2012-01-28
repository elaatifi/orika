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

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Set;

import ma.glasnost.orika.MappingException;
import ma.glasnost.orika.impl.util.ClassUtil;
import ma.glasnost.orika.metadata.Property;

public class CodeSourceBuilder {
    
    private final StringBuilder out = new StringBuilder();
    private int currentIndent = 1;
    
    public CodeSourceBuilder(int indent) {
        this.currentIndent = indent;
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
        
        final String getter = getGetter(source);
        final String setter = getSetter(destination);
        final Class<?> destinationClass = destination.getType();
        converterId = getConverterId(converterId);
        
        newLine();
        // if source's type is primitive, we have to box it
        // if destination's type is primitive we have to unbox it
        String localVariableToConvert = "i_"+source.getExpression().replaceAll("\\.", "_");
        String localVariableConverted = "o_"+destination.getExpression().replaceAll("\\.", "_");
        
        if (!source.isPrimitive()) {
            ifSourceNotNull(source).then();
        }
        
        if(!source.isPrimitive())
            append("Object %s = source.%s;", localVariableToConvert, getter).newLine();
        else {
            String sourceWrapperType = ClassUtil.getWrapperType(source.getType()).getCanonicalName();
            String sourceTypeCononicalName = source.getType().getCanonicalName();
            append("Object %s = %s.valueOf((%s) source.%s);", localVariableToConvert, sourceWrapperType, sourceTypeCononicalName, getter );
        }

        append("Object %s = mapperFacade.convert(%s, %s.class, %s);", localVariableConverted, localVariableToConvert,
                destinationClass.getCanonicalName(), converterId).newLine();
        
        if(!destination.isPrimitive()) 
            append("destination.%s((%s) %s);", setter, destinationClass.getCanonicalName(), localVariableConverted);
        else {
            String convertedWrapperType = ClassUtil.getWrapperType(destination.getType()).getCanonicalName();
            append("destination.%s(((%s)%s).%sValue());", setter, convertedWrapperType, localVariableConverted, getPrimitiveType(destinationClass));
        }
        
        if (!source.isPrimitive()) {
            if (!destination.isPrimitive()) {
                elze().append("destination.%s(null);", setter);
            }
            end();
        }
        
        return this;
    }
    
    private String getConverterId(String converterId) {
        converterId = converterId == null ? "null" : ("\"" + converterId + "\"");
        return converterId;
    }
    
    public CodeSourceBuilder set(Property d, Property s) {
        final String getter = getGetter(s);
        final String setter = getSetter(d);
        return newLine().append("destination.%s(source.%s);", setter, getter);
    }
    
    public CodeSourceBuilder setCollection(Property dp, Property sp, Property ip, Class<?> dc) {
        final Class<?> destinationElementClass = dp.getParameterizedType();
        
        if (destinationElementClass == null) {
            throw new MappingException("cannot determine runtime type of destination collection " + dc.getName() + "." + dp.getName());
        }
        
        String destinationCollection = "List";
        String newStatement = "new java.util.ArrayList()";
        if (List.class.isAssignableFrom(dp.getType())) {
            destinationCollection = "List";
            newStatement = "new java.util.ArrayList()";
        } else if (Set.class.isAssignableFrom(dp.getType())) {
            destinationCollection = "Set";
            newStatement = "new java.util.HashSet()";
        }
        
        final String sourceGetter = getGetter(sp);
        final String destinationGetter = getGetter(dp);
        final String destinationSetter = getSetter(dp);
        boolean destinationHasSetter = false;
        try {
            destinationHasSetter = (dc.getMethod(destinationSetter, dp.getType()) != null);
            
        } catch (Exception e) {
            /* ignored: no destination setter available */
        }
        
        if (destinationHasSetter) {
            newLine().append("if (destination.%s == null) ", destinationGetter)
                    .begin()
                    .append("destination.%s(%s);", destinationSetter, newStatement)
                    .end();
        }
        // Start check if source property ! = null
        ifSourceNotNull(sp).then();
        
        newLine().append("destination.%s.clear();", destinationGetter);
        newLine().append("destination.%s.addAll(mapperFacade.mapAs%s(source.%s, %s.class, mappingContext));", destinationGetter,
                destinationCollection, sourceGetter, destinationElementClass.getCanonicalName());
        if (ip != null) {
            if (ip.isCollection()) {
                newLine().append("for (java.util.Iterator orikaIterator = destination.%s.iterator(); orikaIterator.hasNext();) ",
                        getGetter(dp));
                begin().append("%s orikaCollectionItem = (%s) orikaIterator.next();", dp.getParameterizedType().getCanonicalName(),
                        dp.getParameterizedType().getCanonicalName());
                newLine().append("if (orikaCollectionItem.%s == null) ", getGetter(ip));
                begin();
                if (ip.isSet()) {
                    append("orikaCollectionItem.%s(new java.util.HashSet());", getSetter(ip));
                    newLine();
                } else if (ip.isList()) {
                    append("orikaCollectionItem.%s(new java.util.ArrayList());", getSetter(ip));
                    newLine();
                } else {
                    throw new MappingException("Unsupported collection type: " + ip.getType());
                }
                end();
                append("orikaCollectionItem.%s.add(destination);", getGetter(ip));
                end();
            } else if (ip.isArray()) {
                // TODO To implement
            } else {
                newLine().append("for (java.util.Iterator orikaIterator = destination.%s.iterator(); orikaIterator.hasNext();)",
                        getGetter(dp));
                begin().append("%s orikaCollectionItem = (%s) orikaIterator.next();", dp.getParameterizedType().getCanonicalName(),
                        dp.getParameterizedType().getCanonicalName());
                newLine().append("orikaCollectionItem.%s(destination);", getSetter(ip));
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
        final String getter = getGetter(sp);
        final String setter = getSetter(dp);
        
        newLine();
        if (dp.getType().isPrimitive()) {
            final String wrapperTypeName = ClassUtil.getWrapperType(dp.getType()).getCanonicalName();
            append("destination.%s(%s.valueOf(source.%s).%sValue() );", setter, wrapperTypeName, getter, getPrimitiveType(dp.getType()));
        } else {
            final String wrapperTypeName = dp.getType().getCanonicalName();
            ifSourceNotNull(sp).then();
            append("destination.%s(%s.valueOf(source.%s));", setter, wrapperTypeName, getter);
            end();
        }
        
        return this;
        
    }
    
    public CodeSourceBuilder setToStringConversion(Property dp, Property sp) {
        final String getter = getGetter(sp);
        final String setter = getSetter(dp);
        
        newLine();
        
        if (!sp.getType().isPrimitive()) {
            ifSourceNotNull(sp).then();
            append("destination.%s(source.%s.toString());", setter, getter);
            end();
        } else {
            append("destination.%s(\"\"+source.%s);", setter, getter);
        }
        
        return this;
        
    }
    
    public CodeSourceBuilder setWrapper(Property dp, Property sp) {
        final String getter = getGetter(sp);
        final String setter = getSetter(dp);
        newLine().append("destination.%s(%s.valueOf((%s) source.%s));", setter, dp.getType().getCanonicalName(),
                getPrimitiveType(dp.getType()), getter);
        return this;
    }
    
    public CodeSourceBuilder setPrimitive(Property dp, Property sp) {
        final String getter = getGetter(sp);
        final String setter = getSetter(dp);
        
        if (!sp.getType().isPrimitive()) {
            ifSourceNotNull(sp).then();
        }
        
        newLine().append("destination.%s(source.%s.%sValue());", setter, getter, getPrimitiveType(dp.getType()));
        
        if (!sp.getType().isPrimitive()) {
            end();
        }
        
        return this;
    }
    
    public CodeSourceBuilder setArray(Property dp, Property sp) {
        final String getSizeCode = sp.getType().isArray() ? "length" : "size()";
        final String castSource = sp.getType().isArray() ? sp.getType().getCanonicalName() : "";
        final String castDestination = dp.getType().getCanonicalName();
        final String paramType = dp.getType().getComponentType().getCanonicalName();
        final String getter = getGetter(sp);
        final String setter = getSetter(dp);
        
        ifSourceNotNull(sp).then();
        
        newLine().append("%s[] %s = new %s[source.%s.%s];", paramType, dp.getName(), paramType, getter, getSizeCode);
        /*
         * newLine().append(
         * "for(int %s_i=0; %s_i< source.%s.%s; %s_i++) %s[%s_i] = (%s)(",
         * dp.getName(), dp.getName(), getter, getSizeCode, dp.getName(),
         * dp.getName(), dp.getName(), paramType);
         * if(dp.getType().getComponentType().isPrimitive() &&
         * sp.getType().getComponentType
         * ().equals(dp.getType().getComponentType())) {
         * append("source.%s[%s_i]", getter, dp.getName() ); } else { if
         * (!sp.getType().getComponentType().isPrimitive())
         * append("mapperFacade.map("
         * ).append("source.%s[%s_i], %s.class, mappingContext)", getter,
         * dp.getName(), paramType ); } append(");");
         */
        newLine();
        String convertArrayToList = "asList";
        if (dp.getType().getComponentType().isPrimitive())
            append("mapArray(%s,%s((%s)source.%s), %s.class, mappingContext);", dp.getName(), convertArrayToList, castSource, getter,
                    paramType);
        else
            append("mapperFacade.mapAsArray((%s)%s, %s((%s)source.%s), %s.class, mappingContext);", castDestination, dp.getName(),
                    convertArrayToList, castSource, getter, paramType);
        newLine().append("destination.%s(%s);", setter, dp.getName());
        
        elze().setDestinationNull(dp).end();
        
        return this;
    }
    
    public CodeSourceBuilder setToEnumeration(Property dp, Property sp) {
        final String getter = getGetter(sp);
        final String setter = getSetter(dp);
        
        ifSourceNotNull(sp).then();
        
        newLine().append("destination.%s((%s)Enum.valueOf(%s.class,\"\"+source.%s));", setter, dp.getType().getCanonicalName(),
                dp.getType().getCanonicalName(), getter);
        
        elze().setDestinationNull(dp).end();
        return this;
    }
    
    public CodeSourceBuilder setObject(Property dp, Property sp, Property ip) {
        final String sourceGetter = getGetter(sp);
        
        final String destinationGetter = getGetter(dp);
        final String destinationSetter = getSetter(dp);
        
        newLine().append("/* Start map %s:%s -> %s:%s */", sp.getExpression(), sp.getType().getSimpleName(), dp.getExpression(),
                dp.getType().getSimpleName());
        ifSourceNotNull(sp).then();
        
        newLine().append("if (destination.%s == null) ", destinationGetter);
        begin().append("destination.%s((%s)mapperFacade.map(source.%s, %s.class, mappingContext));", destinationSetter,
                dp.getType().getCanonicalName(), sourceGetter, dp.getType().getCanonicalName());
        elze();
        append("mapperFacade.map(source.%s, destination.%s, mappingContext);", sourceGetter, destinationGetter);
        end();
        if (ip != null) {
            if (ip.isCollection()) {
                append("if (destination.%s.%s == null) ", getGetter(dp), getGetter(ip));
                begin();
                if (ip.isSet()) {
                    append("destination.%s.%s(new java.util.HashSet());", getGetter(dp), getSetter(ip));
                } else if (ip.isList()) {
                    append("destination.%s.%s(new java.util.ArrayList());", getGetter(dp), getSetter(ip));
                } else {
                    throw new MappingException("Unsupported collection type: " + ip.getType());
                }
                end();
                append("destination.%s.%s.add(destination);", getGetter(dp), getGetter(ip));
            } else if (ip.isArray()) {
                // TODO To implement
                newLine().append("/* TODO Orika CodeSourceBuilder.setObject do not support Arrays */").newLine();
            } else {
                append("destination.%s.%s(destination);", getGetter(dp), getSetter(ip));
            }
        }
        
        elze().setDestinationNull(dp).end();
        newLine().append("/* End map %s -> %s */", sp.getExpression(), dp.getExpression());
        return this;
    }
    
    public CodeSourceBuilder ifSourceNotNull(Property sp) {
        newLine();
        append("if(source.%s != null)", sp.hasPath() ? getLongGetter(sp) : (sp.getGetter() + "()"));
        return this;
    }
    
    public CodeSourceBuilder avoidSourceNPE(Property sp) {
        newLine();
        if (sp.hasPath()) {
            final StringBuilder sb = new StringBuilder("source");
            int i = 0;
            append("if(");
            for (final Property p : sp.getPath()) {
                if (i != 0) {
                    append(" && ");
                }
                sb.append(".").append(p.getGetter()).append("()");
                append("%s != null", sb.toString());
                i++;
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
        final StringBuilder destinationBase = new StringBuilder("destination");
        
        for (final Property p : property.getPath()) {
            final int modifier = p.getType().getModifiers();
            if (Modifier.isAbstract(modifier) || Modifier.isInterface(modifier)) {
                throw new MappingException("Abstract types are unsupported for nested properties. \n" + property.toString());
            }
            
            append("if(").append(destinationBase.toString()).append(".").append(p.getGetter()).append("() == null) ");
            newLine();
            append(destinationBase.toString()).append(".").append(p.getSetter()).append("((").append(p.getType().getCanonicalName());
            append(")mapperFacade.newObject(").append("source, ")
                    .append(p.getType().getCanonicalName())
                    .append(".class, mappingContext));");
            
            destinationBase.append(".").append(p.getGetter()).append("()");
        }
        return this;
    }
    
    public CodeSourceBuilder ifSourceInstanceOf(Class<Object> sourceClass) {
        append("if(s instanceof %s)", sourceClass.getCanonicalName());
        return this;
    }
    
    public CodeSourceBuilder setDestinationNull(Property dp) {
        if (dp.getSetter() != null)
            append("destination.%s(null);", dp.hasPath() ? getLongSetter(dp) : (dp.getSetter()));
        return this;
    }
    
    private String getLongGetter(Property property) {
        final StringBuilder sb = new StringBuilder();
        for (final Property p : property.getPath()) {
            sb.append(".").append(p.getGetter()).append("()");
        }
        sb.append(".").append(property.getGetter()).append("()");
        return sb.substring(1);
        
    }
    
    private String getLongSetter(Property property) {
        final StringBuilder sb = new StringBuilder();
        for (final Property p : property.getPath()) {
            sb.append(".").append(p.getGetter()).append("()");
        }
        sb.append(".").append(property.getSetter());
        return sb.substring(1);
    }
    
    private String getGetter(Property property) {
        return property.hasPath() ? getLongGetter(property) : property.getGetter() + "()";
    }
    
    private String getSetter(Property property) {
        return property.hasPath() ? getLongSetter(property) : property.getSetter();
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
        append("%s = source.%s;", var, getGetter(sp));
        return this;
    }
    
    public CodeSourceBuilder assignStringConvertedVar(String var, Property sp) {
        append("%s = \"\" + source.%s;", var, getGetter(sp));
        return this;
    }
    
    public CodeSourceBuilder assignVarConvertedFromString(String var, Property sp, Property dp) {
        append("%s = \"\" + source.%s;", var, getGetter(sp));
        
        final String getter = getGetter(sp);
        
        newLine();
        if (dp.getType().isPrimitive()) {
            final String wrapperTypeName = ClassUtil.getWrapperType(dp.getType()).getCanonicalName();
            append("%s = %s.valueOf(source.%s).%sValue();", var, wrapperTypeName, getter, getPrimitiveType(dp.getType()));
        } else {
            final String wrapperTypeName = dp.getType().getCanonicalName();
            ifSourceNotNull(sp).then();
            append("%s = %s.valueOf(source.%s);", var, wrapperTypeName, getter);
            end();
        }
        
        return this;
    }
    
    //
    
    public CodeSourceBuilder assignObjectVar(String var, Property sp, Class<?> targetClass) {
        append("%s = (%s) mapperFacade.map(source.%s, %s.class);", var, targetClass.getCanonicalName(), getGetter(sp),
                targetClass.getCanonicalName());
        return this;
    }
    
    public CodeSourceBuilder assignCollectionVar(String var, Property sp, Property dp) {
        final Class<?> destinationElementClass = dp.getParameterizedType();
        String destinationCollection = "List";
        String newStatement = "new java.util.ArrayList()";
        if (List.class.isAssignableFrom(dp.getType())) {
            destinationCollection = "List";
            newStatement = "new java.util.ArrayList()";
        } else if (Set.class.isAssignableFrom(dp.getType())) {
            destinationCollection = "Set";
            newStatement = "new java.util.HashSet()";
        }
        
        final String sourceGetter = getGetter(sp);
        // final String destinationGetter = getGetter(dp);
        // final String destinationSetter = getSetter(dp);
        
        append("%s = mapperFacade.mapAs%s(source.%s, %s.class, mappingContext);", var, destinationCollection, sourceGetter,
                destinationElementClass.getCanonicalName());
        return this;
    }
    
    public CodeSourceBuilder assignArrayVar(String var, Property sp, Class<?> targetClass) {
        String getter = getGetter(sp);
        final String getSizeCode = sp.getType().isArray() ? "length" : "size()";
        final String castSource = sp.getType().isArray() ? "Object[]" : "";
        append("%s[] %s = new %s[source.%s.%s];", targetClass, var, targetClass.getCanonicalName(), getter, getSizeCode).append(
                "mapperFacade.mapAsArray((Object[])%s, (%s)source.%s, %s.class, mappingContext);", var, castSource, getter,
                targetClass.getCanonicalName());
        return this;
    }
    
    public CodeSourceBuilder assignPrimitiveToWrapperVar(String var, Property sp, Class<?> targetClass) {
        final String getter = getGetter(sp);
        
        append("%s = %s.valueOf((%s) source.%s);\n", var, targetClass.getCanonicalName(), getPrimitiveType(targetClass), getter);
        return this;
    }
    
    public CodeSourceBuilder assignWrapperToPrimitiveVar(String var, Property sp, Class<?> targetClass) {
        String getter = getGetter(sp);
        append("%s = source.%s.%sValue();\n", var, getter, getPrimitiveType(targetClass));
        return this;
    }
    
    public CodeSourceBuilder assignConvertedVar(String var, Property source, Class<?> targetClass, String converterId) {
        final String getter = getGetter(source);
        converterId = getConverterId(converterId);
        append("%s = ((%s)mapperFacade.convert(source.%s, %s.class, %s)); \n", var, targetClass.getCanonicalName(), getter,
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
