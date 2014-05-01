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
    Set<Rule> followedDereference = new Set();
    for (int i = 0; i < namedRules.length; i++) {
      Rule r = namedRules[i];
      r = dereference(r, rules, followedDereference);
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
      return noCapture ? r.elements[0] : r;
    } else if (rule.elements.length > 0) {
      for (int i = 0; i < rule.elements.length; i++) {
        rule.elements[i] = dereference(rule.elements[i], namedRules, followed);
      }
    }
    return rule;
  }
  
}