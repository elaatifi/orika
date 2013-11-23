package ma.glasnost.orika.test.generator.eclipsejdt;

import ma.glasnost.orika.OrikaSystemProperties;
import ma.glasnost.orika.impl.generator.EclipseJdtCompilerStrategy;
import ma.glasnost.orika.test.DynamicSuite;
import ma.glasnost.orika.test.DynamicSuite.Scenario;
import ma.glasnost.orika.test.DynamicSuite.TestCasePattern;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

/**
 * This is a temporary compromise, which separates the MultiLayeredClassloader
 * test case from others test cases run under the DynamicSuite with
 * eclipse jdt compiler.
 * Some interaction between already compiled classes
 * 
 */
@RunWith(DynamicSuite.class)
@TestCasePattern(".*(SuperTypeMapping|MultiLayeredClassloader)TestCase")
@Scenario(name="eclipseJdt")
public class TestAlternateWithMultiLayeredClassLoader {
   
    @BeforeClass
    public static void eclipseJdt() {
        System.setProperty(OrikaSystemProperties.COMPILER_STRATEGY, 
                EclipseJdtCompilerStrategy.class.getCanonicalName());
    }
   
    @AfterClass
    public static void tearDown() {
        System.clearProperty(OrikaSystemProperties.COMPILER_STRATEGY);
    }

}