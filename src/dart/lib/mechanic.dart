part of lingukit;

class Mechanic {

  /**
   * Creates a set of named rules reachable from any of the given roots.  
   */
  static List<Rule> namedRules(List<Rule> roots) {
    Map<String, Rule> namedRules = new Map();
    Set<Rule> followed = new Set();
    namedRules_(roots, namedRules, followed);
    for (String name in new Set.from(namedRules.keys)) {
      if (name.startsWith("-") && namedRules.containsKey(name.substring(1))) {
        namedRules.remove(name);
      }
    }
    return new List.from(namedRules.values);
  }
  
  static void namedRules_(List<Rule> elements, Map<String, Rule> namedRules, Set<Rule> followed) {
    for (Rule e in elements) {
      if (!followed.contains(e)) {
        followed.add(e);
        if (!e.name.isEmpty && e.type == RuleType.CAPTURE && !namedRules.containsKey(e.name)) {
          namedRules[e.name] = e;
        } 
        namedRules_(e.elements, namedRules, followed);
      }
    }
  }
  
  static List<Rule> finish(List<Rule> namedRules) {
    Map<String,Rule> rules = new Map();
    for (Rule r in namedRules) {
      rules[r.name] = r;
    }
    Map<String, Rule> literals = new Map();
    Set<Rule> followedDereference = new Set();
    Set<Rule> followedContract = new Set();
    Set<Rule> followedDeduplicate = new Set();    
    for (int i = 0; i < namedRules.length; i++) {
      Rule r = namedRules[i];
      r = deduplicate(r, literals, followedDeduplicate);
      r = dereference(r, rules, followedDereference);
      r = compact(r, followedContract);
      namedRules[i] = r;
    }   
    return namedRules;
  }
  
  /**
   * Substitutes reference rules with the actual rule.
   */
  static Rule dereference(Rule rule, Map<String,Rule> namedRules, Set<Rule> followed) {
    if (followed.contains(rule)) {
      return rule;
    }
    followed.add(rule);
    if (rule.type == RuleType.REFERENCE) {
      bool noCapture = rule.name[0] == '-';
      Rule r = namedRules[rule.name];
      if (r == null) {
        String name = rule.name.substring(noCapture ? 1:0);
        r = namedRules[name];
      }
      if (r == null) {
        throw new ArgumentError("No such rule: `"+rule.name+"`\nKnown rules are: "+namedRules.keys.toString());
      }
      r = dereference(r, namedRules, followed);
      return noCapture ? r.elements[0] : r;
    } else if (rule.elements.length > 0) {
      for (int i = 0; i < rule.elements.length; i++) {
        rule.elements[i] = dereference(rule.elements[i], namedRules, followed);
      }
    }
    return rule;
  }
  
  /**
   * Contracts selections with just {@link Terminal}s and 1 character literals
   * to a single {@link Terminal}. A selection of just literal characters will
   * also be contracted to a single terminal.
   */
  static Rule compact(Rule rule, Set<Rule> followed) {
    if (followed.contains(rule)) {
      return rule;
    }
    followed.add(rule);
    if (rule.type == RuleType.SELECTION) {
      int ts = 0;
      int ls = 0;
      for (Rule e in rule.elements) {
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
            t = t.and(Terminal.character(UTF8.codePoint0(r.literal)));
          }
        }
        return new Rule._terminal(t);
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
  static Rule deduplicate(Rule rule, Map<String, Rule> literals, Set<Rule> followed) {
    if (followed.contains(rule)) {
      return rule;
    }
    followed.add(rule);
    if (rule.type == RuleType.LITERAL) {
      String l = UTF8.string(rule.literal);
      Rule r = literals[l];
      if (r != null) {
        return r;
      }
      literals[l] = rule;
    } else if (rule.elements.length > 0) {
      for (int i = 0; i < rule.elements.length; i++) {
        rule.elements[i] = deduplicate(rule.elements[i], literals, followed);
      }
    }
    return rule;
    
  }  
}