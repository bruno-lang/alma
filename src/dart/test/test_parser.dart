import 'package:unittest/unittest.dart';
import 'package:lingukit/lingukit.dart';

main() {
  test('bruno grammar parses', () => 
    expect(() => Parsed.from('/home/jan/proj/bruno/lingukit/etc/bruno.grammar', 'grammar', GRAMMAR), returnsNormally));
  
  test('bruno module parses', () {
    Parsed bruno = Parsed.from('/home/jan/proj/bruno/lingukit/etc/bruno.grammar', 'grammar', GRAMMAR);
    expect(() => Parsed.from('/home/jan/proj/bruno/lingukit/etc/example.mod', 'module', grammarFrom(bruno)), returnsNormally);
  });
  
  test('bruno namespace parses', () {
    Parsed bruno = Parsed.from('/home/jan/proj/bruno/lingukit/etc/bruno.grammar', 'grammar', GRAMMAR);
    expect(() => Parsed.from('/home/jan/proj/bruno/lingukit/etc/example.ns', 'namespace', grammarFrom(bruno)), returnsNormally);
  });
  
  test('lingukit grammar parses', () => 
    expect(() => Parsed.from('/home/jan/proj/bruno/lingukit/etc/lingukit.grammar', 'grammar', GRAMMAR), returnsNormally));
  
  test('lingukit via parsed lingukit grammar parses itself', () {  
    Parsed lingukit = Parsed.from('/home/jan/proj/bruno/lingukit/etc/lingukit.grammar', 'grammar', GRAMMAR);
    expect(() => Parsed.from('/home/jan/proj/bruno/lingukit/etc/lingukit.grammar', 'grammar', grammarFrom(lingukit)), returnsNormally); 
   });
  
  test('xml grammar parses', () => 
    expect(() => Parsed.from('/home/jan/proj/bruno/lingukit/etc/xml.grammar', 'grammar', GRAMMAR), returnsNormally));
  
  test('xml example parses', () {
    Parsed xml = Parsed.from('/home/jan/proj/bruno/lingukit/etc/xml.grammar', 'grammar', GRAMMAR);
    expect(() => Parsed.from('/home/jan/proj/bruno/lingukit/etc/example.xml', 'document', grammarFrom(xml)), returnsNormally);
  });
  
  test('json grammar parses', () => 
      expect(() => Parsed.from('/home/jan/proj/bruno/lingukit/etc/json.grammar', 'grammar', GRAMMAR), returnsNormally)); 
  
  test('json example parses', () {
    Parsed json = Parsed.from('/home/jan/proj/bruno/lingukit/etc/json.grammar', 'grammar', GRAMMAR);
    expect(() => Parsed.from('/home/jan/proj/bruno/lingukit/etc/example.json', 'json', grammarFrom(json)), returnsNormally);
  });
}

Grammar grammarFrom(Parsed p) {
  return new Grammar(Mechanic.finish(Builder.buildGrammar(p)));
}