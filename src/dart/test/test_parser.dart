import 'package:unittest/unittest.dart';
import 'package:lingukit/lingukit.dart';


main() {
  print(GRAMMAR);
  //Parser.parse(UTF8.bytes('foo'), name_);
  Parsed p = Parsed.from('/home/jan/proj/bruno/lingukit/etc/json.grammar', 'grammar', GRAMMAR);
  p = Parsed.from('/home/jan/proj/bruno/lingukit/etc/bruno.grammar', 'grammar', GRAMMAR);
  p = Parsed.from('/home/jan/proj/bruno/lingukit/etc/lingukit.grammar', 'grammar', GRAMMAR);
  p = Parsed.from('/home/jan/proj/bruno/lingukit/etc/xml.grammar', 'grammar', GRAMMAR);
  print(from(p));
  //test('test1', true, equals(true));
}

Grammar from(Parsed p) {
  return new Grammar(Mechanic.finish(Builder.buildGrammar(p)));
}