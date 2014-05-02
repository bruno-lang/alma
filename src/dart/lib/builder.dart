part of lingukit;

/**
 * Builds a {@link Grammar} from a given {@link Parsed} grammar file.
 *  
 * @author jan
 */
class Builder {

  /**
   * Used to indicate the distinct from index as a {@link Rule} that is
   * detected by its identity. It has just this workaround helper
   * functionality within the builder.
   */
  static final Rule DISTINCT_FROM = new Rule.seq(new List(0));
  
  static List<Rule> buildGrammar(Parsed grammar) {
    final List<Rule> rules = new List();
    final ParseTree tokens = grammar.tree;
    final int c = tokens.count();
    int token = 0;
    while (token < c) {
      Rule r = tokens.rule(token);
      if (r == grammar_) {
        token++;
      } else if (r == member_) {
        if (tokens.rule(token+1) == rule_) {
          Rule rule = buildRule(token+1, grammar);
          rules.add(rule);
        }
        token = tokens.next(token);
      }
    }
    return rules;
  }

  static Rule buildRule(int token, Parsed grammar) {
    check(token, grammar, rule_);
    return buildSelection(token+2, grammar).as(grammar.text(token+1));
  }

  static Rule buildSelection(int token, Parsed grammar) {
    check(token, grammar, selection_);
    final List<Rule> alternatives = new List();
    final ParseTree tokens = grammar.tree;
    final int end = tokens.end(token)+1;
    int i = token+1;
    while (tokens.rule(i) == sequence_ && tokens.end(i) <= end) {
      alternatives.add(buildSequence(i, grammar));
      i = tokens.next(i);
    }
    if (alternatives.length == 1) {
      return alternatives[0];
    }
    return new Rule.selection(alternatives);
  }

  static Rule buildSequence(int token, Parsed grammar) {
    check(token, grammar, sequence_);
    final List<Rule> elems = new List();
    final ParseTree tokens = grammar.tree;
    final int end = tokens.end(token)+1;
    int distinctFrom = Rule.UNDISTINGUISHABLE;
    int i = token+1;
    while (tokens.rule(i) == element_ && tokens.end(i) <= end) {
      Rule e = buildElement(i, grammar);
      if (e != DISTINCT_FROM) {
        elems.add(e);
      } else {
        distinctFrom = elems.length;
      }
      i = tokens.next(i);
    }
    if (elems.length == 1) {
      return elems[0];
    }
    return new Rule.seq(elems).distinctFrom(distinctFrom);
  }

  static Rule buildElement(int token, Parsed grammar) {
    check(token, grammar, element_);
    final ParseTree tokens = grammar.tree;
    Occur occur = buildOccur(tokens.next(token+1), grammar, token);
    Rule r = tokens.rule(token+1);
    if (r == distinction_) {
      return DISTINCT_FROM;
    }
    if (r == completion_) {
      return new Rule.completion();
    }
    if (r == group_) {
      return buildCapture(tokens.next(token+2), grammar, buildSelection(token+2, grammar)).occurs(occur);
    }
    if (r == option_) {
      return buildCapture(tokens.next(token+2), grammar, buildSelection(token+2, grammar)).occurs(Occur.qmark);
    }
    if (r == terminal_) {
      Rule t = buildTerminal(token+1, grammar).occurs(occur);
      // a terminal of a single character -> use literal instead
      if (t.type == RuleType.TERMINAL && t.terminal.isSingleCharacter() && t.terminal.ranges[0] >= 0) { 
        return new Rule.string(UTF8.string(UTF8.bytesOf(t.terminal.ranges[0]))).occurs(occur);
      }
      return t;
    }
    if (r == string_) {
      String text = grammar.text(token+1);
      return new Rule.string(text.substring(1, text.length-1)).occurs(occur);
    }
    if (r == ref_) {
      return buildRef(token+1, grammar).occurs(occur);
    }
    throw unexpectedRule(r);
  }

  static Rule buildRef(int token, Parsed grammar) {
    return buildCapture(token+2, grammar, new Rule.ref(grammar.text(token+1)));
  }

  static Rule buildCapture(int token, Parsed grammar, Rule rule) {
    if (grammar.tree.rule(token) == capture_) {
      return rule.as(grammar.text(token+1));
    }
    return rule;
  }

  static Rule buildTerminal(int token, Parsed grammar) {
    check(token, grammar, terminal_);
    Rule r = grammar.tree.rule(token+1);
    if (r == ranges_) {
      return buildRanges(token+1, grammar);
    }
    if (r == figures_) {
      return buildFigures(token+1, grammar);
    }
    if (r == pattern_) {
      return buildPattern(token+1, grammar);
    }
    throw unexpectedRule(r);
  }

  static Rule buildPattern(int token, Parsed grammar) {
    check(token, grammar, pattern_);
    bool not = grammar.tree.rule(token+1) == not_;
    Rule p = patternSelection(token+(not?2:1), grammar);
    return not ? new Rule._pattern(Pattern.not(p.pattern)) : p;
  }

