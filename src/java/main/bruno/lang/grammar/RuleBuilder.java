package bruno.lang.grammar;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import bruno.lang.grammar.Grammar.Rule;
import bruno.lang.grammar.Grammar.RuleType;

/**
 * Builds a {@link Grammar} from a given {@link Parsed} grammar file.
 *  
 * @author jan
 */
public final class RuleBuilder {

	/**
	 * Used to indicate the distinct from index as a {@link Rule} that is
	 * detected by its identity. It has just this workaround helper
	 * functionality within the builder.
	 */
	private static final Rule DISTINCT_FROM = Rule.seq();
	
	public static Rule[] buildRules(Parsed grammar) {
		final List<Rule> rules = new ArrayList<>();
		final ParseTree tokens = grammar.tree;
		final int count = tokens.count();
		int token = 0;
		while (token < count) {
			Rule r = tokens.rule(token);
			if (r == Alma.grammar_) {
				token++;
			} else if (r == Alma.member_) {
				if (tokens.rule(token+1) == Alma.rule_) {
					Rule rule = buildRule(token+1, grammar);
					rules.add(rule);
				}
				token = tokens.next(token);
			}
		}
		return rules.toArray(new Rule[0]);
	}

	private static Rule buildRule(int token, Parsed grammar) {
		check(token, grammar, Alma.rule_);
		String name = grammar.text(token+1);
		if (name.matches("[zZ179@#_,~.^$+*?]")) {
			throw new IllegalArgumentException("A rule cannot be named "+name+" as this is a control sequence in lingukit.");
		}
		return buildSelection(token+2, grammar).as(name);
	}

	private static Rule buildSelection(int token, Parsed grammar) {
		check(token, grammar, Alma.selection_);
		final List<Rule> alternatives = new ArrayList<>();
		final ParseTree tokens = grammar.tree;
		final int count = tokens.count();
		final int end = tokens.end(token)+1;
		int i = token+1;
		while (i < count && tokens.rule(i) == Alma.sequence_ && tokens.end(i) <= end) {
			alternatives.add(buildSequence(i, grammar));
			i = tokens.next(i);
		}
		if (alternatives.size() == 1) {
			return alternatives.get(0);
		}
		return Rule.selection(alternatives.toArray(new Rule[0]));
	}

	private static Rule buildSequence(int token, Parsed grammar) {
		check(token, grammar, Alma.sequence_);
		final List<Rule> elems = new ArrayList<>();
		final ParseTree tokens = grammar.tree;
		final int count = tokens.count();
		final int end = tokens.end(token)+1;
		int distinctFrom = Rule.UNDECIDED;
		int i = token+1;
		while (i < count && tokens.rule(i) == Alma.element_ && tokens.end(i) <= end) {
			Rule e = buildElement(i, grammar);
			if (e != DISTINCT_FROM) {
				elems.add(e);
			} else {
				distinctFrom = elems.size();
			}
			i = tokens.next(i);
		}
		if (elems.size() == 1) {
			return elems.get(0);
		}
		return Rule.seq(elems.toArray(new Rule[0])).decisionAt(distinctFrom);
	}

	private static Rule buildElement(int token, Parsed grammar) {
		check(token, grammar, Alma.element_);
		final ParseTree tokens = grammar.tree;
		Occur occur = buildOccur(tokens.next(token+1), grammar, token);
		Rule r = tokens.rule(token+1);
		if (r == Alma.decision_) {
			return DISTINCT_FROM;
		}
		if (r == Alma.completion_) {
			return Rule.completion();
		}
		if (r == Alma.group_) {
			return buildCapture(tokens.next(token+2), grammar, buildSelection(token+2, grammar)).occurs(occur);
		}
		if (r == Alma.option_) {
			return buildCapture(tokens.next(token+2), grammar, buildSelection(token+2, grammar)).occurs(Occur.qmark);
		}
		if (r == Alma.lookahead_) {
			return buildLookahead(token+1, grammar);
		}
		if (r == Alma.terminal_) {
			Rule t = buildTerminal(token+1, grammar).occurs(occur);
			// a terminal of a single character -> use literal instead
			if (t.type == RuleType.TERMINAL && t.terminal.isSingleCharacter() && t.terminal.ranges[0] >= 0) { 
				return Rule.string(new String(UTF8.bytes(t.terminal.ranges[0]))).occurs(occur);
			}
			return t;
		}
		if (r == Alma.string_) {
			String text = grammar.text(token+1);
			return Rule.string(text.substring(1, text.length()-1)).occurs(occur);
		}
		if (r == Alma.ref_) {
			return buildRef(token+1, grammar).occurs(occur);
		}
		throw unexpectedRule(r);
	}

