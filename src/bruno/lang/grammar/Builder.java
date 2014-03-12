package bruno.lang.grammar;

import java.util.ArrayList;
import java.util.List;

import bruno.lang.grammar.Grammar.Rule;

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
		return selection(token+2, grammar).as(grammar.text(token+1));
	}

	private static Rule selection(int token, Tokenised grammar) {
		final List<Rule> alternatives = new ArrayList<>();
		final Tokens tokens = grammar.tokens;
		final int level = tokens.level(token)+1;
		int i = token+1;
		//FIXME check end index instead of level as level could be same?!
		while (tokens.rule(i) == Grano.sequence && tokens.level(i) == level) {
			alternatives.add(sequence(i, grammar));
			i = tokens.next(i);
		}
		if (alternatives.size() == 1) {
			return alternatives.get(0);
		}
		return Rule.selection(alternatives.toArray(new Rule[0]));
	}

	private static Rule sequence(int token, Tokenised grammar) {
		final List<Rule> elems = new ArrayList<>();
		final Tokens tokens = grammar.tokens;
		final int level = tokens.level(token)+1;
		int i = token+1;
		//FIXME check end index instead of level as level could be same?!
		while (tokens.rule(i) == Grano.element && tokens.level(i) == level) {
			elems.add(element(i, grammar));
			i = tokens.next(i);
		}
		if (elems.size() == 1) {
			return elems.get(0);
		}
		return Rule.seq(elems.toArray(new Rule[0]));
	}

	private static Rule element(int token, Tokenised grammar) {
		final Tokens tokens = grammar.tokens;
		Occur occur = occur(tokens.next(token+1), grammar, token);
		Rule r = tokens.rule(token+1);
		if (r == Grano.completion) {
			return Rule.completion();
		}
		if (r == Grano.group) {
			//TODO capture handling
			return selection(token+2, grammar).occurs(occur);
		}
		if (r == Grano.option) {
			//TODO capture handling
			return selection(token+2, grammar).occurs(Occur.qmark);
		}
		if (r == Grano.terminal) {
			return terminal(token+2, grammar).occurs(occur);
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
			String name = grammar.text(token+1);
			Rule ref = Rule.ref(name);
			if ("alias".equals(tokens.rule(token+2).name)) {
				ref = ref.as(grammar.text(token+2));
			}
			return ref;
		}
		throw new RuntimeException("Unexpected rule: "+r);
	}

	private static Rule terminal(int token, Tokenised grammar) {
		// TODO Auto-generated method stub
		return null;
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
		int min = Integer.parseInt(grammar.text(token+2));
		int max = min;
		if ("to".equals(grammar.tokens.rule(token+3).name)) {
			max = Occur.plus.max;
			if ("max".equals(grammar.tokens.rule(token+4).name)) {
				max = Integer.parseInt(grammar.text(token+4));
			}
		}
		return Occur.occur(min, max);
	}
}
