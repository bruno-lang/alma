package bruno.lang.grammar;

import java.util.ArrayList;
import java.util.Arrays;
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

	private static final byte[] NO_CHARACTER = new byte[0];


	private final IdentityHashMap<String, Rule> rulesByName;
	private final Rule[] rulesById;
	
	public Grammar(Rule... roots) { //OPEN maybe we need other starting points because of links
		super();
		this.rulesByName = new IdentityHashMap<>();
		List<Rule> idOrder = new ArrayList<>();
		idOrder.add(null); // zero index is unused
		for (Rule root : roots) {
			init(root, idOrder);
		}
		this.rulesById = idOrder.toArray(new Rule[idOrder.size()]);
		link(rulesByName.values().toArray(new Rule[0]), new HashSet<String>());
		complete(roots, null, new HashSet<Rule>());
	}

	private void complete(Rule[] elements, Rule completed, java.util.Set<Rule> followed) {
		for (Rule e : elements) {
			if (!followed.contains(e)) {
				followed.add(e);
				if (e.type == RuleType.COMPLETION) {
					if (e.elements[0] == null) {
						e.elements[0] = completed;
					}
				} else if (e.type == RuleType.SEQUENCE && e.elements.length > 1) {
					for (int i = 0; i < e.elements.length-1; i++) {
						complete(new Rule[] { e.elements[i] }, e.elements[i+1], followed);
					}
				} else {
					complete(e.elements, completed, followed);
				}
			}
		}
	}

	private void init(Rule rule, List<Rule> idOrder) {
		if (rule == null || rule.type == RuleType.REFERENCE) {
			return;
		}
		if (!rule.name.isEmpty()) {
			rulesByName.put(rule.name.substring(rule.name.charAt(0) == '-' ? 1 : 0), rule);
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
		if (rule.type == RuleType.COMPLETION) {
			return;
		}
		//FIXME below: when linked rule uses -minus to not capture the capture has to be unboxed
		Rule[] elements = rule.elements;
		if (elements.length > 0) { 
			if (!followed.contains(rule.name)) {
				if (!rule.name.isEmpty()) {
					followed = new HashSet<>(followed);
					followed.add(rule.name);
				}
				for (int i = 0; i < elements.length; i++) {
					Rule e = elements[i];
						if (e.type == RuleType.REFERENCE) {
							elements[i] = rule(e.name);
						} else {
							link(e, followed);
						}
				}
			} else {
				for (int i = 0; i < elements.length; i++) {
					Rule e = elements[i];
					if (e.type == RuleType.REFERENCE) {
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
		boolean noCapture = name.charAt(0) == '-';
		Rule r = rulesByName.get(name.substring(noCapture ? 1 : 0).intern());
		if (r != null) {
			return noCapture && r.type == RuleType.CAPTURE ? r.elements[0].as(name) : r;
		}
		throw new NoSuchElementException("Missing rule: "+name);
	}
	
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		for (Rule r : rulesById) {
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
		LITERAL("lit"), TERMINAL("trm"), ITERATION("itr"), SEQUENCE("seq"), SELECTION("sel"), COMPLETION("cmp"), REFERENCE("ref"), CAPTURE("cap");
		
		public final String code;

		private RuleType(String code) {
			this.code = code;
		}
		
		public boolean bytes() {
			return this == LITERAL || this == TERMINAL;
		}
	}

	public static final class Rule {
		
		public static final Rule ANY_WHITESPACE = terminal(Terminals.whitespace).occurs(Occur.star);
		public static final Rule EMPTY_STRING = literal('$').occurs(Occur.never);
		
		public static Rule completion() {
			return new Rule(RuleType.COMPLETION, "", new Rule[1], Occur.once, null, NO_CHARACTER);
		}
		
		public static Rule ref(String name) {
			return new Rule(RuleType.REFERENCE, name, new Rule[0], Occur.once, null, NO_CHARACTER);
		}
		
		public static Rule selection(Rule...elements) {
			return new Rule(RuleType.SELECTION, "", elements, Occur.once, null, NO_CHARACTER);
		}
		
		public static Rule seq(Rule...elements) {
			return new Rule(RuleType.SEQUENCE, "", elements, Occur.once, null, NO_CHARACTER);
		}
		
		public static Rule token(Rule...elements) {
			return seq(elements).separate(EMPTY_STRING);
		}
		
		public static Rule literal( char l ) {
			return symbol(String.valueOf(l));
		}
		
		public static Rule symbol(String l) {
			return new Rule(RuleType.LITERAL, "", new Rule[0], Occur.once, null, l.getBytes());
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
			return new Rule(RuleType.TERMINAL, "", new Rule[0], Occur.once, terminal, NO_CHARACTER);
		}
		
		public final RuleType type;
		public final String name;
		public final Rule[] elements;
		public final Occur occur;
		public final Terminal terminal;
		public final byte[] literal;
		private int id = 0;
		
		public Rule(RuleType type, String name, Rule[] elements, Occur occur, Terminal terminal, byte[] literal) {
			super();
			this.type = type;
			this.name = name.intern();
			this.elements = elements;
			this.occur = occur;
			this.terminal = terminal;
			this.literal = literal;
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
			if (name.length() > 0 && name.charAt(0) == '-') {
				return new Rule(type, name, elements, occur, terminal, literal);
			}
			Rule[] elems = type == RuleType.CAPTURE ? elements : new Rule[] { this };
			return new Rule(RuleType.CAPTURE, name, elems, Occur.once, null, NO_CHARACTER);
		}
		
		public Rule plus() {
			return occurs(Occur.plus);
		}
		
		public Rule occurs(Occur occur) {
			if (type == RuleType.ITERATION) {
				return occur == Occur.once ? elements[0] : new Rule(RuleType.ITERATION, name, elements, occur, terminal, literal);
			}
			if (occur == Occur.once)
				return this;
			return new Rule(RuleType.ITERATION, "", new Rule[] { this }, occur, null, NO_CHARACTER);
		}
		
		public Rule separate(Rule separation) {
			return new Rule(type, name, elements, occur, terminal, literal);
		}
		
		@Override
		public String toString() {
			if (type == RuleType.CAPTURE) {
				return name;
			}
			if (type == RuleType.TERMINAL) {
				return terminal.toString();
			}
			if (type == RuleType.LITERAL) {
				return "'"+ new String(literal)+"'";
			}
			if (type == RuleType.REFERENCE) {
				return "@"+name;
			}
			if (type == RuleType.COMPLETION) {
				return "..["+elements[0]+"]";
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

	}

	static String print( byte character ) {
		return "'" + Character.valueOf((char) character) + "'";
	}

}
