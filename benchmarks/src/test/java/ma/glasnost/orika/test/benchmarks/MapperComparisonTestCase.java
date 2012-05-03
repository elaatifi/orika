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
import ma.glasnost.orika.metadata.ClassMapBuilder;
import ma.glasnost.orika.test.benchmarks.util.BenchmarkAssert;
import ma.glasnost.orika.test.benchmarks.util.BenchmarkAssert.MetricType;

import org.dozer.DozerBeanMapper;
import org.dozer.Mapper;
import org.junit.Test;

import com.google.caliper.Param;
import com.google.caliper.Result;
import com.google.caliper.ResultsReader;
import com.google.caliper.Runner;
import com.google.caliper.SimpleBenchmark;

public class MapperComparisonTestCase {
    
	public static class Product {
		private String productName;
	    private String productDescription;
	    private Double price;
	    private Boolean availability;
		public String getProductName() {
			return productName;
		}
		public void setProductName(String productName) {
			this.productName = productName;
		}
		public String getProductDescription() {
			return productDescription;
		}
		public void setProductDescription(String productDescription) {
			this.productDescription = productDescription;
		}
		public Double getPrice() {
			return price;
		}
		public void setPrice(Double price) {
			this.price = price;
		}
		public Boolean getAvailability() {
			return availability;
		}
		public void setAvailability(Boolean availability) {
			this.availability = availability;
		} 
	}
	
	
	public static class ProductDto {
		private String productName;
	    private String productDescription;
	    private Double price;
	    private Boolean availability;
		public String getProductName() {
			return productName;
		}
		public void setProductName(String productName) {
			this.productName = productName;
		}
		public String getProductDescription() {
			return productDescription;
		}
		public void setProductDescription(String productDescription) {
			this.productDescription = productDescription;
		}
		public Double getPrice() {
			return price;
		}
		public void setPrice(Double price) {
			this.price = price;
		}
		public Boolean getAvailability() {
			return availability;
		}
		public void setAvailability(Boolean availability) {
			this.availability = availability;
		}   
	}

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
		
		double meanRatio_Orika_vs_ByHand = BenchmarkAssert.getMetricRatio(result, "SameObjectInstance", 
				"strategy", "ORIKA", "BY_HAND", MetricType.MEAN);
		
		double meanRatio_Dozer_vs_Orika = BenchmarkAssert.getMetricRatio(result, "SameObjectInstance", 
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
	
	private static Product createProduct() {
		Product product = new Product();
	    product.setAvailability(true);
	    product.setPrice(123d);
	    product.setProductDescription("desc");
	    product.setProductName("name");
	    
	    return product;
	}
	
	public enum Strategy {
		DOZER {
			private Mapper mapper = new DozerBeanMapper();
			
			@Override
			ProductDto map(Product product) {
				return mapper.map(product, ProductDto.class);
			}
		},
		ORIKA {
			private MapperFacade facade;
			{   
				System.setProperty(OrikaSystemProperties.WRITE_CLASS_FILES, ""+false);
				System.setProperty(OrikaSystemProperties.WRITE_SOURCE_FILES, ""+false);
		        MapperFactory mapperFactory = new
		                DefaultMapperFactory.Builder().build();
		        mapperFactory.registerClassMap(ClassMapBuilder.map(Product.class,
		                ProductDto.class).byDefault().toClassMap());
		        mapperFactory.build();
		        facade = mapperFactory.getMapperFacade();
			}
			@Override
			ProductDto map(Product product) {
				return facade.map(product, ProductDto.class);
			}
		},
		ORIKA_ECLIPSE_JDT {

			private MapperFacade facade;
			{   
				System.setProperty(OrikaSystemProperties.WRITE_CLASS_FILES, ""+false);
				System.setProperty(OrikaSystemProperties.WRITE_SOURCE_FILES, ""+false);
		        MapperFactory mapperFactory = new DefaultMapperFactory.Builder()
		        		.compilerStrategy(new EclipseJdtCompilerStrategy())
		        		.build();
		        
		        mapperFactory.registerClassMap(ClassMapBuilder.map(Product.class,
		                ProductDto.class).byDefault().toClassMap());
		        mapperFactory.build();
		        facade = mapperFactory.getMapperFacade();
			}
			@Override
			ProductDto map(Product product) {
				return facade.map(product, ProductDto.class);
			}
			
		},
		BY_HAND {
			@Override
			ProductDto map(Product product) {
				ProductDto dto = new ProductDto();
		        dto.setAvailability(product.getAvailability());
		        dto.setProductDescription(product.getProductDescription());
		        dto.setPrice(product.getPrice());
		        dto.setProductName(product.getProductName());
		        return dto;
			}
		};
		
		abstract ProductDto map(Product product);
	}
	
	public static class Benchmark extends SimpleBenchmark {
	
		@Param
		Strategy strategy;
		
		public int timeSameObjectInstance(int reps) {
	
		    Product product = createProduct();
		    int dummy = 0;
		    for (int i = 0; i < reps; i++) {
		        dummy += strategy.map(product).hashCode();
		    }
		    return dummy;
		}

		public int timeNewObjectInstance(int reps) {
		
		    int dummy = 0;
		    for (int i = 0; i < reps; i++) {
		        dummy += strategy.map(createProduct()).hashCode();
		    }
		    return dummy;
		}

	}
}
