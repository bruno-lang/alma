package bruno.lang.grammar;

import java.nio.ByteBuffer;

/**
 * Matching sets of UTF-8 characters (for a single position in the stream).
 * 
 * The matching is based on UTF-8 code point ranges. Ranges with negative 
 * numbers are excluding the range (and including all other) while ranges with
 * positive code points are including the range. Ranges are sorted so that
 * all excluding ranges are before all including one. 
 *  
 * @author jan
 */
public final class Terminal {
	
	/**
	 * Simply all unicodes are contained in the range of the terminal.
	 */
	public static final Terminal WILDCARD = range(0, UTF8.MAX_CODE_POINT);
	
	/**
	 * Should follow the Unicode 5.0 standard,
	 * see https://spreadsheets.google.com/pub?key=pd8dAQyHbdewRsnE5x5GzKQ
	 */
	public static final Terminal WHITESPACE = range(9, 13).and(character(32));
	
	public static final Terminal
		DIGIT = range('0', '9'),
		HEX = DIGIT.and(range('A', 'F')),
		OCTAL = range('0', '7'),
		BINARY = range('0', '1'),
		LETTER = range('a', 'z').and(range('A','Z'));
	
	public static Terminal notRange(int minCodePoint, int maxCodePoint) {
		return new Terminal(new int[] { -minCodePoint, -maxCodePoint }); 
	}
	
	public static Terminal range(int minCodePoint, int maxCodePoint) {
		return new Terminal(new int[] { minCodePoint, maxCodePoint });
	}
	
	public static Terminal character(int codePoint) {
		return range(codePoint, codePoint);
	}
	
	public static Terminal notCharacter(int codePoint) {
		return notRange(codePoint, codePoint);
	}
	
	public final int[] asciis;
	public final int[] ranges; // 2 int's give min CP and max CP (negative when excluding)
	
	public Terminal(int[] ranges) {
		super();
		this.asciis = asciis(ranges);
		this.ranges = ranges;
	}
	
	private int[] asciis(int[] ranges) {
		int[] ascii = new int[4];
		if (ranges[0] < 0) {
			ascii[0] = -1;
			ascii[1] = -1;
			ascii[2] = -1;
			ascii[3] = -1;
		}
		for (int i = 0; i < ranges.length; i+=2) {
			if (ranges[i] < 0) {
				int min = Math.abs(ranges[i]);
				if (min < 128) {
					int max = Math.abs(ranges[i+1]);
					for (int cp = min; cp <= Math.min(127, max); cp++) {
						ascii[cp/32] &= ~(1 << (cp % 32));
					}
				}
			} else {
				int min = ranges[i];
				if (min < 128) {
					int max = ranges[i+1];
					for (int cp = min; cp <= Math.min(127, max); cp++) {
						ascii[cp/32] |= 1 << (cp % 32);
					}
				}
			}
		}
		return ascii;
	}
	
	private int excluding() {
		int i = 0;
		while (i < ranges.length && ranges[i] < 0) { i++; }
		return i;
	}
	
	public Terminal not() {
		int[] not = ranges.clone();
		for (int i = 0; i < not.length; i++) {
			not[i] = -not[i];
		}
		return new Terminal(not);
	}
	
	public Terminal and(Terminal other) {
		int[] merged = new int[ranges.length+other.ranges.length];
		int ex = excluding();
		if (ex > 0) {
			System.arraycopy(ranges, 0, merged, 0, ex);
		}
		int oex = other.excluding();
		if (oex > 0) {
			System.arraycopy(other.ranges, 0, merged, ex, oex);
		}
		if (ex < ranges.length) {
			System.arraycopy(ranges, ex, merged, ex+oex, ranges.length-ex);
		}
		if (oex < other.ranges.length) {
			System.arraycopy(other.ranges, oex, merged, ranges.length+oex, other.ranges.length-oex);
		}
		return new Terminal(merged);
	}
	
	public boolean contains(ByteBuffer input, int position) {
		byte b = input.get(position);
		if (b >= 0) {
			return (asciis[b/32] & 1 << b % 32) != 0;
		}
		final int codePoint = UTF8.codePoint(input, position);
		int i = 0;
		boolean excluded = false;
		while (i < ranges.length && ranges[i] < 0) {
			if (!excluded) {
				excluded = codePoint >= Math.abs(ranges[i]) && codePoint <= Math.abs(ranges[i+1]);
			} // else - forward to positive/including ranges
			i+=2;
		}
		while (i < ranges.length) {
			if (codePoint >= ranges[i] && codePoint <= ranges[i+1]) {
				return true;
			}
			i+= 2;
		}
		return !excluded && ranges[0] < 0; // just in case there was excluding ranges it means all others where included
	}
	
	public boolean isSingleCharacter() {
		return ranges.length == 2 && ranges[0] == ranges[1];
	}
	
	@Override
	public String toString() {
		if (ranges.length == 2 && ranges[0] == 0 && ranges[1] == UTF8.MAX_CODE_POINT) {
			return ".";
		}
		//TODO special any whitespace _
		StringBuilder b = new StringBuilder();
		if (!isSingleCharacter()) {
			b.append("{ ");
		}
		for (int i = 0; i < ranges.length; i+=2) {
			if (i > 0) {
				b.append(" ");
			}
			if (ranges[i] < 0) {
				b.append('!');
			}
			if (ranges[i] == ranges[i+1]) {
				appendChar(ranges[i], b);
			} else {
				appendChar(ranges[i], b);
				b.append('-');
				appendChar(ranges[i+1], b);
			}
		}
		if (!isSingleCharacter()) {
			b.append(" }");
		}
		return b.toString();
	}

	private void appendChar(int codePoint, StringBuilder b) {
		codePoint = Math.abs(codePoint);
		if (codePoint > 31 && codePoint < 256) {
			b.append('\'');
			b.append((char)codePoint);
			b.append('\'');
		} else if (codePoint == '\t') {
			b.append("\\t");
		} else if (codePoint == '\n') {
			b.append("\\n");
		} else if (codePoint == '\r') {
			b.append("\\r");
		} else {
			b.append("U+");
			b.append(String.format("%04x", codePoint));
		}
	}
}
