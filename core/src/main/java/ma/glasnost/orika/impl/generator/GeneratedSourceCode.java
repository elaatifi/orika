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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javassist.CannotCompileException;
import javassist.NotFoundException;
import ma.glasnost.orika.impl.generator.CompilerStrategy.SourceCodeGenerationException;

/**
 * Acts as an intermediate to gather source code as methods are added to an
 * object which is being generated; useful for providing source code for
 * troubleshooting of generated objects as desired.
 * 
 * @author matt.deboer@gmail.com
 */
public class GeneratedSourceCode {

    /**
     * @deprecated use <code>OrikaSystemProperties.WRITE_CLASS_FILES</code> instead
     */
    @Deprecated
    public static final String PROPERTY_WRITE_CLASS_FILES = "ma.glasnost.orika.GeneratedSourceCode.writeClassFiles";

    /**
     * @deprecated use <code>OrikaSystemProperties.WRITE_SOURCE_FILES</code> instead
     */
    @Deprecated
    public static final String PROPERTY_WRITE_SOURCE_FILES = "ma.glasnost.orika.GeneratedSourceCode.writeSourceFiles";

    private StringBuilder sourceBuilder;
    private String classSimpleName;
    private String packageName;
    private String className;
    private CompilerStrategy compilerStrategy;
    private List<String> methods;
    private List<String> fields;
    private Class<?> superClass;

    /**
     * @param baseClassName
     *            The base name of the class to generated; the final name chosen
     *            may include an extra suffix for uniqueness
     * @param superClass
     *            The type of the base class to be extended by the generated
     *            class
     * @param compilerStrategy
     * 		  The strategy to use when performing the compilation
     * @throws CannotCompileException
     * @throws NotFoundException
     */
	public GeneratedSourceCode(final String baseClassName, Class<?> superClass,
	        CompilerStrategy compilerStrategy) {

		this.compilerStrategy = compilerStrategy;
		this.sourceBuilder = new StringBuilder();
		this.classSimpleName = baseClassName;
		this.superClass = superClass;

		int namePos = baseClassName.lastIndexOf(".");
		if (namePos > 0) {
			this.packageName = baseClassName.substring(0, namePos - 1);
			this.classSimpleName = baseClassName.substring(namePos + 1);
		} else {
			this.packageName = "ma.glasnost.orika.generated";
		}
		this.className = this.packageName + "." + this.classSimpleName;
		this.methods = new ArrayList<String>();
		this.fields = new ArrayList<String>();

		sourceBuilder.append("package " + packageName + ";\n\n");
		sourceBuilder.append("public class " + classSimpleName + " extends "
		        + superClass.getCanonicalName() + " {\n");
	}

    /**
     * @return the StringBuilder containing the current accumulated source.
     */
	protected StringBuilder getSourceBuilder() {
		return sourceBuilder;
	}

	public Class<?> getSuperClass() {
		return superClass;
	}

	public String getClassSimpleName() {
		return classSimpleName;
	}

	public String getPackageName() {
		return packageName;
	}

	public String getClassName() {
		return className;
	}

	List<String> getFields() {
		return fields;
	}

	List<String> getMethods() {
		return methods;
	}

    
	/**
	 * Adds a method definition to the class based on the provided source.
	 * 
	 * @param methodSource
	 */
	public void addMethod(String methodSource) {
		sourceBuilder.append("\n" + methodSource + "\n");
		this.methods.add(methodSource);
	}

	/**
	 * Adds a field definition to the class based on the provided source.
	 * 
	 * @param fieldSource
	 *            the source from which to compile the field
	 */
	public void addField(String fieldSource) {
		sourceBuilder.append("\n" + fieldSource + "\n");
		this.fields.add(fieldSource);
	}

	/**
	 * @return the completed generated java source for the class.
	 */
	public String toSourceFile() {
		return sourceBuilder.toString() + "\n}";
	}

	/**
	 * Compile and return the (generated) class; this will also cause the
	 * generated class to be detached from the class-pool, and any (optional)
	 * source and/or class files to be written.
	 * 
	 * @return the (generated) compiled class
	 * @throws CannotCompileException
	 * @throws IOException
	 */
	protected Class<?> compileClass() throws SourceCodeGenerationException {
		return compilerStrategy.compileClass(this);
	}

	/**
	 * @return a new instance of the (generated) compiled class
	 * @throws CannotCompileException
	 * @throws IOException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	@SuppressWarnings("unchecked")
	public <T> T getInstance() throws SourceCodeGenerationException,
	        InstantiationException, IllegalAccessException {

		return (T) compileClass().newInstance();
	}

}