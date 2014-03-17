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
public class Builder {

	public static Grammar build(Tokenised grammar) {
		final List<Rule> rules = new ArrayList<>();
		final Tokens tokens = grammar.tokens;
		final int c = tokens.count();
		int token = 0;
		while (token < c) {
			Rule r = tokens.rule(token);
			if (r == FregeFL.grammar) {
				token++;
			} else if (r == FregeFL.member) {
				if (tokens.rule(token+1) == FregeFL.rule) {
					rules.add(rule(token+1, grammar));
				}
				token = tokens.next(token);
			}
		}
		return new Grammar(rules.toArray(new Rule[rules.size()]));
	}

	private static Rule rule(int token, Tokenised grammar) {
		check(token, grammar, FregeFL.rule);
		return selection(token+2, grammar).as(grammar.text(token+1));
	}

	private static Rule selection(int token, Tokenised grammar) {
		check(token, grammar, FregeFL.selection);
		final List<Rule> alternatives = new ArrayList<>();
		final Tokens tokens = grammar.tokens;
		final int end = tokens.end(token)+1;
		int i = token+1;
		while (tokens.rule(i) == FregeFL.sequence && tokens.end(i) <= end) {
			alternatives.add(sequence(i, grammar));
			i = tokens.next(i);
		}
		if (alternatives.size() == 1) {
			return alternatives.get(0);
		}
		return Rule.selection(alternatives.toArray(new Rule[0]));
	}

	private static Rule sequence(int token, Tokenised grammar) {
		check(token, grammar, FregeFL.sequence);
		final List<Rule> elems = new ArrayList<>();
		final Tokens tokens = grammar.tokens;
		final int end = tokens.end(token)+1;
		int i = token+1;
		while (tokens.rule(i) == FregeFL.element && tokens.end(i) <= end) {
			elems.add(element(i, grammar));
			i = tokens.next(i);
		}
		if (elems.size() == 1) {
			return elems.get(0);
		}
		return Rule.seq(elems.toArray(new Rule[0]));
	}

	private static Rule element(int token, Tokenised grammar) {
		check(token, grammar, FregeFL.element);
		final Tokens tokens = grammar.tokens;
		Occur occur = occur(tokens.next(token+1), grammar, token);
		Rule r = tokens.rule(token+1);
		if (r == FregeFL.completion) {
			return Rule.completion();
		}
		if (r == FregeFL.group) {
			return capture(tokens.next(token+2), grammar, selection(token+2, grammar)).occurs(occur);
		}
		if (r == FregeFL.option) {
			return capture(tokens.next(token+2), grammar, selection(token+2, grammar)).occurs(Occur.qmark);
		}
		if (r == FregeFL.terminal) {
			Rule t = terminal(token+1, grammar).occurs(occur);
			// a terminal of a single character -> use literal instead
			if (t.type == RuleType.TERMINAL && t.terminal.isSingleCharacter() && t.terminal.ranges[0] >= 0) { 
				return Rule.symbol(new String(UTF8.bytes(t.terminal.ranges[0]))).occurs(occur);
			}
			return t;
		}
		if (r == FregeFL.symbol) {
			//TODO reuse equal symbols
			String text = grammar.text(token+1);
			return Rule.symbol(text.substring(1, text.length()-1)).occurs(occur);
		}
		if (r == FregeFL.ref) {
			return ref(token+1, grammar).occurs(occur);
		}
		throw unexpectedRule(r);
	}

	private static Rule ref(int token, Tokenised grammar) {
		return capture(token+2, grammar, Rule.ref(grammar.text(token+1)));
	}

	private static Rule capture(int token, Tokenised grammar, Rule rule) {
		if (grammar.tokens.rule(token) == FregeFL.capture) {
			String name = grammar.text(token+1);
			if (name.contains(":")) { 
				throw new IllegalStateException();
			}
			return rule.as(name);
		}
		return rule;
	}

	private static Rule terminal(int token, Tokenised grammar) {
		check(token, grammar, FregeFL.terminal);
		Rule r = grammar.tokens.rule(token+1);
		if (r == FregeFL.ranges) {
			return ranges(token+1, grammar);
		}
		if (r == FregeFL.figures) {
			return figures(token+1, grammar);
		}
		if (r == FregeFL.pattern) {
			return pattern(token+1, grammar);
		}
		throw unexpectedRule(r);
	}

	private static Rule pattern(int token, Tokenised grammar) {
		check(token, grammar, FregeFL.pattern);
		boolean not = grammar.tokens.rule(token+1) == FregeFL.not;
		Rule p = patternSelection(token+(not?2:1), grammar);
		return not ? Rule.pattern(Patterns.not(p.pattern)) : p;
	}

