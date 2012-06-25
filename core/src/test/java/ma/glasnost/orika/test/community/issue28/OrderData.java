package ma.glasnost.orika.test.community.issue28;

import java.io.Serializable;

public class OrderData implements Serializable {
	private static final long serialVersionUID = 1L;
	private Long number;

	public OrderData() {
	}

	public OrderData(Long number) {
		this.number = number;
	}

	public Long getNumber() {
		return number;
	}

	public void setNumber(Long number) {
		this.number = number;
	}
}
