/*
 * Orika - simpler, better and faster Java bean mapping
 * 
 * Copyright (C) 2011 Orika authors
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ma.glasnost.orika.impl.generator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import javassist.CannotCompileException;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtNewMethod;
import javassist.NotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Uses Javassist to generate compiled class for the passed GeneratedSourceCode
 * object.<br>
 * <br>
 * 
 * By default this compiler strategy writes no source or class files.
 * 
 * @author matt.deboer@gmail.com
 */
public class JavassistCompilerStrategy extends CompilerStrategy {
    
    private static final String WRITE_SOURCE_FILES_BY_DEFAULT = "false";
    private static final String WRITE_CLASS_FILES_BY_DEFAULT = "false";
    
    private final static Logger LOG = LoggerFactory.getLogger(JavassistCompilerStrategy.class);
    
    private ClassPool classPool;
    
    /**
     */
    public JavassistCompilerStrategy() {
        super(WRITE_SOURCE_FILES_BY_DEFAULT, WRITE_CLASS_FILES_BY_DEFAULT);
        
        this.classPool = ClassPool.getDefault();
    }
    
    /**
     * Produces the requested source and/or class files for debugging purposes.
     * 
     * @throws CannotCompileException
     * @throws IOException
     */
    protected void writeFiles(GeneratedSourceCode sourceCode, CtClass byteCodeClass) throws IOException {
        
        if (writeClassFiles || writeSourceFiles) {
            
            String path = getClass().getResource("/").getFile().toString() + sourceCode.getPackageName().replaceAll("\\.", "/") + "/";
            File parentDir = new File(path);
            if (!parentDir.exists() && !parentDir.mkdirs()) {
                throw new IOException("Could not write source file for " + sourceCode.getClassName());
            }
            
            if (writeClassFiles) {
                try {
                    byteCodeClass.writeFile(getClass().getResource("/").getFile().toString());
                } catch (CannotCompileException e) {
                    throw new IllegalArgumentException(e);
                }
            }
            
            if (writeSourceFiles) {
                File sourceFile = new File(parentDir, sourceCode.getClassSimpleName() + ".java");
                if (!sourceFile.exists() && !sourceFile.createNewFile()) {
                    throw new IOException("Could not write source file for " + sourceCode.getClassName());
                }
                FileWriter fw = new FileWriter(sourceFile);
                fw.append(sourceCode.toSourceFile());
                fw.close();
            }
        }
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see ma.glasnost.orika.impl.GeneratedSourceCodeCompilerStrategy#
     * assertClassLoaderAccessible(java.lang.Class)
     */
    public void assureTypeIsAccessible(Class<?> type) throws SourceCodeGenerationException {
        // ClassLoader loader = type.getClassLoader();
        // if (loader != null && !mappedLoaders.containsKey(loader)) {
        // mappedLoaders.put(loader, Boolean.TRUE);
        // classPool.insertClassPath(new ClassClassPath(type));
        // }
        // try {
        // CtNewMethod.make("public void test(" + type.getCanonicalName() +
        // " t) { }", methodTestClass);
        // } catch (CannotCompileException e) {
        // throw new SourceCodeGenerationException("Type " + type +
        // " is not accessible",e);
        // }
        
        if (!type.isPrimitive() && type.getClassLoader() != null) {
            
            String resourceName = type.getName().replace('.', '/') + ".class";
            if (type.isArray()) {
                // Strip off the "[L" prefix from the internal name
                resourceName = resourceName.substring(2);
            }
            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName);
            if (is == null) {
                throw new SourceCodeGenerationException(type + " is not accessible");
            }
        }
        
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * ma.glasnost.orika.impl.GeneratedSourceCodeCompilerStrategy#compileClass
     * (ma.glasnost.orika.impl.GeneratedSourceCode)
     */
    public Class<?> compileClass(GeneratedSourceCode sourceCode) throws SourceCodeGenerationException {
        
        String className = sourceCode.getClassName();
        
        CtClass byteCodeClass = classPool.makeClass(className);
        
        CtClass abstractMapperClass;
        Class<?> compiledClass;
        
        try {
            assureTypeIsAccessible(this.getClass());
            
            if (classPool.find(sourceCode.getSuperClass().getCanonicalName()) == null) {
                classPool.insertClassPath(new ClassClassPath(sourceCode.getSuperClass()));
            }
            
            abstractMapperClass = classPool.getCtClass(sourceCode.getSuperClass().getCanonicalName());
            byteCodeClass.setSuperclass(abstractMapperClass);
            
            for (String fieldDef : sourceCode.getFields()) {
                try {
                    byteCodeClass.addField(CtField.make(fieldDef, byteCodeClass));
                } catch (CannotCompileException e) {
                    LOG.error("An exception occured while compiling: " + fieldDef + " for " + sourceCode.getClassName(), e);
                    throw e;
                }
            }
            
            for (String methodDef : sourceCode.getMethods()) {
                try {
                    byteCodeClass.addMethod(CtNewMethod.make(methodDef, byteCodeClass));
                } catch (CannotCompileException e) {
                    LOG.error("An exception occured while compiling: " + methodDef + " for " + sourceCode.getClassName(), e);
                    throw e;
                }
                
            }
            compiledClass = byteCodeClass.toClass();
            
            writeFiles(sourceCode, byteCodeClass);
            
        } catch (NotFoundException e) {
            throw new SourceCodeGenerationException(e);
        } catch (CannotCompileException e) {
            throw new SourceCodeGenerationException(e);
        } catch (IOException e) {
            throw new SourceCodeGenerationException("Could not write files for " + sourceCode.getClassName(), e);
        }
        
        return compiledClass;
    }
    
}