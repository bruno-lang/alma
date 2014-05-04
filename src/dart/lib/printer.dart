part of lingukit;

  const String BOLD = ' bold';
  const String UNDERLINED = ' underline';
  const String BLACK = ' black';
  
  final List<String> RAINBOW = [ "blue", "cyan", "green", "yellow", "red", "magenta" ];
  
  String rainbow(int no) {
    return RAINBOW[no.abs() % RAINBOW.length];
  }

class Printer {
  
  final IOSink out;
  
  Printer(this.out);
  
  printTree(Parsed t) {
    ParseTree tokens = t.tree.sequential();
    printPlain('<!DOCTYPE html><head><style>pre { font-family: Consola, monospace; background-color: #aaa; }');
    for (String c in RAINBOW) {
      printPlain('\n.$c { color: $c; }');
    }
    printPlain('\n.bold { font-weight: bold; }');
    printPlain('\n.underline { text-decoration: underline; }');
    printPlain('</style></head><body><pre>');
    for (int i = 0; i < tokens.count(); i++) {
      printToken(tokens, t.file, i);
    }
    printPlain('</pre></body></html>');
    printPlain('\n');
  }
  
  printToken(ParseTree tokens, List<int> file, int index) {
    int s = tokens.start(index);
    int e = tokens.end(index);
    if (e == s) {
      return;
    }
    int l = tokens.level(index);
    if (l < 0) {
      printColor(file, BLACK+BOLD, s, e);
      return;
    }
    Rule r = tokens.rule(index);
    printColorBlock(file, rainbow(r.name.hashCode~/2-1), s, e);
    printEndOf();
  }
  
  printPlain(String s) {
    out.write(s);
  }
  
  void printColorBlock(List<int> input, String color, int s, int e) {
    if (s+1 == e) {
      printColor(input, color + BOLD+ UNDERLINED, s, e);
    } else {
      printColor(input, color+BOLD, s, s+1);
      printColor(input, color, s+1, e-1);
      printColor(input, color+UNDERLINED, e-1, e);
    }
  }

  void printColor(List<int> input, String color, int start, int end) {
    if (start < end) {
      printColorText(color, UTF8.string(input.sublist(start, end)));
    }
  }

  void printColorText(String color, String text) {
    final int l = text.length;
    int lf = text.indexOf('\n');
    int s = 0;
    while (lf >= 0 || s < l) {
      int e = lf < 0 ? l : lf+1;
      printStartOf(color);
      printEscaped(text.substring(s, e));
      printEndOf();
      s = e;
      lf = text.indexOf('\n', e);
    }
  }
  
  printEscaped(String text) {
    printPlain(Encoding.HTML_ESCAPE.convert(text));
  }
  
  printStartOf(String color) {
    printPlain("<span class='$color'>");
  }
  
  printEndOf() {
    printPlain("</span>");
  }
  
}