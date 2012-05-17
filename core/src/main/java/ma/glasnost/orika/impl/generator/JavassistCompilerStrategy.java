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
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.Map;
import java.util.Random;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

import javassist.CannotCompileException;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtNewMethod;
import javassist.LoaderClassPath;
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
    private final static Map<Class<?>, Boolean> superClasses = new ConcurrentHashMap<Class<?>,Boolean>(3);
    
    private ClassPool classPool;
    
    /**
     * Keep a set of class-loaders that have already been added to the javassist class-pool
     * Use a WeakHashMap to avoid retaining references to child class-loaders
     */
    private WeakHashMap<ClassLoader,Boolean> referencedLoaders = new WeakHashMap<ClassLoader,Boolean>(8);
    
    /**
     */
    public JavassistCompilerStrategy() {
        super(WRITE_SOURCE_FILES_BY_DEFAULT, WRITE_CLASS_FILES_BY_DEFAULT);
        
        this.classPool = ClassPool.getDefault();
    }
    
    /**
     * Produces the requested class files for debugging purposes.
     * 
     * @throws CannotCompileException
     * @throws IOException
     */
    protected void writeClassFile(GeneratedSourceCode sourceCode, CtClass byteCodeClass) throws IOException {
    	if (writeClassFiles) {
            try {
            	File parentDir = preparePackageOutputPath(this.pathToWriteClassFiles, "");
                byteCodeClass.writeFile(parentDir.getAbsolutePath());
            } catch (CannotCompileException e) {
                throw new IllegalArgumentException(e);
            }
        }
    }
    
    /**
     * Produces the requested source file for debugging purposes.
     * 
     * @throws IOException
     */
    protected void writeSourceFile(GeneratedSourceCode sourceCode) throws IOException {
    	if (writeSourceFiles) {
        	File parentDir = preparePackageOutputPath(this.pathToWriteSourceFiles, sourceCode.getPackageName());
        	File sourceFile = new File(parentDir, sourceCode.getClassSimpleName() + ".java");
            if (!sourceFile.exists() && !sourceFile.createNewFile()) {
                throw new IOException("Could not write source file for " + sourceCode.getClassName());
            }
            FileWriter fw = new FileWriter(sourceFile);
            fw.append(sourceCode.toSourceFile());
            fw.close();
        }
    }
    
    
    
    /**
     * Attempts to register a class-loader in the maintained list of referenced
     * class-loaders. Returns true if the class-loader was registered as a 
     * result of the call; false is returned if the class-loader was already
     * registered.
     * 
     * @param cl
     * @return true if the class-loader was registered as a result of this call; false
     * if the class-loader was already registered
     */
    private boolean registerClassLoader(ClassLoader cl) {
    	Boolean found = referencedLoaders.get(cl);
    	if (found==null) {
    		synchronized(cl) {
    			found = referencedLoaders.get(cl);
    			if (found==null) {
    				referencedLoaders.put(cl,Boolean.TRUE);
    			}
    		}
    	}
    	return found==null || !found;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see ma.glasnost.orika.impl.GeneratedSourceCodeCompilerStrategy#
     * assertClassLoaderAccessible(java.lang.Class)
     */
    public void assureTypeIsAccessible(Class<?> type) throws SourceCodeGenerationException {
        
        if (!type.isPrimitive() && type.getClassLoader() != null) {
            
            if (!Modifier.isPublic(type.getModifiers())) {
                throw new SourceCodeGenerationException(type + " is not accessible");
            } else if (type.isMemberClass()) {
                /*
                 * The type needs to be publicly accessible (including it's
                 * enclosing classes if any)
                 */
                Class<?> currentType = type;
                while (currentType != null) {
                    if (!Modifier.isPublic(type.getModifiers())) {
                        throw new SourceCodeGenerationException(type + " is not accessible");
                    }
                    currentType = currentType.getEnclosingClass();
                }
            }
            
            String resourceName = type.getName();
            if (type.isArray()) {
                // Strip off the "[L" prefix from the internal name
                resourceName = resourceName.substring(2, resourceName.length()-1);
            }
            resourceName = resourceName.replace('.', '/') + ".class";
            URL resource = Thread.currentThread().getContextClassLoader().getResource(resourceName);
            if (resource == null) {
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
        CtClass byteCodeClass = null;
        int attempts = 0;
        Random rand = new Random();
        while (byteCodeClass==null) {
	        try {
	        	byteCodeClass = classPool.makeClass(className);
			} catch (RuntimeException e) {
				if (attempts < 5) {
					className = className + Integer.toHexString(rand.nextInt());
				} else {
					// No longer likely to be accidental name collision; propagate the error
					throw e;
				}
			}
    	}
        
        CtClass abstractMapperClass;
        Class<?> compiledClass;
        
        try {
        	writeSourceFile(sourceCode);
        	
        	// TODO: do we really need this check here?
            assureTypeIsAccessible(this.getClass());
            
            Boolean existing = superClasses.put(sourceCode.getSuperClass(), true);
            if (existing==null || !existing) {
                classPool.insertClassPath(new ClassClassPath(sourceCode.getSuperClass()));
            }
            
            if (registerClassLoader(Thread.currentThread().getContextClassLoader())) {
            	classPool.insertClassPath(new LoaderClassPath(Thread.currentThread().getContextClassLoader()));
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
                    throw new SourceCodeGenerationException(
                    		"An exception occured while compiling the following method:\n\n " + methodDef + 
                    		"\n\n for " + sourceCode.getClassName() + "\n", e);
                    
                }
                
            }
            compiledClass = byteCodeClass.toClass();
            
            writeClassFile(sourceCode, byteCodeClass);
            
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