package bruno.lang.grammar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import bruno.lang.grammar.Grammar.Rule;
import bruno.lang.grammar.Grammar.RuleType;

/**
 * Builds a {@link Grammar} from a given {@link Tokenised} grammar file.
 *  
 * @author jan
 */
public final class Builder {

	public static Rule[] grammar(Tokenised grammar) {
		final List<Rule> rules = new ArrayList<>();
		final Tokens tokens = grammar.tokens;
		final int c = tokens.count();
		int token = 0;
		while (token < c) {
			Rule r = tokens.rule(token);
			if (r == NOA.grammar) {
				token++;
			} else if (r == NOA.member) {
				if (tokens.rule(token+1) == NOA.rule) {
					Rule rule = rule(token+1, grammar);
					rules.add(rule);
				}
				token = tokens.next(token);
			}
		}
		return rules.toArray(new Rule[0]);
	}

	private static Rule rule(int token, Tokenised grammar) {
		check(token, grammar, NOA.rule);
		return selection(token+2, grammar).as(grammar.text(token+1));
	}

	private static Rule selection(int token, Tokenised grammar) {
		check(token, grammar, NOA.selection);
		final List<Rule> alternatives = new ArrayList<>();
		final Tokens tokens = grammar.tokens;
		final int end = tokens.end(token)+1;
		int i = token+1;
		while (tokens.rule(i) == NOA.sequence && tokens.end(i) <= end) {
			alternatives.add(sequence(i, grammar));
			i = tokens.next(i);
		}
		if (alternatives.size() == 1) {
			return alternatives.get(0);
		}
		return Rule.selection(alternatives.toArray(new Rule[0]));
	}

	private static Rule sequence(int token, Tokenised grammar) {
		check(token, grammar, NOA.sequence);
		final List<Rule> elems = new ArrayList<>();
		final Tokens tokens = grammar.tokens;
		final int end = tokens.end(token)+1;
		int i = token+1;
		while (tokens.rule(i) == NOA.element && tokens.end(i) <= end) {
			elems.add(element(i, grammar));
			i = tokens.next(i);
		}
		if (elems.size() == 1) {
			return elems.get(0);
		}
		return Rule.seq(elems.toArray(new Rule[0]));
	}

	private static Rule element(int token, Tokenised grammar) {
		check(token, grammar, NOA.element);
		final Tokens tokens = grammar.tokens;
		Occur occur = occur(tokens.next(token+1), grammar, token);
		Rule r = tokens.rule(token+1);
		if (r == NOA.completion) {
			return Rule.completion();
		}
		if (r == NOA.group) {
			return capture(tokens.next(token+2), grammar, selection(token+2, grammar)).occurs(occur);
		}
		if (r == NOA.option) {
			return capture(tokens.next(token+2), grammar, selection(token+2, grammar)).occurs(Occur.qmark);
		}
		if (r == NOA.terminal) {
			Rule t = terminal(token+1, grammar).occurs(occur);
			// a terminal of a single character -> use literal instead
			if (t.type == RuleType.TERMINAL && t.terminal.isSingleCharacter() && t.terminal.ranges[0] >= 0) { 
				return Rule.string(new String(UTF8.bytes(t.terminal.ranges[0]))).occurs(occur);
			}
			return t;
		}
		if (r == NOA.string) {
			String text = grammar.text(token+1);
			return Rule.string(text.substring(1, text.length()-1)).occurs(occur);
		}
		if (r == NOA.ref) {
			return ref(token+1, grammar).occurs(occur);
		}
		throw unexpectedRule(r);
	}

	private static Rule ref(int token, Tokenised grammar) {
		return capture(token+2, grammar, Rule.ref(grammar.text(token+1)));
	}

	private static Rule capture(int token, Tokenised grammar, Rule rule) {
		if (grammar.tokens.rule(token) == NOA.capture) {
			return rule.as(grammar.text(token+1));
		}
		return rule;
	}

	private static Rule terminal(int token, Tokenised grammar) {
		check(token, grammar, NOA.terminal);
		Rule r = grammar.tokens.rule(token+1);
		if (r == NOA.ranges) {
			return ranges(token+1, grammar);
		}
		if (r == NOA.figures) {
			return figures(token+1, grammar);
		}
		if (r == NOA.pattern) {
			return pattern(token+1, grammar);
		}
		throw unexpectedRule(r);
	}

	private static Rule pattern(int token, Tokenised grammar) {
		check(token, grammar, NOA.pattern);
		boolean not = grammar.tokens.rule(token+1) == NOA.not;
		Rule p = patternSelection(token+(not?2:1), grammar);
		return not ? Rule.pattern(Patterns.not(p.pattern)) : p;
	}

