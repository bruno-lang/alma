package bruno.lang.grammar;

import java.nio.ByteBuffer;

public final class Terminals {

	public static final Terminal comma = new Gap();
	public static final Terminal any = new Any();
	public static final Terminal whitespace = new Whitespace();

	public static byte toByte( char c ) {
		return String.valueOf(c).getBytes()[0];
	}	
	
	public static Terminal not( Terminal excluded ) {
		return new Not(excluded);
	}

	public static Terminal not( char s ) {
		return not(new Is(toByte(s)));
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

	public static Terminal set( char low, char high ) {
		return set(toByte(low) , toByte(high));
	}

	public static Terminal set( byte low, byte high ) {
		return new Set(low, high);
	}

	public static Terminal in( char... cs ) {
		byte[] bs = new byte[cs.length];
		for ( int i = 0; i < bs.length; i++ ) {
			bs[i] = toByte(cs[i]);
		}
		return new In(bs);
	}
	
	public static Terminal ascii( Terminal...terminals ) {
		//TODO
		return new ASCIIs(0L, 0L);
	}
	
	
	
	static final class Gap implements Terminal {

		@Override
		public int length(ByteBuffer input, int position) {
			int c = 0;
			while (Character.isWhitespace(input.get(position++))) { c++; }
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
			return Character.isWhitespace(input.get(position)) ? 1 : 0;
		}
		
		@Override
		public String toString() {
			return "_";
		}
		
	}
	
	static final class Any implements Terminal {
		@Override
		public int length(ByteBuffer input, int position) {
			return utf8length(input, position);
		}

		@Override
		public String toString() {
			return ".";
		}
	}

	/**
	 * Matches any set of ASCII characters indicated by bits in 2 long masks.
	 *  
	 * @author jan
	 */
	private static final class ASCIIs implements Terminal {

		private final long _0_63;
		private final long _64_127;
		
		ASCIIs(long _0_63, long _64_127) {
			super();
			this._0_63 = _0_63;
			this._64_127 = _64_127;
		}

		@Override
		public int length(ByteBuffer input, int position) {
			int p = position;
			while (contains(input.get(p))) {
				p++;
			}
			return p-position;
		}

		private boolean contains(byte code) {
			if (code < 64) {
				if (code < 0)
					return false;
				long m = 1L << code;
				return (m & _0_63) > 0L; 
			}
			long m = 1L << (code - 64);
			return (m & _64_127) > 0L;
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
			int c = input.get(position);
			return c >= low && c <= high ? 1 : 0;
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
			return l > 0 ? 0 : utf8length(input, position);
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
			byte c = input.get(position);
			for ( int i = 0; i < members.length; i++ ) {
				if ( members[i] == c ) {
					return 1;
				}
			}
			return 0;
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
	
	private static final class Is
			implements Terminal {

		private final byte s;

		Is( byte s ) {
			super();
			this.s = s;
		}

		@Override
		public int length(ByteBuffer input, int position) {
			return input.get(position) == s ? 1 : 0;
		}
		
		@Override
		public String toString() {
			return Grammar.print(s);
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
