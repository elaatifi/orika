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

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javassist.CannotCompileException;
import ma.glasnost.orika.impl.generator.eclipsejdt.CompilationUnit;
import ma.glasnost.orika.impl.generator.eclipsejdt.CompilerRequestor;
import ma.glasnost.orika.impl.generator.eclipsejdt.NameEnvironment;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.TextEdit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Uses Eclipse JDT to format and compile the source for the specified
 * GeneratedSourceCode objects.<br><br>
 * 
 * By default, this compiler strategy writes formatted source files relative to the current
 * class path root.
 * 
 * @author matt.deboer@gmail.com
 */
public class EclipseJdtCompilerStrategy extends CompilerStrategy {

    private final static Logger LOG = LoggerFactory.getLogger(EclipseJdtCompilerStrategy.class);

    private static final String JAVA_COMPILER_SOURCE_VERSION = "1.5";
    private static final String JAVA_COMPILER_COMPLIANCE_VERSION = "1.5";
    private static final String JAVA_COMPILER_CODEGEN_TARGET_PLATFORM_VERSION = "1.5";
    private static final String JAVA_SOURCE_ENCODING = "UTF-8";
    
    private static final String WRITE_SOURCE_FILES_BY_DEFAULT = "true";
    private static final String WRITE_CLASS_FILES_BY_DEFAULT = "false";

    private final ByteCodeClassLoader byteCodeClassLoader;
    private final CodeFormatter formatter;
    private final INameEnvironment compilerNameEnvironment;
    private final CompilerRequestor compilerRequester;
    private final Compiler compiler;
    
    
   
	public EclipseJdtCompilerStrategy() {
		super(WRITE_SOURCE_FILES_BY_DEFAULT, WRITE_CLASS_FILES_BY_DEFAULT);

		this.byteCodeClassLoader = new ByteCodeClassLoader(getClass()
		        .getClassLoader());
		this.formatter = ToolFactory
		        .createCodeFormatter(getFormattingOptions());
		this.compilerNameEnvironment = new NameEnvironment(
		        this.byteCodeClassLoader);
		this.compilerRequester = new CompilerRequestor();
		this.compiler = new Compiler(
		        compilerNameEnvironment,
		        DefaultErrorHandlingPolicies.proceedWithAllProblems(),
		        getCompilerOptions(),
		        compilerRequester,
		        new DefaultProblemFactory(Locale.getDefault()));
	}

