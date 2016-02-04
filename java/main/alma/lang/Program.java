package alma.lang;

public final class Program {

	private byte[] prog;

	public static Program compile(String prog) {
		return compile(prog.getBytes());
	}

	public static Program compile(byte[] prog) {
		return new Program(prog);
	}

	private Program(byte[] prog) {
		super();
		this.prog = compile0(prog);
	}

	public byte[] compile0(byte[] prog) {
		// times to blocks
		// subst hex literals
		// subst lit-switch
		// = into blocks
		// resolve references

		return prog;
	}

	private static boolean isLooping(byte op) {
		return op == '*' || op == '+' || op == '?' || op >= '1' && op <= '9';
	}

	private static boolean isWhitspace(byte op) {
		return op == ' ' || op == '\t' || op == '\n';
	}

	private static boolean isName(byte op) {
		return op >= 'a' && op <= 'z' || op >= 'A' && op <= 'Z' || op == '-';
	}

	public int parse(String data) {
		return parse(data.getBytes());
	}

	public int parse(byte[] data) {
		return parse(data, new ParseTree(data.length/2));
	}

	public int parse(byte[] data, ParseTree tree) {
		Parser parser = new Parser(prog, data, tree);
		int end = parser.parse();
		return end;
	}

	@Override
	public String toString() {
		return new String(prog);
	}
}
