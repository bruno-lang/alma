package bruno.lang.grammar;

import static java.lang.Character.isWhitespace;
import static java.lang.Math.min;

import java.nio.ByteBuffer;

/**
 * A parser driven by a parsing program in binary format.
 * 
 * @author jan
 *
 */
public class BParser {

	public static int parse(int p0, ByteBuffer data, int r0, ByteBuffer lang) {
		return read(p0, data, r0, lang, new BParseTree(lang, 500));
	}
	
	public static int parse(int p0, ByteBuffer data, int r0, ByteBuffer lang, BParseTree tree) {
		return read(p0, data, r0, lang, tree);
	}
	
	/**
	 * @param r0 start rule in language
	 * @param p0 start position in data buffer
	 * @return position in data after matched rule r0
	 */
	private static int read(int p0, ByteBuffer data, int r0, ByteBuffer lang, BParseTree tree) {
		byte op = lang.get(r0);
		System.out.println(String.format("p0: %3d  r0: %3d => op: %3d [%s]", p0, r0, op, Character.valueOf((char) op)));
		final int pE = data.limit();
		int p = p0; // "current" position pointer 
		switch (op) {
		case '|' : // alternatives
			p = mismatch(p0);
			int rN = r0;
			for (int i = lang.get(r0+1); i > 0; i--) {
				rN += 2;
				int pN = read(p0, data, rN, lang, tree);
				if (pN >= 0) {
					return pN;
				}
				p = min(p, pN);
			}
			tree.erase(p0);
			return p;			
		case '&' : // sequence
			boolean decided = false;
			int pL = Integer.MAX_VALUE;
			rN = r0; 
			for (int i = lang.get(r0+1); i > 0; i--) {
				rN += 2;
				switch (lang.get(rN)) {
				case '<': // decision
					decided = true; 
					break;
				case '~': // fill
					int rF = rN + 2;
					while (p < pE && read(p, data, rF, lang, tree) < 0) { p++; }
					if (p < pE) {
						tree.erase(p);
						break;
					} else {
						return mismatch(pE);
					}
				case '>': // look-ahead
					pL = p; // the end of the previous rule is the result
					break;
				default:
					int pN = read(p, data, rN, lang, tree);
					if (pN < 0) {
						if (decided) {
							tree.erase(p);
							throw new BParseException(data, p, pN, tree);
						}
						tree.erase(p0);
						return pN;
					}
					p = pN;
				}
			}
			return min(p, pL);			
		case '*' : // repetition
			final int min = lang.getShort(r0 + 4); // +2*2
			final int max = lang.getShort(r0 + 6); // +3*2
			rN = r0 + 2;
			for (int i = 0; i < max; i++) {
				int pN = read(p, data, rN, lang, tree);
				if (pN < 0) {
					tree.erase(p);
					if (i < min) {
						return pN;
					}
					return p;
				} else {
					p = pN;
				}
			}
			return p;			
		case '_' : // character set
			if (p0 >= pE)
				return -pE;
			byte c = data.get(p0);
			int mask = 1 << c % 8;
			return (lang.get(r0 + 2 +(c/8)) & mask) == mask ? p0 + 1 : mismatch(p0);
		case '=' : // capture
			tree.push(r0, p0);
			int end = read(p0, data, r0 + 2, lang , tree);
			if (end > p0) {
				tree.done(end); 
			} else {
				tree.pop();
			}
			return end;			
		case '\'': // literal 
			int n = lang.get(r0+1);
			int bN = r0+2;
			if (n > pE-p0)
				return mismatch(pE);
			for (int i = n; i > 0; i--) {
				if (lang.get(bN) != data.get(p)) {
					return mismatch(p);
				}
				p++;
				bN++;
			}
			return p;
		case '\\' : // include
			throw new IllegalArgumentException("Unresolved include: "+lang.getShort(r0+2));
		// whitespace 
		case ',' : 
		case ';' :
			while (p < pE && isIndent(data.get(p))) { p++; }
			return p > p0 || op == ',' ? p : mismatch(p0);
		case '.' :
		case ':' :
			while (p < pE &&	isWhitespace(data.get(p))) { p++; }
			return p > p0 || op == '.' ? p : mismatch(p0);			
		case '!' :
			while (p  < pE && isIndent(data.get(p))) { p++; }
			if (p >= pE) {
				return p; // end of input is also treated as wrap
			}
			final int w = p;
			while (p < pE && isWrap(data.get(p))) { p++; }
			if (w == p) {
				return mismatch(p0);
			}
			while (p < pE && isIndent(data.get(p))) { p++; }
			return p;
		default  : // goto
			return read(p0, data, lang.getShort(r0) << 5, lang, tree);
		}
	}

	private static int mismatch(int pos) {
		return -pos-1;
	}
	
	private static boolean isIndent(int b) {
		return b == ' ' || b == '\t';
	}
	
	private static boolean isWrap(int b) {
		return b == '\n' || b == '\r';
	}
}