	private static Rule patternSelection(int token, Tokenised grammar) {
		Rule r = grammar.tokens.rule(token);
		if (r == NOA.gap) {
			return Rule.pattern(Patterns.GAP);
		}
		if (r == NOA.pad) {
			return Rule.pattern(Patterns.PAD);
		}
		if (r == NOA.indent) {
			return Rule.pattern(Patterns.INDENT);
		}
		if (r == NOA.separator) {
			return Rule.pattern(Patterns.SEPARATOR);
		}
		if (r == NOA.wrap) {
			return Rule.pattern(Patterns.WRAP);
		}
		throw unexpectedRule(r);
	}

	private static Rule figures(int token, Tokenised grammar) {
		check(token, grammar, NOA.figures);
		final Tokens tokens = grammar.tokens;
		final int end = tokens.end(token);
		Terminal terminal = null;
		int i = token+1;
		List<Rule> refs = new ArrayList<>();
		while (tokens.end(i) <= end && tokens.rule(i) != NOA.capture) {
			Rule figure = tokens.rule(i);
			if (figure == NOA.ranges) {
				Rule ranges = ranges(i, grammar);
				terminal = terminal == null ? ranges.terminal : terminal.and(ranges.terminal);
			} else if (figure == NOA.name) {
				String name = grammar.text(i);
				if (name.charAt(0) != '-') {
					name = "-"+name; // always do not capture these
				}
				refs.add(Rule.ref(name));
			}
			i = tokens.next(i);
		}
		Rule r = terminal == null ? Rule.selection(refs.toArray(new Rule[0])) : Rule.terminal(terminal);
		if (!refs.isEmpty() && terminal != null) {
			Rule[] a = Arrays.copyOf(refs.toArray(new Rule[0]), refs.size() + 1);
			a[a.length-1] = r;
			r = Rule.selection(a);
		}
		return capture(i, grammar, r);
	}

	private static Rule ranges(int token, Tokenised grammar) {
		check(token, grammar, NOA.ranges);
		boolean not = grammar.tokens.rule(token+1) == NOA.not;
		Rule ranges = rangesSelection(token +(not ? 2 : 1), grammar);
		return not ? Rule.terminal(ranges.terminal.not()) : ranges;
	}

	private static Rule rangesSelection(int token, Tokenised grammar) {
		Rule r = grammar.tokens.rule(token);
		if (r == NOA.wildcard) {
			return Rule.terminal(Terminal.WILDCARD);
		}
		if (r == NOA.letter) {
			return Rule.terminal(Terminal.LETTERS);
		}
		if (r == NOA.hex) {
			return Rule.terminal(Terminal.HEX_NUMBER);
		}
		if (r == NOA.octal) {
			return Rule.terminal(Terminal.OCTAL_NUMBER);
		}
		if (r == NOA.binary) {
			return Rule.terminal(Terminal.BINARY_NUMBER);
		}
		if (r == NOA.digit) {
			return Rule.terminal(Terminal.DIGITS);
		}
		if (r == NOA.category) {
			//TODO
			throw new UnsupportedOperationException("Not available yet");
		}
		if (r == NOA.range) {
			return Rule.terminal(Terminal.range(literal(token+1, grammar), literal(token+3, grammar)));
		}
		if (r == NOA.literal) {
			return Rule.terminal(Terminal.character(literal(token, grammar)));
		}
		if (r == NOA.whitespace) {
			return Rule.terminal(Terminal.WHITESPACE);
		}
		if (r == NOA.shortname) {
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

	private static int literal(int token, Tokenised grammar) {
		check(token, grammar, NOA.literal);
		Rule r = grammar.tokens.rule(token+1);
		if (r == NOA.symbol) {
			return grammar.text(token+1).codePointAt(1);
		}
		if (r == NOA.code_point) {
			return Integer.parseInt(grammar.text(token+1).substring(2), 16);
		}
		throw unexpectedRule(r);
	}

	private static Occur occur(int token, Tokenised grammar, int parent) {
		// there might not be an occurrence token or it belongs to a outer parent 
		if (grammar.tokens.rule(token) != NOA.occurrence || grammar.tokens.end(parent) < grammar.tokens.end(token)) {
			return Occur.once;
		}
		Rule occur = grammar.tokens.rule(token+1);
		if (occur == NOA.plus) {
			return Occur.plus;
		}
		if (occur == NOA.star) {
			return Occur.star;
		}
		if (occur == NOA.qmark) {
			return Occur.qmark;
		}
		int min = Integer.parseInt(grammar.text(token+1));
		int max = min;
		if ("to".equals(grammar.tokens.rule(token+2).name)) {
			max = Occur.plus.max;
			if ("max".equals(grammar.tokens.rule(token+3).name)) {
				max = Integer.parseInt(grammar.text(token+3));
			}
		}
		return Occur.occur(min, max);
	}
	
	private static RuntimeException unexpectedRule(Rule r) {
		return new RuntimeException("Unexpected rule: "+r);
	}
	
	private static void check(int token, Tokenised grammar, Rule expected) {
		if (grammar.tokens.rule(token) != expected) {
			throw new RuntimeException("expected "+expected+" but got: "+grammar.tokens.rule(token));
		}
	}
}
