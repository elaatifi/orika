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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtNewMethod;
import javassist.NotFoundException;

/**
 * Acts as an intermediate to gather source code as methods are added
 * to an object which is being generated; useful for providing source code
 * for troubleshooting of generated objects as desired.
 * 
 * @author matt.deboer@gmail.com
 */
public class GeneratedSourceCode {
	
	/**
	 * Set this system property to "true" to cause class files to be written for automatically generated classes
	 */
	public static final String PROPERTY_WRITE_CLASS_FILES = "ma.glasnost.orika.GeneratedSourceCode.writeClassFiles";
	
	/**
	 * Set this system property to "true" to cause source files to be written for automatically generated classes
	 */
	public static final String PROPERTY_WRITE_SOURCE_FILES = "ma.glasnost.orika.GeneratedSourceCode.writeSourceFiles";
	
	private CtClass byteCodeClass;
	private StringBuilder sourceBuilder;
	private Class<?> compiledClass;
	private final boolean writeClassFiles;
	private final boolean writeSourceFiles;
	private String className;
	private String packageName;
	private boolean detached = false;
	
	/**
	 * @param className The name of the class to generated
	 * @param classPool The shared class pool from which to obtain/create object definitions
	 * @param superClass The type of the base class to be extended by the generated class
	 * @throws CannotCompileException
	 * @throws NotFoundException
	 */
	public GeneratedSourceCode(final String className, final ClassPool classPool, Class<?> superClass) throws CannotCompileException, NotFoundException {
		this.byteCodeClass = classPool.makeClass(className);
    	final CtClass abstractMapperClass = classPool.getCtClass(superClass.getName());
    	this.sourceBuilder = new StringBuilder();
    	this.className = className;
    	
    	int namePos = className.lastIndexOf(".");
    	if (namePos > 0) {
    		this.packageName = className.substring(0,namePos-1);
    		this.className = className.substring(namePos+1);
    	} else {
    		this.packageName = "ma.glasnost.orika.generated";
    	}
    	
    	byteCodeClass.setSuperclass(abstractMapperClass);
    	
    	sourceBuilder.append("package " + packageName + ";\n\n");
    	sourceBuilder.append("public class " + className + " extends " + superClass.getName() + " {\n");
    	
        writeClassFiles = Boolean.valueOf(System.getProperty(PROPERTY_WRITE_CLASS_FILES));
        writeSourceFiles = Boolean.valueOf(System.getProperty(PROPERTY_WRITE_SOURCE_FILES));
	}
	
	/**
	 * @return the Javassist CtClass representing the byte code class definition.
	 */
	protected CtClass getByteCodeClass() {
		return byteCodeClass;
	}
	
	/**
	 * @return the StringBuilder containing the current accumulated source.
	 */
	protected StringBuilder getSourceBuilder() {
		return sourceBuilder;
	}
	
	/**
	 * Adds a method definition to the class based on the provided source.
	 * 
	 * @param methodSource
	 * @throws CannotCompileException
	 */
	public void addMethod(String methodSource) throws CannotCompileException {
		assertNotDetached();
		sourceBuilder.append("\n" + methodSource + "\n");
		byteCodeClass.addMethod(CtNewMethod.make(methodSource, byteCodeClass));
	}
	
	/**
	 * Adds a field definition to the class based on the provided source.
	 * 
	 * @param fieldSource the source from which to compile the field
	 * @throws CannotCompileException
	 */
	public void addField(String fieldSource) throws CannotCompileException {
		assertNotDetached();
		sourceBuilder.append("\n" + fieldSource + "\n");
		byteCodeClass.addField(CtField.make(fieldSource, byteCodeClass));
	}
	
	/**
	 * Confirms that the byte code class has not been detached
	 */
	private void assertNotDetached() {
		if (detached) {
			throw new IllegalStateException("modification cannot be performed after instantiation");
		}
	}
	
	/**
	 * @return the completed generated java source for the class.
	 */
	public String toSourceFile() {
		return sourceBuilder.toString() + "\n}";
	}
	
	/**
	 * Produces the requested source and/or class files for debugging purposes.
	 * 
	 * @throws CannotCompileException
	 * @throws IOException
	 */
	private void writeFiles() throws CannotCompileException, IOException {
		
		if (writeClassFiles || writeSourceFiles) {
			String path = getClass().getResource("/").getFile().toString()
	        		+ packageName.replaceAll("\\.", "/") + "/";
			File parentDir = new File(path);
			if (!parentDir.exists() && !parentDir.mkdirs()) {
				throw new IOException("Could not write source file for " + packageName + "." + className);
			}
			
			if (writeClassFiles) {    
				byteCodeClass.writeFile(path);
			}
			
			if (writeSourceFiles) {
				File sourceFile = new File(parentDir, className + ".java");
	            if (!sourceFile.exists() && !sourceFile.createNewFile()) {
	            	throw new IOException("Could not write source file for " + packageName + "." + className);
	            }
	            FileWriter fw = new FileWriter(sourceFile);
	            fw.append(toSourceFile());
	            fw.close();
	        }
		}
	}
	
	/**
	 * Compile and return the (generated) class; this will also cause the generated
	 * class to be detached from the class-pool, and any (optional) source and/or
	 * class files to be written.
	 * 
	 * @return the (generated) compiled class
	 * @throws CannotCompileException
	 * @throws IOException
	 */
	private Class<?> compiledClass() throws CannotCompileException, IOException {
		if (compiledClass==null) {
			compiledClass = (Class<?>)byteCodeClass.toClass();
	        writeFiles();
			byteCodeClass.detach();
			this.detached = true; // No more modifications allowed
		}
		return compiledClass;
	}
	
	/**
	 * @return a new instance of the (generated) compiled class
	 * @throws CannotCompileException
	 * @throws IOException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	@SuppressWarnings("unchecked")
	public <T> T getInstance() throws CannotCompileException, IOException, InstantiationException, IllegalAccessException {
		
        return (T)compiledClass().newInstance();
	}
	
}