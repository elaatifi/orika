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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import ma.glasnost.orika.test.generator.TestAlternateCompilerStrategy;

import org.junit.AfterClass;
import org.junit.BeforeClass;
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
 * DynamicSuite resolves and runs a test suite dynamically containing all classes matched by
 * a specified pattern.<br>
 * Use the <code>@RunWith</code> annotation, specifying DyanimcSuite.class as the
 * value in order to run a test class as a dynamic suite.
 * <br><br>
 * 
 * The pattern may be customized by specifying an value with the <code>TestCasePattern</code> 
 * annotation.<br><br>
 * 
 * The tests may also be run as a "scenario" by marking the class with the 
 * <code>@Scenario</code> annotation. Running tests as a scenario will cause
 * all of the resolved test cases' methods to be suffixed with the scenario name.<br>
 * This is necessary in case you want to run these tests again, under a new "scenario",
 * since normally, JUnit attempts to avoid running the same test method more than once.
 * <br><br>
 * The JUnit 4+ <code>@BeforeClass</code> and <code>@AfterClass</code> annotations may used 
 * to define setup and tear-down methods to be performed before and after the entire suite,
 * respectively.
 * 
 * @author matt.deboer@gmail.com
 * 
 */
public class DynamicSuite extends ParentRunner<Runner> {
    
    private static final Pattern DEFAULT_TEST_CASE_PATTERN = Pattern.compile(".*TestCase.class");
    
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
     * The <code>Scenario</code> annotation is used to mark the dynamic suite with a specific name
     * that should be appended to each executed test name. This is useful in the case where you want to
     * create multiple copies of a particular dynamic suite definition, but would like to run them
     * with slightly different configuration for the entire suite (which could be achieved using
     * the <code>@BeforeClass</code> and <code>@AfterClass</code> annotations for setup/tear-down of
     * the entire suite).<br><br>
     * If the 'name' parameter is not supplied, then the class simpleName is used as a default.<br>
     * Without the unique scenario name, multiple copies of the tests resolved by the suite would 
     * not be run as JUnit avoids running the same test more than once, where uniqueness based 
     * on test name.
     * 
     * @see @@org.junit.BeforeClass, @org.junit.AfterClass
     * 
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface Scenario {
        public String name() default "";
    }
    
    /**
     * Resolves the <code>@Scenario</code> annotation if present; if found, the scenario will be
     * given a unique name suffix for all of the tests, otherwise, a default scenario is run with
     * no name suffix.
     * <br>
     * Also resolves the <code>@BeforeClass</code> and <code>@AfterClass</code> methods to be run
     * before and after the test cases resolved by the suite.
     * 
     * @param testClass the class which defines the DynamicSuite
     * @return
     */
    private static ScenarioDescriptor getScenario(TestClass testClass) {
        
        Scenario s = testClass.getJavaClass().getAnnotation(Scenario.class);
        String name = null;
        if (s!=null) {
        	name = "".equals(s.name().trim()) ? testClass.getJavaClass().getSimpleName() : s.name();
        }
        List<FrameworkMethod> beforeClass = testClass.getAnnotatedMethods(BeforeClass.class);
        List<FrameworkMethod> afterClass = testClass.getAnnotatedMethods(AfterClass.class);
        return new ScenarioDescriptor(beforeClass,afterClass,name);
    }
    
