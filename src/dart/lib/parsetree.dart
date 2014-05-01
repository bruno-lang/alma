part of lingukit;

class Parsed {
  
  final List<int> file;
  final ParseTree tree;
  
  Parsed(this.file, this.tree);
  
  String text(int index) {
    return UTF8.string(file.sublist(tree.start(index), tree.end(index)));
  }

  static Parsed from(String srcFile, String start, Grammar grammar) {
    List<int> buffer = new File(srcFile).readAsBytesSync();
    ParseTree tree = Parser.parse(buffer, grammar.rule(start));
    return new Parsed(buffer, tree);
  }
}

class ParseTree {
  
  final List<Rule> rules;
  final List<int> starts;
  final List<int> ends;
  final List<int> levels;
  
  final List<int> indexStack = new List(50);
  
  int lev;
  int top;
  
  ParseTree(this.rules, this.starts, this.ends, this.levels, this.lev, this.top);
  
  ParseTree.fixed(int length) : this(new List(length), new List(length), new List(length), new List(length), -1, -1);

  void push(Rule rule, int start) {
    starts[++top] = start;
    ends[top] = start;
    rules[top] = rule;
    lev++;
    levels[top] = lev;
    indexStack[lev] = top;
  }
  
  int end([int index = 0]) {
    return ends.length == 0 ? -1 : ends[index];
  }
  
  int start(int index) {
    return starts[index];
  }
  
  int level(int index) {
    return levels[index];
  }
  
  Rule rule(int index) {
    return rules[index];
  }
  
  int count() {
    return top+1;
  }

  void pop() {
    top = indexStack[lev]-1;
    lev--;
  }

  void done(int end) {
    ends[indexStack[lev]] = end;
    lev--;
  }
  
  void erase(int position) {
    while (ends[top] > position) {
      top--;
    }
  }

  String toString() {
    if (top < 0)
      return "(empty)";
    StringBuffer b = new StringBuffer();
    for (int i = 0; i <= top; i++) {
      _toString(b, "", i);
    }
    return b.toString();
  }
  
  void _toString(StringBuffer b, String indent, int index) {
    for (int i = 0; i < levels[index].abs(); i++) {
      b.write(' ');
    }
    b.write(rules[index].name);
    b.write(' ');
    b.write(starts[index]);
    b.write(':');
    b.write(ends[index]);
    b.write('\n');
  }
  
  /*
   * further processing utility functions below.
   */
  
  int next(int index) {
    final int l = level(index);
    final int c = count();
    while (index < c) {
      if (level(++index) <= l) {
        return index;
      }
    }
    return c;
  }
  
  ParseTree debug() {
    int t = 0;
    while (t < rules.length && rules[t] != null) { t++; }
    return new ParseTree(rules, starts, ends, levels, 0, t-1);
  }
  
  bool isSequential() {
    return starts[1] == ends[0];
  }
  
  ParseTree sequential() {
    if (isSequential()) {
      return this;
    }
    ParseTree l = new ParseTree.fixed(rules.length);
    _sequential(l, 0);
    return l;
  }
  
  int _sequential(ParseTree dest, final int index) {
    int i = index;
    final int l = level(i);
    final int nextLevel = l+1; // the level we are looking for
    final int c = count();
    i++;
    if (i >= c || level(i) <= l) {
      dest._push(rule(index), l, start(index), end(index));
      return i;
    }
    int s0 = start(index);
    while (i < c && level(i) == nextLevel) {
      int s = start(i);
      if (s > s0) {
        dest._push(rule(index), -level(index), s0, s);
      }
      i = _sequential(dest, i);
      s0 = dest.ends[dest.top];
    }
    int e0 = end(index);
    if (e0 > s0) {
      dest._push(rule(index), -l, s0, e0);
    }
    return i;
  }
  
  void _push(Rule rule, int level, int start, int end) {
    rules[++top] = rule;
    levels[top] = level;
    starts[top] = start;
    ends[top] = end;
  }

}