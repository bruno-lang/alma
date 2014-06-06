import 'package:unittest/unittest.dart';
import 'package:lingukit/lingukit.dart';
import 'dart:io';

main() {
  group('bruno', () {
    test('grammar parses', () => 
      expect(() => Parsed.from(etc('bruno.grammar'), 'grammar', GRAMMAR), returnsNormally));
    
    test('module parses', () {
      Parsed bruno = Parsed.from(etc('bruno.grammar'), 'grammar', GRAMMAR);
      expect(() => Parsed.from(etc('example.mod'), 'module', grammarFrom(bruno)), returnsNormally);
    });
    
    test('namespace parses', () {
      Parsed bruno = Parsed.from(etc('bruno.grammar'), 'grammar', GRAMMAR);
      expect(() => Parsed.from(etc('example.ns'), 'namespace', grammarFrom(bruno)), returnsNormally);
    });
  });
  
  group('lingukit', () {
    test('grammar parses', () => 
        expect(() => Parsed.from(etc('lingukit.grammar'), 'grammar', GRAMMAR), returnsNormally));
    
    test('via parsed lingukit grammar parses itself', () {  
      Parsed lingukit = Parsed.from(etc('lingukit.grammar'), 'grammar', GRAMMAR);
      expect(() => Parsed.from(etc('lingukit.grammar'), 'grammar', grammarFrom(lingukit)), returnsNormally); 
    });
  });

  group('XML', () {
    test('grammar parses', () => 
        expect(() => Parsed.from(etc('xml.grammar'), 'grammar', GRAMMAR), returnsNormally));
    
    test('example parses', () {
      Parsed xml = Parsed.from(etc('xml.grammar'), 'grammar', GRAMMAR);
      expect(() => Parsed.from(etc('example.xml'), 'document', grammarFrom(xml)), returnsNormally);
    });
  });
  
  group('JSON', () {
    test('grammar parses', () => 
        expect(() => Parsed.from(etc('json.grammar'), 'grammar', GRAMMAR), returnsNormally)); 
    
    test('example parses', () {
      Parsed json = Parsed.from(etc('json.grammar'), 'grammar', GRAMMAR);
      expect(() => Parsed.from(etc('example.json'), 'json', grammarFrom(json)), returnsNormally);
    });
  });
  
  group('cross check:', () {
    test('JSON does not parse XML', () {
      Parsed json = Parsed.from(etc('json.grammar'), 'grammar', GRAMMAR);
      expect(() => Parsed.from(etc('example.xml'), 'json', grammarFrom(json)), throwsException);
    });
  });
}

File etc(String filename) {
  Directory testDir = Directory.current;
  return new File(testDir.parent.parent.parent.path+'/etc/'+filename);
}

Grammar grammarFrom(Parsed p) {
  return new Grammar(Linguist.finish(Builder.buildGrammar(p)));
}