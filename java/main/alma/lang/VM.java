package alma.lang;

import static java.lang.Integer.MAX_VALUE;
import static java.lang.Math.min;

public class VM {
	
	public VM(String prog, String data) {
		this(prog.getBytes(), data.getBytes());
	}
	
	public VM(byte[] prog, byte[] data) {
		super();
		this.prog = prog;
		this.data = data;
	}

	private final byte[] prog; 
	private final byte[] data;

	private int pc;
	
	public int eval(int i0) {
		final int pc0 = pc;
		int pcc = -1;// PC for next stair-case
		int i = i0;  // current data index
		int ic = i0; // data index at the end of last successful repetition 
		int il = MAX_VALUE; // data index for look-ahead
		int min = 1;
		int max = 1;
		int c = 0;   // repetition count
		boolean decided = false;
		while (i < data.length && pc < prog.length) {
			byte op = prog[pc++];
			
			switch(op) {
			// whitespace
			case ',': i = indentAt(i, true); break;
			case ';': i = indentAt(i, false); break;
			case '.': i = whitespaceAt(i, true); break;
			case ':': i = whitespaceAt(i, false); break;
			case '!': i = linebreakAt(i); break;
			// literals and sets
			case '#': i = charAt(i); break;
			case '\'':i = literalAt(i); break;
			case '$': i = memberAt(i); break;
			// seq mod
			case '~': break; // fill TODO
			case '>': il = i; break; // look-ahead
			case '<': decided = true; break; // decision
			// repetition
			case '+': max = MAX_VALUE; break;
			case '?': min = 0; break;
			case '*': min = 0; max = MAX_VALUE; break;
			case '-': min = uint1(); max = uint1(); break;
			case '|': pcc = uint2(); break;
			// ref
			case '\\':
			case '&': int pcr = pc+2; pc = uint2(); eval(i); pc = pcr; break;
			// nest-return
			case '(': i = eval(i); break;
			case ']': // this is the same as )
			case ')': 
				if (++c == max)
					return min(i, il);
				if (c > max)
					return mismatch(i);
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
				if (pcc < 0) { // no alternatives
					return c < min ? i : ic;
				}
				// there is an alternative
				pc = pcc;
				i = i0;
				ic = i0;
			}
		}
		return min(i, il);
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
		return prog[pc++] == data[i0] ? i0+1 : mismatch(i0);
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
