package ma.glasnost.orika.test.community;

import junit.framework.Assert;
import ma.glasnost.orika.CustomConverter;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.metadata.ClassMap;
import ma.glasnost.orika.metadata.ClassMapBuilder;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.test.MappingUtil;
import ma.glasnost.orika.test.community.issue26.OrderID;
import ma.glasnost.orika.test.community.issue28.Order;
import ma.glasnost.orika.test.community.issue28.OrderData;

import org.junit.Test;

public class Issue28TestCase {
	@Test
	public void testMapping() {
		MapperFactory mapperFactory = MappingUtil.getMapperFactory(true);
		mapperFactory.getConverterFactory().registerConverter(new OrderIdConverter());
		ClassMapBuilder<Order, OrderData> builder = ClassMapBuilder.map(Order.class, OrderData.class);
		ClassMap<Order, OrderData> classMap = builder.field("id", "number").byDefault().toClassMap();
		mapperFactory.registerClassMap(classMap);
		MapperFacade facade = mapperFactory.getMapperFacade();
		OrderData data = new OrderData(1234l);
		Order order = facade.map(data, Order.class);
		Assert.assertEquals(Long.valueOf(1234l), order.getId());
	}
	
	
	public static class OrderIdConverter extends CustomConverter<Long, OrderID> {

        public OrderID convert(Long source, Type<? extends OrderID> destinationType) {
            return new OrderID(source);
        }
	}
	
}
