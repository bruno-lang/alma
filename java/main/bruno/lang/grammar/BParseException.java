package bruno.lang.grammar;

import java.nio.ByteBuffer;

public class BParseException extends RuntimeException {

	public final int determinationPosition;
	public final int errorPosition;
	public final BParseTree tree;
	private final ByteBuffer input;

	public BParseException(ByteBuffer input, int determinationPosition, int errorPosition, BParseTree tree) {
		super();
		this.input = input;
		this.determinationPosition = determinationPosition;
		this.errorPosition = errorPosition;
		this.tree = tree;
	}
	
	@Override
	public String toString() {
		return "TODO";
	}
}
