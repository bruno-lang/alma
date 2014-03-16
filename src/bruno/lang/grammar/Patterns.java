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

	public static byte toByte( char c ) {
		return String.valueOf(c).getBytes()[0];
	}
	
	public static Pattern not( Pattern excluded ) { //TODO allow for not pattern in grammar
		return new Not(excluded);
	}

	public static Pattern or( Pattern a, Pattern b ) {
		if (a == null)
			return b;
		if (b == null)
			return a;
		return new Or(a, b);
	}
	
	static final class Separator implements Pattern {

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
	
	static final class Indent implements Pattern {

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
	
	static final class Pad implements Pattern {

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
	
	static final class Not implements Pattern {

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
			return a+" | "+b;
		}

	}
}
