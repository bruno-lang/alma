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
public final class CharacterSet {
	
	public static final CharacterSet EMPTY = new CharacterSet(new int[0]);
	
	public static CharacterSet range(int minCodePoint, int maxCodePoint) {
		return new CharacterSet(new int[] { minCodePoint, maxCodePoint });
	}
	
	public static CharacterSet character(int codePoint) {
		return range(codePoint, codePoint);
	}
	
	public final int[] asciis;
	public final int[] ranges; // 2 int's give min CP and max CP (negative when excluding)
	
	public CharacterSet(int[] ranges) {
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
	
	public CharacterSet not() {
		if (this == EMPTY)
			return this;
		int[] not = ranges.clone();
		for (int i = 0; i < not.length; i++) {
			not[i] = -not[i];
		}
		return new CharacterSet(not);
	}
	
	public CharacterSet and(CharacterSet other) {
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
		return new CharacterSet(merged);
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
		if (isSingleCharacter()) {
			return "[{"+toBoundary(ranges[0])+"}]"+(ranges[0] < 0 ? "^" : "");
		}
		StringBuilder b = new StringBuilder();
		b.append("[");
		boolean lastWasSingleASCII = false;
		boolean lastWasExcluding = false;
		for (int i = 0; i < ranges.length; i+=2) {
			if (lastWasExcluding && ranges[i] >= 0) {
				b.append("]^ [");
			}
			String boundary = toBoundary(ranges[i]);
			if (ranges[i] == ranges[i+1]) {
				boolean isSingleASCII = boundary.startsWith("'");
				if (lastWasSingleASCII && isSingleASCII) {
					b.setLength(b.length()-1);
					b.append(boundary.substring(1));
				} else if (isSingleASCII) {
					b.append(boundary);
				} else {
					b.append('{');
					b.append(boundary);
					b.append('}');
				}
				lastWasSingleASCII = isSingleASCII;
			} else {
				b.append('{');
				b.append(boundary);
				b.append('-');
				b.append(toBoundary(ranges[i+1]));
				b.append("}");
			}
			lastWasExcluding = ranges[i] < 0;
		}
		b.append("]");
		if (lastWasExcluding) {
			b.append("^");
		}
		return b.toString();
	}
	
	private static String toBoundary(int codePoint) {
		codePoint = Math.abs(codePoint);
		if (codePoint <=32 || codePoint == 127) {
			return String.valueOf(codePoint);
		}
		if (codePoint < 127) {
			return "'"+Character.valueOf((char) codePoint)+"'";
		}
		return "#"+Integer.toHexString(codePoint).toUpperCase();
	}
}
