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
	
	public static Rule[] finish(Rule[] namedRules, boolean deduplicate) {
		final LinkedHashMap<String,Rule> rules = new LinkedHashMap<>();
		for (Rule r : namedRules) {
			rules.put(r.name, r);
		}
		Map<String, Rule> literals = new HashMap<>();
		Set<Rule> followedContract = new HashSet<>();
		Set<Rule> followedDereference = new HashSet<>();
		for (int i = 0; i < namedRules.length; i++) {
			Rule r = namedRules[i];
			if (deduplicate) {
				r = deduplicate(r, literals);
			}
			r = dereference(r, rules, followedDereference);
			r = compact(r, followedContract);
			namedRules[i] = r;
		}		
		return namedRules;
	}
	
	
	public static Rule compact(Rule rule, Set<Rule> followed) {
		if (followed.contains(rule)) {
			return rule;
		}
		followed.add(rule);
		if (rule.type == RuleType.SELECTION) {
			int c = 0;
			for (Rule e : rule.elements) {
				if (e.type == RuleType.TERMINAL) {
					c++;
				}
			}
			if (c == rule.elements.length) {
				Terminal t = rule.elements[0].terminal;
				for (int i = 1; i < rule.elements.length; i++) {
					t = t.and(rule.elements[i].terminal);
				}
				return Rule.terminal(t);
			}
			//TODO a better version that also merges just single char literals and terminals together
		} 
		if (rule.elements.length > 0) {
			for (int i = 0; i < rule.elements.length; i++) {
				rule.elements[i] = compact(rule.elements[i], followed);
			}
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

	public static Rule dereference(Rule rule, Map<String,Rule> namedRules, Set<Rule> followed) {
		if (followed.contains(rule)) {
			return rule;
		}
		followed.add(rule);
		if (rule.type == RuleType.REFERENCE) {
			boolean noCapture = rule.name.charAt(0) == '-';
			String name = rule.name.substring(noCapture ? 1:0);
			Rule r = namedRules.get(name); //FIXME try with minus first (in case rule is also named like that)
			return noCapture ? r.elements[0] : r;
		} else if (rule.elements.length > 0) {
			for (int i = 0; i < rule.elements.length; i++) {
				rule.elements[i] = dereference(rule.elements[i], namedRules, followed);
			}
		}
		return rule;
	}
	
}
