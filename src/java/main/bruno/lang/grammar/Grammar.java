package bruno.lang.grammar;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;


/**
 * The *data* model of a formal grammar. 
 * 
 * A grammar is literally a set of named {@link Rule}s.
 */
public final class Grammar implements Iterable<Grammar.Rule>{

	public static enum RuleType {
		LITERAL, CHARACTER_SET, PATTERN, REPETITION, SEQUENCE, ALTERNATIVES, FILL, LOOKAHEAD, CAPTURE, DECISION,
		// Initialization only...
		INCLUDE;
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
		char n0 = name.charAt(0);
		boolean noCapture = n0 == '-' || n0 == '\\';
		for (Rule r : rules) {
			if (r != null) {
				if (r.name.equals(name)) {
					return r;
				}
				if ( name.equals("-"+r.name)) {
					return noCapture && r.type == RuleType.CAPTURE ? r.elements[0].as(name) : r;
				}
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
				if (type == RuleType.SEQUENCE || e.type == RuleType.ALTERNATIVES) {
					s = s.substring(1, s.length()-1);
				}
				b.append(s);
				b.append(' ');
				b.append('\n');
			}
		}
		b.append("-\n");
		return b.toString();
	}

	public static final class Rule {

		private static final Rule[] NO_ELEMENTS = new Rule[0];
		private static final byte[] NO_LITERAL = new byte[0];

		public static final Rule DECISION = new Rule(RuleType.DECISION, false, "", new Rule[0], Occur.never, NO_LITERAL, null, null);

		public static Rule lookahead(Rule ahead) {
			return new Rule(RuleType.LOOKAHEAD, false, "", new Rule[] { ahead }, Occur.once, NO_LITERAL, null, null);
		}

		public static Rule decision() {
			return DECISION;
		}
		
		public static Rule fill() {
			return new Rule(RuleType.FILL, false, "", new Rule[1], Occur.once, NO_LITERAL, null, null);
		}

		public static Rule include(String name) {
			return new Rule(RuleType.INCLUDE, false, name, NO_ELEMENTS, Occur.once, NO_LITERAL, null, null);
		}

		public static Rule alt(Rule...elements) {
			return new Rule(RuleType.ALTERNATIVES, false, "", elements, Occur.once, NO_LITERAL, null, null);
		}

		public static Rule seq(Rule...elements) {
			complete(elements);
			return new Rule(RuleType.SEQUENCE, false, "", elements, Occur.once, NO_LITERAL, null, null);
		}

		public static Rule symbol( int codePoint ) {
			return string(new String(UTF8.bytes(codePoint)));
		}

		public static Rule literal(byte[] l) {
			return new Rule(RuleType.LITERAL, false, "", NO_ELEMENTS, Occur.once, l, null, null);
		}
		
		public static Rule string(String l) {
			return new Rule(RuleType.LITERAL, false, "", NO_ELEMENTS, Occur.once, UTF8.bytes(l), null, null);
		}

		public static Rule pattern(Pattern p) {
			return new Rule(RuleType.PATTERN, false, "", NO_ELEMENTS, Occur.once, NO_LITERAL, null, p);
		}

		public static Rule charset(CharacterSet t) {
			return new Rule(RuleType.CHARACTER_SET, false, "", NO_ELEMENTS, Occur.once, NO_LITERAL, t, null);
		}
		
		public static Rule charset(CharacterSet t, String... refs) {
			if (refs.length == 0)
				return charset(t);
			Rule[] refRules = new Rule[refs.length];
			for (int i = 0; i < refs.length; i++) {
				refRules[i] = include(refs[i]);
			}
			return new Rule(RuleType.CHARACTER_SET, false, "", refRules, Occur.once, NO_LITERAL, t, null);
		}

		public final RuleType type;
		public final boolean substitute;
		public final String name;
		public final Rule[] elements;
		public final Occur occur;
		public final byte[] literal;
		public final CharacterSet charset;
		public final Pattern pattern;

		private Rule(RuleType type, boolean substitute, String name, Rule[] elements, Occur occur, byte[] literal, CharacterSet terminal, Pattern pattern) {
			super();
			this.type = type;
			this.substitute = substitute;
			this.name = name.intern();
			this.elements = elements;
			this.occur = occur;
			this.literal = literal;
			this.charset = terminal;
			this.pattern = pattern;
		}

		public Rule is(String name) {
			return named(name, false);
		}
		
		public Rule as(String name) {
			return named(name, true);
		}
		
		public Rule named(String name, boolean unique) {
			Rule[] elems = type == RuleType.CAPTURE ? elements : new Rule[] { this };
			return new Rule(RuleType.CAPTURE, unique, name, elems, Occur.once, NO_LITERAL, null, null);
		}

		public Rule plus() {
			return occurs(Occur.plus);
		}

		public Rule star() {
			return occurs(Occur.star);
		}

		public Rule qmark() {
			return occurs(Occur.qmark);
		}
		
		public Rule occurs(Occur occur) {
			if (type == RuleType.REPETITION) {
				return occur == Occur.once ? elements[0] : new Rule(RuleType.REPETITION, false, name, elements, occur, literal, charset, pattern);
			}
			if (occur == Occur.once)
				return this;
			return new Rule(RuleType.REPETITION, false, "", new Rule[] { this }, occur, NO_LITERAL, null, null);
		}
		
		private static void complete(Rule[] elements) {
			for (int i = 0; i < elements.length-1; i++) {
				RuleType t = elements[i].type;
				if (t == RuleType.FILL) {
					elements[i].elements[0] = elements[i+1];
				} else if (t == RuleType.CAPTURE && elements[i].elements[0].type == RuleType.FILL) {
					elements[i].elements[0].elements[0] = elements[i+1];
				}
			}
		}		

		@Override
		public String toString() {
			return toString(this, new Rule[0]);
		}

		public String toString(Rule root, Rule[] rules) {
			switch (type) {
			case CAPTURE:
				if (substitute) {
					// also try to resolve the original rule
					return elements[0].toString(root, rules)+"@"+name;
				}
				return name;
			case DECISION:
				return "<";
			case CHARACTER_SET:
				//FIXME cannot use this as this also substitutes the rule definition itself
				for (Rule r : rules) {
					if (r.elements[0].charset == charset && r.elements[0] != root) {
						return "\\"+r.name;
					}
				}
				return charset.toString();
			case PATTERN:
				return pattern.toString();
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
				return "("+elements[0].toString(root, rules)+")>";
			case ALTERNATIVES: {
				StringBuilder b = new StringBuilder();
				for (int i = 0; i<  elements.length; i++) {
					b.append(" | ").append(elements[i].toString(root, rules));
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