	private static Rule buildLookahead(int token, Parsed grammar) {
		check(token, grammar, Alma.lookahead_);
		return Rule.lookahead(buildSelection(token+1, grammar));
	}

	private static Rule buildRef(int token, Parsed grammar) {
		return buildCapture(token+2, grammar, Rule.ref(grammar.text(token+1)));
	}

	private static Rule buildCapture(int token, Parsed grammar, Rule rule) {
		if (grammar.tree.rule(token) == Alma.capture_) {
			return rule.as(grammar.text(token+1));
		}
		return rule;
	}

	private static Rule buildTerminal(int token, Parsed grammar) {
		check(token, grammar, Alma.terminal_);
		Rule r = grammar.tree.rule(token+1);
		if (r == Alma.ranges_) {
			return buildRanges(token+1, grammar);
		}
		if (r == Alma.figures_) {
			return buildFigures(token+1, grammar);
		}
		if (r == Alma.pattern_) {
			return buildPattern(token+1, grammar);
		}
		throw unexpectedRule(r);
	}

	private static Rule buildPattern(int token, Parsed grammar) {
		check(token, grammar, Alma.pattern_);
		boolean not = grammar.tree.rule(token+1) == Alma.not_;
		Rule p = patternSelection(token+(not?2:1), grammar);
		return not ? Rule.pattern(Patterns.not(p.pattern)) : p;
	}

	private static Rule patternSelection(int token, Parsed grammar) {
		Rule r = grammar.tree.rule(token);
		if (r == Alma.gap_) {
			return Rule.pattern(Patterns.GAP);
		}
		if (r == Alma.pad_) {
			return Rule.pattern(Patterns.PAD);
		}
		if (r == Alma.indent_) {
			return Rule.pattern(Patterns.INDENT);
		}
		if (r == Alma.separator_) {
			return Rule.pattern(Patterns.SEPARATOR);
		}
		if (r == Alma.wrap_) {
			return Rule.pattern(Patterns.WRAP);
		}
		throw unexpectedRule(r);
	}

	private static Rule buildFigures(int token, Parsed grammar) {
		check(token, grammar, Alma.figures_);
		boolean not = grammar.tree.rule(token+1) == Alma.not_;
		
		final ParseTree tokens = grammar.tree;
		final int count = tokens.count();
		final int end = tokens.end(token);
		if (not) token++;
		Terminal terminal = Terminal.EMPTY;
		int i = token+1;
		List<String> refs = new ArrayList<>();
		while (i < count && tokens.end(i) <= end && tokens.rule(i) != Alma.capture_) {
			Rule figure = tokens.rule(i);
			if (figure == Alma.ranges_) {
				Rule ranges = buildRanges(i, grammar);
				terminal = terminal.and(ranges.terminal);
			} else if (figure == Alma.name_) {
				refs.add(grammar.text(i));
			}
			i = tokens.next(i);
		}
		if (not)
			terminal = terminal.not();
		//when there are refs in a charset we create a Rule with an array of elements, first the terminal, than refs to all that should be included. 
		return buildCapture(i, grammar, Rule.terminal(terminal, refs.toArray(new String[0])));
	}

	private static Rule buildRanges(int token, Parsed grammar) {
		check(token, grammar, Alma.ranges_);
		return rangesSelection(token +1, grammar);
	}

