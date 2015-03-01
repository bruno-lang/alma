package bruno.lang.grammar;

import java.nio.ByteBuffer;

import bruno.lang.grammar.print.Print;

public final class ParseException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	public final int determinationPosition;
	public final int errorPosition;
	public final ParseTree tree;
	private final ByteBuffer input;

	public ParseException(ByteBuffer input, int determinationPosition, int errorPosition, ParseTree tree) {
		super();
		this.input = input;
		this.determinationPosition = determinationPosition;
		this.errorPosition = errorPosition;
		this.tree = tree;
	}
	
	@Override
	public String toString() {
		int pos = Math.abs(errorPosition);
		String msg = "Failed to parse at "+pos+":";
		System.err.println(msg);
		ParseTree debug = tree.debug();
		new Print.ParseTreePrinter(System.err).print(new Parsed(input, debug));
		input.position(pos);
		//FIXME what if end of file...
		byte[] x = new byte[Math.min(60, input.limit()-pos)];
		input.get(x);
		return new String(x);
	}
}
