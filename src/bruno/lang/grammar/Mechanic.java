package bruno.lang.grammar;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import bruno.lang.grammar.Grammar.Rule;
import bruno.lang.grammar.Grammar.RuleType;

public final class Mechanic {

	public static Rule[] namedRules(Rule...roots) {
		LinkedHashMap<String, Rule> namedRules = new LinkedHashMap<>();
		Set<Rule> followed = new HashSet<>();
		namedRules(roots, namedRules, followed);
		// TODO remove those with - where a variant with - and without is known
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
	
	public static Rule[] deploy(Rule[] namedRules) {
		final LinkedHashMap<String,Rule> rules = new LinkedHashMap<>();
		for (Rule r : namedRules) {
			rules.put(r.name, r);
		}
		Map<String, Rule> literals = new HashMap<>();
		for (int i = 0; i < namedRules.length; i++) {
			Rule r = namedRules[i];
			r = deduplicate(r, literals);
			r = dereference(r, rules);
			r = contract(r);
			namedRules[i] = r;
		}		
		return namedRules;
	}
	
	
	public static Rule contract(Rule rule) {
		if (rule.type == RuleType.SELECTION) {
			// find multiple terminals within it
			// merge terminals in one, build new selection
		}
		return rule;
	}
	
	public static Rule deduplicate(Rule rule, Map<String, Rule> literals) {
		if (rule.type == RuleType.LITERAL) {
			String l = new String(rule.literal);
			Rule r = literals.get(l);
			if (r != null) {
				return r;
			}
			literals.put(l, rule);
		} else if (rule.elements.length > 0) {
			for (int i = 0; i < rule.elements.length; i++) {
				rule.elements[i] = deduplicate(rule.elements[i], literals);
			}
		}
		return rule;
		
	}

	public static Rule dereference(Rule rule, Map<String,Rule> namedRules) {
		if (rule.type == RuleType.REFERENCE) {
			boolean noCapture = rule.name.charAt(0) == '-';
			String name = rule.name.substring(noCapture ? 1:0);
			Rule r = namedRules.get(name); //FIXME try with minus first (in case rule is also named like that)
			return noCapture ? r.elements[0] : r;
		} else if (rule.elements.length > 0) {
			for (int i = 0; i < rule.elements.length; i++) {
				rule.elements[i] = dereference(rule.elements[i], namedRules);
			}
		}
		return rule;
	}
	
}
