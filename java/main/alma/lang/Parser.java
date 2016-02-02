package alma.lang;

import static java.lang.Integer.MAX_VALUE;
import static java.lang.Math.min;

final class Parser {

	Parser(byte[] prog, byte[] data, ParseTree tree) {
		super();
		this.prog = prog;
		this.data = data;
		this.tree = tree;
	}

	private final byte[] prog;
	private final byte[] data;
	private final ParseTree tree;

	private int pc;

	public int parse() {
		return eval(0, false);
	}

	private int eval(int i0, boolean recover) {
		int pc0 = pc;
		int pcc = -1; // PC for next stair-case
		int pcr = -1; // index to return to from recursive call
		int i = i0;  // current data index
		int ic = i0; // data index at the end of last successful repetition
		int il = MAX_VALUE; // data index for look-ahead
		int min = 1;
		int max = 1;
		int nodes = 0; // capture: 0 = no; > 0 amount of opened nodes
		int c = 0;   // repetition count
		boolean decided = false;
		while (pc < prog.length) { //TODO always append an ) to the end of "main" so it will terminate by )
			byte op = prog[pc++];

			switch(op) {
			// whitespace
			case ',': i = indentAt(i, true); break;
			case ';': i = indentAt(i, false); break;
			case '.': i = whitespaceAt(i, true); break;
			case ':': i = whitespaceAt(i, false); break;
			case '!': i = linebreakAt(i); break;
			// literals and sets
			case '_': i++; break;
			case '`': i = charAt(i); break;
			case '\'':i = literalAt(i); break;
			case '$': i = memberAt(i); break;
			// sequences
			case '~': break; // fill TODO
			case '>': il = i; break; // look-ahead
			case '<': decided = true; break; // decision
			// repetition
			case '+': max = MAX_VALUE; break;
			case '?': min = 0; break;
			case '*': min = 0; max = MAX_VALUE; break;
			case '#': min = uint1(); max = uint1(); break;
			case '1': min = 1; max=1; break;
			case '2': min = 2; max=2; break;
			case '3': min = 3; max=3; break;
			case '4': min = 4; max=4; break;
			case '5': min = 5; max=5; break;
			case '6': min = 6; max=6; break;
			case '7': min = 7; max=7; break;
			case '8': min = 8; max=8; break;
			case '9': min = 9; max=9; break;
			// capture
			case '=': tree.push(prog[pc++], i); nodes++; break; //TODO this just supports single char names
			// ref
			case '@': pcr = pc+2; pc = uint2(); eval(i, false); pc = pcr; break;
			// nest-return
			case '(': i = block(i, false); break;
			case '[': i = block(i, true); break;
			case ']': // this is the same as )
			case '|': // this is also a return but not found when scanning for closing ] or )
			case ')':
				++c;
				if (c == max) {
					if (nodes > 0) { tree.done(nodes, i); }
					return min(i, il);
				}
				if (c > max) {
					if (nodes > 0) { tree.pop(nodes); }
					return mismatch(i);
				}
				ic = min(i, il); // remember last successful repetition
				pc = pc0;
				break;
			default :
				throw new RuntimeException("No such op: "+(char)op);
			}
			if (i < 0) { // mismatch
				if (decided) {
					throw new RuntimeException("Parse error: "+i);
				}
				if (recover) {
					while (pc < prog.length && prog[pc] != '|' && prog[pc] != ']') {
						pc++;
					}
					pcc = prog[pc] == '|' ? pc+1 : -1;
				}
				if (pcc < 0) { // no alternatives
					tree.pop(nodes);
					return c < min ? i : ic; // OBS! i is a mismatch already!
				}
				// there is an alternative
				pc = pcc;
				pc0 = pc;
				i = i0;
				ic = i0;
			}
		}
		return min(i, il);
	}

	private int block(int i, boolean recover) {
		int pcr = afterNext(prog, pc, ')', '(');
		i = eval(i, recover);
		pc = pcr;
		return i;
	}

	private static int afterNext(byte[] arr, int i0, char close, char open) {
		int i = i0;
		int c = 1;
		while (c > 0 && i < arr.length) {
			if (arr[i] == close) {
				c--;
			} else if (arr[i] == open) {
				c++;
			}
			i++;
		}
		return i;
	}

	private int memberAt(int i0) {
		int n = data[i0];
		int bn = n / 8;
		int sn = n % 8;
		byte b = prog[pc+bn];
		pc+=32;
		return ((1 << sn) & b) > 0 ? i0+1 : mismatch(i0);
	}

	private int linebreakAt(int i0) {
		final int ie = data.length;
		int i = i0;
		i = indentAt(i, true);
		if (i >= ie) {
			return i; // end of input is also treated as wrap
		}
		final int w = i;
		while (i < ie && isLinebreak(data[i])) { i++; }
		if (w == i) {
			return mismatch(i);
		}
		return indentAt(i, true);
	}

	private int indentAt(int i0, boolean optional) {
		int i = i0;
		while (i < data.length && isIndent(data[i])) { i++; }
		return i > i0 || optional ? i : mismatch(i0);
	}

	private int whitespaceAt(int i0, boolean optional) {
		int i = i0;
		while (i < data.length && isWhitespace(data[i])) { i++; }
		return i > i0 || optional ? i : mismatch(i0);
	}

	private static boolean isIndent(int b) {
		return b == ' ' || b == '\t';
	}

	private static boolean isLinebreak(int b) {
		return b == '\n' || b == '\r';
	}

	private static boolean isWhitespace(int b) {
		return b >= 9 && b <=13 || b == 32;
	}

	private int charAt(int i0) {
		return i0 < data.length && prog[pc++] == data[i0] ? i0+1 : mismatch(i0);
	}

	private int literalAt(int i0) {
		int i = i0;
		while (i < data.length && pc < prog.length && prog[pc] == data[i]) { pc++; i++; }
		return prog[pc++] == '\'' ? i : mismatch(i);
	}

	private int uint2() {
		return (uint1() << 8) | uint1();
	}

	private int uint1() {
		return prog[pc++] & 0xFF;
	}

	private static int mismatch(int pos) {
		return -pos-1;
	}

}
