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
			if (r == Grano.grammar) {
				token++;
			} else if (r == Grano.member) {
				if (tokens.rule(token+1) == Grano.rule) {
					rules.add(rule(token+1, grammar));
				}
				token = tokens.next(token);
			}
		}
		return new Grammar(rules.toArray(new Rule[rules.size()]));
	}

	private static Rule rule(int token, Tokenised grammar) {
		check(token, grammar, Grano.rule);
		return selection(token+2, grammar).as(grammar.text(token+1));
	}

	private static Rule selection(int token, Tokenised grammar) {
		check(token, grammar, Grano.selection);
		final List<Rule> alternatives = new ArrayList<>();
		final Tokens tokens = grammar.tokens;
		final int end = tokens.end(token)+1;
		int i = token+1;
		while (tokens.rule(i) == Grano.sequence && tokens.end(i) <= end) {
			alternatives.add(sequence(i, grammar));
			i = tokens.next(i);
		}
		if (alternatives.size() == 1) {
			return alternatives.get(0);
		}
		return Rule.selection(alternatives.toArray(new Rule[0]));
	}

	private static Rule sequence(int token, Tokenised grammar) {
		check(token, grammar, Grano.sequence);
		final List<Rule> elems = new ArrayList<>();
		final Tokens tokens = grammar.tokens;
		final int end = tokens.end(token)+1;
		int i = token+1;
		while (tokens.rule(i) == Grano.element && tokens.end(i) <= end) {
			elems.add(element(i, grammar));
			i = tokens.next(i);
		}
		if (elems.size() == 1) {
			return elems.get(0);
		}
		return Rule.seq(elems.toArray(new Rule[0]));
	}

	private static Rule element(int token, Tokenised grammar) {
		check(token, grammar, Grano.element);
		final Tokens tokens = grammar.tokens;
		Occur occur = occur(tokens.next(token+1), grammar, token);
		Rule r = tokens.rule(token+1);
		if (r == Grano.completion) {
			return Rule.completion();
		}
		if (r == Grano.group) {
			return capture(tokens.next(token+2), grammar, selection(token+2, grammar)).occurs(occur);
		}
		if (r == Grano.option) {
			return capture(tokens.next(token+2), grammar, selection(token+2, grammar)).occurs(Occur.qmark);
		}
		if (r == Grano.terminal) {
			return terminal(token+1, grammar).occurs(occur); //FIXME when terminal uses occur this is reset here...
		}
		if (r == Grano.symbol) {
			//TODO reuse equal symbols
			String text = grammar.text(token+1);
			return Rule.symbol(text.substring(1, text.length()-1)).occurs(occur);
		}
		if (r == Grano.literal) {
			//TODO reuse equal literals
			return Rule.literal(grammar.text(token+1).charAt(1)).occurs(occur);
		}
		if (r == Grano.ref) {
			return ref(token+1, grammar).occurs(occur);
		}
		throw new RuntimeException("Unexpected rule: "+r);
	}

	private static Rule ref(int token, Tokenised grammar) {
		return capture(token+2, grammar, Rule.ref(grammar.text(token+1)));
	}

	private static Rule capture(int token, Tokenised grammar, Rule rule) {
		if (grammar.tokens.rule(token) == Grano.capture) {
			String name = grammar.text(token+1);
			if (name.contains(":")) { 
				throw new IllegalStateException();
			}
			return rule.as(name);
		}
		return rule;
	}

	private static Rule terminal(int token, Tokenised grammar) {
		check(token, grammar, Grano.terminal);
		Rule r = grammar.tokens.rule(token+1);
		if (r == Grano.utf8_set) {
			return utf8set(token+1, grammar);
		}
		if (r == Grano.figures) {
			return figures(token+1, grammar);
		}
		if (r == Grano.pattern) {
			return pattern(token+1, grammar);
		}
		throw new RuntimeException("Unexpected rule: "+r);
	}

	private static Rule pattern(int token, Tokenised grammar) {
		check(token, grammar, Grano.pattern);
		Rule r = grammar.tokens.rule(token+1);
		if (r == Grano.gap) {
			return Rule.pattern(Terminals.gap);
		}
		if (r == Grano.pad) {
			return Rule.pattern(Terminals.pad);
		}
		if (r == Grano.indent) {
			return Rule.pattern(Terminals.indent);
		}
		if (r == Grano.separator) {
			return Rule.pattern(Terminals.separator);
		}		
		throw new RuntimeException("Unexpected rule: "+r);
	}

	private static Rule figures(int token, Tokenised grammar) {
		check(token, grammar, Grano.figures);
		final Tokens tokens = grammar.tokens;
		final int end = tokens.end(token);
		Pattern t = null;
		int i = token+1;
		List<Rule> refs = new ArrayList<>();
		while (tokens.end(i) <= end && tokens.rule(i) != Grano.capture) {
			Rule figure = tokens.rule(i);
			if (figure == Grano.utf8_set) {
				Rule f = utf8set(i, grammar);
				if (f.type == RuleType.PATTERN) {
					t = t == null ? f.pattern : Terminals.or(t, f.pattern);
				} else if (f.type == RuleType.LITERAL) {
					Pattern tl = Terminals.in(new String(f.literal).charAt(0));
					t = t == null ? tl : Terminals.or(t, tl);
				} else {
					throw new RuntimeException("Unexpected rule: "+f);
				}
			} else if (figure == Grano.ref) {
				refs.add(ref(i, grammar));
			}
			i = tokens.next(i);
		}
		Rule r = t == null ? Rule.selection(refs.toArray(new Rule[0])) : Rule.pattern(t);
		if (!refs.isEmpty() && t != null) {
			Rule[] a = Arrays.copyOf(refs.toArray(new Rule[0]), refs.size() + 1);
			a[a.length-1] = r;
			r = Rule.selection(a);
		}
		return capture(i, grammar, r);
	}

	private static Rule utf8set(int token, Tokenised grammar) {
		check(token, grammar, Grano.utf8_set);
		boolean not = grammar.tokens.rule(token+1) == Grano.not;
		Rule utf8set = utf8setBody(token +(not ? 2 : 1), grammar);
		if (not) {
			if (utf8set.type == RuleType.PATTERN) {
				return Rule.pattern(Terminals.not(utf8set.pattern));
			}
			return Rule.pattern(Terminals.not(new String(utf8set.literal).charAt(0)));
		}
		return utf8set;
	}

	private static Rule utf8setBody(int token, Tokenised grammar) {
		Rule r = grammar.tokens.rule(token);
		if (r == Grano.wildcard) {
			return Rule.pattern(Terminals.wildcard);
		}
		if (r == Grano.letter) {
			return Rule.pattern(Grano.LETTER);
		}
		if (r == Grano.hex) {
			return Rule.pattern(Grano.HEX);
		}
		if (r == Grano.octal) {
			return Rule.pattern(Grano.OCTAL);
		}
		if (r == Grano.binary) {
			return Rule.pattern(Grano.BINARY);
		}
		if (r == Grano.digit) {
			return Rule.pattern(Grano.DIGIT);
		}
		if (r == Grano.utf8_class) {
			//TODO
		}
		if (r == Grano.range) {
			return Rule.pattern(Terminals.range(literal(token+1, grammar), literal(token+3, grammar)));
		}
		if (r == Grano.literal) {
			return Rule.literal(literal(token, grammar));
		}
		if (r == Grano.whitespace) {
			return Rule.pattern(Terminals.whitespace);
		}
		if (r == Grano.shortname) {
			String name = grammar.text(token+1);
			char c = name.charAt(1);
			if (c == 't') {
				return Rule.literal('\t');
			}
			if (c == 'n') {
				return Rule.literal('\n');
			}
			if (c == 'r') {
				return Rule.literal('\r');
			}
			throw new NoSuchElementException(name);
		}
		throw new RuntimeException("Unexpected rule: "+r);
	}

	private static char literal(int token, Tokenised grammar) {
		check(token, grammar, Grano.literal);
		Rule r = grammar.tokens.rule(token+1);
		if (r == Grano.atom) {
			return grammar.text(token+1).charAt(1);
		}
		if (r == Grano.utf8) {
			return (char) Integer.parseInt(grammar.text(token+1).substring(2), 16);
		}
		throw new RuntimeException("Unexpected rule: "+r);
	}

	private static Occur occur(int token, Tokenised grammar, int parent) {
		// there might not be an occurrence token or it belongs to a outer parent 
		if (grammar.tokens.rule(token) != Grano.occurrence || grammar.tokens.end(parent) < grammar.tokens.end(token)) {
			return Occur.once;
		}
		Rule occur = grammar.tokens.rule(token+1);
		if (occur == Grano.plus) {
			return Occur.plus;
		}
		if (occur == Grano.star) {
			return Occur.star;
		}
		if (occur == Grano.qmark) {
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
	
	private static void check(int token, Tokenised grammar, Rule expected) {
		if (grammar.tokens.rule(token) != expected) {
			throw new RuntimeException("expected "+expected+" but got: "+grammar.tokens.rule(token));
		}
	}
}