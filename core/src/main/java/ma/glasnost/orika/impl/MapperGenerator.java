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

import static ma.glasnost.orika.impl.Specifications.aCollection;
import static ma.glasnost.orika.impl.Specifications.aPrimitiveToWrapper;
import static ma.glasnost.orika.impl.Specifications.aWrapperToPrimitive;
import static ma.glasnost.orika.impl.Specifications.anArray;
import static ma.glasnost.orika.impl.Specifications.immutable;
import static ma.glasnost.orika.impl.Specifications.toAnEnumeration;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javassist.CannotCompileException;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtNewMethod;
import ma.glasnost.orika.Converter;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.MappingException;
import ma.glasnost.orika.metadata.ClassMap;
import ma.glasnost.orika.metadata.FieldMap;
import ma.glasnost.orika.metadata.MapperKey;
import ma.glasnost.orika.metadata.Property;

final class MapperGenerator {
    
    private final MapperFactory mapperFactory;
    private final ClassPool classPool;
    private final Map<ClassLoader, Boolean> mappedLoaders;
    
    public MapperGenerator(MapperFactory mapperFactory) {
        this.mapperFactory = mapperFactory;
        this.classPool = ClassPool.getDefault();
        
        classPool.insertClassPath(new ClassClassPath(this.getClass()));
        
        mappedLoaders = new ConcurrentHashMap<ClassLoader, Boolean>();
    }
    
    private void putClassLoadersIfAbsent(ClassMap<?, ?> classMap) {
        if (!mappedLoaders.containsKey(classMap.getAType().getClassLoader())) {
            mappedLoaders.put(classMap.getAType().getClassLoader(), Boolean.TRUE);
            classPool.insertClassPath(new ClassClassPath(classMap.getAType()));
        }
        
        if (!mappedLoaders.containsKey(classMap.getBType().getClassLoader())) {
            mappedLoaders.put(classMap.getBType().getClassLoader(), Boolean.TRUE);
            classPool.insertClassPath(new ClassClassPath(classMap.getBType()));
        }
    }
    
    public GeneratedMapperBase build(ClassMap<?, ?> classMap) {
        
        putClassLoadersIfAbsent(classMap);
        
        final CtClass mapperClass = classPool.makeClass(classMap.getMapperClassName());
        
        try {
            final CtClass abstractMapperClass = classPool.getCtClass(GeneratedMapperBase.class.getName());
            mapperClass.setSuperclass(abstractMapperClass);
            addGetTypeMethod(mapperClass, "getAType", classMap.getAType());
            addGetTypeMethod(mapperClass, "getBType", classMap.getBType());
            addMapMethod(mapperClass, true, classMap);
            addMapMethod(mapperClass, false, classMap);
            
            return (GeneratedMapperBase) mapperClass.toClass().newInstance();
        } catch (final Exception e) {
            throw new MappingException(e);
        }
    }
    
    private void addMapMethod(CtClass mapperClass, boolean aToB, ClassMap<?, ?> classMap) throws CannotCompileException {
        final CodeSourceBuilder out = new CodeSourceBuilder();
        final String mapMethod = "map" + (aToB ? "AtoB" : "BtoA");
        out.append("public void ")
                .append(mapMethod)
                .append("(java.lang.Object a, java.lang.Object b, %s mappingContext) {", MappingContext.class.getName());
        
        Class<?> sourceClass, destinationClass;
        if (aToB) {
            sourceClass = classMap.getAType();
            destinationClass = classMap.getBType();
        } else {
            sourceClass = classMap.getBType();
            destinationClass = classMap.getAType();
        }
        out.assertType("a", sourceClass);
        out.assertType("b", destinationClass);
        out.append("super.").append(mapMethod).append("(a,b, mappingContext);\n");
        out.append(sourceClass.getName()).append(" source = (").append(sourceClass.getName()).append(") a; \n");
        out.append(destinationClass.getName()).append(" destination = (").append(destinationClass.getName()).append(") b; \n");
        
        for (FieldMap fieldMap : classMap.getFieldsMapping()) {
            if (isAlreadyExistsInUsedMappers(fieldMap, classMap)) {
                continue;
            }
            if (!aToB) {
                fieldMap = fieldMap.flip();
            }
            if (!fieldMap.isIgnored()) {
                try {
                    generateFieldMapCode(out, fieldMap, destinationClass);
                } catch (final Exception e) {
                    throw new MappingException(e);
                }
            }
        }
        out.append("\nif(customMapper != null) customMapper.").append(mapMethod).append("(source, destination, mappingContext);\n");
        
        out.append("\n}");
        
        try {
            mapperClass.addMethod(CtNewMethod.make(out.toString(), mapperClass));
        } catch (final CannotCompileException e) {
            System.out.println("An exception occured while compiling: " + out.toString());
            e.printStackTrace();
            throw e;
        }
    }
    
