package bruno.lang.grammar;

import static java.lang.Integer.parseInt;

import java.util.ArrayList;
import java.util.List;

import bruno.lang.grammar.Grammar.Occur;
import bruno.lang.grammar.Grammar.Rule;
import bruno.lang.grammar.Grammar.RuleType;

public class GrammerCompiler {

	public static Grammar compile(Tokenised t) {
		List<Rule> rules = new ArrayList<>();
		final int c = t.tokens.count();
		int token = 0;
		while (token < c) {
			Rule r = t.tokens.rule(token);
			if (r == BNF.grammar) {
				token++;
			} else if (r == BNF.member) {
				if (t.tokens.rule(token+1) == BNF.rule) {
					rules.add(rule(token+1, t));
				}
				token = t.tokens.next(token);
			} else {
				// if other -> manipulate current rule
			}
		}
		return new Grammar(rules.toArray(new Rule[rules.size()]));
	}
	
	public static Rule rule(int token, Tokenised t) {
		int nameIndex = token+1;
		String seperation = null;
		Rule cur = t.tokens.rule(nameIndex);
		if (cur == BNF.separation) {
			nameIndex++;
			if (t.tokens.level(nameIndex) == t.tokens.level(nameIndex-1)+1) {
				seperation = t.text(nameIndex);
				nameIndex++;
			} else {
				seperation = "";
			}
		}
		Rule r = selection(nameIndex+1, t).as(t.text(nameIndex));
		if (seperation != null) {
			r = r.separate(seperation.isEmpty() ? Rule.EMPTY_STRING : Rule.link(seperation));
		}
		return r;
	}
	
	public static Rule selection(int token, Tokenised t) {
		if (t.tokens.rule(token) != BNF.selection) {
			throw new RuntimeException(t.tokens.rule(token).toString());
		}
		List<Rule> elems = new ArrayList<>();
		int i = token+1;
		int level = t.tokens.level(token)+1;
		while (t.tokens.rule(i) == BNF.elems && t.tokens.level(i) == level) {
			elems.add(elems(i, t));
			i = t.tokens.next(i);
		}
		if (elems.size() == 1) {
			return elems.get(0);
		}
		return Rule.selection(elems.toArray(new Rule[elems.size()]));
	}
	
	public static Rule elems(int token, Tokenised t) {
		List<Rule> elems = new ArrayList<>();
		int i = token+1;
		int level = t.tokens.level(token)+1;
		while (t.tokens.rule(i) == BNF.elem && t.tokens.level(i) == level) {
			elems.add(elem(i, t));
			i = t.tokens.next(i);
		}
		if (elems.size() == 1) {
			return elems.get(0);
		}
		return Rule.sequence(elems.toArray(new Rule[elems.size()]));

	}
	
	public static Rule elem(int token, Tokenised t) {
		Occur occur = Grammar.once;
		int oi = t.tokens.next(token+1);
		if (t.tokens.rule(oi) == BNF.occurrence) {
			Rule occurance = t.tokens.rule(oi+1);
			if (occurance == BNF.plus) {
				occur = Grammar.plus;
			} else if (occurance == BNF.star) {
				occur = Grammar.star;
			} else if (occurance == BNF.qmark) {
				occur = Grammar.qmark;
			} else {
				occur = Grammar.occur(parseInt(t.text(oi+2)), parseInt(t.text(oi+2)));
			}
		}
		Rule elem = t.tokens.rule(token+1);
		if (elem == BNF.atom) {
			return atom(token+1, t).occurs(occur);
		}
		if (elem == BNF.group) {
			return selection(token+2, t);
		}
		if (elem == BNF.ellipsis) {
			return Rule.completion(null);
		}
		return Rule.ANY_WHITESPACE;
	}
	
	public static Rule atom(int token, Tokenised t) {
		int type = token+1;
		Rule atom = t.tokens.rule(type);
		if (atom == BNF.not) {
			Rule neg = atom(type+1, t);
			if (neg.type == RuleType.LITERAL) {
				return Rule.terminal(Grammar.not(new String(neg.literal).charAt(0)));
			}
			return Rule.terminal(Grammar.not(neg.terminal));
		}
		if (atom == BNF.range) {
			String min = t.text(type+1);
			String max = t.text(type+2);
			return Rule.terminal(Grammar.set(min.charAt(1), max.charAt(1)));
		}
		if (atom == BNF.any) {
			return Rule.terminal(Grammar.any);
		}
		if (atom == BNF.whitespace) {
			return Rule.ANY_WHITESPACE;
		}
		if (atom == BNF.eol) {
			return Rule.literal('\n');
		}
		if (atom == BNF.name) {
			return Rule.link(t.text(type));
		}
		if (atom == BNF.terminal) {
			String literal = t.text(type);
			return Rule.literal(literal.substring(1, literal.length()-1));
		}
		return Rule.ANY_WHITESPACE;
	}
	
	public static Occur occurence(int token, Tokenised t) {
		return Grammar.once;
	}
	
	public static Rule compile(Rule r, int token, Tokenised t) {
		//TODO
		return r;
	}
}
