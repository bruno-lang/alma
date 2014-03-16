package bruno.lang.grammar;

import java.nio.ByteBuffer;

public final class Patterns {

	/**
	 * <code>,</code>
	 */
	public static final Pattern gap = new Gap();
	/**
	 * <code>~</code>
	 */
	public static final Pattern pad = new Pad();
	/**
	 * <code>>></code>
	 */
	public static final Pattern indent = new Indent();
	/**
	 * <code>^</code>
	 */
	public static final Pattern separator = new Separator();

	public static final Pattern whitespace = new Whitespace();
	public static final Pattern wildcard = new Wildcard();

	public static byte toByte( char c ) {
		return String.valueOf(c).getBytes()[0];
	}	
	
	public static Pattern not( Pattern excluded ) {
		return new Not(excluded);
	}

	public static Pattern not( char s ) {
		return not(in(s));
	}

	public static Pattern or( Pattern s, Pattern... more ) {
		Pattern or = s;
		for ( Pattern m : more ) {
			or = or(or , m);
		}
		return or;
	}

	public static Pattern or( Pattern a, Pattern b ) {
		if (a == null)
			return b;
		if (b == null)
			return a;
		return new Or(a, b);
	}

	public static Pattern range( char low, char high ) {
		return range(toByte(low) , toByte(high));
	}

	public static Pattern range( byte low, byte high ) {
		return new Set(low, high);
	}

	public static Pattern in( char... cs ) {
		byte[] bs = new byte[cs.length];
		for ( int i = 0; i < bs.length; i++ ) {
			bs[i] = toByte(cs[i]);
		}
		return new In(bs);
	}
	
	public static class Separator implements Pattern {

		@Override
		public int length(ByteBuffer input, int position) {
			int p = position;
			while (p < input.limit() && isIndent(input.get(p))) { p++; }
			int c = p-position;
			return c == 0 ? NOT_MACHTING : c;
		}
		
		@Override
		public String toString() {
			return "^";
		}

	}
	
	static boolean isIndent(int b) {
		return b == ' ' || b == '\t';
	}
	
	public static class Indent implements Pattern {

		@Override
		public int length(ByteBuffer input, int position) {
			int p = position;
			while (p < input.limit() && isIndent(input.get(p))) { p++; }
			return p-position;
		}
		
		
		@Override
		public String toString() {
			return ">>";
		}

	}
	
	public static class Pad implements Pattern {

		@Override
		public int length(ByteBuffer input, int position) {
			int c = 0;
			while ( 
					Character.isWhitespace(input.get(position++))) { c++; }
			return c == 0 ? NOT_MACHTING : c;
		}
		
		@Override
		public String toString() {
			return "~";
		}

	}
	
	static final class Gap implements Pattern {

		@Override
		public int length(ByteBuffer input, int position) {
			int c = 0;
			while (  position < input.limit() &&
					 Character.isWhitespace(input.get(position++))) { c++; }
			return c;
		}
		
		@Override
		public String toString() {
			return ",";
		}
	}

	static final class Whitespace implements Pattern {

		@Override
		public int length(ByteBuffer input, int position) {
			return  position < input.limit() &&
					Character.isWhitespace(input.get(position)) ? 1 : NOT_MACHTING;
		}
		
		@Override
		public String toString() {
			return "_";
		}
		
	}
	
	static final class Wildcard implements Pattern {
		@Override
		public int length(ByteBuffer input, int position) {
			return UTF8.byteLength(input, position);
		}

		@Override
		public String toString() {
			return ".";
		}
	}
	
	private static final class Set
			implements Pattern {

		final byte low;
		final byte high;

		Set( byte low, byte high ) {
			super();
			this.low = low;
			this.high = high;
		}
		
		@Override
		public int length(ByteBuffer input, int position) {
			if (position >= input.limit())
				return NOT_MACHTING;
			int c = input.get(position);
			return c >= low && c <= high ? 1 : NOT_MACHTING;
		}
		
		@Override
		public String toString() {
			return Grammar.print(low) + " - " + Grammar.print(high);
		}
	}

	private static final class Not
			implements Pattern {

		private final Pattern excluded;

		Not( Pattern excluded ) {
			super();
			this.excluded = excluded;
		}

		@Override
		public int length(ByteBuffer input, int position) {
			int l = excluded.length(input, position);
			return l < 0 ? UTF8.byteLength(input, position) : NOT_MACHTING;
		}

		@Override
		public String toString() {
			return "!" + excluded;
		}
	}

	private static final class In
			implements Pattern {

		private final byte[] members;

		In( byte[] members ) {
			super();
			this.members = members;
		}

		@Override
		public int length(ByteBuffer input, int position) {
			if (position >= input.limit()) {
				return NOT_MACHTING;
			}
			byte c = input.get(position);
			for ( int i = 0; i < members.length; i++ ) {
				if ( members[i] == c ) {
					return 1;
				}
			}
			return NOT_MACHTING;
		}

		@Override
		public String toString() {
			StringBuilder b = new StringBuilder();
			for ( byte c : members ) {
				b.append(" ").append(Grammar.print(c));
			}
			return "{"+ b.substring(1) +"}";
		}
	}

	private static final class Or
			implements Pattern {

		private final Pattern a;
		private final Pattern b;

		Or( Pattern a, Pattern b ) {
			super();
			this.a = a;
			this.b = b;
		}
		
		@Override
		public int length(ByteBuffer input, int position) {
			return Math.max(a.length(input, position), b.length(input, position));
		}

		@Override
		public String toString() {
			String as = a.toString();
			if (a instanceof Or || a instanceof In) {
				as = as.substring(1, as.length()-1);
			}
			String bs = b.toString();
			if (b instanceof Or || b instanceof In) {
				bs = bs.substring(1, bs.length()-1);
			}
			return "{"+ as + " " + bs+"}";
		}

	}
}
