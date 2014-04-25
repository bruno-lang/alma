part of lingukit;

class ParseTree {
  
  final List<Rule> rules;
  final List<int> starts;
  final List<int> ends;
  final List<int> levels;
  
  final List<int> indexStack;
  
  int level = -1;
  int top = -1;
  
  ParseTree() {
    rules = [];
    starts = [];
    ends = [];
    levels = [];
    indexStack = [];
  }

  void push(Rule rule, int start) {
    starts[++top] = start;
    ends[top] = start;
    rules[top] = rule;
    level++;
    levels[top] = level;
    indexStack[level] = top;
  }
  
  int end(int index) {
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
  
  int end() {
    return ends[0];
  }
  
  int count() {
    return top+1;
  }

  void pop() {
    top = indexStack[level]-1;
    level--;
  }

  void done(int end) {
    ends[indexStack[level]] = end;
    level--;
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