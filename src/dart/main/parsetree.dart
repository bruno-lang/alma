part of lingukit;

class ParseTree {
  
  final List<Rule> rules;
  final List<int> starts;
  final List<int> ends;
  final List<int> levels;
  
  final List<int> indexStack;
  
  int lev = -1;
  int top = -1;
  
  ParseTree(this.rules, this.starts, this.ends, this.levels, this.indexStack);
  
  ParseTree.empty() : this([], [], [], [], []);

  void push(Rule rule, int start) {
    starts[++top] = start;
    ends[top] = start;
    rules[top] = rule;
    lev++;
    levels[top] = lev;
    indexStack[lev] = top;
  }
  
  int end([int index = 0]) {
    return ends[index];
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
}