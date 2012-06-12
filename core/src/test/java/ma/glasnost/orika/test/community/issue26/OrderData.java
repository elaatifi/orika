package ma.glasnost.orika.test.community.issue26;

import java.io.Serializable;

public class OrderData implements Serializable {
	private static final long serialVersionUID = 1L;
	private Long orderId;

	public OrderData() {
	}

	public OrderData(Long Id) {
		this.orderId = Id;
	}

	public Long getOrderId() {
		return orderId;
	}

	public void setOrderId(Long Id) {
		this.orderId = Id;
	}
}
