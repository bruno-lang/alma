package bruno.lang.grammar;

import java.nio.ByteBuffer;

public final class Terminals {

	public static final Terminal gap = new Gap();
	public static final Terminal pad = new Pad();
	public static final Terminal indent = new Indent();
	public static final Terminal separator = new Separator();

	public static final Terminal whitespace = new Whitespace();
	public static final Terminal wildcard = new Wildcard();

	public static byte toByte( char c ) {
		return String.valueOf(c).getBytes()[0];
	}	
	
	public static Terminal not( Terminal excluded ) {
		return new Not(excluded);
	}

	public static Terminal not( char s ) {
		return not(in(s));
	}

	public static Terminal or( Terminal s, Terminal... more ) {
		Terminal or = s;
		for ( Terminal m : more ) {
			or = or(or , m);
		}
		return or;
	}

	public static Terminal or( Terminal a, Terminal b ) {
		return new Or(a, b);
	}

	public static Terminal range( char low, char high ) {
		return range(toByte(low) , toByte(high));
	}

	public static Terminal range( byte low, byte high ) {
		return new Set(low, high);
	}

	public static Terminal in( char... cs ) {
		byte[] bs = new byte[cs.length];
		for ( int i = 0; i < bs.length; i++ ) {
			bs[i] = toByte(cs[i]);
		}
		return new In(bs);
	}
	
	public static class Separator implements Terminal {

		@Override
		public int length(ByteBuffer input, int position) {
			int c = 0;
			int b = input.get(position);
			while (b == ' ' || b == '\t') { b = input.get(position+ ++c); }
			return c == 0 ? NOT_MACHTING : c;
		}
		
		@Override
		public String toString() {
			return "^";
		}

	}
	
	public static class Indent implements Terminal {

		static final byte tab = '\t';
		static final byte space = ' ';
		
		@Override
		public int length(ByteBuffer input, int position) {
			int c = position;
			while (c < input.limit() && isIndent(input.get(c))) { c++; }
			return c-position;
		}
		
		private static boolean isIndent(int b) {
			return b == space || b == tab;
		}
		
		@Override
		public String toString() {
			return ">>";
		}

	}
	
	public static class Pad implements Terminal {

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
	
	static final class Gap implements Terminal {

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

	static final class Whitespace implements Terminal {

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
	
	static final class Wildcard implements Terminal {
		@Override
		public int length(ByteBuffer input, int position) {
			return utf8length(input, position);
		}

		@Override
		public String toString() {
			return ".";
		}
	}
	
	private static final class Set
			implements Terminal {

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
			implements Terminal {

		private final Terminal excluded;

		Not( Terminal excluded ) {
			super();
			this.excluded = excluded;
		}

		@Override
		public int length(ByteBuffer input, int position) {
			int l = excluded.length(input, position);
			return l < 0 ? utf8length(input, position) : NOT_MACHTING;
		}

		@Override
		public String toString() {
			return "!" + excluded;
		}
	}

	private static final class In
			implements Terminal {

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
			implements Terminal {

		private final Terminal a;
		private final Terminal b;

		Or( Terminal a, Terminal b ) {
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
	
	public static int utf8length(ByteBuffer input, int position) {
		byte b = input.get(position);
		if (b >= 0)
			return 1;
		int p = position;
		while (input.get(++p) < 0) { ; }
		return p - position;
		
	}
}
