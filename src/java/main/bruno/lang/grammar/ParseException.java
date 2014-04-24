package bruno.lang.grammar;

public final class ParseException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	public final int determinationPosition;
	public final int errorPosition;

	public ParseException(int determinationPosition, int errorPosition) {
		super();
		this.determinationPosition = determinationPosition;
		this.errorPosition = errorPosition;
	}
	
}
