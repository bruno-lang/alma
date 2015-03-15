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
		return parse(p0, data, r0, lang, new BParseTree(lang, 500));
	}
	
	/**
	 * @param r0 start rule in language
	 * @param p0 start position in data buffer
	 * @return position in data after matched rule r0
	 */
	private static int parse(int p0, ByteBuffer data, int r0, ByteBuffer lang, BParseTree tree) {
		byte op = lang.get(r0);
		System.out.println(String.format("p0: %3d  r0: %3d => op: %3d [%s]", p0, r0, op, Character.valueOf((char) op)));
		final int pE = data.limit();
		int p = p0; // "current" position pointer 
		switch (op) {
		case '|' : // alternatives
			p = mismatch(p0);
			int ruleN = r0 + 2;
			while (lang.get(ruleN) != 0 || lang.get(ruleN + 1) != 0) {
				int pN = parse(p0, data, lang.getShort(ruleN), lang, tree);
				if (pN >= 0) {
					return pN;
				}
				p = min(p, pN);
				ruleN += 2;
			}
			tree.erase(p0);
			return p;			
		case '&' : // sequence
			boolean decided = false;
			int pL = Integer.MAX_VALUE;
			ruleN = r0 + 2; 
			while (lang.get(ruleN) != 0 || lang.get(ruleN + 1) != 0) {
				byte ruleOp = lang.get(ruleN);
				if (ruleOp == '<') { // decision
					decided = true;
				} else if (ruleOp == '~') { // fill
					int rF = lang.getShort(ruleN + 2);
					while (p < pE && parse(p, data, rF, lang, tree) < 0) { p++; }
					if (p < pE) {
						tree.erase(p);
					} else {
						return mismatch(pE);
					}
				} else {
					int pN = parse(p, data, lang.getShort(ruleN), lang, tree);
					if (pN < 0) {
						if (decided) {
							tree.erase(p);
							throw new BParseException(data, p, pN, tree);
						}
						tree.erase(p0);
						return pN;
					}
					if (ruleOp == '>') { // look-ahead
						pL = p; // the end of the previous rule is the result
					}
					p = pN;
				}
				ruleN += 2;
			}
			return min(p, pL);			
		case '*' : // repetition
			final int min = lang.getShort(r0 + 4); // +2*2
			final int max = lang.getShort(r0 + 6); // +3*2
			final int rN = lang.getShort(r0 + 2);
			int n = 0;
			while (n < max) {
				int pN = parse(p, data, rN, lang, tree);
				if (pN < 0) {
					tree.erase(p);
					if (n < min) {
						return pN;
					}
					return p;
				} else {
					p = pN;
					n++;
				}
			}
			return p;			
		case '_' : // character set
			byte c = data.get(p0);
			int mask = 1 << c % 8;
			return (lang.get(r0 + 2 +(c/8)) & mask) == mask ? p0 + 1 : mismatch(p0);
		case '=' : // capture
			tree.push(r0, p0);
			int end = parse(p0, data, lang.getInt(r0 + 2), lang , tree);
			if (end > p0) {
				tree.done(end); 
			} else {
				tree.pop();
			}
			return end;			
		case '\'': // literal
			int bi = lang.getShort(r0 + 2) + 2; 
			byte b = lang.get(bi);
			while (b != 0 && p < pE && b == data.get(p)) {
				p++;
				b = lang.get(++bi);
			}
			return b == 0 ? p : mismatch(p);
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
		// error
		case '~' :
		case '<' :
		case '>' : throw new IllegalStateException("Not processed in seqeuence: "+(char)op);
		case '@' : throw new IllegalStateException("Should be preprocessed to goto: "+(char)op);
		case '\\': throw new IllegalStateException("Should be embedded: "+(char)op);
		case '#' : throw new IllegalStateException("Do not dispatch to binary directly!");
		default  : throw new IllegalStateException("Not an op-code: "+op);
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