    private boolean isAlreadyExistsInUsedMappers(FieldMap fieldMap, ClassMap<?, ?> classMap) {
        
        Set<ClassMap<Object, Object>> usedClassMapSet = mapperFactory.lookupUsedClassMap(new MapperKey(classMap.getAType(),
                classMap.getBType()));
        
        boolean exists = false;
        
        for (ClassMap<Object, Object> usedClassMap : usedClassMapSet) {
            if (usedClassMap.getFieldsMapping().contains(fieldMap))
                exists = true;
            break;
        }
        
        return exists;
    }
    
    private void generateFieldMapCode(CodeSourceBuilder code, FieldMap fieldMap, Class<?> destinationClass) throws Exception {
        final Property sp = fieldMap.getSource(), dp = fieldMap.getDestination();
        
        if (sp.getGetter() == null || ((dp.getSetter() == null) && !Collection.class.isAssignableFrom(dp.getType()))) {
            return;
        }
        
        if (generateConverterCode(code, fieldMap)) {
            return;
        }
        try {
            if (fieldMap.is(toAnEnumeration())) {
                code.ifSourceNotNull(sp).then().ifDestinationNull(dp).setToEnumeration(dp, sp).end();
            } else if (fieldMap.is(immutable())) {
                code.ifSourceNotNull(sp).then().ifDestinationNull(dp).set(dp, sp).end();
            } else if (fieldMap.is(anArray())) {
                code.ifSourceNotNull(sp).then().ifDestinationNull(dp).setArray(dp, sp).end();
            } else if (fieldMap.is(aCollection())) {
                code.ifSourceNotNull(sp).then().ifDestinationNull(dp).setCollection(dp, sp, fieldMap.getInverse(), destinationClass).end();
            } else if (fieldMap.is(aWrapperToPrimitive())) {
                code.ifSourceNotNull(sp).then().ifDestinationNull(dp).setPrimitive(dp, sp).end();
            } else if (fieldMap.is(aPrimitiveToWrapper())) {
                code.ifSourceNotNull(sp).then().ifDestinationNull(dp).setWrapper(dp, sp).end();
            } else { /**/
                code.ifSourceNotNull(sp).then().ifDestinationNull(dp).setObject(dp, sp, fieldMap.getInverse()).end();
            }
            
        } catch (final Exception e) {
            if (fieldMap.isConfigured()) {
                throw e;
                // elsewise ignore
            }
        }
    }
    
    private boolean generateConverterCode(final CodeSourceBuilder code, final FieldMap fieldMap) {
        final Property sp = fieldMap.getSource(), dp = fieldMap.getDestination();
        
        final Class<?> destinationClass = dp.getType();
        final Converter<?, ?> converter = mapperFactory.lookupConverter(sp.getType(), destinationClass);
        if (converter != null) {
            code.ifSourceNotNull(sp).then().ifDestinationNull(dp).convert(dp, sp).end();
            return true;
        } else {
            return false;
        }
    }
    
    private void addGetTypeMethod(CtClass mapperClass, String methodName, Class<?> value) throws CannotCompileException {
        final StringBuilder output = new StringBuilder();
        output.append("\n")
                .append("public java.lang.Class ")
                .append(methodName)
                .append("() { return ")
                .append(value.getName())
                .append(".class; }");
        mapperClass.addMethod(CtNewMethod.make(output.toString(), mapperClass));
    }
}
