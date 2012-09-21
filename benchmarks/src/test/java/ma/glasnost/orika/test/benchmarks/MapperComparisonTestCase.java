package ma.glasnost.orika.test.benchmarks;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.OrikaSystemProperties;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.impl.generator.EclipseJdtCompilerStrategy;
import ma.glasnost.orika.test.benchmarks.TestClasses.One;
import ma.glasnost.orika.test.benchmarks.TestClasses.Parent;
import ma.glasnost.orika.test.benchmarks.TestClasses.Two;
import ma.glasnost.orika.test.benchmarks.util.BenchmarkAssert;
import ma.glasnost.orika.test.benchmarks.util.BenchmarkAssert.MetricType;

import org.dozer.DozerBeanMapper;
import org.dozer.Mapper;
import org.eclipse.core.internal.runtime.Product;
import org.junit.Test;

import com.google.caliper.Param;
import com.google.caliper.Result;
import com.google.caliper.ResultsReader;
import com.google.caliper.Runner;
import com.google.caliper.SimpleBenchmark;
import com.inspiresoftware.lib.dto.geda.adapter.BeanFactory;
import com.inspiresoftware.lib.dto.geda.assembler.Assembler;
import com.inspiresoftware.lib.dto.geda.assembler.DTOAssembler;

public class MapperComparisonTestCase {

	protected Result runBenchmark(Class<? extends com.google.caliper.Benchmark> benchmark) throws IOException {
		
		File targetFolder = new File(getClass().getClassLoader().getResource("").getFile()).getParentFile();
		File resultFile = new File(targetFolder, "caliper-results/results.json");
		
		String[] args = new String[]{
				benchmark.getCanonicalName(), 
				"--saveResults", resultFile.getAbsolutePath()//,
				};
		new Runner().run(args);
		
		// TODO: why can't we just directly capture the Result object?
		Result result = new ResultsReader().getResult(new FileInputStream(resultFile));
		
		return result;
	}
	