	private static Rule rangesSelection(int token, Parsed grammar) {
		Rule r = grammar.tree.rule(token);
		if (r == Alma.wildcard_) {
			return Rule.terminal(Terminal.WILDCARD);
		}
		if (r == Alma.letter_) {
			return Rule.terminal(Terminal.LETTERS);
		}
		if (r == Alma.upper_) {
			return Rule.terminal(Terminal.UPPER_LETTERS);
		}
		if (r == Alma.lower_) {
			return Rule.terminal(Terminal.LOWER_LETTERS);
		}
		if (r == Alma.hex_) {
			return Rule.terminal(Terminal.HEX_NUMBER);
		}
		if (r == Alma.octal_) {
			return Rule.terminal(Terminal.OCTAL_NUMBER);
		}
		if (r == Alma.binary_) {
			return Rule.terminal(Terminal.BINARY_NUMBER);
		}
		if (r == Alma.digit_) {
			return Rule.terminal(Terminal.DIGITS);
		}
		if (r == Alma.category_) {
			//TODO
			throw new UnsupportedOperationException("Not available yet");
		}
		if (r == Alma.range_) {
			return Rule.terminal(Terminal.range(buildLiteral(token+1, grammar), buildLiteral(token+3, grammar)));
		}
		if (r == Alma.literal_) {
			return Rule.terminal(Terminal.character(buildLiteral(token, grammar)));
		}
		if (r == Alma.whitespace_) {
			return Rule.terminal(Terminal.WHITESPACE);
		}
		if (r == Alma.shortname_) {
			String name = grammar.text(token+1);
			int c = name.charAt(1);
			if (c == 't') {
				return Rule.terminal(Terminal.character('\t'));
			}
			if (c == 'n') {
				return Rule.terminal(Terminal.character('\n'));
			}
			if (c == 'r') {
				return Rule.terminal(Terminal.character('\r'));
			}
			throw new NoSuchElementException(name);
		}
		throw unexpectedRule(r);
	}

	private static int buildLiteral(int token, Parsed grammar) {
		check(token, grammar, Alma.literal_);
		Rule r = grammar.tree.rule(token+1);
		if (r == Alma.symbol_) {
			return grammar.text(token+1).codePointAt(1);
		}
		if (r == Alma.code_point_) {
			return Integer.parseInt(grammar.text(token+1).substring(2), 16);
		}
		throw unexpectedRule(r);
	}

	private static Occur buildOccur(int token, Parsed grammar, int parent) {
		// there might not be an occurrence token or it belongs to a outer parent 
		if (grammar.tree.rule(token) != Alma.occurrence_ || grammar.tree.end(parent) < grammar.tree.end(token)) {
			return Occur.once;
		}
		Rule occur = grammar.tree.rule(token+1);
		if (occur == Alma.plus_) {
			return Occur.plus;
		}
		if (occur == Alma.star_) {
			return Occur.star;
		}
		if (occur == Alma.qmark_) {
			return Occur.qmark;
		}
		if (occur == Alma.quantity_) {
			int min = Integer.parseInt(grammar.text(token+2));
			int max = min;
			if ("to".equals(grammar.tree.rule(token+3).name)) {
				max = Occur.plus.max;
				if ("max".equals(grammar.tree.rule(token+4).name)) {
					max = Integer.parseInt(grammar.text(token+4));
				}
			}
			return Occur.occur(min, max);
		}
		if (occur == Alma.element_) {
			throw new UnsupportedOperationException(occur.toString());
		}
		throw unexpectedRule(occur);
	}
	
	private static RuntimeException unexpectedRule(Rule r) {
		return new RuntimeException("Unexpected rule: "+r);
	}
	
	private static void check(int token, Parsed grammar, Rule expected) {
		if (grammar.tree.rule(token) != expected) {
			throw new RuntimeException("expected "+expected+" but got: "+grammar.tree.rule(token));
		}
	}
}
