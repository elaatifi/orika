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
	
	public CtClass getByteCodeClass() {
		return byteCodeClass;
	}
	public void setByteCodeClass(CtClass byteCodeClass) {
		this.byteCodeClass = byteCodeClass;
	}
	public StringBuilder getSourceBuilder() {
		return sourceBuilder;
	}
	
	public void addMethod(String methodSource) throws CannotCompileException {
		sourceBuilder.append("\n" + methodSource + "\n");
		byteCodeClass.addMethod(CtNewMethod.make(methodSource, byteCodeClass));
	}
	
	public String toSourceFile() {
		return sourceBuilder.toString() + "\n}";
	}
	
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
	 * @return the (generated) compiled class
	 * @throws CannotCompileException
	 * @throws IOException
	 */
	private Class<?> getCompiledClass() throws CannotCompileException, IOException {
		if (compiledClass==null) {
			compiledClass = (Class<?>)byteCodeClass.toClass();
	        writeFiles();
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
		
        return (T)getCompiledClass().newInstance();
	}
	
}