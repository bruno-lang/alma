package alma.lang;

public class Program {

	private byte[] prog;

	public static Program compile(String prog) {
		return compile(prog.getBytes());
	}

	public static Program compile(byte[] prog) {
		return new Program(prog);
	}

	private Program(byte[] prog) {
		super();
		this.prog = prog;
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
}
