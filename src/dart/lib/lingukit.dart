library lingukit;

import 'dart:math' as Math;
import 'dart:convert' as Encoding show UTF8, HTML_ESCAPE;
import 'dart:typed_data' show Uint8List;
import 'dart:io';

part 'grammar.dart';
part 'linguist.dart';
part 'terminal.dart';
part 'occur.dart';
part 'parsetree.dart';
part 'parser.dart';
part 'utf8.dart';
part 'pattern.dart';
part 'builder.dart';
part 'printer.dart';

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
    name_ = seq([sym('-').qmark(), sym('\\').qmark(), terminal(Terminal.LETTERS), terminal(Terminal.LETTERS.and(Terminal.DIGITS).and(Terminal.char('_')).and(Terminal.char('-'))).star()]).as("name"),
    capture_ = seq([sym(':'), name_.as("alias")]).qmark().as("capture"),
    ref_ = seq([name_, capture_]).as("ref"),

    wildcard_ = new Rule.string('\$').as("wildcard"),
    symbol_ = seq([a, terminal(Terminal.WILDCARD), a]).as("symbol"),
    code_point_ = seq([string("U+"), terminal(Terminal.HEX_NUMBER).occurs(occur(4, 8))]).as("code-point"), 
    literal_ = selection([code_point_, symbol_]).as("literal"),
    range_ = seq([literal_, g, sym('-'), g, literal_]).as("range"),
    letter_ = sym('@').as("letter"),
    upper_ = sym('Z').as("upper"),
    lower_ = sym('z').as("lower"),
    digit_ = sym('9').as("digit"),
    hex_ = sym('#').as("hex"),
    octal_ = sym('7').as("octal"),
    binary_ = sym('1').as("binary"),
    not_ = sym('!').as("not"),
    whitespace_ = sym('_').as("whitespace"),
    gap_ = sym(',').as("gap"),
    pad_ = sym('~').as("pad"),
    wrap_ = sym('.').as("wrap"),
    indent_ = string(">>").as("indent"),
    separator_ = sym('^').as("separator"),

    tab_ = string("\\t").as("tab"),
    lf_ = string("\\n").as("lf"),
    cr_ = string("\\r").as("cr"),
    shortname_ = selection([tab_, lf_, cr_]).as("shortname"),

    category_ = seq([string("U+{"), terminal(Terminal.LETTERS).plus(), sym('}')]).as("category"),
    ranges_ = seq([not_.qmark(), selection([wildcard_, letter_, upper_, lower_, digit_, hex_, octal_, binary_, category_, range_, literal_, whitespace_, shortname_])]).as("ranges"),

    figure_ = selection([ranges_, name_]).as("-figure"),
    figures_ = seq([sym('{'), g, seq([figure_, seq([g, figure_]).star()]) , g, sym('}'), capture_]).as("figures"),
    pattern_ = seq([not_.qmark(), selection([gap_, pad_, indent_, separator_, wrap_])]).as("pattern"),
    terminal_ = selection([pattern_, ranges_, figures_]).as("terminal"),

    string_ = seq([a, terminal(Terminal.notChar('\'')).occurs(occur(2, Occur.plus.max)), a]).as("string"),

    num_ = terminal(Terminal.DIGITS).plus().as("num"),
    star_ = sym('*').as("star"),
    plus_ = sym('+').as("plus"),
    qmark_ = sym('?').as("qmark"),
    occurrence_ = selection([seq([sym('x').qmark(), num_.as("min"), terminal(Terminal.char('-').and(Terminal.char('+'))).as("to").qmark(), num_.as("max").qmark()]), qmark_, star_, plus_]).as("occurrence"),

    option_ = seq([sym('['), g, ref("selection"), g, sym(']'), capture_]).as("option"),
    group_ = seq([sym('('), g, ref("selection"), g, sym(')'), capture_]).as("group"),
    completion_ = seq([string(".."), capture_]).as("completion"),
    distinction_ = sym('<').as("distinction"),
    lookahead_ = seq([string(">("), g, ref("selection"), g, sym(')')]).as("lookahead"),
    element_ = seq([selection([distinction_, completion_, group_, option_, lookahead_, string_, terminal_, ref_]), occurrence_.qmark()]).as("element"),

    sequence_ = seq([element_, seq([i, element_]).star()]).as("sequence"),
    selection_ = seq([sequence_, seq([g, sym('|'), i, sequence_]).star()]).as("selection"),

    rule_ = seq([name_, g, selection([sym('='), seq([sym(':'), sym(':').qmark(), sym('=').qmark()])]), g, selection_, sym(';').qmark(), w]).as("rule"),
    comment_ = seq([sym('%'), terminal(Terminal.notChar('\n')).plus().as("text")]).as("comment"),
    member_ = selection([comment_, rule_]).as("member"), 
    grammar_ = seq([member_, seq([g, member_]).star()]).as("grammar") 
    ;
  
  final Grammar GRAMMAR = new Grammar(Linguist.finish(Linguist.namedRules([grammar_])));
