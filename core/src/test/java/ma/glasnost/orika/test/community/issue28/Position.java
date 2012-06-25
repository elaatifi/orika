package ma.glasnost.orika.test.community.issue28;

public class Position<T extends PositionContainer> {
	private T order;

	public T getOrder() {
		return order;
	}

	public void setOrder(T order) {
		this.order = order;
	}
}
