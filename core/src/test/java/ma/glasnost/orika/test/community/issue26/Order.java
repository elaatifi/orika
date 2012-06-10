package ma.glasnost.orika.test.community.issue26;

public final class Order extends AbstractEntity<OrderID> {
	private static final long serialVersionUID = 1L;

	public Order() {
		this(null);
	}

	public Order(OrderID id) {
		super(id);
	}
}