	@Test
	public void run() throws IOException {
		
		Result result = runBenchmark(Benchmark.class);
		
		double meanRatio_Orika_vs_ByHand = BenchmarkAssert.getMetricRatio(result, "DeepObjectGraphInstance", 
				"strategy", "ORIKA", "BY_HAND", MetricType.MEAN);
		
		double meanRatio_Dozer_vs_Orika = BenchmarkAssert.getMetricRatio(result, "DeepObjectGraphInstance", 
				"strategy", "DOZER", "ORIKA", MetricType.MEAN);
		
		/*
		 * Expect that Orika is less than 20 times slower than mapping by hand
		 */
		assertTrue(meanRatio_Orika_vs_ByHand < 20);
		
		// assertThat(benchmark("strategy=ORIKA"), isSlowerThan(benchmark("strategy=BY_HAND")).byMaxFactorOf(20));
		
		/*
		 * Expect that Orika is at least 2.5 times faster than Dozer
		 */
		assertTrue(meanRatio_Dozer_vs_Orika > 2.5);
		
		// assertThat(benchmark("strategy=ORIKA"), isFasterThan(benchmark("strategy=DOZER")).byMinFactorOf(5));
	}
	
	
	public enum Strategy {
		DOZER {
			private Mapper mapper = new DozerBeanMapper();
			
			@Override
			Parent map(Parent product) {
				return mapper.map(product, Parent.class);
			}
		},
		ORIKA {
			private MapperFacade facade;
			{   
				System.setProperty(OrikaSystemProperties.WRITE_CLASS_FILES, ""+false);
				System.setProperty(OrikaSystemProperties.WRITE_SOURCE_FILES, ""+false);
		        MapperFactory factory = new
		                DefaultMapperFactory.Builder().build();
		        factory.registerClassMap(factory.classMap(Parent.class,
		        		Parent.class).byDefault().toClassMap());
		        factory.registerClassMap(factory.classMap(One.class,
		        		One.class).byDefault().toClassMap());
		        factory.registerClassMap(factory.classMap(Two.class,
		        		Two.class).byDefault().toClassMap());
		        facade = factory.getMapperFacade();
			}
			
			@Override
			Parent map(Parent product) {
				return facade.map(product, Parent.class);
			}
		},
//		ORIKA_NON_CYCLIC {
//			private MapperFacade facade;
//			{   
//				System.setProperty(OrikaSystemProperties.WRITE_CLASS_FILES, ""+false);
//				System.setProperty(OrikaSystemProperties.WRITE_SOURCE_FILES, ""+false);
//		        MapperFactory factory = new
//		                DefaultMapperFactory.Builder()
//		        			.mappingContextFactory(new NonCyclicMappingContext.Factory())
//		        			.build();
//		        factory.registerClassMap(factory.classMap(Parent.class,
//		        		Parent.class).byDefault().toClassMap());
//		        factory.registerClassMap(factory.classMap(One.class,
//		        		One.class).byDefault().toClassMap());
//		        factory.registerClassMap(factory.classMap(Two.class,
//		        		Two.class).byDefault().toClassMap());
//		        facade = factory.getMapperFacade();
//			}
//			
//			@Override
//			Parent map(Parent product) {
//				return facade.map(product, Parent.class);
//			}
//		},
//		ORIKA_ECLIPSE_JDT {
//
//			private MapperFacade facade;
//			{   
//				System.setProperty(OrikaSystemProperties.WRITE_CLASS_FILES, ""+false);
//				System.setProperty(OrikaSystemProperties.WRITE_SOURCE_FILES, ""+false);
//		        MapperFactory factory = new DefaultMapperFactory.Builder()
//		        		.compilerStrategy(new EclipseJdtCompilerStrategy())
//		        		.build();
//		        
//		        factory.registerClassMap(factory.classMap(Parent.class,
//		        		Parent.class).byDefault().toClassMap());
//		        factory.registerClassMap(factory.classMap(One.class,
//		        		One.class).byDefault().toClassMap());
//		        factory.registerClassMap(factory.classMap(Two.class,
//		        		Two.class).byDefault().toClassMap());
//		        facade = factory.getMapperFacade();
//			}
//			@Override
//			Parent map(Parent product) {
//				return facade.map(product, Parent.class);
//			}
//			
//		},
		GEDA {
		    
		    final Assembler asm = DTOAssembler.newAssembler(Parent.class, Parent.class);
		    final BeanFactory beanFactory = new BeanFactory() {

                @Override
                public Object get(String key) {
                    if ("Parent".equals(key)) {
                        return new Parent();
                    } else if ("One".equals(key)) {
                        return new One();
                    } else if ("Two".equals(key)) {
                        return new Two();
                    }
                    return null;
                }
		        
		    };
		    
		    
		    Parent map(Parent product) {
		        
		        Parent result = (Parent) beanFactory.get("Parent");
		        asm.assembleDto(product, result, null, beanFactory);
		        return result;
		    }
		},
		BY_HAND {
			@Override
			Parent map(Parent parent) {
				Parent newParent = new Parent();
				newParent.availability = parent.availability;
				newParent.price = parent.price;
				newParent.productDescription = parent.productDescription;
				newParent.productName = parent.productName;
				for (One one: parent.oneList) {
					One newOne = new One();
					newOne.setName(one.name);
					newParent.oneList.add(newOne);
					for (Two two: one.twos) {
						Two newTwo = new Two();
						newTwo.setName(two.name);
						newOne.twos.add(newTwo);
					}
				}
				for (Two two: parent.twoList) {
					Two newTwo = new Two();
					newTwo.setName(two.name);
					newParent.twoList.add(newTwo);
				}
		        return newParent;
			}
		};
		
		abstract Parent map(Parent product);
	}
	
	public static class Benchmark extends SimpleBenchmark {
	
	    
	    private Parent[] objectsToMap;
	    
	    public void setUp() {
	        objectsToMap = new Parent[100];
	        for (int i =0; i < objectsToMap.length; ++i) {
	           objectsToMap[i]= TestClasses.createParentWithChildren();
	        }
	    }
	    
		@Param
		Strategy strategy;

		public int timeNewObjectInstance(int reps) {
		
		    int dummy = 0;
		    for (int i = 0; i < reps; i++) {
		        dummy += strategy.map(TestClasses.createParent()).hashCode();
		    }
		    return dummy;
		}
		
		public int timeDeepObjectGraphInstance(int reps) {
			int dummy = 0;
		    for (int i = 0; i < reps; i++) {
		        dummy += strategy.map(objectsToMap[(reps % 100)]).hashCode();
		    }
		    return dummy;
		}

	}
}
