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

package ma.glasnost.orika.test;

import java.io.File;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import ma.glasnost.orika.test.generator.TestAlternateCompilerStrategy;

import org.junit.internal.builders.AllDefaultPossibilitiesBuilder;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;

/**
 * Resolves and runs a test suite dynamically
 * containing all classes matched by the specified pattern.<br>
 * <br>
 * 
 * The pattern may be customized by specifying an alternate value with the
 * <code>TestCasePattern</code> annotation.<br><br>
 * 
 * The tests may also be run with different custom scenarios, such that
 * a public static method marked with <code>@Scenario</code> in the test suite class
 * will be executed once before each test in the suite, and each test
 * method will be marked with that scenario's name.<br>
 * Multiple such scenario methods may be defined this way, and a copy of all 
 * resolved test classes will be executed for each (after applying the
 * scenario method).
 * 
 * @author matt.deboer@gmail.com
 * 
 */
public class DynamicSuite extends ParentRunner<Runner> {

    private static final Pattern DEFAULT_TEST_CASE_PATTERN = Pattern
	    .compile(".*TestCase.class");

    /**
     * The <code>TestCasePattern</code> annotation specifies the pattern from
     * which test case classes should be matched.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface TestCasePattern {
	public String value();
    }

    /**
     * The <code>TestCasePattern</code> annotation specifies the pattern from
     * which test case classes should be matched.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface Scenario {
	public String value() default "";
    }

    private static Collection<ScenarioDescriptor> getScenarios(TestClass testClass) throws Exception {
	
	List<FrameworkMethod> methods = testClass.getAnnotatedMethods(Scenario.class);
	Map<String,ScenarioDescriptor> scenarios = new HashMap<String,ScenarioDescriptor>(methods.size());
	
	for (FrameworkMethod method : methods) {
	    int modifiers = method.getMethod().getModifiers();
	    if (!Modifier.isStatic(modifiers) || !Modifier.isPublic(modifiers)) {
		throw new Exception(
			"@Scenario can only be applied to public static method on class "
				+ testClass.getName());
	    } else {
		Scenario annotation = method.getAnnotation(Scenario.class);
		String name = annotation.value();
		if (name.isEmpty()) {
		    name = method.getName();
		}
		if (scenarios.containsKey(name)) {
		    throw new Exception(
				"@Scenario methods must have unique names/'name' attributes for class "
					+ testClass.getName());
		} else {
		    scenarios.put(name,new ScenarioDescriptor(method,name));
		}
	    }
	}
	return scenarios.values();
    }

    private static List<Class<?>> findAllTestCases(Class<?> klass) {
	try {
	    Pattern testCasePattern = getTestCasePattern(klass);

	    ClassLoader tccl = Thread.currentThread().getContextClassLoader();
	    List<Class<?>> testCases = new ArrayList<Class<?>>();

	    File classFolder = new File(
		    TestAlternateCompilerStrategy.class
			    .getResource("/").getFile());
	    int classFolderPathLength = classFolder.getAbsolutePath().length();

	    LinkedList<File> stack = new LinkedList<File>();
	    stack.addAll(Arrays.asList(classFolder.listFiles()));
	    File currentDirectory = classFolder;
	    String currentPackage = "";
	    while (!stack.isEmpty()) {

		File file = stack.removeFirst();
		if (file.isDirectory()) {
		    // push
		    stack.addAll(Arrays.asList(file.listFiles()));
		} else if (testCasePattern.matcher(file.getName()).matches()) {
		    if (!currentDirectory.equals(file.getParentFile())) {
			currentDirectory = file.getParentFile();
			currentPackage = currentDirectory.getAbsolutePath()
				.substring(classFolderPathLength + 1);
			currentPackage = currentPackage
				.replaceAll("[\\/]", ".");
		    }
		    String className = currentPackage
			    + "."
			    + file.getName()
				    .substring(
					    0,
					    file.getName().length()
						    - ".class".length());
		    testCases.add(Class.forName(className, false, tccl));
		}
	    }

	    return testCases;
	} catch (Exception e) {
	    throw new RuntimeException(e);
	}
    }

    private static Pattern getTestCasePattern(Class<?> klass) {

	Pattern pattern = DEFAULT_TEST_CASE_PATTERN;
	TestCasePattern annotation = klass.getAnnotation(TestCasePattern.class);
	if (annotation != null) {
	    pattern = Pattern.compile(annotation.value());
	}
	return pattern;
    }

    
    // =============================================================================
    
    
    private final List<Runner> fRunners;

    public DynamicSuite(Class<?> klass, RunnerBuilder builder)
	    throws InitializationError {
	this(builder, klass, findAllTestCases(klass).toArray(new Class<?>[0]));
    }

    public DynamicSuite(RunnerBuilder builder, Class<?>[] classes)
	    throws InitializationError {
	this(null, builder.runners(null, classes));
    }

    protected DynamicSuite(Class<?> klass, Class<?>[] suiteClasses)
	    throws InitializationError {
	this(new AllDefaultPossibilitiesBuilder(true), klass, suiteClasses);
    }

    protected DynamicSuite(RunnerBuilder builder, Class<?> klass,
	    Class<?>[] suiteClasses) throws InitializationError {
	this(klass, builder.runners(klass, suiteClasses));
    }

    protected DynamicSuite(Class<?> klass, List<Runner> runners)
	    throws InitializationError {
	super(klass);
	try {
	    Collection<ScenarioDescriptor> scenarios = getScenarios(getTestClass());
	    if (scenarios!=null && !scenarios.isEmpty()) {
		
		fRunners = new ArrayList<Runner>(scenarios.size());
		
		for(ScenarioDescriptor scenario: scenarios) {
		    ArrayList<Runner> clonedRunners = new ArrayList<Runner>(runners.size());
		    for (Runner runner: runners) {
			clonedRunners.add(new TestScenarioClassRunner(runner.getDescription().getTestClass(),scenario.getName()));
		    }
		    fRunners.add(new TestScenarioRunner(klass,scenario,clonedRunners));
		}
		
	    } else {
	        fRunners = runners;
	    }
	} catch (Exception e) {
	    throw new InitializationError(e);
	}
	    
    }

    @Override
    protected List<Runner> getChildren() {
	return fRunners;
    }

    @Override
    protected Description describeChild(Runner child) {
	return child.getDescription();
    }

    @Override
    protected void runChild(Runner runner, final RunNotifier notifier) {
	runner.run(notifier);
    }

    /**
     * 
     *
     */
    private static class ScenarioDescriptor {
	
