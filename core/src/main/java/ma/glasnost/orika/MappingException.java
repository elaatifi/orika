package ma.glasnost.orika;

public class MappingException extends RuntimeException {

	private static final long serialVersionUID = -1485137975363692382L;

	public MappingException(Throwable e) {
		super(e);
	}

	public MappingException(String message) {
		super(message);
	}
}
