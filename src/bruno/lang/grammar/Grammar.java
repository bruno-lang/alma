package bruno.lang.grammar;

import java.util.Arrays;
import java.util.NoSuchElementException;


/**
 * The *data* model of a formal grammar. 
 * 
 * @author jan
 */
public final class Grammar {

	private final Rule[] rules;

	public Grammar(Rule... namedRules) {
		super();
		this.rules = namedRules;
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
			b.append(String.format("%-"+l+"s: ", r.name));
			for (Rule elem : r.elements) {
				String s = elem.toString();
				RuleType type = elem.type;
				if (type == RuleType.SEQUENCE || elem.type == RuleType.SELECTION) {
					s = s.substring(1, s.length()-1);
				}
				b.append(s);
				b.append(' ');
			}
			b.append('\n');
			}
		}
		return b.toString();
	}

	public static enum RuleType {
		LITERAL("lit"), TERMINAL("trm"), PATTERN("pat"), ITERATION("itr"), SEQUENCE("seq"), SELECTION("sel"), COMPLETION("cpl"), REFERENCE("ref"), CAPTURE("cap");

		public final String code;

		private RuleType(String code) {
			this.code = code;
		}

	}

	public static final class Rule {

		private static final Rule[] NO_ELEMENTS = new Rule[0];
		private static final byte[] NO_LITERAL = new byte[0];

		public static Rule completion() {
			return new Rule(RuleType.COMPLETION, "", new Rule[1], Occur.once, NO_LITERAL, null, null);
		}

		public static Rule ref(String name) {
			return new Rule(RuleType.REFERENCE, name, NO_ELEMENTS, Occur.once, NO_LITERAL, null, null);
		}

		public static Rule selection(Rule...elements) {
			return new Rule(RuleType.SELECTION, "", elements, Occur.once, NO_LITERAL, null, null);
		}

		public static Rule seq(Rule...elements) {
			complete(elements);
			return new Rule(RuleType.SEQUENCE, "", elements, Occur.once, NO_LITERAL, null, null);
		}

		public static Rule symbol( int codePoint ) {
			return string(new String(UTF8.bytes(codePoint)));
		}

		public static Rule string(String l) {
			return new Rule(RuleType.LITERAL, "", NO_ELEMENTS, Occur.once, l.getBytes(), null, null);
		}

		public static Rule pattern(Pattern p) {
			return new Rule(RuleType.PATTERN, "", NO_ELEMENTS, Occur.once, NO_LITERAL, null, p);
		}

		public static Rule terminal(Terminal t) {
			return new Rule(RuleType.TERMINAL, "", NO_ELEMENTS, Occur.once, NO_LITERAL, t, null);
		}

		public final RuleType type;
		public final String name;
		public final Rule[] elements;
		public final Occur occur;
		public final byte[] literal;
		public final Terminal terminal;
		public final Pattern pattern;

		private Rule(RuleType type, String name, Rule[] elements, Occur occur, byte[] literal, Terminal terminal, Pattern pattern) {
			super();
			this.type = type;
			this.name = name.intern();
			this.elements = elements;
			this.occur = occur;
			this.literal = literal;
			this.terminal = terminal;
			this.pattern = pattern;
		}

		public Rule as(String name) {
			if (name.length() > 0 && name.charAt(0) == '-') {
				return new Rule(type, name, elements, occur, literal, terminal, pattern);
			}
			Rule[] elems = type == RuleType.CAPTURE ? elements : new Rule[] { this };
			return new Rule(RuleType.CAPTURE, name, elems, Occur.once, NO_LITERAL, null, null);
		}

		public Rule plus() {
			return occurs(Occur.plus);
		}

		public Rule occurs(Occur occur) {
			if (type == RuleType.ITERATION) {
				return occur == Occur.once ? elements[0] : new Rule(RuleType.ITERATION, name, elements, occur, literal, terminal, pattern);
			}
			if (occur == Occur.once)
				return this;
			return new Rule(RuleType.ITERATION, "", new Rule[] { this }, occur, NO_LITERAL, null, null);
		}

		@Override
		public String toString() {
			if (type == RuleType.CAPTURE) {
				return name;
			}
			if (type == RuleType.TERMINAL) {
				return terminal.toString();
			}
			if (type == RuleType.PATTERN) {
				return pattern.toString();
			}
			if (type == RuleType.LITERAL) {
				if (UTF8.characters(literal) == 1) {
					return UTF8.toLiteral(UTF8.codePoint(literal));
				}
				return "'"+ new String(literal)+"'";
			}
			if (type == RuleType.REFERENCE) {
				return "@"+name;
			}
			if (type == RuleType.COMPLETION) {
				return "..`"+elements[0]+"`";
			}
			if (type == RuleType.SELECTION) {
				StringBuilder b = new StringBuilder();
				for (Rule e : elements) {
					b.append(" | ").append(e);
				}
				return "("+b.substring(3)+")";
			}
			if (type == RuleType.SEQUENCE) {
				StringBuilder b = new StringBuilder();
				for (Rule e : elements) {
					b.append(" ").append(e);
				}
				return "("+b.substring(1)+")";
			}
			if (type == RuleType.ITERATION) {
				if (occur == Occur.qmark && elements[0].type == RuleType.SEQUENCE) {
					String seq = elements[0].toString();
					return "["+seq.substring(1, seq.length()-1)+"]";
				}
				String iter= occur.toString();
				return elements[0]+iter;
			}
			return type+" "+Arrays.toString(elements);
		}

		public Rule star() {
			return occurs(Occur.star);
		}

		public Rule qmark() {
			return occurs(Occur.qmark);
		}

		private static void complete(Rule[] elements) {
			for (int i = 0; i < elements.length-1; i++) {
				RuleType t = elements[i].type;
				if (t == RuleType.COMPLETION) {
					elements[i].elements[0] = elements[i+1];
				} else if (t == RuleType.CAPTURE && elements[i].elements[0].type == RuleType.COMPLETION) {
					elements[i].elements[0].elements[0] = elements[i+1];
				}
			}
		}
	}

}
