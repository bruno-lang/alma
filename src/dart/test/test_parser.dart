import 'package:unittest/unittest.dart';
import 'package:lingukit/lingukit.dart';


main() {
  print(GRAMMAR);
  //Parser.parse(UTF8.bytes('foo'), name_);
  Parsed p = Parsed.from('/home/jan/proj/bruno/lingukit/etc/json.grammar', 'grammar', GRAMMAR);
  print(p);
  //test('test1', true, equals(true));
}