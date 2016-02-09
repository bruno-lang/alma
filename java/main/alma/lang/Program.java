package alma.lang;

import static java.lang.System.arraycopy;

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
		this.prog = desugar(prog);
	}

	/**
	 * Assignments of type "name = ..." are desugared by rewriting the src
	 * program first as just a short section has to be moved/changed that is
	 * guaranteed to fit into same space.
	 */
	private static void desugarAssignments(byte[] src) {
		// name = (
		// j      k
		// name = ....\n
		// j          k
		int i = 0;
		while (i+1 < src.length) {
			i++;
			if (isRec(src[i]) && isNoop(src[i-1]) && isNoop(src[i+1])) {
				int j = i-1;
				while (j >= 0 && isNoop(src[j])) j--;
				while (j >= 0 && isName(src[j])) j--;
				j++;
				arraycopy(src, j, src, j+2, i-j-1);
				int k = i+2;
				while (k < src.length && isNoop(src[k])) k++;
				if (isBlockStart(src[k])) {
					src[j]   = src[k];
					src[j+1] = '=';
					src[k]   = ' ';
				} else { // to end of line
					while (k < src.length-1 && src[k] != '\n') k++;
					src[j]   = '(';
					src[j+1] = '=';
					src[k]   = ')';
				}
			}
		}
	}

	/**
	 * Basic strategy: scan left to right, identify and move sections when sugar
	 * is encountered. This mostly depends on WS.
	 */
	public static byte[] desugar(byte[] src) {
		desugarAssignments(src);
		desugarLoops(src);
		return src;
	}

	private static void desugarLoops(byte[] src) {
		//FIXME one NOOP can become )( so it cannot be done just in the src array
	}

	private static int afterPrevious(char target, char inverse, byte[] prog, int pc) {
		int c = 1;
		while (c > 0 && pc > 0) {
			pc--;
			if (prog[pc] == target) { c--; }
			else if (prog[pc] == inverse) c++;
		}
		return pc+1;
	}

	private static boolean isRec(byte op) {
		return op == '=';
	}

	private static boolean isBlockEnd(byte op) {
		return op == ')' || op == ']';
	}

	private static boolean isBlockStart(byte op) {
		return op == '(' || op == '[';
	}

	private static boolean isLooping(byte op) {
		return op == '*' || op == '+' || op == '?' || op >= '1' && op <= '9' || op == '-';
	}

	private static boolean isNoop(byte op) {
		return op == ' ' || op == '\t';
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
