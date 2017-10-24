package alma.lang;

import static alma.lang.Parser.mismatch;

/**
 * HiperX are High performance eXpressions.
 *
 * These are strictly sequential expressions (or pattern) or a
 * "Sequence Matching Machine". A form of a simpler "regex" that does no need to
 * be compiled and still is very efficient. Think of regex stripped of all the
 * features not needed so that it is both useful but also very simple and
 * compact to implement the matching algorithm.
 *
 * The matching algorithm is a single function (utilizing recursion).
 *
 * Sets:
 * <pre>
 * # = {0-9}
 * @ = {a-zA-Z}
 * _  any byte
 * ^  any non ASCII whitespace byte
 * $  any non ASCII byte (code > 127)
 * </pre>
 * Blocks:
 * <pre>
 * (...)
 * [...] = (...) 0-1 times
 * {...}  byte set, x-y is a range, to add - to set it has to be first
 * </pre>
 * Flexibility:
 * <pre>
 * ~  skip until next block matches
 * +  retry previous (failed [ ] must not be tried again)
 * </pre>
 */
public final class HiperX {


	public static long match(byte[] pattern, int p0, byte[] data, int d0) {
		return match(pattern, p0+1, data, d0, pattern[p0], false, -1);
	}

	/**
	 * Matches the pattern against the data and returns the end position of
	 * pattern and data at the end of a match or mismatch.
	 *
	 * @param pattern
	 *            the "match program"
	 * @param p0
	 *            starting position in the program
	 * @param data
	 *            the content to match
	 * @param d0
	 *            starting position in the content
	 * @param end
	 *            the bytes that marks the end of matching
	 * @param rep
	 *            true, if the match was called from a repetition '+' (so it is
	 *            "optional")
	 * @param maxOps
	 *            maximal number of operations evaluated before returning
	 * @return end positions (pn,dn) implemented as long to make the algorithm
	 *         allocation free. pn is next position in pattern, dn next position
	 *         in data after the match. On mismatch dn is (-position -1).
	 */
	public static long match(byte[] pattern, int p0, byte[] data, int d0, byte end, boolean rep, int maxOps) {
		int pn = p0;
		int dn = d0;
		int p1 = p0; // position from where to repeat
		byte rend = '+';
		byte c;
		while (pn < pattern.length && dn < data.length && maxOps-- != 0) {
			byte op  = pattern[pn++];
			switch (op) {
			case '_': dn++; break;
			case '$': if (data[dn++] >= 0) return pos(pn, mismatch(dn-1));
			case '^': c=data[dn++]; if (c == ' ' || c == '\t' || c == '\n' || c == '\r') return pos(pn, mismatch(dn-1)); break;
			case '@': c=data[dn++]; if (!(c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z')) return pos(pn, mismatch(dn-1)); break;
			case '#': c=data[dn++]; if (!(c >= '0' && c <= '9')) return pos(pn, mismatch(dn-1)); break;
			case '~': // fill
				int dnf = -1;
				int dnf0 = dn;
				do {
					dnf = (int)match(pattern, pn, data, dn, end, false, 1);
				} while (dnf < 0 && ++dn < data.length);
				if (dnf < 0)
					return pos(pn, mismatch(dnf0));
				break;
			case '(': // group:
			case '[': // optional group:
				p1 = pn;
				rend = (byte)(op == '(' ? ')' : ']');
				long res = match(pattern, pn, data, dn, rend, false, maxOps == 0 ? -1 : maxOps);
				if ((int)res < 0) {
					if (op == '(') {
						return res; // mismatch for (...)
					}
					//TODO skipping fails when a set in the block contains ]
					int level = 1;
					while (level > 0 && pn < pattern.length) { if (pattern[pn] == '[') level++; if (pattern[pn++] ==']') level--; } // skip rest of an optional block
				} else {
					pn = (int)(res >> 32);
					dn = (int)res;
				}
				break;
			case '+': // repetition:
				//if (dn < data.length) {
					if (rep) {
						pn = p0;
					} else {
						c = pattern[pn-2];
						dn = (int)match(pattern, c == '}' || c == ')' || c == ']' ? p1 : pn-2, data, dn, rend, true, maxOps);
						if (dn < 0) dn = mismatch(dn); // reverses a mismatch by applying function again
					}
				//}
				break;
			case '{': // set (of symbols):
				p1 = pn-1;
				c = data[dn++];
				boolean done = false;
				while (!done) {
					final byte m = pattern[pn++];
					done =     m == c   // match
							|| m == '}' // end of set
							|| m == '-' && pattern[pn-2] != '{' && c <= pattern[pn++] && c > pattern[pn-3]; // range
							// order of tests is important so that pn advances after end of a set
				}
				if (pattern[pn-1] !='}') { // match (since we did net reach the end of the set)
					if (rep) { // only jump to end if we don't know yet if there is a +
						pn = p0; // performance optimization: to directly go to start of set instead of skipping to the end and letting + do it
					} else {
						while (pattern[pn++] != '}'); // jump to end of set when match found
					}
				} else {
					return pos(p1, mismatch(dn-1));
				}
				break;
			default: // literals:
				if (op == end)
					return pos(pn, dn);
				if (op != data[dn])
					return pos(pn, mismatch(dn));
				dn++;
			}
		}
		return pos(pn, dn);
	}

	private static long pos(int pn, int dn) {
		return  (long)pn << 32 | dn & 0xFFFFFFFFL;
	}
}
