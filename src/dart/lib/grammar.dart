part of lingukit;

/**
 * A set of rules.
 */
class Grammar  {
  
  final List<Rule> rules;
  
  Grammar(this.rules);
  
  Rule rule(String name) {
    var n0 = name[0];
    bool noCapture = n0 == '-' || n0 == '\\';
    for (Rule r in rules) {
      if (r != null) {
        if (r.name == name) {
          return r;
        }
        if ( name == "-"+r.name) {
          return noCapture && r.type == RuleType.CAPTURE ? r.elements[0].as(name) : r;
        }
      }
    }
    throw new ArgumentError("Missing rule: "+name);
  }
  
  String toString() {
    StringBuffer b = new StringBuffer();
    for (Rule r in rules) {
      if (r != null && !r.name.isEmpty) {
        b.write(r.name);
        for (int i = r.name.length; i < 15; i++) {
          b.write(' ');
        }
        b.write(" = ");
        for (Rule elem in r.elements) {
          String s = elem.toString();
          RuleType type = elem.type;
          if (type == RuleType.SEQUENCE || elem.type == RuleType.SELECTION) {
            s = s.substring(1, s.length-1);
          }
          b.write(s);
          b.write(' ');
        }
        b.write('\n');
      }
    }
    return b.toString();
  }
}

/**
 * Possible types of rules.
 */
class RuleType {

  static const LITERAL = const RuleType._("lit");
  static const TERMINAL = const RuleType._("trm");
  static const PATTERN = const RuleType._("pat");
  static const ITERATION = const RuleType._("itr");
  static const SEQUENCE = const RuleType._("seq");
  static const SELECTION = const RuleType._("sel");
  static const COMPLETION = const RuleType._("cpl");
  static const REFERENCE = const RuleType._("ref");
  static const CAPTURE = const RuleType._("cap");

  final String code;

  const RuleType._(this.code);
}

/**
 * A rule.
 */
class Rule {
  
  static final int UNDISTINGUISHABLE =  Math.pow(2,31);
  
  static final List<Rule> NO_ELEMENTS = new List<Rule>(0);
  static final List<int> NO_LITERAL = new List<int>(0);
  
  Rule.completion() : this(RuleType.COMPLETION, "", new List<Rule>(1), Occur.once, NO_LITERAL, null, null, 0);

  Rule.ref(String name) : this(RuleType.REFERENCE, name, NO_ELEMENTS, Occur.once, NO_LITERAL, null, null, 0);

  Rule.selection(List<Rule> elements) : this(RuleType.SELECTION, "", elements, Occur.once, NO_LITERAL, null, null, 0);

  Rule.seq(List<Rule> elements) : this (RuleType.SEQUENCE, "", complete(elements), Occur.once, NO_LITERAL, null, null, UNDISTINGUISHABLE);

  Rule.symbol( int codePoint ) : this(RuleType.LITERAL, "", NO_ELEMENTS, Occur.once, UTF8.bytesOf(codePoint), null, null, 0);

  Rule.string(String l) : this(RuleType.LITERAL, "", NO_ELEMENTS, Occur.once, UTF8.bytes(l), null, null, 0);

  Rule._pattern(Pattern p) : this(RuleType.PATTERN, "", NO_ELEMENTS, Occur.once, NO_LITERAL, null, p, 0);

  Rule._terminal(Terminal t) : this (RuleType.TERMINAL, "", NO_ELEMENTS, Occur.once, NO_LITERAL, t, null, 0);
  
  Rule(this.type, this.name, this.elements, this.occur, this.literal, this.terminal, this.pattern, this.distinctFromIndex);
  
  final RuleType type;
  final String name;
  final List<Rule> elements;
  final Occur occur;
  final List<int> literal;
  final Terminal terminal;
  final Pattern pattern;
  final int distinctFromIndex;
  
  Rule as(String name) {
    if (name.length > 0 && name[0] == '-') {
      return new Rule(type, name, elements, occur, literal, terminal, pattern, distinctFromIndex);
    }
    List<Rule> elems = type == RuleType.CAPTURE ? elements : [this];
    return new Rule(RuleType.CAPTURE, name, elems, Occur.once, NO_LITERAL, null, null, 0);
  }

  Rule plus() {
    return occurs(Occur.plus);
  }

  Rule star() {
    return occurs(Occur.star);
  }

  Rule qmark() {
    return occurs(Occur.qmark);
  }
  
  Rule occurs(Occur occur) {
    if (type == RuleType.ITERATION) {
      return occur == Occur.once ? elements[0] : new Rule(RuleType.ITERATION, name, elements, occur, literal, terminal, pattern, 0);
    }
    if (occur == Occur.once)
      return this;
    return new Rule(RuleType.ITERATION, "", [this], occur, NO_LITERAL, null, null, 0);
  }
  
  Rule distinctFrom(int index) {
    return new Rule(type, name, elements, occur, literal, terminal, pattern, index);
  }

  bool isDistinctive() {
    return distinctFromIndex != Rule.UNDISTINGUISHABLE;
  }
  
  static List<Rule> complete(List<Rule> elements) {
    for (int i = 0; i < elements.length-1; i++) {
      RuleType t = elements[i].type;
      if (t == RuleType.COMPLETION) {
        elements[i].elements[0] = elements[i+1];
      } else if (t == RuleType.CAPTURE && elements[i].elements[0].type == RuleType.COMPLETION) {
        elements[i].elements[0].elements[0] = elements[i+1];
      }
    }
    return elements;
  }
  
  String toString() {
    if (type == RuleType.CAPTURE) {
      return name;
    }
    if (type == RuleType.TERMINAL) {
      return terminal.toString();
    }
    if (type == RuleType.PATTERN) {
      return pattern.toString();
    }
    if (type == RuleType.LITERAL) {
      if (UTF8.characters(literal) == 1) {
        return UTF8.toLiteral(UTF8.codePoint0(literal));
      }
      return "'"+ UTF8.string(literal)+"'";
    }
    if (type == RuleType.REFERENCE) {
      return "@"+name;
    }
    if (type == RuleType.COMPLETION) {
      return "..`"+elements[0].toString()+"`";
    }
    if (type == RuleType.SELECTION) {
      StringBuffer b = new StringBuffer();
      for (Rule e in elements) {
        b.write(" | ");
        b.write(e.toString());
      }
      return "("+b.toString().substring(3)+")";
    }
    if (type == RuleType.SEQUENCE) {
      StringBuffer b = new StringBuffer();
      for (Rule e in elements) {
        b.write(" ");
        b.write(e);
      }
      return "("+b.toString().substring(1)+")";
    }
    if (type == RuleType.ITERATION) {
      if (occur == Occur.qmark && elements[0].type == RuleType.SEQUENCE) {
        String seq = elements[0].toString();
        return "["+seq.substring(1, seq.length-1)+"]";
      }
      return elements[0].toString()+occur.toString();
    }
    return type.toString()+" "+elements.toString();
  }  

}