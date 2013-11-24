package bruno.lang.grammar;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
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

	public static final Occur never = occur(0, 0);
	
	
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
		
		@Override
		public String toString() {
			return "{"+min+":"+max+"}";
		}

	}
	
	public static final Terminal any = new Any();
	public static final Terminal whitespace = new Whitespace();

	public static interface Terminal {

		// maybe like this: int matching(ByteBuffer input, int position); // returns the number of bytes that matches from the starting pos passed.
		boolean matches( byte c );
	}
	
	private static final int NO_CHARACTER = -1;
	
	private final IdentityHashMap<String, Rule> rulesByName;
	private final Rule[] rulesById;
	
	public Grammar(Rule root) {
		super();
		this.rulesByName = new IdentityHashMap<>();
		List<Rule> idOrder = new ArrayList<>();
		idOrder.add(null); // zero index is unused
		init(root, idOrder);
		this.rulesById = idOrder.toArray(new Rule[idOrder.size()]);
		link(rulesByName.values().toArray(new Rule[0]), new HashSet<String>());
	}
	
	private void init(Rule rule, List<Rule> idOrder) {
		if (rule.type == RuleType.LINK) {
			return;
		}
		if (!rule.name.isEmpty()) {
			rulesByName.put(rule.name, rule);
		}
		if (rule.id() == 0) {
			rule.id(idOrder.size());
			idOrder.add(rule);
		}
		for (Rule r : rule.elements) {
			init(r, idOrder);
		}
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
				if (!rule.name.isEmpty()) {
					followed = new HashSet<>(followed);
					followed.add(rule.name);
				}
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
	
	public Rule rule(int id) {
		return rulesById[id];
	}

	public Rule rule(String name) {
		Rule r = rulesByName.get(name);
		if (r != null)
			return r;
		throw new NoSuchElementException("Missing rule: "+name);
	}
	
	@Override
	public String toString() {
		return rulesByName.toString();
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

	public static byte toByte( char c ) {
		return String.valueOf(c).getBytes()[0];
	}
	
	public static enum RuleType {
		CHARACTER("chr"), TERMINAL("trm"), TOKEN("tok"), ITERATION("itr"), SEQUENCE("seq"), SELECTION("sel"), LINK("lnk"), CAPTURE("cap");
		
		public final String code;

		private RuleType(String code) {
			this.code = code;
		}
	}

	public static final class Rule {
		
		public static Rule link(String name) {
			return new Rule(RuleType.LINK, name, new Rule[0], once, null, NO_CHARACTER);
		}
		
		public static Rule selection(Rule...elements) {
			return new Rule(RuleType.SELECTION, "", elements, once, null, NO_CHARACTER);
		}
		
		public static Rule sequence(Rule...elements) {
			return new Rule(RuleType.SEQUENCE, "", elements, once, null, NO_CHARACTER);
		}
		
		public static Rule token(Rule...elements) {
			return new Rule(RuleType.TOKEN, "", elements.length == 1 ? elements : new Rule[] { sequence(elements) }, once, null, NO_CHARACTER);
		}
		
		public static Rule character( char s ) {
			return new Rule(RuleType.CHARACTER, "", new Rule[0], once, null, s);
		}
		
		public static Rule string(String seq) {
			if (seq.length() == 1) {
				return character(seq.charAt(0));
			}
			Rule[] sequence = new Rule[seq.length()];
			for (int i = 0; i < sequence.length; i++) {
				sequence[i] = character(seq.charAt(i));
			}
			return token(sequence);
		}
		
		public static Rule terminal(Terminal...seq) {
			if (seq.length == 1) {
				return terminal(seq[0]);
			}
			Rule[] sequence = new Rule[seq.length];
			for (int i = 0; i < sequence.length; i++) {
				sequence[i] = terminal(seq[i]);
			}
			return token(sequence);
		}

		private static Rule terminal(Terminal terminal) {
			return new Rule(RuleType.TERMINAL, "", new Rule[0], once, terminal, NO_CHARACTER);
		}
		
		public final RuleType type;
		public final String name;
		public final Rule[] elements;
		public final Occur occur;
		public final Terminal terminal;
		public final int character;
		private int id = 0;
		
		public Rule(RuleType type, String name, Rule[] elements,
				Occur occur, Terminal symbol, int character) {
			super();
			this.type = type;
			this.name = name.intern();
			this.elements = elements;
			this.occur = occur;
			this.terminal = symbol;
			this.character = character;
		}
		
		public int id() {
			return id;
		}
		
		public void id(int id) {
			if (this.id == 0) {
				this.id = id;
			}
		}
		
		public Rule as(String name) {
			return new Rule(RuleType.CAPTURE, name, new Rule[] { this }, Grammar.once, null, NO_CHARACTER);
		}
		
		public Rule plus() {
			return occur(plus);
		}
		
		public Rule occur(Occur occur) {
			return new Rule(RuleType.ITERATION, "", new Rule[] { this }, occur, null, NO_CHARACTER);
		}
		
		@Override
		public String toString() {
			return type == RuleType.TERMINAL ? terminal.toString() : name;
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

	private static final class Whitespace implements Terminal {

		@Override
		public boolean matches(byte c) {
			return Character.isWhitespace(c);
		}
		
		@Override
		public String toString() {
			return "(whitespace)";
		}
		
	}
	
	private static final class Any implements Terminal {
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
			implements Terminal {

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
			implements Terminal {

		private final Terminal excluded;

		Not( Terminal excluded ) {
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
			implements Terminal {

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
			implements Terminal {

		private final Terminal a;
		private final Terminal b;

		Or( Terminal a, Terminal b ) {
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
			implements Terminal {

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
