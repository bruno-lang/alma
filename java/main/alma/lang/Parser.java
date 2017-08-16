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

	private final byte[] prog; //TODO split in list of named blocks and names during compile step, refs use indexes to list
	private final byte[] data;
	private final ParseTree tree;

	private int pc;

	public int parse() {
		return eval(0);
	}

	//TODO idea: add a space (noop) after ( and [ so that the length can be encoded there. do this on encounter, if there is a noop than the parser can fill with length, but how does the parser know that this is the length? => must be for all
	
	//IDEA do internal length encoding for cases | and names = directly following the code
	// a block without further cases (the last case) has length -length
	
	private int eval(final int i0) {
		int pc0 = pc;
		int pcr = -1; // index to return to from recursive call
		int i = i0;  // current data index
		int ic = i0; // data index at the end of last successful repetition
		int il = MAX_VALUE; // data index for look-ahead
		int c = 0;   // repetition count
		int min = 1;
		int max = 1;
		boolean setMin = true;
		boolean locked = false;
		boolean hasCases = false;
		int pce = pc0;
		while (!hasCases && pce < prog.length)
			hasCases = prog[pce++] == '|';
		int pushed = 0;
		while (pc < prog.length) { //TODO idea: always append an ) to the end of "main" so it will terminate by ) then no check will be needed
			byte op = prog[pc++];

			switch(op) {
			// noop
			case ' ': break;
			// whitespace
			case ',': i = gapAt(i, true); break;
			case ';': i = gapAt(i, false); break;
			case '.': i = whitespaceAt(i, true); break;
			case ':': i = whitespaceAt(i, false); break;
			case '!': i = wrapAt(i); break;
			case '\\': i = linebreakAt(i); break;
			// other character sets
			case '_': i++; break; // any
			case '"': i = memberAt(i); break;
			// literals 
			case '\'':i = literalAt(i); break;
			// sequences
			case '~': i = fillAt(i); break; // fill
			case '>': il = i; break; // look-ahead
			case '<': locked = true; break; // lock
			// repetition
			case '-': setMin = false; break;
			case '?': min = 0; break;
			case '*': min = 0; // intentional fall-through 
			case '+': max = MAX_VALUE; break;
			case '0': max=0; if (setMin) { min=0; } break;
			case '1': max=1; if (setMin) { min=1; } break;
			case '2': max=2; if (setMin) { min=2; } break;
			case '3': max=3; if (setMin) { min=3; } break;
			case '4': max=4; if (setMin) { min=4; } break;
			case '5': max=5; if (setMin) { min=5; } break;
			case '6': max=6; if (setMin) { min=6; } break;
			case '7': max=7; if (setMin) { min=7; } break;
			case '8': max=8; if (setMin) { min=8; } break;
			case '9': max=9; if (setMin) { min=9; } break;
			case '^': max=uint1(); if (setMin) { min=max; } break;
			// capture
			case '=': tree.push(prog[pc++], i); pushed++; break; //TODO this just supports single char names
			// ref
			case '@': pcr = pc+2; pc = uint2(); eval(i); pc = pcr; break;
			// blocks
			case '(': i = blockAt(i); break;
			case '|': // end of case; intentional fall-through 
			case ')': // end of block
				++c;
				if (c > max) {
					tree.pop(pushed);
					return mismatch(i);
				}
				ic = min(i, il); // remember last successful repetition
				tree.done(ic, pushed);
				if (c == max) {
					return ic;
				}
				pushed = 0;
				pc = pc0;
				break;
			default :
				throw new RuntimeException("No such op: "+(char)op);
			}
			if (i < 0) { // mismatch
				if (locked) {
					throw new NoMatch(data, i, i, tree);
				}
				int pcc = -1; // PC for next case
				if (hasCases) {
					// jump to next case
					while (pc < prog.length && prog[pc] != '|' && prog[pc] != ')') {
						pc++;
					}
					pcc = pc < prog.length && prog[pc] == '|' ? pc+1 : -1;
				}
				if (pcc < 0) { // no more cases
					tree.pop(pushed);
					return c < min ? i : ic; // OBS! i is a mismatch already!
				}
				// there is another case
				pc = pcc;
				pc0 = pc;
				i = i0;
				ic = i0;
				il = MAX_VALUE; // each case has its own look-ahead
				setMin=true; min=1;	max=1;
			}
		}
		return min(i, il);
	}

	private int blockAt(int i0) {
		final int pc0 = pc;
		final int i = eval(i0);
		pc = end(pc0)+1;
		return i;
	}
	
	public int fillAt(int i0) {
		final int iE = data.length;
		final int pc0 = pc;
		int i = i0;
		while (true) { 
			if (i >= iE)
				return mismatch(iE);
			int iN = eval(i);
			pc = pc0;
			if (iN > 0)
				return i;
			i++;
		}
	}
	
	private int end(int pc0) {
		return Program.end(prog, pc0);
	}

	private int memberAt(int i0) {
		int x = data[i0];
		int m = prog[pc];
		while (m != x && m != '"') { m = prog[++pc]; }
		if (m != x) {
			return mismatch(i0);
		}
		while (prog[pc] != '"') pc++;
		return i0+1;
	}
	
	private int linebreakAt(final int i0) {
		int i = i0;
		if (i < data.length && data[i] == '\r') // mac, win
			i++;
		if (i < data.length && data[i] == '\n') // linux, win
			i++;
		return i == i0 ? mismatch(i) : i;
	}

	private int wrapAt(int i0) {
		int i = i0;
		i = gapAt(i, true);
		if (i >= data.length)
			return i; // end of input is also treated as wrap
		if (!isWrap(data[i])) 
			return mismatch(i);
		return whitespaceAt(i, true);
	}

	private int gapAt(int i0, boolean optional) {
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

	private static boolean isWrap(int b) {
		return b == '\n' || b == '\r';
	}

	private static boolean isWhitespace(int b) {
		return b >= 9 && b <=13 || b == 32;
	}

	private int literalAt(int i0) {
		final int end = prog[pc-1];
		int i = i0;
		while (i < data.length && pc < prog.length && prog[pc] == data[i]) { pc++; i++; }
		return pc < prog.length && prog[pc++] == end ? i : mismatch(i);
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