    /**
     * Resolves the test classes that are matched by the specified test pattern.
     * 
     * @param klass the root class which defines the DynamicSuite
     * @return
     */
    private static List<Class<?>> findAllTestCases(Class<?> klass) {
        try {
            Pattern testCasePattern = getTestCasePattern(klass);
            
            ClassLoader tccl = Thread.currentThread().getContextClassLoader();
            List<Class<?>> testCases = new ArrayList<Class<?>>();
            
            File classFolder = new File(TestAlternateCompilerStrategy.class.getResource("/").getFile());
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
                        currentPackage = currentDirectory.getAbsolutePath().substring(classFolderPathLength + 1);
                        currentPackage = currentPackage.replaceAll("[\\/]", ".");
                    }
                    String className = currentPackage + "." + file.getName().substring(0, file.getName().length() - ".class".length());
                    className = className.replace('\\', '.').replace('/', '.');
                    testCases.add(Class.forName(className, false, tccl));
                }
            }
            
            return testCases;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Resolves a test class pattern (regular expression) which is used to resolve
     * the names of classes that will be included in this test suite.
     * 
     * @param klass the class which defines the DynamicSuite
     * @return the compiled Pattern
     */
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
    private final String name;
    private final ScenarioDescriptor scenario;
    
    public DynamicSuite(Class<?> klass, RunnerBuilder builder) throws InitializationError {
        this(builder, klass, findAllTestCases(klass).toArray(new Class<?>[0]));
    }
    
    public DynamicSuite(RunnerBuilder builder, Class<?>[] classes) throws InitializationError {
        this(null, builder.runners(null, classes));
    }
    
    protected DynamicSuite(Class<?> klass, Class<?>[] suiteClasses) throws InitializationError {
        this(new AllDefaultPossibilitiesBuilder(true), klass, suiteClasses);
    }
    
    protected DynamicSuite(RunnerBuilder builder, Class<?> klass, Class<?>[] suiteClasses) throws InitializationError {
        this(klass, builder.runners(klass, suiteClasses));
    }
    
    protected DynamicSuite(Class<?> klass, List<Runner> runners) throws InitializationError {
        super(klass);
        try {
            this.scenario = getScenario(getTestClass());
            
            if (scenario.getName() == null) {    
            	this.fRunners = runners;
            	this.name = klass.getName();
            } else {
            	this.name = klass.getName() + "[" + scenario.getName() + "]";
            	this.fRunners = new ArrayList<Runner>(runners.size());
                for (Runner runner : runners) {
                	fRunners.add(new TestScenarioClassRunner(runner.getDescription().getTestClass(), scenario.getName()));
                }
            }
        } catch (Exception e) {
            throw new InitializationError(e);
        }
        
    }
    
        
    @Override
    protected String getName() {
        return name;
    }
    
    @Override
    protected Statement classBlock(RunNotifier notifier) {
    	
    	try {
    		List<FrameworkMethod> beforeClass = scenario.getMethodsBeforeClass();
            if (beforeClass!=null) {
            	for(FrameworkMethod method: beforeClass) {
            		method.invokeExplosively(null, new Object[0]);
            	}
            }
        } catch (Throwable e) {
            throw new RuntimeException("error invoking @BeforeClass method", e);
        }
    	
    	Statement result = childrenInvoker(notifier);
        
    	try {
    		List<FrameworkMethod> afterClass = scenario.getMethodsAfterClass();
            if (afterClass!=null) {
            	for(FrameworkMethod method: afterClass) {
            		method.invokeExplosively(null, new Object[0]);
            	}
            }
        } catch (Throwable e) {
        	throw new RuntimeException("error invoking @AfterClass method", e);
        }
        
    	return result;
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
     * ScenarioDescriptor describes the scenario to be executed by this dynamic suite;
     * particularly the name and <code>@BeforeClass</code> and <code>@AfterClass</code> methods.
     *
     */
    private static class ScenarioDescriptor {
        
        private final List<FrameworkMethod> beforeClass;
        private final List<FrameworkMethod> afterClass;
        private final String name;
        
        ScenarioDescriptor(List<FrameworkMethod> beforeClass, List<FrameworkMethod> afterClass, String name) {
            this.beforeClass = beforeClass;
            this.afterClass = afterClass;
            this.name = name;
        }
        
        public List<FrameworkMethod> getMethodsBeforeClass() {
            return beforeClass;
        }
        
        public List<FrameworkMethod> getMethodsAfterClass() {
            return afterClass;
        }
        
        public String getName() {
            return name;
        }
        
    }
     
    /**
     * Provides a unique name for each test based on appending the scenario
     * name.
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
