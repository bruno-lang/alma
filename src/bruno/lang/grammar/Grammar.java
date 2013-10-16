package bruno.lang.grammar;

import java.util.Arrays;
import java.util.HashSet;
import java.util.NoSuchElementException;


/**
 * The model of a formal grammar. 
 *  
 * @author jan
 */
public final class Grammar {

	/**
	 * <pre>
	 * { min, max }
	 * </pre>
	 */
	public static Occur occur( int min, int max ) {
		return new Occur(min, max);
	}

	/**
	 * <pre>
	 * *
	 * </pre>
	 */
	public static final Occur once = occur(1 , 1);

	/**
	 * <pre>
	 * *
	 * </pre>
	 */
	public static final Occur star = occur(0 , 1000);

	/**
	 * <pre>
	 * +
	 * </pre>
	 */
	public static final Occur plus = occur(1 , 1000);

	/**
	 * <pre>
	 * ?
	 * </pre>
	 */
	public static final Occur qmark = occur(0 , 1);

	public static final class Occur {

		public final int min;
		public final int max;

		Occur( int min, int max ) {
			super();
			this.min = min;
			this.max = max;
		}

	}
	
	public static final Symbol any = new Any();

	public static interface Symbol {

		boolean matches( byte c );
	}
	
	public final Rule[] rules;
	
	public Grammar(Rule... rules) {
		super();
		this.rules = rules;
		link(rules, new HashSet<String>());
	}
	
	private void link(Rule[] rules, java.util.Set<String> followed) {
		for (int i = 0; i < rules.length; i++) {
			link(rules[i], followed);
		}
	}

	private void link(Rule rule, java.util.Set<String> followed) {
		Rule[] elements = rule.elements;
		if (elements.length > 0) { 
			if (!followed.contains(rule.name)) {
				followed = new HashSet<>(followed);
				followed.add(rule.name);
				for (int i = 0; i < elements.length; i++) {
					Rule e = elements[i];
					if (e.type == RuleType.LINK) {
						elements[i] = rule(e.name);
					} else {
						link(e, followed);
					}
				}
			} else {
				for (int i = 0; i < elements.length; i++) {
					Rule e = elements[i];
					if (e.type == RuleType.LINK) {
						elements[i] = rule(e.name);
					}
				}
			}
		}
	}

	public Rule rule(String name) {
		for (Rule r : rules) {
			if (r.name == name)
				return r;
		}
		throw new NoSuchElementException("Missing rule: "+name);
	}
	
	@Override
	public String toString() {
		return Arrays.toString(rules);
	}

	public static Symbol symbol( char s ) {
		return new Is(toByte(s));
	}

	public static Symbol not( Symbol excluded ) {
		return new Not(excluded);
	}

	public static Symbol not( char s ) {
		return not(symbol(s));
	}

	public static Symbol or( Symbol s, Symbol... more ) {
		Symbol or = s;
		for ( Symbol m : more ) {
			or = or(or , m);
		}
		return or;
	}

	public static Symbol or( Symbol a, Symbol b ) {
		return new Or(a, b);
	}

	public static Symbol set( char low, char high ) {
		return set(toByte(low) , toByte(high));
	}

	public static Symbol set( byte low, byte high ) {
		return new Set(low, high);
	}

	public static Symbol in( char... cs ) {
		byte[] bs = new byte[cs.length];
		for ( int i = 0; i < bs.length; i++ ) {
			bs[i] = toByte(cs[i]);
		}
		return new In(bs);
	}

	public static byte toByte( char c ) {
		return String.valueOf(c).getBytes()[0];
	}
	
	static enum RuleType {
		SYMBOL("sym"), TOKEN("tok"), REPETITION("rep"), SEQUENCE("seq"), DECISION("dec"), LINK("lnk");
		public final String code;

		private RuleType(String code) {
			this.code = code;
		}
		
	}

	public static class Rule {
		
		public static Rule link(String name) {
			return new Rule(RuleType.LINK, name, new Rule[0], once, null);
		}
		
		public static Rule rule(String name, Rule...elements) {
			return new Rule(RuleType.SEQUENCE, name, elements, once, null);
		}
		
