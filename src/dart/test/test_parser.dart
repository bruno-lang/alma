import 'package:unittest/unittest.dart';
import 'package:lingukit/lingukit.dart';
import 'dart:io';

main() {
  print(GRAMMAR);
  //Parser.parse(UTF8.bytes('foo'), name_);
  Parsed bruno = Parsed.from('/home/jan/proj/bruno/lingukit/etc/bruno.grammar', 'grammar', GRAMMAR);
  Parsed lingukit = Parsed.from('/home/jan/proj/bruno/lingukit/etc/lingukit.grammar', 'grammar', GRAMMAR);
  Parsed json = Parsed.from('/home/jan/proj/bruno/lingukit/etc/json.grammar', 'grammar', GRAMMAR);
  new Printer(stdout).printTree(Parsed.from('/home/jan/proj/bruno/lingukit/etc/example.json', 'json', grammarFrom(json)));
  Parsed xml = Parsed.from('/home/jan/proj/bruno/lingukit/etc/xml.grammar', 'grammar', GRAMMAR);
  new Printer(stdout).printTree(Parsed.from('/home/jan/proj/bruno/lingukit/etc/example.xml', 'document', grammarFrom(xml)));
}

Grammar grammarFrom(Parsed p) {
  return new Grammar(Mechanic.finish(Builder.buildGrammar(p)));
}