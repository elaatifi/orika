package ma.glasnost.orika.test.community.issue26;

import ma.glasnost.orika.CustomConverter;
import ma.glasnost.orika.metadata.Type;

public class OrderIDConverter extends CustomConverter<Long, OrderID> {

	/* (non-Javadoc)
	 * @see ma.glasnost.orika.Converter#convert(java.lang.Object, ma.glasnost.orika.metadata.Type)
	 */
	public OrderID convert(Long source, Type<? extends OrderID> destinationType) {
		return new OrderID(source);
	}
}