		public static Rule token(String name, Rule...elements) {
			return new Rule(RuleType.TOKEN, name, elements.length == 1 ? elements : new Rule[] { rule("", elements) }, once, null);
		}
		
		public static Rule terminal(String symbols) {
			Symbol[] sequence = new Symbol[symbols.length()];
			for (int i = 0; i < sequence.length; i++) {
				sequence[i] = symbol(symbols.charAt(i));
			}
			return terminal(sequence);
		}
		
		public static Rule terminal(Symbol...symbols) {
			if (symbols.length == 1) {
				return terminal(symbols[0]);
			}
			Rule[] sequence = new Rule[symbols.length];
			for (int i = 0; i < sequence.length; i++) {
				sequence[i] = terminal(symbols[i]);
			}
			return token("", sequence);
		}

		private static Rule terminal(Symbol symbol) {
			return new Rule(RuleType.SYMBOL, "", new Rule[0], once, symbol);
		}
		
		public final RuleType type;
		public final String name;
		public final Rule[] elements;
		public final Occur occur;
		public final Symbol symbol;
		
		public Rule(RuleType type, String name, Rule[] elements,
				Occur occur, Symbol symbol) {
			super();
			this.type = type;
			this.name = name.intern();
			this.elements = elements;
			this.occur = occur;
			this.symbol = symbol;
		}
		
		public Rule decision() {
			return new Rule(RuleType.DECISION, name, elements, occur, symbol);
		}

		public Rule plus() {
			return occur(plus);
		}
		
		public Rule occur(Occur occur) {
			return new Rule(RuleType.REPETITION, "", new Rule[] { this }, occur, null);
		}
		
		@Override
		public String toString() {
			return type == RuleType.SYMBOL ? symbol.toString() : name;
		}

		public Rule star() {
			return occur(star);
		}

		public Rule qmark() {
			return occur(qmark);
		}
	}


	static String print( byte character ) {
		return "'" + Character.valueOf((char) character) + "'";
	}

	private static final class Any implements Symbol {
		@Override
		public boolean matches(byte c) {
			return true;
		}
		
		@Override
		public String toString() {
			return ".";
		}
	}
	
	private static final class Set
			implements Symbol {

		final byte low;
		final byte high;

		Set( byte low, byte high ) {
			super();
			this.low = low;
			this.high = high;
		}

		@Override
		public boolean matches( byte c ) {
			return c >= low && c <= high;
		}
		
		@Override
		public String toString() {
			return Grammar.print(low) + " - " + Grammar.print(high);
		}
	}

	private static final class Not
			implements Symbol {

		private final Symbol excluded;

		Not( Symbol excluded ) {
			super();
			this.excluded = excluded;
		}

		@Override
		public boolean matches( byte c ) {
			return !excluded.matches(c);
		}

		@Override
		public String toString() {
			return "!" + excluded;
		}
	}

	private static final class In
			implements Symbol {

		private final byte[] members;

		In( byte[] members ) {
			super();
			this.members = members;
		}

		@Override
		public boolean matches( byte c ) {
			for ( int i = 0; i < members.length; i++ ) {
				if ( members[i] == c ) {
					return true;
				}
			}
			return false;
		}

		@Override
		public String toString() {
			StringBuilder b = new StringBuilder();
			for ( byte c : members ) {
				b.append(" | ").append(Grammar.print(c));
			}
			return b.substring(3);
		}
	}

	private static final class Or
			implements Symbol {

		private final Symbol a;
		private final Symbol b;

		Or( Symbol a, Symbol b ) {
			super();
			this.a = a;
			this.b = b;
		}

		@Override
		public boolean matches( byte c ) {
			return a.matches(c) || b.matches(c);
		}

		@Override
		public String toString() {
			return a + " | " + b;
		}

	}

	private static final class Is
			implements Symbol {

		private final byte s;

		Is( byte s ) {
			super();
			this.s = s;
		}

		@Override
		public boolean matches( byte c ) {
			return c == s;
		}
		
		@Override
		public String toString() {
			return Grammar.print(s);
		}
	}

}
