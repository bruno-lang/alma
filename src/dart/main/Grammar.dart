part of lingukit;

class RuleType {

  static const LITERAL = const RuleType._("lit");
  static const TERMINAL = const RuleType._("trm");
  static const PATTERN = const RuleType._("pat");
  static const ITERATION = const RuleType._("itr");
  static const SEQUENCE = const RuleType._("seq");
  static const SELECTION = const RuleType._("sel");
  static const COMPLETION = const RuleType._("cpl");
  static const REFERENCE = const RuleType._("ref");
  static const CAPTURE = const RuleType._("cap");

  final String code;

  const RuleType._(this.value);
}

class Rule {
  final RuleType type;
  final String name;
  final List<Rule> elements;
  final Occur occur;
  final List<Int> literal;
  final Terminal terminal;
  final Pattern pattern;
  final int distinctFrom;
}