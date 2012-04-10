package ma.glasnost.orika.test.benchmarks;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.impl.generator.EclipseJdtCompilerStrategy;
import ma.glasnost.orika.metadata.ClassMapBuilder;

import org.dozer.DozerBeanMapper;
import org.dozer.Mapper;
import org.junit.Test;

import com.google.caliper.Param;
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

	@Test
	public void run() {
		new Runner().run(Benchmark.class.getCanonicalName());
		// TODO: need a way to capture and assert regarding benchmark result
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
		ORIKA_JAVASSIST {
			private MapperFacade facade;
			{   
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
		
		public int timeMappingSameObjectInstance(int reps) {
	
		    Product product = createProduct();
		    int dummy = 0;
		    for (int i = 0; i < reps; i++) {
		        dummy += strategy.map(product).hashCode();
		    }
		    return dummy;
		}

		public int timeMappingNewObjectInstance(int reps) {
		
		    int dummy = 0;
		    for (int i = 0; i < reps; i++) {
		        dummy += strategy.map(createProduct()).hashCode();
		    }
		    return dummy;
		}

	}
}
