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

package ma.glasnost.orika.impl;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Set;

import ma.glasnost.orika.MappingException;
import ma.glasnost.orika.metadata.NestedProperty;
import ma.glasnost.orika.metadata.Property;

public class CodeSourceBuilder {
    
    private final StringBuilder out = new StringBuilder();
    
    public CodeSourceBuilder assertType(String var, Class<?> clazz) {
        append("if(!(" + var + " instanceof ").append(clazz.getName()).append(
                ")) throw new IllegalStateException(\"[" + var + "] is not an instance of " + clazz.getName() + " \");\n");
        return this;
    }
    
    public CodeSourceBuilder convert(Property destination, Property source) {
        final String getter = getGetter(source);
        final String setter = getSetter(destination);
        final Class<?> destinationClass = destination.getType();
        append("destination.%s((%s)mapperFacade.convert(source.%s, %s.class)); \n", setter, destinationClass.getName(), getter,
                destinationClass.getName());
        return this;
        
    }
    
    public CodeSourceBuilder set(Property d, Property s) {
        final String getter = getGetter(s);
        final String setter = getSetter(d);
        
        append("destination.%s(source.%s);", setter, getter);
        
        return this;
    }
    
    public CodeSourceBuilder setCollection(Property dp, Property sp, Property ip) {
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
        final String destinationGetter = getGetter(dp);
        final String destinationSetter = getSetter(dp);
        
        append("if (destination.%s == null) {\n", destinationGetter);
        append("destination.%s(%s);\n", destinationSetter, newStatement);
        append("}\n");
        append("destination.%s.clear();\n", destinationGetter);
        append("destination.%s.addAll(mapperFacade.mapAs%s(source.%s, %s.class, mappingContext));", destinationGetter,
                destinationCollection, sourceGetter, destinationElementClass.getName());
        if (ip != null) {
            if (ip.isCollection()) {
                append("for (java.util.Iterator orikaIterator = destination.%s.iterator(); orikaIterator.hasNext();) {\n", getGetter(dp));
                append("%s orikaCollectionItem = (%s) orikaIterator.next();\n", dp.getParameterizedType().getName(), dp
                        .getParameterizedType().getName());
                append("if (orikaCollectionItem.%s == null) {\n", getGetter(ip));
                if (ip.isSet()) {
                    append("orikaCollectionItem.%s(new java.util.HashSet());\n", getSetter(ip));
                } else if (ip.isList()) {
                    append("orikaCollectionItem.%s(new java.util.ArrayList());\n", getSetter(ip));
                } else {
                    throw new MappingException("Unsupported collection type: " + ip.getType());
                }
                append("}\n");
                append("orikaCollectionItem.%s.add(destination);\n", getGetter(ip));
                append("}\n");
            } else if (ip.isArray()) {
                // TODO To implement
            } else {
                append("for (java.util.Iterator orikaIterator = destination.%s.iterator(); orikaIterator.hasNext();) {\n", getGetter(dp));
                append("%s orikaCollectionItem = (%s) orikaIterator.next();\n", dp.getParameterizedType().getName(), dp
                        .getParameterizedType().getName());
                append("orikaCollectionItem.%s(destination);\n", getSetter(ip));
                append("}\n");
            }
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
        return this;
    }
    
    public CodeSourceBuilder end() {
        append("}\n");
        return this;
    }
    
    @Override
    public String toString() {
        return out.toString();
    }
    
    public CodeSourceBuilder setWrapper(Property dp, Property sp) {
        final String getter = getGetter(sp);
        final String setter = getSetter(dp);
        
        append("destination.%s(%s.valueOf((%s) source.%s));\n", setter, dp.getType().getName(), getPrimitiveType(dp.getType()), getter);
        return this;
    }
    
    public CodeSourceBuilder setPrimitive(Property dp, Property sp) {
        final String getter = getGetter(sp);
        final String setter = getSetter(dp);
        
        append("destination.%s(source.%s.%sValue());\n", setter, getter, getPrimitiveType(dp.getType()));
        return this;
    }
    
    public CodeSourceBuilder setArray(Property dp, Property sp) {
        final String getSizeCode = sp.getType().isArray() ? "length" : "size()";
        final String castSource = sp.getType().isArray() ? "Object[]" : "";
        final String paramType = dp.getType().getComponentType().getName();
        final String getter = getGetter(sp);
        final String setter = getSetter(dp);
        
        append("%s[] %s = new %s[source.%s.%s];", paramType, dp.getName(), paramType, getter, getSizeCode).append(
                "mapperFacade.mapAsArray((Object[])%s, (%s)source.%s, %s.class, mappingContext);", dp.getName(), castSource, getter,
                paramType).append("destination.%s(%s);", setter, dp.getName());
        
        return this;
    }
    
    public CodeSourceBuilder setObject(Property dp, Property sp, Property ip) {
        final String sourceGetter = getGetter(sp);
        
        final String destinationGetter = getGetter(dp);
        final String destinationSetter = getSetter(dp);
        append("if (destination.%s == null) {\n", destinationGetter);
        append("destination.%s((%s)mapperFacade.map(source.%s, %s.class, mappingContext));\n", destinationSetter, dp.getType().getName(),
                sourceGetter, dp.getType().getName());
        append("} else {\n");
        append("mapperFacade.map(source.%s, destination.%s, mappingContext);\n", sourceGetter, destinationGetter);
        append("}\n");
        if (ip != null) {
            if (ip.isCollection()) {
                append("if (destination.%s.%s == null) {\n", getGetter(dp), getGetter(ip));
                if (ip.isSet()) {
                    append("destination.%s.%s(new java.util.HashSet());\n", getGetter(dp), getSetter(ip));
                } else if (ip.isList()) {
                    append("destination.%s.%s(new java.util.ArrayList());\n", getGetter(dp), getSetter(ip));
                } else {
                    throw new MappingException("Unsupported collection type: " + ip.getType());
                }
                append("}\n");
                
                append("destination.%s.%s.add(destination);\n", getGetter(dp), getGetter(ip));
            } else if (ip.isArray()) {
                // TODO To implement
            } else {
                append("destination.%s.%s(destination);\n", getGetter(dp), getSetter(ip));
            }
        }
        
        return this;
    }
    
    public CodeSourceBuilder ifSourceNotNull(Property sp) {
        
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
            if (!sp.isPrimitive()) {
                append(" && source.").append(getLongGetter((NestedProperty) sp)).append(" != null");
            }
            append(")");
        } else if (!sp.isPrimitive()) {
            append("if(source.%s() != null)", sp.getGetter());
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
        if (!property.hasPath()) {
            return this;
        }
        
        final StringBuilder destinationBase = new StringBuilder("destination");
        
        for (final Property p : property.getPath()) {
            final int modifier = p.getType().getModifiers();
            if (Modifier.isAbstract(modifier) || Modifier.isInterface(modifier)) {
                throw new MappingException("Abstract types are unsupported for nested properties. \n" + property.toString());
            }
            
            append("if(").append(destinationBase.toString()).append(".").append(p.getGetter()).append("() == null)");
            append(destinationBase.toString()).append(".").append(p.getSetter()).append("((").append(p.getType().getName());
            append(")mapperFacade.newObject(").append("source, ").append(p.getType().getName()).append(".class));");
            
            destinationBase.append(".").append(p.getGetter()).append("()");
        }
        return this;
    }
    
    private String getLongGetter(NestedProperty property) {
        final StringBuilder sb = new StringBuilder();
        for (final Property p : property.getPath()) {
            sb.append(".").append(p.getGetter()).append("()");
        }
        sb.append(".").append(property.getGetter()).append("()");
        return sb.substring(1);
        
    }
    
    private String getLongSetter(NestedProperty property) {
        final StringBuilder sb = new StringBuilder();
        for (final Property p : property.getPath()) {
            sb.append(".").append(p.getGetter()).append("()");
        }
        sb.append(".").append(property.getSetter());
        return sb.substring(1);
    }
    
    private String getGetter(Property property) {
        return property.hasPath() ? getLongGetter((NestedProperty) property) : property.getGetter() + "()";
    }
    
    private String getSetter(Property property) {
        return property.hasPath() ? getLongSetter((NestedProperty) property) : property.getSetter();
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
}
