package bruno.lang.grammar;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import bruno.lang.grammar.Grammar.Rule;
import bruno.lang.grammar.Grammar.RuleType;

/**
 * The {@link Mechanic} is a utility that optimizes and finishes a set of
 * {@link Rule}s used to build a {@link Grammar}.
 * 
 * Such a set is usually created by the {@link Builder} that does not deliver a
 * working grammar but the set of rules as objects as given in a grammar file.
 * 
 * @author jan
 */
public final class Mechanic {

	/**
	 * Creates a set of named {@link Rule}s reachable from any of the given roots.  
	 */
	public static Rule[] namedRules(Rule...roots) {
		LinkedHashMap<String, Rule> namedRules = new LinkedHashMap<>();
		Set<Rule> followed = new HashSet<>();
		namedRules(roots, namedRules, followed);
		for (String name : new HashSet<>(namedRules.keySet())) {
			if (name.startsWith("-") && namedRules.containsKey(name.substring(1))) {
				namedRules.remove(name);
			}
		}
		return namedRules.values().toArray(new Rule[0]);
	}
	
	private static void namedRules(Rule[] elements, Map<String, Rule> namedRules, Set<Rule> followed) {
		for (Rule e : elements) {
			if (!followed.contains(e)) {
				followed.add(e);
				if (!e.name.isEmpty() && e.type == RuleType.CAPTURE && !namedRules.containsKey(e.name)) {
					namedRules.put(e.name, e);
				} 
				namedRules(e.elements, namedRules, followed);
			}
		}
	}
	
	public static Rule[] finish(Rule[] namedRules) {
		final LinkedHashMap<String,Rule> rules = new LinkedHashMap<>();
		for (Rule r : namedRules) {
			rules.put(r.name, r);
		}
		Map<String, Rule> literals = new HashMap<>();
		Set<Rule> followedContract = new HashSet<>();
		Set<Rule> followedDereference = new HashSet<>();
		Set<Rule> followedUnpack = new HashSet<>();
		Set<Rule> followedDeduplicate = new HashSet<>();
		for (int i = 0; i < namedRules.length; i++) {
			Rule r = namedRules[i];
			r = deduplicate(r, literals, followedDeduplicate);
			r = dereference(r, rules, followedDereference);
			r = unpack(r, followedUnpack);
			r = compact(r, followedContract);
			namedRules[i] = r;
		}		
		return namedRules;
	}
	
	/**
	 * Strips out unnecessary single element sequences and selections as well as
	 * non capturing captures.
	 */
	public static Rule unpack(Rule rule, Set<Rule> followed) {
		if (followed.contains(rule)) {
			return rule;
		}
		followed.add(rule);
		if ((rule.type == RuleType.SEQUENCE || rule.type == RuleType.SELECTION) && rule.elements.length == 1) {
			return rule.elements[0];
		}
		if (rule.type == RuleType.CAPTURE && rule.name.startsWith("-")) {
			return rule.elements[0];
		}
		return rule;
	}
	
	/**
	 * Contracts selections with just {@link Terminal}s and 1 character literals
	 * to a single {@link Terminal}. A selection of just literal characters will
	 * also be contracted to a single terminal.
	 */
	public static Rule compact(Rule rule, Set<Rule> followed) {
		if (followed.contains(rule)) {
			return rule;
		}
		followed.add(rule);
		if (rule.type == RuleType.SELECTION) {
			int ts = 0;
			int ls = 0;
			for (Rule e : rule.elements) {
				if (e.type == RuleType.TERMINAL) {
					ts++;
				}
				if (e.type == RuleType.LITERAL && UTF8.characters(e.literal) == 1) {
					ls++;
				}
			}
			if (ts+ls == rule.elements.length) {
				Terminal t = rule.elements[0].terminal;
				for (int i = 1; i < rule.elements.length; i++) {
					Rule r = rule.elements[i];
					if (r.type == RuleType.TERMINAL) {
						t = t.and(r.terminal);
					} else {
						t = t.and(Terminal.character(UTF8.codePoint(r.literal)));
					}
				}
				return Rule.terminal(t);
			}
		} 
		if (rule.elements.length > 0) {
			for (int i = 0; i < rule.elements.length; i++) {
				rule.elements[i] = compact(rule.elements[i], followed);
			}
		}
		return rule;
	}
	
	/**
	 * Reuses same instance of a {@link RuleType#LITERAL} {@link Rule} for equal
	 * {@link String} literals.
	 */
	public static Rule deduplicate(Rule rule, Map<String, Rule> literals, Set<Rule> followed) {
		if (followed.contains(rule)) {
			return rule;
		}
		followed.add(rule);
		if (rule.type == RuleType.LITERAL) {
			String l = new String(rule.literal);
			Rule r = literals.get(l);
			if (r != null) {
				return r;
			}
			literals.put(l, rule);
		} else if (rule.elements.length > 0) {
			for (int i = 0; i < rule.elements.length; i++) {
				rule.elements[i] = deduplicate(rule.elements[i], literals, followed);
			}
		}
		return rule;
		
	}

	/**
	 * Substitutes {@link RuleType#REFERENCE} {@link Rule}s with the actual rule.
	 */
	public static Rule dereference(Rule rule, Map<String,Rule> namedRules, Set<Rule> followed) {
		if (followed.contains(rule)) {
			return rule;
		}
		followed.add(rule);
		if (rule.type == RuleType.REFERENCE) {
			boolean noCapture = rule.name.charAt(0) == '-';
			Rule r = namedRules.get(rule.name);
			if (r == null) {
				String name = rule.name.substring(noCapture ? 1:0);
				r = namedRules.get(name);
			}
			if (r == null) {
				throw new NoSuchElementException("No such rule: `"+r.name+"`\nKnown rules are: "+namedRules.keySet());
			}
			return noCapture ? r.elements[0] : r;
		} else if (rule.elements.length > 0) {
			for (int i = 0; i < rule.elements.length; i++) {
				rule.elements[i] = dereference(rule.elements[i], namedRules, followed);
			}
		}
		return rule;
	}
	
}
