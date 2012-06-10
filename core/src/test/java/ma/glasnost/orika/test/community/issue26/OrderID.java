package ma.glasnost.orika.test.community.issue26;

public class OrderID extends AbstractOrderID {
	
	private static final long serialVersionUID = 1L;

	public OrderID() { /* Required by Orika mapping */
		this(null);
	}

	public OrderID(Long orderID) {
		super(orderID);
	}

	public OrderID(long orderID) {
		super(orderID);
	}
	
}