	private final FrameworkMethod method;
	private final String name;
	
	ScenarioDescriptor(FrameworkMethod method,String name) {
	    this.method = method;
	    this.name = name;
	}
	
	public FrameworkMethod getMethod() {
	    return method;
	}
	public String getName() {
	    return name;
	}
	
    } 
    
    /**
     * Runs a scenario by first executing the specified scenario method,
     * then running the associated tests
     *
     */
    private static class TestScenarioRunner extends ParentRunner<Runner> {

	private final ScenarioDescriptor scenario;
	private final List<Runner> children;
	private final String name;
	
	public TestScenarioRunner(Class<?> klass, ScenarioDescriptor scenario, List<Runner> children) throws InitializationError {
	    super(klass);
	    this.scenario = scenario;
	    this.children = children;
	    this.name = "@Scenario( " + scenario.getName() + " )";
	} 

	@Override
	protected String getName() {
	    return name;
	}


	@Override
	protected Statement classBlock(RunNotifier notifier) {
	    try {
		this.scenario.getMethod().invokeExplosively(null, new Object[0]);
	    } catch (Throwable e) {
		throw new RuntimeException("problem invoking scenario "
			+ scenario.getName(), e);
	    }
	    return childrenInvoker(notifier);
	}

	@Override
	protected List<Runner> getChildren() {
	    return children;
	}
	
	@Override
	protected Description describeChild(Runner child) {
	    return child.getDescription();
	}

	@Override
	protected void runChild(Runner child, RunNotifier notifier) {
	    child.run(notifier);
	}
	
    }
    
    /**
     * Provides a unique name for each test based on appending the scenario name.
     * 
     * @author matt.deboer@gmail.com
     *
     */
    private static class TestScenarioClassRunner extends BlockJUnit4ClassRunner {
	
	private final String scenarioName;
	
	public TestScenarioClassRunner(Class<?> klass, String scenarioName) throws InitializationError {
	    super(klass);
	    this.scenarioName = scenarioName;
	}
	
	@Override
	protected String testName(final FrameworkMethod method) {
		return String.format("%s[%s]", method.getName(), scenarioName);
	}
	
    }

}
