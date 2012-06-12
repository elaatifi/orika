package ma.glasnost.orika.test.community.issue26;

import junit.framework.Assert;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.metadata.ClassMapBuilder;

import org.junit.Test;

public class MapperTest {
	@Test
	public void testMapping() {
		MapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();
		
		mapperFactory.registerClassMap(
				ClassMapBuilder.map(Order.class, OrderData.class)
				.field("entityID", "orderId").byDefault().toClassMap());
	
		mapperFactory.getConverterFactory().registerConverter(new OrderIDConverter());
		MapperFacade facade = mapperFactory.getMapperFacade();
		
		OrderData data = new OrderData(1234l);
		Order order = facade.map(data, Order.class);
		Assert.assertEquals(new OrderID(1234l), order.getEntityID());
	}
}
