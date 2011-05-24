package ma.glasnost.orika.impl;

import java.util.List;
import java.util.Set;

import ma.glasnost.orika.metadata.NestedProperty;
import ma.glasnost.orika.metadata.Property;

public class CodeSourceBuilder {
    
    private final StringBuilder out = new StringBuilder();
    
    public CodeSourceBuilder assertType(String var, Class<?> clazz) {
        append("if(!(" + var + " instanceof ").append(clazz.getName()).append(
                ")) throw new IllegalStateException(\"[" + var + "] is not an instance of " + clazz.getName() + " \");\n");
        return this;
    }
    
    public CodeSourceBuilder set(Property d, Property s) {
        if (d.hasPath()) {
            append("/*One way for nested properties skipping mapping */\n");
            return this;
        }
        String getter = getGetter(s);
        append("destination.%s(source.%s);", d.getSetter(), getter);
        return this;
    }
    
    CodeSourceBuilder set(Property d, String s) {
        append("destination.%s(%s);", d.getSetter(), s);
        return this;
    }
    
    public CodeSourceBuilder setCollection(Property dp, Property sp) {
        Class<?> destinationElementClass = dp.getParameterizedType();
        String destinationCollection = "List";
        if (List.class.isAssignableFrom(dp.getType())) {
            destinationCollection = "List";
        } else if (Set.class.isAssignableFrom(dp.getType())) {
            destinationCollection = "Set";
        }
        
        String getter = getGetter(sp);
        append("destination.%s(mapperFacade.mapAs%s(source.%s, %s.class, mappingContext));", dp.getSetter(), destinationCollection, getter,
                destinationElementClass.getName());
        
        return this;
    }
    
    private String getGetter(Property sp) {
        return sp.hasPath() ? ((NestedProperty) sp).getLongGetter() : sp.getGetter() + "()";
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
    
    // TODO add support nested properties
    public CodeSourceBuilder setWrapper(Property dp, Property sp) {
        String getter = getGetter(sp);
        append("destination.%s(%s.valueOf((%s) source.%s));\n", dp.getSetter(), dp.getType().getName(), getType(dp.getType()), getter);
        return this;
    }
    
    private String getType(Class<?> clazz) {
        String type = clazz.getSimpleName().toLowerCase();
        if ("integer".equals(type)) {
            type = "int";
        } else if ("character".equals(type)) {
            type = "char";
        }
        return type;
    }
    
    // TODO add support nested properties
    public CodeSourceBuilder setPrimitive(Property dp, Property sp) {
        String getter = getGetter(sp);
        append("destination.%s(source.%s.%sValue());\n", dp.getSetter(), getter, getType(dp.getType()));
        return this;
    }
    
    // TODO add support nested properties
    public CodeSourceBuilder setArray(Property dp, Property sp) {
        String getSizeCode = sp.getType().isArray() ? "length" : "size()";
        String castSource = sp.getType().isArray() ? "Object[]" : "";
        String paramType = dp.getType().getComponentType().getName();
        
        String getter = getGetter(sp);
        
        append("%s[] %s = new %s[source.%s.%s];", paramType, dp.getName(), paramType, getter, getSizeCode).append(
                "mapperFacade.mapAsArray((Object[])%s, (%s)source.%s, %s.class, mappingContext);", dp.getName(), castSource, getter,
                paramType).set(dp, dp.getName());
        
        return this;
    }
    
    public CodeSourceBuilder setObject(Property dp, Property sp) {
        String getter = getGetter(sp);
        append("destination.%s((%s)mapperFacade.map(source.%s, %s.class, mappingContext));\n", dp.getSetter(), dp.getType().getName(),
                getter, dp.getType().getName());
        return this;
    }
    
    // TODO add support nested properties
    public CodeSourceBuilder ifSourceNotNull(Property sp) {
        
        /*
         * in the case of nested properties ex. source.getBar().getFoo()
         * if(source.getBar() != null && source.getBar().getFoo() != null)
         */
        if (sp.hasPath()) {
            StringBuilder sb = new StringBuilder("source");
            int i = 0;
            append("if(");
            for (Property p : sp.getPath()) {
                if (i != 0) {
                    append(" && ");
                }
                sb.append(".").append(p.getGetter()).append("()");
                append("%s != null", sb.toString());
                i++;
            }
            if (!sp.isPrimitive()) {
                append(" && source.").append(((NestedProperty) sp).getLongGetter()).append(" != null");
            }
            append(")");
        } else if (!sp.isPrimitive()) {
            append("if(source.%s() != null)", sp.getGetter());
        }
        
        return this;
    }
    
    public CodeSourceBuilder convert(Property destination, Property source) {
        String getter = getGetter(source);
        String setter = destination.getSetter();
        Class<?> destinationClass = destination.getType();
        append("destination.%s((%s)mapperFacade.convert(source.%s, %s.class)); \n", setter, destinationClass.getName(), getter,
                destinationClass.getName());
        return this;
        
    }
}
