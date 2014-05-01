part of lingukit;

class ParseException implements Exception {

  final int determinationPosition;
  final int errorPosition;

  ParseException(this.determinationPosition, this.errorPosition);
  
}

class Parser {

  static ParseTree parse(List<int> input, Rule start) {
    ParseTree tree = new ParseTree.fixed(input.length);
    int t = 0;
    try {
      t = parseRule(start, input, 0, tree);
    } on ParseException catch(e) { 
      t = e.errorPosition;
    } catch (e, s) {
      print(e);
      print(s);
    }
    if (tree.end() != input.length) {
      int pos = t.abs();
      String msg = "Failed to parse at $pos:";
      print(msg);
      print(tree.debug());
      //FIXME what if end of file...
      print(UTF8.string(input.sublist(pos, Math.min(pos+20, input.length))));
      throw new ParseException(-1, pos);
    }
    //TODO verify and visualize errors
    return tree;
  }
  
  static int parseRule(Rule rule, List<int> input, int position, ParseTree tree) {
    switch (rule.type) {
    case RuleType.LITERAL:
      return parseLiteral(rule, input, position);
    case RuleType.TERMINAL:
      return parseTerminal(rule, input, position);
    case RuleType.PATTERN:
      return parsePattern(rule, input, position);
    case RuleType.ITERATION:
      return parseIteration(rule, input, position, tree);
    case RuleType.SEQUENCE:
      return parseSequence(rule, input, position, tree);
    case RuleType.SELECTION:
      return parseSelection(rule, input, position, tree);
    case RuleType.COMPLETION:
      return parseCompletion(rule, input, position, tree);
    case RuleType.CAPTURE:
      return parseCapture(rule, input, position, tree);
    default:
      throw new ArgumentError("`"+rule.toString()+"` has no proper type: "+rule.type.toString());
    }
  }
  
  static int mismatch(int position) {
    return -position-1;
  }

  static int parseCompletion(Rule rule, List<int> input, int position, ParseTree tree) {
    final int l = input.length;
    while (position < l) {
      int end = parseRule(rule.elements[0], input, position, tree);
      if (end < 0) {
        position++;
      } else {
        tree.erase(position);
        return position;
      }
    }
    tree.erase(position);
    return mismatch(l);
  }

  static int parseLiteral(Rule rule, List<int> input, int position) {
    final List<int> literal = rule.literal;
    final int limit = input.length;
    for (int i = 0; i < literal.length; i++) {
      if (position >= limit)
        return mismatch(limit);
      if (input[position] != literal[i])
        return mismatch(position);
      position++;
    }
    return position;
  }

  static int parseCapture(Rule rule, List<int> input, int position, ParseTree tree) {
    tree.push(rule, position);
    int end = parseRule(rule.elements[0], input, position, tree);
    if (end > position) {
      tree.done(end);
    } else {
      tree.pop();
    }
    return end;
  }

  static int parseSelection(Rule rule, List<int> input, int position, ParseTree tree) {
    int end = mismatch(position);
    for (Rule r in rule.elements) {
      int endPosition = parseRule(r, input, position, tree);
      if (endPosition >= 0) {
        return endPosition;
      }
      end = Math.min(end, endPosition);
    }
    tree.erase(position);
    return end;
  }

  static int parseSequence(Rule rule, List<int> input, int position, ParseTree tree) {
    int end = position;
    for (int i = 0; i < rule.elements.length; i++) {
      Rule r = rule.elements[i];
      int endPosition = parseRule(r, input, end, tree);
      if (endPosition < 0) {
        if (rule.distinctFromIndex <= i) {
          tree.erase(end);
          throw new ParseException(end, endPosition);
        }
        tree.erase(position);
        return endPosition;
      }
      end = endPosition;
    }
    return end;
  }

  static int parseIteration(Rule rule, List<int> input, int position, ParseTree tree) {
    int end = position;
    int c = 0;
    while (c < rule.occur.max) {
      int endPosition = parseRule(rule.elements[0], input, end, tree);
      if (endPosition < 0) {
        tree.erase(end);
        if (c < rule.occur.min) {
          return endPosition;
        }
        return end;
      } else {
        end = endPosition;
        c++;
      }
    }
    return end;
  }

  static int parseTerminal(Rule rule, List<int> input, int position) {
    if (position >= input.length)
      return mismatch(position);
    if (rule.terminal.contains(input, position)) {
      return position + UTF8.byteCount(input[position]);
    }
    return mismatch(position);
  }
  
  static int parsePattern(Rule rule, List<int> input, int position) {
    final int l = rule.pattern.length(input, position);
    return l < 0 ? mismatch(position) : position + l;
  }

}