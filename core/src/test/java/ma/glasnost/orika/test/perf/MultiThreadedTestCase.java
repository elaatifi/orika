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

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Assert;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.metadata.TypeFactory;
import ma.glasnost.orika.test.ConcurrentRule;
import ma.glasnost.orika.test.ConcurrentRule.Concurrent;
import ma.glasnost.orika.test.DynamicSuite;

import org.junit.Rule;
import org.junit.Test;

/**
 * @author matt.deboer@gmail.com
 *
 */
public class MultiThreadedTestCase {

	
	/**
	 * Allows us to run methods concurrently by marking with <code>@Concurrent</code>;
	 * note that in the current implementation, such methods will have the
	 * <code>@Before</code> and <code>@After</code> methods also invoked concurrently.
	 */
	@Rule
	public ConcurrentRule concurrentRule = new ConcurrentRule();
	
	private volatile Set<Class<?>> classes = new HashSet<Class<?>>(getClassesList());
	
	private static List<Class<?>> getClassesList() {
		File classFolder;
		try {
			classFolder = new File(URLDecoder.decode(MultiThreadedTestCase.class.getResource("/")
					.getFile(), "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		return DynamicSuite.findTestCases(classFolder, ".*");
	}
	
	private final AtomicInteger threadIndex = new AtomicInteger(0);
	private Type<?>[] typeResults = new Type<?>[15];
	private CountDownLatch finishLine = new CountDownLatch(15);
	
	@Test
	@Concurrent(15)
	public void testDefineSingleTypeSimultaneously() throws InterruptedException {
		
		int myIndex = threadIndex.getAndAdd(1);
		typeResults[myIndex] = TypeFactory.valueOf(Integer.class);
		
		finishLine.countDown();

		finishLine.await();
		
		Type<?> firstType = typeResults[0];
		for (Type<?> type: typeResults) {
			Assert.assertEquals(firstType, type);
		}
	}
	
	/**
	 * Verifies that multiple threads requesting the type for the same set of classes receive
	 * the same set of values over a large number of classes.
	 */
	@Test
	@Concurrent(15)
	public void testDefineTypesSimultaneously() {
		
		Set<Type<?>> types = new HashSet<Type<?>>();
		for (Class<?> aClass: classes) {
			Type<?> aType = TypeFactory.valueOf(aClass);
			boolean exists = types.add(aType);
			if (exists) {
				throw new IllegalStateException("type already exists for " + aClass + ": " + aType);
			}
		}
		
		Assert.assertEquals(classes.size(), types.size());
	}
	
}
