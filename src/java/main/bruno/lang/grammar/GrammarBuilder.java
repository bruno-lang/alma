package bruno.lang.grammar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import bruno.lang.grammar.Grammar.Rule;
import bruno.lang.grammar.Grammar.RuleType;

/**
 * The {@link GrammarBuilder} is a utility that optimizes and finishes a set of
 * {@link Rule}s used to build a {@link Grammar}.
 * 
 * Such a set is usually created by the {@link RuleBuilder} that does not deliver a
 * working grammar but the set of rules as objects as given in a grammar file.
 * 
 * @author jan
 */
public final class GrammarBuilder {

	public static Grammar buildGrammar(Parsed grammar) {
		return new Grammar(finish(RuleBuilder.buildRules(grammar)));
	}
	
	public static Grammar buildGrammar(Rule...roots) {
		return new Grammar(finish(namedRules(roots)));
	}
	
	/**
	 * Creates a set of named {@link Rule}s reachable from any of the given roots.  
	 */
	public static Rule[] namedRules(Rule...roots) {
		LinkedHashMap<String, Rule> namedRules = new LinkedHashMap<>();
		Set<Rule> followed = new HashSet<>();
		extractNamedRules(roots, namedRules, followed);
		for (String name : new HashSet<>(namedRules.keySet())) {
			if (name.startsWith("-") && namedRules.containsKey(name.substring(1))) {
				namedRules.remove(name);
			}
		}
		return namedRules.values().toArray(new Rule[0]);
	}
	
	private static void extractNamedRules(Rule[] elements, Map<String, Rule> namedRules, Set<Rule> followed) {
		for (Rule e : elements) {
			if (!followed.contains(e)) {
				followed.add(e);
				if (!e.name.isEmpty() && e.type == RuleType.CAPTURE && !namedRules.containsKey(e.name)) {
					namedRules.put(e.name, e);
				} 
				extractNamedRules(e.elements, namedRules, followed);
			}
		}
	}
	
	public static Rule[] finish(Rule[] namedRules) {
		final LinkedHashMap<String,Rule> rules = new LinkedHashMap<>();
		for (Rule r : namedRules) {
			rules.put(r.name, r);
		}
		Map<String, Rule> literals = new HashMap<>();
		Set<Rule> followed = new HashSet<>();
		for (int i = 0; i < namedRules.length; i++) {
			Rule rule = namedRules[i];
			rule = deduplicateLiterals(rule, literals, followed); followed.clear();
			rule = resolveIncludes(rule, rules, followed); followed.clear();
			rule = unwrapSingleElements(rule, followed); followed.clear();
			rule = flattenNestedSequences(rule, followed); followed.clear();
			rule = compactCharsets(rule, followed);
			namedRules[i] = rule;
		}		
		return namedRules;
	}
	
	/**
	 * If a sequence has an element that itself is a sequence the elements or
	 * that sequence can be inserted for that element. 
	 */
	private static Rule flattenNestedSequences(Rule rule, Set<Rule> followed) {
		if (followed.contains(rule)) {
			return rule;
		}
		followed.add(rule);
		if (rule.type == RuleType.SEQUENCE) {
			ArrayList<Rule> elems = new ArrayList<>(rule.elements.length);
			for (Rule e : rule.elements) {
				if (e.type == RuleType.FILL || e.type == RuleType.CAPTURE && e.elements[0].type == RuleType.FILL) {
					return rule; // this gets messy otherwise
				} else if (e.type != RuleType.SEQUENCE || e.isDecisionMaking()) {
					elems.add(e);
				} else {
					elems.addAll(Arrays.asList(e.elements)); // FIXME this is just 1 level not recursive
				}
			}
			if (elems.size() == rule.elements.length) { // nothing changed
				return rule;
			}
			// flattened some...
			Rule seq = Rule.seq(elems.toArray(new Rule[elems.size()]));
			return seq;
		}
		if (rule.elements.length > 0) {
			for (int i = 0; i < rule.elements.length; i++) {
				rule.elements[i] = flattenNestedSequences(rule.elements[i], followed);
			}
		}
		return rule;
	}

