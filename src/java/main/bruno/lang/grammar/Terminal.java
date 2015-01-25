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
	
	public static final Terminal EMPTY = new Terminal(new int[0]);
	
	public static final Terminal
		DIGITS = range('0', '9'),
		HEX_NUMBER = DIGITS.and(range('A', 'F')),
		OCTAL_NUMBER = range('0', '7'),
		BINARY_NUMBER = range('0', '1'),
		UPPER_LETTERS = range('A','Z'),
		LOWER_LETTERS = range('a', 'z'), 
		LETTERS = UPPER_LETTERS.and(LOWER_LETTERS)
		;
	
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
		if (ranges.length == 0)
			return ranges;
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
		if (this == EMPTY)
			return this;
		int[] not = ranges.clone();
		for (int i = 0; i < not.length; i++) {
			not[i] = -not[i];
		}
		return new Terminal(not);
	}
	
	public Terminal and(Terminal other) {
		if (this == EMPTY)
			return other;
		if (other == EMPTY)
			return this;
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
			return "$";
		}
		if (this == WHITESPACE) {
			return "_";
		}
		if (this == LETTERS) {
			return "@";
		}
		if (this == LOWER_LETTERS) {
			return "z";
		}
		if (this == UPPER_LETTERS) {
			return "Z";
		}
		if (this == DIGITS) {
			return "9";
		}
		if (this == HEX_NUMBER) {
			return "#";
		}
		StringBuilder b = new StringBuilder();
		if (!isSingleCharacter()) {
			b.append("{ ");
		}
		for (int i = 0; i < ranges.length; i+=2) {
			if (i > 0) {
				b.append(" ");
			}
			if (ranges[i] < 0) {
				b.append("-{");
			}
			if (ranges[i] == ranges[i+1]) {
				b.append(UTF8.toLiteral(ranges[i]));
			} else {
				b.append(UTF8.toLiteral(ranges[i]));
				b.append('-');
				b.append(UTF8.toLiteral(ranges[i+1]));
			}
			if (ranges[i] < 0) {
				b.append("}");
			}
		}
		if (!isSingleCharacter()) {
			b.append(" }");
		}
		return b.toString();
	}
}
