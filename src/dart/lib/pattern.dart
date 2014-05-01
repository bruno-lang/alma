part of lingukit;

abstract class Pattern {
  
  static const int NOT_MACHTING = -1;
  
  int length(List<int> bytes, int position);

  /**
   * <code>,</code>
   */
  static final Pattern GAP = new Gap();
  /**
   * <code>~</code>
   */
  static final Pattern PAD = new Pad();
  /**
   * <code>>></code>
   */
  static final Pattern INDENT = new Indent();
  /**
   * <code>^</code>
   */
  static final Pattern SEPARATOR = new Separator();
  /**
   * <code>.</code>
   */
  static final Pattern WRAP = new Wrap();

  static Pattern not( Pattern excluded ) {
    return new Not(excluded);
  }
  
  static Pattern or( Pattern a, Pattern b ) {
    if (a == null)
      return b;
    if (b == null)
      return a;
    return new Or(a, b);
  }
}

  
  
class Wrap implements Pattern {

  int length(List<int> input, int position) {
    final int l = input.length;
    int p = position;
    while (p  < l && isIndent(input[p])) { p++; }
    if (p >= l) {
      return p - position;
    }
    int w = p;
    while (p < l && isWrap(input[p])) { p++; }
    if (w == p) {
      return Pattern.NOT_MACHTING;
    }
    while (p < l && isIndent(input[p])) { p++; }
    return p - position;
  }
  
  static bool isWrap(int b) {
    return b == 10 || b == 13;
  }

  String toString() {
    return ".";
  }
  
}
  
class Separator implements Pattern {

  int length(List<int> input, int position) {
    int p = position;
    while (p < input.length && isIndent(input[p])) { p++; }
    int c = p-position;
    return c == 0 ? Pattern.NOT_MACHTING : c;
  }
  
  String toString() {
    return "^";
  }

}

bool isIndent(int b) {
  return b == 32 || b == 9;
}

class Indent implements Pattern {

  int length(List<int> input, int position) {
    int p = position;
    while (p < input.length && isIndent(input[p])) { p++; }
    return p-position;
  }
  
  
 String toString() {
    return ">>";
  }

}
  
class Pad implements Pattern {

  int length(List<int> input, int position) {
    int c = 0;
    while ( 
        UTF8.isWhitespace(input[position++])) { c++; }
    return c == 0 ? Pattern.NOT_MACHTING : c;
  }
  
  String toString() {
    return "~";
  }

}
  
class Gap implements Pattern {

  int length(List<int> input, int position) {
    int c = 0;
    while (  position < input.length &&
         UTF8.isWhitespace(input[position++])) { c++; }
    return c;
  }
  
  String toString() {
    return ",";
  }
}
  
class Not implements Pattern {

  final Pattern excluded;

  Not(this.excluded);

  int length(List<int> input, int position) {
    int l = excluded.length(input, position);
    return l < 0 ? UTF8.byteCount(input[position]) : Pattern.NOT_MACHTING;
  }

  String toString() {
    return "!" + excluded.toString();
  }
}

class Or implements Pattern {

  final Pattern a;
  final Pattern b;

  Or(this.a, this.b);
  
  int length(List<int> input, int position) {
    return Math.max(a.length(input, position), b.length(input, position));
  }

  String toString() {
    return a.toString()+" | "+b.toString();
  }
}  
