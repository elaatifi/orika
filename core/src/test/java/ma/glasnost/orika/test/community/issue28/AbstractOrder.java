package ma.glasnost.orika.test.community.issue28;

public abstract class AbstractOrder<T extends AbstractOrder<T>> extends PositionContainer<T> {
	private static final long serialVersionUID = 3L;

	private Long number;

	public AbstractOrder() {
		this(null);
	}

	public AbstractOrder(Long number) {
		this.number = number;
	}

	public Long getId() {
		return number;
	}
}