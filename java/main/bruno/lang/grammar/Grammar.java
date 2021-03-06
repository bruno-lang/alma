package bruno.lang.grammar;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;


/**
 * The *data* model of a formal grammar. 
 * 
 * A grammar is literally a set of named {@link Rule}s.
 */
public final class Grammar implements Iterable<Grammar.Rule> {

	public static enum RuleType {
		LITERAL, CHARACTER_SET, WHITESPACE, REPETITION, SEQUENCE, CASCADE, FILL, LOOKAHEAD, CAPTURE, DECISION,
		// Initialization only...
		INCLUDE;
	}
	
	public static enum Whitespace {
		MAY_BE_WS, MUST_BE_WS,
		MAY_BE_INDENT, MUST_BE_INDENT,
		MUST_BE_WRAP
	}
	
	private final Rule[] rules;

	public Grammar(Rule... namedRules) {
		super();
		this.rules = namedRules;
	}
	
	@Override
	public Iterator<Rule> iterator() {
		return Arrays.asList(rules).iterator();
	}

	public Rule rule(String name) {
		for (Rule r : rules) {
			if (r != null && r.name.equals(name)) {
					return r;
			}
		}
		throw new NoSuchElementException("Missing rule: "+name);
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		for (Rule r : rules) {
			if (r != null && !r.name.isEmpty()) {
				int l = 15;
				b.append(String.format("-%-"+l+"s= ", r.name));
				Rule e = r.elements[0];
				String s = e.toString(e, rules);
				RuleType type = e.type;
				if (type == RuleType.SEQUENCE || e.type == RuleType.CASCADE) {
					s = s.substring(1, s.length()-1);
				}
				b.append(s);
				b.append(' ');
				b.append('\n');
			}
		}
		return b.toString();
	}

	public static final class Rule {

		private static final Rule[] NO_ELEMENTS = new Rule[0];
		private static final byte[] NO_LITERAL = new byte[0];

		public static final Rule DECISION = new Rule(RuleType.DECISION, false, "", new Rule[0], Occur.ONCE, NO_LITERAL, null, null);
		public static final Rule LOOKAHEAD = new Rule(RuleType.LOOKAHEAD, false, "", new Rule[0], Occur.ONCE, NO_LITERAL, null, null);
		public static final Rule FILL = new Rule(RuleType.FILL, false, "", new Rule[0], Occur.ONCE, NO_LITERAL, null, null);

		public static Rule lookahead() {
			return LOOKAHEAD; 
		}

		public static Rule decision() {
			return DECISION;
		}
		
		public static Rule fill() {
			return FILL;
		}

		public static Rule include(String name) {
			return new Rule(RuleType.INCLUDE, false, name, NO_ELEMENTS, Occur.ONCE, NO_LITERAL, null, null);
		}

		public static Rule alt(Rule...elements) {
			return new Rule(RuleType.CASCADE, false, "", elements, Occur.ONCE, NO_LITERAL, null, null);
		}

		public static Rule seq(Rule...elements) {
			return new Rule(RuleType.SEQUENCE, false, "", elements, Occur.ONCE, NO_LITERAL, null, null);
		}

		public static Rule literal(byte[] l) {
			return new Rule(RuleType.LITERAL, false, "", NO_ELEMENTS, Occur.ONCE, l, null, null);
		}
		
		public static Rule pattern(Whitespace ws) {
			return new Rule(RuleType.WHITESPACE, false, "", NO_ELEMENTS, Occur.ONCE, NO_LITERAL, null, ws);
		}

		public static Rule charset(CharacterSet t) {
			return new Rule(RuleType.CHARACTER_SET, false, "", NO_ELEMENTS, Occur.ONCE, NO_LITERAL, t, null);
		}

		public final RuleType type;
		public final boolean substitute;
		public final String name;
		public final Rule[] elements;
		public final Occur occur;
		public final byte[] literal;
		public final CharacterSet charset;
		public final Whitespace ws;

		private Rule(RuleType type, boolean substitute, String name, Rule[] elements, Occur occur, byte[] literal, CharacterSet charset, Whitespace ws) {
			super();
			this.type = type;
			this.substitute = substitute;
			this.name = name.intern();
			this.elements = elements;
			this.occur = occur;
			this.literal = literal;
			this.charset = charset;
			this.ws = ws;
		}

		public Rule is(String name) {
			return named(name, false);
		}
		
		public Rule as(String name) {
			return named(name, true);
		}
		
		public Rule named(String name, boolean unique) {
			Rule[] elems = type == RuleType.CAPTURE ? elements : new Rule[] { this };
			return new Rule(RuleType.CAPTURE, unique, name, elems, Occur.ONCE, NO_LITERAL, null, null);
		}
		
		public Rule subst() {
			return new Rule(type, true, name, elements, occur, literal, charset, ws);
		}

		public Rule occurs(Occur occur) {
			if (type == RuleType.REPETITION) {
				return occur == Occur.ONCE ? elements[0] : new Rule(RuleType.REPETITION, false, name, elements, occur, literal, charset, ws);
			}
			if (occur == Occur.ONCE)
				return this;
			return new Rule(RuleType.REPETITION, false, "", new Rule[] { this }, occur, NO_LITERAL, null, null);
		}

		@Override
		public String toString() {
			return toString(this, new Rule[0]);
		}

		public String toString(Rule root, Rule[] rules) {
			switch (type) {
			case CAPTURE:
				if (substitute) { //also show the original rule
					for (Rule r : rules) {
						if (r.elements[0] == elements[0]) {
							return r.name+"@"+name;
						}
					}
					return elements[0].toString(root, rules)+"@"+name;
				}
				return name;
			case DECISION:
				return "<";
			case CHARACTER_SET:
				for (Rule r : rules) {
					if (r.elements[0].charset == charset && r.elements[0] != root) {
						return "\\"+r.name;
					}
				}
				return charset.toString();
			case WHITESPACE:
				switch (ws) {
				case MAY_BE_INDENT: return ",";
				case MUST_BE_INDENT: return ";";
				case MAY_BE_WS: return ".";
				case MUST_BE_WS: return ":";
				case MUST_BE_WRAP: return "!";
				}
			case LITERAL:
				if (UTF8.characters(literal) == 1) {
					return UTF8.toLiteral(UTF8.codePoint(literal));
				}
				return "'"+ new String(literal)+"'";
			case INCLUDE:
				return "\\"+name;
			case FILL:
				return "~";
			case LOOKAHEAD:
				return ">";
			case CASCADE: {
				StringBuilder b = new StringBuilder();
				for (int i = 0; i<  elements.length; i++) {
					String alt = elements[i].toString(root, rules);
					if (alt.startsWith("(") && alt.endsWith(")")) {
						alt = alt.substring(1, alt.length()-1);
					}
					b.append(" | ").append(alt);
				}
				return "("+b.substring(3)+")";
			}
			case SEQUENCE: {
				StringBuilder b = new StringBuilder();
				for (int i = 0; i <  elements.length; i++) {
					b.append(" ").append(elements[i].toString(root, rules));
				}
				return "("+b.substring(1)+")";
			}
			case REPETITION:
				return elements[0].toString(root, rules)+occur.toString();
			default: 
				throw new UnsupportedOperationException("Forgot to implement type: "+type);
			}
		}

		public boolean isDecisionMaking() {
			if (type != RuleType.SEQUENCE)
				return false;
			for (int i = 0; i < elements.length; i++)
				if (elements[i] == DECISION)
					return true;
			return false;
		}

	}
}