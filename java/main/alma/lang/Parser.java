package alma.lang;

import static alma.lang.Program.isStartOfCase;
import static java.lang.Byte.toUnsignedInt;
import static java.lang.Integer.MAX_VALUE;
import static java.lang.Math.min;

/**
 * Universal Binary Parsing Machine
 *  
 * @author jan
 */
final class Parser {

	Parser(byte[] prog, int[] indices, byte[] data, ParseTree tree) {
		super();
		this.prog = prog;
		this.indices = indices;
		this.data = data;
		this.tree = tree;
	}

	//TODO as an alternative to a single sequence program a program can be sliced in blocks
	// each new block getting a number (named or not) and we jump into them
	// this has the huge benefit that we only enter blocks via jump (the "(" would be cut out when slicing the program)
	// and thereby never have to "find" the end of the block when exiting one => O(1)
	private final byte[] prog;
	private final int[] indices;
	private final byte[] data;
	private final ParseTree tree;

	private int pc;

	public int parse() {
		return eval(0);
	}

	private int eval(final int i0) {
		int pc0 = pc;
		int i = i0;  // current data index
		int iR = i0; // data index at the end of last successful repetition
		int iL = MAX_VALUE; // data index for look-ahead
		int rC = 0;   // repetition count
		int rMin = 1;
		int rMax = 1;
		boolean setMin = true;
		boolean locked = false;
		int pcCaseEnd = pc0 >= 2 && isStartOfCase(prog[pc0-2]) ? pc0 + prog[pc0-1]-2 : -1;
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
			case '#': i = digitAt(i); break;
			// literals 
			case '\'':i = literalAt(i); break;
			// sequences
			case '~': i = fillAt(i); break; // fill
			case '>': iL = i; break; // look-ahead
			case '<': locked = true; break; // lock
			// repetition
			case '-': setMin = false; break;
			case '?': rMin = 0; break;
			case '*': rMin = 0; // intentional fall-through 
			case '+': rMax = MAX_VALUE; break;
			case '0': if (setMin) { rMin=0; } break;
			case '1': rMax=1; if (setMin) { rMin=1; } break;
			case '2': rMax=2; if (setMin) { rMin=2; } break;
			case '3': rMax=3; if (setMin) { rMin=3; } break;
			case '4': rMax=4; if (setMin) { rMin=4; } break;
			case '5': rMax=5; if (setMin) { rMin=5; } break;
			case '6': rMax=6; if (setMin) { rMin=6; } break;
			case '7': rMax=7; if (setMin) { rMin=7; } break;
			case '8': rMax=8; if (setMin) { rMin=8; } break;
			case '9': rMax=9; if (setMin) { rMin=9; } break;
			case '^': rMax=uint1(); if (setMin) { rMin=rMax; } break;
			// capture
			case '=': tree.push(uint1(), i); pushed++; break;
			// blocks
			case '@': i = jumpAt(i); break;
			case '(': i = blockAt(i); break;
			case '|': // end of case; intentional fall-through 
			case ')': // end of block
				++rC;
				if (rC > rMax) {
					tree.pop(pushed);
					return mismatch(i);
				}
				iR = min(i, iL); // remember last successful repetition
				tree.done(iR, pushed);
				if (rC == rMax) {
					return iR;
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
				tree.pop(pushed);
				pushed = 0;
				if (rC >= rMin) {
					return iR;
				}
				if (pcCaseEnd < 0) {
					pcCaseEnd = Program.end(prog, pc, '(', ')', '|');
				}
				if (pcCaseEnd < prog.length && prog[pcCaseEnd] == '|') { // there is another case
					pc = pcCaseEnd+2;
					pcCaseEnd += prog[pcCaseEnd+1]; 
					pc0 = pc;
					i = i0;
					iR = i0;
					iL = MAX_VALUE; // each case has its own look-ahead
					setMin=true;  //TODO maybe just reset setMin?					
				} else { // no more cases
					return i; // OBS! i is a mismatch already!
				}
			}
		}
		return min(i, iL);
	}

	private int jumpAt(int i0) {
		int pc0 = pc; 
		pc = indices[uint1()-1]; // forwards pc after index argument 
		i0 = eval(i0); 
		pc = pc0+1;
		return i0;
	}

	private int blockAt(int i0) {
		int len = prog[pc++]; // inc pc by 1 to skip length argument for now
		final int pc0 = pc; 
		final int i = eval(i0);
//		if (prog[pc-1] == ')') {
//			pc++;
//			return i;
//		}
		pc = prog[pc-1] == '|' ? pc-1 : pc0-2+len;
		while (prog[pc] == '|') { 
			pc+= prog[pc+1]; 
		}
		pc++; // skip the )
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
	
	private int memberAt(int i0) {
		if (i0 >= data.length)
			return mismatch(i0);
		int b = data[i0]; // the byte to match
		int m = prog[pc];
		if (m == '^') {
			if (prog[pc+1] == '"') {
				pc+=2;
				return b == '"' ? mismatch(i0) : i0+1;
			}
			do {
				m = prog[++pc];
				if (b == m)
					return mismatch(i0);
			} while (m != '"');
			pc++;
		} else if (m == '&') {
			int pc0 = pc;
			for (int i = 7; i >= 0; i--) {
				byte x = prog[++pc];
				if (x == '0' && ((b & (1 << i)) == 1) || (x == '1') && ((b & (1 << i)) == 0))
					return mismatch(i0);
			}
			pc = pc0 + 10;
		} else {
			if (m == '"') {
				pc++;
				return b == '"' ? i0+1 : mismatch(i0);
			}
			while (m != b && m != '"') { m = prog[++pc]; }
			if (m == '"') {
				return mismatch(i0);
			}
			while (prog[pc++] != '"');
		}
		return i0+1;
	}
	
	private int digitAt(int i0) {
		if (i0 >= data.length)
			return mismatch(i0);
		byte d = data[i0];
		return d >= '0' && d <= '9' ? i0+1 : mismatch(i0);
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

	//TODO '' should match a single '
	private int literalAt(int i0) {
		final int end = prog[pc-1];
		int i = i0;
		while (i < data.length && pc < prog.length && prog[pc] == data[i]) { pc++; i++; }
		return pc < prog.length && prog[pc++] == end ? i : mismatch(i);
	}
	
	private int uint1() {
		return toUnsignedInt(prog[pc++]);
	}

	static int mismatch(int pos) {
		return -pos-1;
	}

}
