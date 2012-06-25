package ma.glasnost.orika.test.community.issue28;

public class Order extends AbstractOrder<Order> {
	private static final long serialVersionUID = 1L;
	private int customerNumber;

	public Order() {
		this(null);
	}

	public Order(Long id) {
		super(id);
	}

	public int getCustomerNumber() {
		return customerNumber;
	}

	public void setCustomerNumber(int customerNumber) {
		this.customerNumber = customerNumber;
	}
}