    /**
     * Return the options to be passed when creating {@link CodeFormatter}
     * instance.
     * 
     * @return
     */
	private Map<Object, Object> getFormattingOptions() {

		@SuppressWarnings("unchecked")
		Map<Object, Object> options = DefaultCodeFormatterConstants
		        .getEclipseDefaultSettings();
		options.put(JavaCore.COMPILER_SOURCE, JAVA_COMPILER_SOURCE_VERSION);
		options.put(JavaCore.COMPILER_COMPLIANCE, JAVA_COMPILER_COMPLIANCE_VERSION);
		options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM,
		        JAVA_COMPILER_CODEGEN_TARGET_PLATFORM_VERSION);
		return options;
	}

	private CompilerOptions getCompilerOptions() {

		Map<Object, Object> options = new HashMap<Object, Object>();

		options.put(CompilerOptions.OPTION_LocalVariableAttribute, CompilerOptions.GENERATE);
		options.put(CompilerOptions.OPTION_LineNumberAttribute, CompilerOptions.GENERATE);
		options.put(CompilerOptions.OPTION_SourceFileAttribute, CompilerOptions.GENERATE);

		options.put(CompilerOptions.OPTION_SuppressWarnings, CompilerOptions.ENABLED);

		options.put(CompilerOptions.OPTION_Source, JAVA_COMPILER_SOURCE_VERSION);
		options.put(CompilerOptions.OPTION_TargetPlatform, JAVA_COMPILER_CODEGEN_TARGET_PLATFORM_VERSION);
		options.put(CompilerOptions.OPTION_Encoding, JAVA_SOURCE_ENCODING);
		options.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.IGNORE);

		// Ignore unchecked types and raw types
		options.put(JavaCore.COMPILER_PB_UNCHECKED_TYPE_OPERATION, CompilerOptions.IGNORE);
		options.put(JavaCore.COMPILER_PB_RAW_TYPE_REFERENCE, CompilerOptions.IGNORE);

		return new CompilerOptions(options);
	}

    /**
     * Format the source code using the Eclipse text formatter
     */
	private String formatSource(String code) {

		String lineSeparator = "\n";

		TextEdit te = formatter.format(CodeFormatter.K_COMPILATION_UNIT, code,
		        0, code.length(), 0, lineSeparator);
		if (te == null) {
			throw new IllegalArgumentException(
			        "source code was unable to be formatted; \n"
			                + "//--- BEGIN ---\n" + code + "\n//--- END ---");
		}

		IDocument doc = new Document(code);
		try {
			te.apply(doc);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		String formattedCode = doc.get();

		return formattedCode;
	}

    /**
     * Produces the requested source and/or class files for debugging purposes.
     * 
     * @throws CannotCompileException
     * @throws IOException
     */
	protected void writeSourceFile(String sourceText, String packageName,
	        String className) throws IOException {

		File parentDir = preparePackageOutputPath(this.pathToWriteSourceFiles, packageName);

		File outputSourceFile = new File(parentDir, className + ".java");
		if (!outputSourceFile.exists() && !outputSourceFile.createNewFile()) {
			throw new IOException("Could not write source file for "
			        + packageName + "." + className);
		}

		FileWriter fw = new FileWriter(outputSourceFile);
		fw.append(sourceText);
		fw.close();

    }
    
	protected void writeClassFile(String packageName, String simpleClassName,
	        byte[] data) throws IOException {
		
		File parentDir = preparePackageOutputPath(this.pathToWriteClassFiles, packageName);

		File outputSourceFile = new File(parentDir, simpleClassName + ".class");
		if (!outputSourceFile.exists() && !outputSourceFile.createNewFile()) {
			throw new IOException("Could not write class file for "
			        + packageName + "." + simpleClassName);
		}

		FileOutputStream fout = new FileOutputStream(outputSourceFile);
		fout.write(data);
		fout.close();
	}
    

    public void assureTypeIsAccessible(Class<?> type)
	    throws SourceCodeGenerationException {
	
		if (!type.isPrimitive() && type.getClassLoader() != null) {

			String resourceName = type.getName().replace('.', '/') + ".class";
			if (type.isArray()) {
				// Strip off the "[L" prefix from the internal name
				resourceName = resourceName.substring(2);
			}
			InputStream is = byteCodeClassLoader
			        .getResourceAsStream(resourceName);
			if (is == null) {
				throw new SourceCodeGenerationException(type
				        + " is not accessible");
			}
		}

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
    public Class<?> compileClass(GeneratedSourceCode sourceCode)
	    throws SourceCodeGenerationException {

		Class<?> compiledClass = null;
		String sourceText = formatSource(sourceCode.toSourceFile());
		String packageName = sourceCode.getPackageName();
		String classSimpleName = sourceCode.getClassSimpleName();
		String className = sourceCode.getClassName();
		byte[] data = null;
		try {

			// Write source file before compilation in case of failure
			if (writeSourceFiles) {
				writeSourceFile(sourceText, packageName, classSimpleName);
			}

			Map<String, byte[]> compiledClasses = compile(sourceText,
			        packageName, classSimpleName, Thread.currentThread()
			                .getContextClassLoader());

			data = compiledClasses.get(className);

			if (writeClassFiles) {
				writeClassFile(packageName, classSimpleName, data);
			}

			byteCodeClassLoader.putClassData(className, data);

		} catch (IOException e) {
			throw new RuntimeException("Failed to write files for " + className, e);
		}

		try {
			compiledClass = byteCodeClassLoader.loadClass(className);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}

		return compiledClass;
    }

    private Map<String, byte[]> compile(String source, String packageName,
    		String className, ClassLoader classLoader) {

	
		CompilationUnit unit = new CompilationUnit(source, packageName,
		        className);

		Map<String, byte[]> compiledClasses = null;

		synchronized (compiler) {
			compilerRequester.reset();
			compiler.compile(new ICompilationUnit[] { unit });

			if (compilerRequester.getProblems() != null) {
				StringBuilder warningText = new StringBuilder();
				StringBuilder errorText = new StringBuilder();
				boolean hasErrors = false;
				for (IProblem p : compilerRequester.getProblems()) {
					if (p.isError()) {
						hasErrors = true;
						errorText.append("ERROR: " + p.toString() + "\n\n");
					} else {
						warningText.append("WARNING: " + p.toString() + "\n\n");
					}
				}
				if (hasErrors) {
					throw new RuntimeException(
					        "Compilation encountered errors:\n"
					                + errorText.toString() + "\n\n"
					                + warningText.toString());
				} else {
					LOG.warn("Compiler warnings:" + warningText.toString());
				}
			}
			compiledClasses = compilerRequester.getCompiledClassFiles();
		}
		return compiledClasses;
    }

}