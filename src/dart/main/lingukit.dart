library lingukit;

import 'dart:math' as Math;
import 'dart:convert' as Encoding;

part 'grammar.dart';
part 'mechanic.dart';
part 'terminal.dart';
part 'occur.dart';
part 'parsetree.dart';
part 'parser.dart';
part 'utf8.dart';
part 'pattern.dart';

  main() {
    print(GRAMMAR);
  }


  Rule seq(List<Rule> elements) {
    return new Rule.seq(elements);
  }
  
  Rule pat(Pattern p) {
    return new Rule._pattern(p);
  }
  
  Rule sym(String s) {
    return new Rule.string(s);
  }
  
  Rule terminal(Terminal t) {
    return new Rule._terminal(t);
  }
  
  Rule string(String s) {
    return new Rule.string(s);
  }
  
  Rule selection(List<Rule> elements) {
    return new Rule.selection(elements);
  }
  
  Rule ref(String name) {
    return new Rule.ref(name);
  }
   
  Occur occur(int min, int max) {
    return new Occur.occur(min, max);
  }

  final Rule
    g = pat(Pattern.GAP),
    i = pat(Pattern.INDENT),
    w = pat(Pattern.WRAP),
    a = sym("\'");

  final Rule
    name = seq([sym('-').qmark(), sym('\\').qmark(), terminal(Terminal.LETTERS), terminal(Terminal.LETTERS.and(Terminal.DIGITS).and(Terminal.char('_')).and(Terminal.char('-'))).star()]).as("name"),
    capture = seq([sym(':'), name.as("alias")]).qmark().as("capture"),
    ref_ = seq([name, capture]).as("ref"),

    wildcard = new Rule.string('\$').as("wildcard"),
    symbol = seq([a, terminal(Terminal.WILDCARD), a]).as("symbol"),
    code_point = seq([string("U+"), terminal(Terminal.HEX_NUMBER).occurs(occur(4, 8))]).as("code-point"), 
    literal = selection([code_point, symbol]).as("literal"),
    range = seq([literal, g, sym('-'), g, literal]).as("range"),
    letter = sym('@').as("letter"),
    upper = sym('Z').as("upper"),
    lower = sym('z').as("lower"),
    digit = sym('9').as("digit"),
    hex = sym('#').as("hex"),
    octal = sym('7').as("octal"),
    binary = sym('1').as("binary"),
    not = sym('!').as("not"),
    whitespace = sym('_').as("whitespace"),
    gap = sym(',').as("gap"),
    pad = sym('~').as("pad"),
    wrap = sym('.').as("wrap"),
    indent = string(">>").as("indent"),
    separator = sym('^').as("separator"),

    tab = string("\\t").as("tab"),
    lf = string("\\n").as("lf"),
    cr = string("\\r").as("cr"),
    shortname = selection([tab, lf, cr]).as("shortname"),

    category = seq([string("U+{"), terminal(Terminal.LETTERS).plus(), sym('}')]).as("category"),
    ranges = seq([not.qmark(), selection([wildcard, letter, upper, lower, digit, hex, octal, binary, category, range, literal, whitespace, shortname])]).as("ranges"),

    figure = selection([ranges, name]).as("-figure"),
    figures = seq([sym('{'), g, seq([figure, seq([g, figure]).star()]) , g, sym('}'), capture]).as("figures"),
    pattern = seq([not.qmark(), selection([gap, pad, indent, separator, wrap])]).as("pattern"),
    terminal_ = selection([pattern, ranges, figures]).as("terminal"),

    string_ = seq([a, terminal(Terminal.notChar('\'')).occurs(occur(2, Occur.plus.max)), a]).as("string"),

    num = terminal(Terminal.DIGITS).plus().as("num"),
    star = sym('*').as("star"),
    plus = sym('+').as("plus"),
    qmark = sym('?').as("qmark"),
    occurrence = selection([seq([sym('x').qmark(), num.as("min"), terminal(Terminal.char('-').and(Terminal.char('+'))).as("to").qmark(), num.as("max").qmark()]), qmark, star, plus]).as("occurrence"),

    option = seq([sym('['), g, ref("selection"), g, sym(']'), capture]).as("option"),
    group = seq([sym('('), g, ref("selection"), g, sym(')'), capture]).as("group"),
    completion = seq([string(".."), capture]).as("completion"),
    distinction = sym('<').as("distinction"),
    element = seq([selection([distinction, completion, group, option, string_, terminal_, ref_]), occurrence.qmark()]).as("element"),

    sequence = seq([element, seq([i, element]).star()]).as("sequence"),
    selection_ = seq([sequence, seq([g, sym('|'), i, sequence]).star()]).as("selection"),

    rule = seq([name, g, selection([sym('='), seq([sym(':'), sym(':').qmark(), sym('=').qmark()])]), g, selection_, sym(';').qmark(), w]).as("rule"),
    comment = seq([sym('%'), terminal(Terminal.notChar('\n')).plus().as("text")]).as("comment"),
    member = selection([comment, rule]).as("member"), 
    grammar = seq([member, seq([g, member]).star()]).as("grammar") 
    ;
  
  final Grammar GRAMMAR = new Grammar(Mechanic.finish(Mechanic.namedRules([grammar])));
