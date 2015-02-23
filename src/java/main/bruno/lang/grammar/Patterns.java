package bruno.lang.grammar;

import java.nio.ByteBuffer;

public final class Patterns {

	/**
	 * May be whitespace. <code>,</code>
	 */
	public static final Pattern GAP = new Gap();
	/**
	 * Must be whitespace. <code>;</code>
	 */
	public static final Pattern PAD = new Pad();
	/**
	 * May be indent. <code>></code>
	 */
	public static final Pattern INDENT = new Indent();
	/**
	 * Must be indent. <code>>></code>
	 */
	public static final Pattern SEPARATOR = new Separator();
	/**
	 * Must be line-wrap. <code>.</code>
	 */
	public static final Pattern WRAP = new Wrap();

	
	public static Pattern not( Pattern excluded ) {
		return new Not(excluded);
	}

	public static Pattern or( Pattern a, Pattern b ) {
		if (a == null)
			return b;
		if (b == null)
			return a;
		return new Or(a, b);
	}
	
	static final class Wrap implements Pattern {

		@Override
		public int length(ByteBuffer input, int position) {
			final int l = input.limit();
			int p = position;
			while (p  < l && isIndent(input.get(p))) { p++; }
			if (p >= l) {
				return p - position;
			}
			int w = p;
			while (p < l && isWrap(input.get(p))) { p++; }
			if (w == p) {
				return NOT_MACHTING;
			}
			while (p < l && isIndent(input.get(p))) { p++; }
			return p - position;
		}
		
		private boolean isWrap(int b) {
			return b == '\n' || b == '\r';
		}

		@Override
		public String toString() {
			return ".";
		}
		
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
			return ">>";
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
			return ">";
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
			return ";";
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
			return l < 0 ? UTF8.byteCount(input, position) : NOT_MACHTING;
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
