import 'package:unittest/unittest.dart';
import 'package:lingukit/lingukit.dart';
import 'dart:io';

main() {
  test('bruno grammar parses', () => 
    expect(() => Parsed.from(etc('bruno.grammar'), 'grammar', GRAMMAR), returnsNormally));
  
  test('bruno module parses', () {
    Parsed bruno = Parsed.from(etc('bruno.grammar'), 'grammar', GRAMMAR);
    expect(() => Parsed.from(etc('example.mod'), 'module', grammarFrom(bruno)), returnsNormally);
  });
  
  test('bruno namespace parses', () {
    Parsed bruno = Parsed.from(etc('bruno.grammar'), 'grammar', GRAMMAR);
    expect(() => Parsed.from(etc('example.ns'), 'namespace', grammarFrom(bruno)), returnsNormally);
  });
  
  test('lingukit grammar parses', () => 
    expect(() => Parsed.from(etc('lingukit.grammar'), 'grammar', GRAMMAR), returnsNormally));
  
  test('lingukit via parsed lingukit grammar parses itself', () {  
    Parsed lingukit = Parsed.from(etc('lingukit.grammar'), 'grammar', GRAMMAR);
    expect(() => Parsed.from(etc('lingukit.grammar'), 'grammar', grammarFrom(lingukit)), returnsNormally); 
   });
  
  test('xml grammar parses', () => 
    expect(() => Parsed.from(etc('xml.grammar'), 'grammar', GRAMMAR), returnsNormally));
  
  test('xml example parses', () {
    Parsed xml = Parsed.from(etc('xml.grammar'), 'grammar', GRAMMAR);
    expect(() => Parsed.from(etc('example.xml'), 'document', grammarFrom(xml)), returnsNormally);
  });
  
  test('json grammar parses', () => 
      expect(() => Parsed.from(etc('json.grammar'), 'grammar', GRAMMAR), returnsNormally)); 
  
  test('json example parses', () {
    Parsed json = Parsed.from(etc('json.grammar'), 'grammar', GRAMMAR);
    expect(() => Parsed.from(etc('example.json'), 'json', grammarFrom(json)), returnsNormally);
  });
}

File etc(String filename) {
  Directory testDir = Directory.current;
  return new File(testDir.parent.parent.parent.path+'/etc/'+filename);
}

Grammar grammarFrom(Parsed p) {
  return new Grammar(Mechanic.finish(Builder.buildGrammar(p)));
}