	/**
	 * Strips out unnecessary single element sequences and alternatives as well
	 * as non capturing captures.
	 */
	public static Rule unwrapSingleElements(Rule rule, Set<Rule> followed) {
		if (followed.contains(rule)) {
			// TODO return the unpacked version of the rule 
			return rule;
		}
		followed.add(rule);
		if ((rule.type == RuleType.SEQUENCE || rule.type == RuleType.ALTERNATIVES) && rule.elements.length == 1) {
			return rule.elements[0];
		}
		if (rule.type == RuleType.SEQUENCE) { // inline nested sequences
			if (!rule.isDecisionMaking() && hasSequenceElement(rule)) {
				List<Rule> elems = new ArrayList<>();
				for (Rule e : rule.elements) {
					Rule u = unwrapSingleElements(e, followed);
					if (u.type == RuleType.SEQUENCE && !u.isDecisionMaking()) {
						elems.addAll(Arrays.asList(u.elements));
					} else {
						elems.add(u);
					}
				}
				return Rule.seq(elems.toArray(new Rule[0]));
			}
		}
		if (rule.type == RuleType.CAPTURE && rule.substitute) {
			return rule.elements[0];
		}
		//TODO recursion? unpacked rules also have to be collected so they are replaced everywhere referenced
		return rule;
	}
	
	private static boolean hasSequenceElement(Rule rule) {
		for (int i = 0; i < rule.elements.length; i++) {
			Rule e = rule.elements[i];
			if (e.type == RuleType.SEQUENCE && !e.isDecisionMaking()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Contracts selections with just {@link CharacterSet}s and 1 character literals
	 * to a single {@link CharacterSet}. A selection of just literal characters will
	 * also be contracted to a single terminal.
	 */
	public static Rule compactCharsets(Rule rule, Set<Rule> followed) {
		if (followed.contains(rule)) {
			return rule;
		}
		followed.add(rule);
		if (rule.type == RuleType.ALTERNATIVES) {
			int ts = 0;
			int ls = 0;
			for (Rule e : rule.elements) {
				if (e.type == RuleType.CHARACTER_SET) {
					ts++;
				}
				if (e.type == RuleType.LITERAL && UTF8.characters(e.literal) == 1) {
					ls++;
				}
			}
			if (ts+ls == rule.elements.length) {
				CharacterSet t = terminalOf(rule.elements[0]);
				for (int i = 1; i < rule.elements.length; i++) {
					Rule r = rule.elements[i];
					t = t.and(terminalOf(r));
				}
				return Rule.charset(t);
			}
		} 
		if (rule.elements.length > 0) {
			for (int i = 0; i < rule.elements.length; i++) {
				rule.elements[i] = compactCharsets(rule.elements[i], followed);
			}
		}
		return rule;
	}
	
	private static CharacterSet terminalOf(Rule r) {
		return r.type == RuleType.CHARACTER_SET ? r.charset : CharacterSet.character(UTF8.codePoint(r.literal));
	}
	
	/**
	 * Reuses same instance of a {@link RuleType#LITERAL} {@link Rule} for equal
	 * {@link String} literals.
	 */
	public static Rule deduplicateLiterals(Rule rule, Map<String, Rule> literals, Set<Rule> followed) {
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
				rule.elements[i] = deduplicateLiterals(rule.elements[i], literals, followed);
			}
		}
		return rule;
		
	}

	/**
	 * Substitutes {@link RuleType#INCLUDE} {@link Rule}s with the actual rule.
	 */
	public static Rule resolveIncludes(Rule rule, Map<String,Rule> namedRules, Set<Rule> followed) {
		if (followed.contains(rule)) {
			return rule;
		}
		followed.add(rule);
		if (rule.type == RuleType.INCLUDE) {
			Rule r = namedRules.get(rule.name);
			if (r == null) {
				throw new NoSuchElementException("No such rule: `"+rule.name+"`\nKnown rules are: "+namedRules.keySet());
			}
			r = resolveIncludes(r, namedRules, followed);
			return rule.substitute ? r.elements[0] : r;
		} else if (rule.elements.length > 0) {
			if (rule.type == RuleType.CHARACTER_SET) {
				CharacterSet t = rule.charset;
				for (Rule ref : rule.elements) {
					Rule t2 = namedRules.get(ref.name);
					if (t2.type == RuleType.CAPTURE)
						t2 = t2.elements[0];
					if (t2.type == RuleType.LITERAL) {
						t2 = Rule.charset(CharacterSet.character(UTF8.codePoint(t2.literal)));
					}
					if (t2.type != RuleType.CHARACTER_SET)
						throw new IllegalArgumentException("Cannot merge rule of type "+t2.type+" with a terminal: "+t2);
					t = t.and(t2.charset);
				}
				return Rule.charset(t);
			}
			for (int i = 0; i < rule.elements.length; i++) {
				rule.elements[i] = resolveIncludes(rule.elements[i], namedRules, followed);
				if (rule.type == RuleType.CAPTURE && rule.elements[i].type == RuleType.CAPTURE) {
					rule.elements[i] = rule.elements[i].elements[0]; // unpack double capture
				}
			}
		}
		return rule;
	}
	
}