  static Rule patternSelection(int token, Parsed grammar) {
    Rule r = grammar.tree.rule(token);
    if (r == gap_) {
      return new Rule._pattern(Pattern.GAP);
    }
    if (r == pad_) {
      return new Rule._pattern(Pattern.PAD);
    }
    if (r == indent_) {
      return new Rule._pattern(Pattern.INDENT);
    }
    if (r == separator_) {
      return new Rule._pattern(Pattern.SEPARATOR);
    }
    if (r == wrap_) {
      return new Rule._pattern(Pattern.WRAP);
    }
    throw unexpectedRule(r);
  }

  static Rule buildFigures(int token, Parsed grammar) {
    check(token, grammar, figures_);
    final ParseTree tokens = grammar.tree;
    final int end = tokens.end(token);
    Terminal terminal = null;
    int i = token+1;
    List<Rule> refs = new List();
    while (tokens.end(i) <= end && tokens.rule(i) != capture_) {
      Rule figure = tokens.rule(i);
      if (figure == ranges_) {
        Rule ranges = buildRanges(i, grammar);
        terminal = terminal == null ? ranges.terminal : terminal.and(ranges.terminal);
      } else if (figure == name_) {
        String name = grammar.text(i);
        if (name[0] != '-') {
          name = "-"+name; // always do not capture these
        }
        refs.add(new Rule.ref(name));
      }
      i = tokens.next(i);
    }
    Rule r = terminal == null ? new Rule.selection(refs) : new Rule._terminal(terminal);
    if (!refs.isEmpty && terminal != null) {
      refs.add(r);
      r = new Rule.selection(refs);
    }
    return buildCapture(i, grammar, r);
  }

  static Rule buildRanges(int token, Parsed grammar) {
    check(token, grammar, ranges_);
    bool not = grammar.tree.rule(token+1) == not_;
    Rule ranges = rangesSelection(token +(not ? 2 : 1), grammar);
    return not ? new Rule._terminal(ranges.terminal.not()) : ranges;
  }

  static Rule rangesSelection(int token, Parsed grammar) {
    Rule r = grammar.tree.rule(token);
    if (r == wildcard_) {
      return new Rule._terminal(Terminal.WILDCARD);
    }
    if (r == letter_) {
      return new Rule._terminal(Terminal.LETTERS);
    }
    if (r == upper_) {
      return new Rule._terminal(Terminal.UPPER_LETTERS);
    }
    if (r == lower_) {
      return new Rule._terminal(Terminal.LOWER_LETTERS);
    }
    if (r == hex_) {
      return new Rule._terminal(Terminal.HEX_NUMBER);
    }
    if (r == octal_) {
      return new Rule._terminal(Terminal.OCTAL_NUMBER);
    }
    if (r == binary_) {
      return new Rule._terminal(Terminal.BINARY_NUMBER);
    }
    if (r == digit_) {
      return new Rule._terminal(Terminal.DIGITS);
    }
    if (r == category_) {
      //TODO
      throw new ArgumentError("Not available yet");
    }
    if (r == range_) {
      return new Rule._terminal(Terminal.range(buildLiteral(token+1, grammar), buildLiteral(token+3, grammar)));
    }
    if (r == literal_) {
      return new Rule._terminal(Terminal.character(buildLiteral(token, grammar)));
    }
    if (r == whitespace_) {
      return new Rule._terminal(Terminal.WHITESPACE);
    }
    if (r == shortname_) {
      String name = grammar.text(token+1);
      String c = name[1];
      if (c == 't') {
        return new Rule._terminal(Terminal.char('\t'));
      }
      if (c == 'n') {
        return new Rule._terminal(Terminal.char('\n'));
      }
      if (c == 'r') {
        return new Rule._terminal(Terminal.char('\r'));
      }
      throw new ArgumentError("No such element"+name);
    }
    throw unexpectedRule(r);
  }

  static int buildLiteral(int token, Parsed grammar) {
    check(token, grammar, literal_);
    Rule r = grammar.tree.rule(token+1);
    if (r == symbol_) {
      return UTF8.codePointAt(grammar.bytes(token+1), 1);
    }
    if (r == code_point_) {
      return int.parse(grammar.text(token+1).substring(2), radix : 16);
    }
    throw unexpectedRule(r);
  }

  static Occur buildOccur(int token, Parsed grammar, int parent) {
    // there might not be an occurrence token or it belongs to a outer parent 
    if (grammar.tree.rule(token) != occurrence_ || grammar.tree.end(parent) < grammar.tree.end(token)) {
      return Occur.once;
    }
    Rule occur = grammar.tree.rule(token+1);
    if (occur == plus_) {
      return Occur.plus;
    }
    if (occur == star_) {
      return Occur.star;
    }
    if (occur == qmark_) {
      return Occur.qmark;
    }
    int min = int.parse(grammar.text(token+1));
    int max = min;
    if ("to" == grammar.tree.rule(token+2).name) {
      max = Occur.plus.max;
      if ("max" == grammar.tree.rule(token+3).name) {
        max = int.parse(grammar.text(token+3));
      }
    }
    return new Occur.occur(min, max);
  }
  
  static ArgumentError unexpectedRule(Rule r) {
    return new ArgumentError("Unexpected rule: "+r.toString());
  }
  
  static void check(int token, Parsed grammar, Rule expected) {
    if (grammar.tree.rule(token) != expected) {
      throw new ArgumentError("expected "+expected.toString()+" but got: "+grammar.tree.rule(token).toString());
    }
  }
}
