package alma.lang;

import static java.lang.Math.max;
import static java.lang.System.arraycopy;

import java.util.Arrays;

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
				int k = i+2;
				while (k < src.length && isNoop(src[k])) k++;
				if (isBlockStart(src[k])) { // name_=_(...) => _(=name_ 
					arraycopy(src, j, src, j+3, i-j-1);
					src[j]   = ' ';
					src[j+1] = src[k];
					src[j+2] = '=';
					src[k]   = ' ';
				} else { // name_=_...\n => (=name_...)
					arraycopy(src, j, src, j+2, i-j-1);
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
		src = desugarLoops(src);
		return src;
	}

	private static byte[] cleanup(byte[] src) {
		byte[] dest = new byte[src.length];
		int si = 0;
		int di = 0;
		while (si < src.length) {
			byte op = src[si++];
			dest[di++] = op;
			if (isNoop(op)) {
				di--;
			}
		}
		return Arrays.copyOf(dest, di);
	}

	private static byte[] desugarLoops(byte[] src) {
		byte[] dest = new byte[src.length+max(16, src.length/5)];
		int di = 0;
		int si = 0;
		while (si < src.length) {
			byte op = src[si];
			if (isComment(op)) {
				si++;
				while (!isComment(src[si]))
				op = src[si];
			}
			dest[di] = op;
			if (di > 0 && isNoop(op) && isLooping(dest[di-1])) {
				int n = 1;
				while (n < di && isLooping(dest[di-1-n])) n++;
				if (isBlockEnd(dest[di-1-n])) {
					int d0 = openingBracket( dest, di-1-n);
					if (d0 > 0 && isNoop(dest[d0-1])) {
						dest[d0-1] = dest[d0];
						if (n == 1) { // just move the ( to space and loop to (
							dest[d0] = dest[di-1];
						} else {
							moveRegion(dest, d0+1, di-n, n-1);
							arraycopy(src, si-n, dest, d0, n);
						}
						dest[--di] = ' ';
					}
				} else {
					int d0 = di-1;
					while (d0 >= 0 && !isNoop(dest[d0])) d0--;
					if (d0 >= 0) {
						dest[d0] = '('; // space becomes (
						moveRegion(dest, d0+1, di, n);
						arraycopy(src, si-n, dest, d0+1, n);
						dest[di] = ')';
						dest[++di] = ' '; // have to keep the space for later loopings
					}
				}
			}
			if (di == 0 || !isNoop(dest[di]) || !isNoop(dest[di-1])) { // remove double NOOPs
				di++;
			} else {
				dest[di-1] = ' '; // unify to space NOOPs
			}
			si++;
		}
		return Arrays.copyOf(dest, di);
	}

	private static boolean isComment(byte op) {
		return op == '%';
	}

	private static void moveRegion(byte[] arr, int start, int end, int by) {
		arraycopy(arr, start, arr, start+by, end-start);
	}

	private static int openingBracket(byte[] prog, int closingIndex) {
		int pc = closingIndex;
		byte inverse = prog[pc];
		byte target = (byte) (inverse == ')' ? '(' : '[');
		int c = 1;
		while (c > 0 && pc > 0) {
			pc--;
			if (prog[pc] == target) { c--; }
			else if (prog[pc] == inverse) c++;
		}
		return pc;
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