	private static Rule patternSelection(int token, Tokenised grammar) {
		Rule r = grammar.tokens.rule(token);
		if (r == FregeFL.gap) {
			return Rule.pattern(Patterns.GAP);
		}
		if (r == FregeFL.pad) {
			return Rule.pattern(Patterns.PAD);
		}
		if (r == FregeFL.indent) {
			return Rule.pattern(Patterns.INDENT);
		}
		if (r == FregeFL.separator) {
			return Rule.pattern(Patterns.SEPARATOR);
		}		
		throw unexpectedRule(r);
	}

	private static Rule figures(int token, Tokenised grammar) {
		check(token, grammar, FregeFL.figures);
		final Tokens tokens = grammar.tokens;
		final int end = tokens.end(token);
		Terminal terminal = null;
		int i = token+1;
		List<Rule> nonTerminals = new ArrayList<>();
		while (tokens.end(i) <= end && tokens.rule(i) != FregeFL.capture) {
			Rule figure = tokens.rule(i);
			if (figure == FregeFL.ranges) {
				Rule ranges = ranges(i, grammar);
				terminal = terminal == null ? ranges.terminal : terminal.and(ranges.terminal);
			} else if (figure == FregeFL.ref) {
				nonTerminals.add(ref(i, grammar));
			}
			i = tokens.next(i);
		}
		Rule r = terminal == null ? Rule.selection(nonTerminals.toArray(new Rule[0])) : Rule.terminal(terminal);
		if (!nonTerminals.isEmpty() && terminal != null) {
			Rule[] a = Arrays.copyOf(nonTerminals.toArray(new Rule[0]), nonTerminals.size() + 1);
			a[a.length-1] = r;
			r = Rule.selection(a);
		}
		return capture(i, grammar, r);
	}

	private static Rule ranges(int token, Tokenised grammar) {
		check(token, grammar, FregeFL.ranges);
		boolean not = grammar.tokens.rule(token+1) == FregeFL.not;
		Rule ranges = rangesSelection(token +(not ? 2 : 1), grammar);
		return not ? Rule.terminal(ranges.terminal.not()) : ranges;
	}

	private static Rule rangesSelection(int token, Tokenised grammar) {
		Rule r = grammar.tokens.rule(token);
		if (r == FregeFL.wildcard) {
			return Rule.terminal(Terminal.WILDCARD);
		}
		if (r == FregeFL.letter) {
			return Rule.terminal(Terminal.LETTERS);
		}
		if (r == FregeFL.hex) {
			return Rule.terminal(Terminal.HEX_NUMBER);
		}
		if (r == FregeFL.octal) {
			return Rule.terminal(Terminal.OCTAL_NUMBER);
		}
		if (r == FregeFL.binary) {
			return Rule.terminal(Terminal.BINARY_NUMBER);
		}
		if (r == FregeFL.digit) {
			return Rule.terminal(Terminal.DIGITS);
		}
		if (r == FregeFL.category) {
			//TODO
			throw new UnsupportedOperationException("Not available yet");
		}
		if (r == FregeFL.range) {
			return Rule.terminal(Terminal.range(literal(token+1, grammar), literal(token+3, grammar)));
		}
		if (r == FregeFL.literal) {
			return Rule.terminal(Terminal.character(literal(token, grammar)));
		}
		if (r == FregeFL.whitespace) {
			return Rule.terminal(Terminal.WHITESPACE);
		}
		if (r == FregeFL.shortname) {
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
		check(token, grammar, FregeFL.literal);
		Rule r = grammar.tokens.rule(token+1);
		if (r == FregeFL.atom) {
			return grammar.text(token+1).codePointAt(1);
		}
		if (r == FregeFL.code_point) {
			return Integer.parseInt(grammar.text(token+1).substring(2), 16);
		}
		throw unexpectedRule(r);
	}

	private static Occur occur(int token, Tokenised grammar, int parent) {
		// there might not be an occurrence token or it belongs to a outer parent 
		if (grammar.tokens.rule(token) != FregeFL.occurrence || grammar.tokens.end(parent) < grammar.tokens.end(token)) {
			return Occur.once;
		}
		Rule occur = grammar.tokens.rule(token+1);
		if (occur == FregeFL.plus) {
			return Occur.plus;
		}
		if (occur == FregeFL.star) {
			return Occur.star;
		}
		if (occur == FregeFL.qmark) {
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
