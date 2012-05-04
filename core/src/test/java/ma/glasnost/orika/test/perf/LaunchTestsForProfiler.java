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

package ma.glasnost.orika.test.perf;

import java.io.IOException;

import ma.glasnost.orika.OrikaSystemProperties;
import ma.glasnost.orika.test.DynamicSuite;
import ma.glasnost.orika.test.DynamicSuite.TestCasePattern;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.runner.RunWith;

/**
 * This provides a launcher for using the VisualVm profiler while running
 * all of the unit tests.<br>
 * It provides a break at the beginning pausing for input which allows
 * attaching/configuring the profiler, as well as a similar pause at the
 * end to allow for capturing/saving results.<br><br>
 * 
 * Note: you must remove the <code>@Ignore</code> annotation to use it;
 * but please don't check it in that way! 
 * 
 * @author matt.deboer@gmail.com
 *
 */ 
@RunWith(DynamicSuite.class)
@TestCasePattern(".*TestCase.class")
@Ignore
public class LaunchTestsForProfiler {
    
    @BeforeClass
    public static void setup() throws IOException {
        System.setProperty(OrikaSystemProperties.USE_STRATEGY_CACHE, ""+true);
        System.out.println("Press any key when ready to start...");
        System.in.read();
    }
    
    @AfterClass
    public static void teardown() throws IOException {
        System.out.println("Press any key when ready to quit...");
        System.in.read();
        System.in.read();
    }
}
