part of lingukit;

class Terminal {
  
  /**
   * Simply all unicodes are contained in the range of the terminal.
   */
  static final Terminal WILDCARD = range(0, UTF8.MAX_CODE_POINT);
  
  /**
   * Should follow the Unicode 5.0 standard,
   * see https://spreadsheets.google.com/pub?key=pd8dAQyHbdewRsnE5x5GzKQ
   */
  static final Terminal WHITESPACE = range(9, 13).and(character(32));
  
  static final Terminal
    DIGITS = characterRange('0', '9'),
    HEX_NUMBER = DIGITS.and(characterRange('A', 'F')),
    OCTAL_NUMBER = characterRange('0', '7'),
    BINARY_NUMBER = characterRange('0', '1'),
    UPPER_LETTERS = characterRange('A','Z'),
    LOWER_LETTERS = characterRange('a', 'z'), 
    LETTERS = UPPER_LETTERS.and(LOWER_LETTERS)
    ;
  
  static Terminal notRange(int minCodePoint, int maxCodePoint) {
    return new Terminal.of([-minCodePoint, -maxCodePoint ]); 
  }
  
  static Terminal characterRange(String minCodePoint, String maxCodePoint) {
    return range(UTF8.bytes(minCodePoint).first, UTF8.bytes(maxCodePoint).first);
  }
  
  static Terminal range(int minCodePoint, int maxCodePoint) {
    return new Terminal.of([minCodePoint, maxCodePoint ]);
  }
  
  static Terminal character(int codePoint) {
    return range(codePoint, codePoint);
  }
  
  static Terminal notCharacter(int codePoint) {
    return notRange(codePoint, codePoint);
  }
  
  final List<int> asciis;
  final List<int> ranges; // 2 int's give min CP and max CP (negative when excluding)
  
  Terminal.of(List<int> ranges) : this(ranges, asciisOf(ranges));
  
  Terminal(this.ranges, this.asciis);
  
  static List<int> asciisOf(List<int> ranges) {
    List<int> ascii = new List<int>(4);
    if (ranges[0] < 0) {
      ascii[0] = -1;
      ascii[1] = -1;
      ascii[2] = -1;
      ascii[3] = -1;
    }
    for (int i = 0; i < ranges.length; i+=2) {
      if (ranges[i] < 0) {
        int min = ranges[i].abs();
        if (min < 128) {
          int max = ranges[i+1].abs();
          for (int cp = min; cp <= Math.min(127, max); cp++) {
            ascii[cp~/32] &= ~(1 << (cp % 32));
          }
        }
      } else {
        int min = ranges[i];
        if (min < 128) {
          int max = ranges[i+1];
          for (int cp = min; cp <= Math.min(127, max); cp++) {
            ascii[cp~/32] |= 1 << (cp % 32);
          }
        }
      }
    }
    return ascii;
  }
  
  int excluding() {
    int i = 0;
    while (i < ranges.length && ranges[i] < 0) { i++; }
    return i;
  }
  
  Terminal not() {
    List<int> not = new List.from(ranges);
    for (int i = 0; i < not.length; i++) {
      not[i] = -not[i];
    }
    return new Terminal.of(not);
  }
  
  Terminal and(Terminal other) {
    List<int> merged = [];
    int ex = excluding();
    if (ex > 0) {
      merged.addAll(ranges.sublist(0, ex));
    }
    int oex = other.excluding();
    if (oex > 0) {
      merged.addAll(other.ranges.sublist(0, oex));
    }
    if (ex < ranges.length) {
      merged.addAll(ranges.sublist(ex));
    }
    if (oex < other.ranges.length) {
      merged.addAll(other.ranges.sublist(oex));
    }
    return new Terminal.of(merged);
  }
  
  bool contains(List<int> input, int position) {
    int b = input[position];
    if (UTF8.byteCount(b) == 1) {
      return (asciis[b~/32] & 1 << b % 32) != 0;
    }
    int codePoint = UTF8.codePointAt(input, position);
    int i = 0;
    bool excluded = false;
    while (i < ranges.length && ranges[i] < 0) {
      if (!excluded) {
        excluded = codePoint >= ranges[i].abs() && codePoint <= ranges[i+1].abs();
      } // else - forward to positive/including ranges
      i+=2;
    }
    while (i < ranges.length) {
      if (codePoint >= ranges[i] && codePoint <= ranges[i+1]) {
        return true;
      }
      i+= 2;
    }
    return !excluded && ranges[0] < 0; // just in case there was excluding ranges it means all others where included
  }
  
  bool isSingleCharacter() {
    return ranges.length == 2 && ranges[0] == ranges[1];
  }
  
  String toString() {
    if (ranges.length == 2 && ranges[0] == 0 && ranges[1] == UTF8.MAX_CODE_POINT) {
      return '\$';
    }
    if (this == WHITESPACE) {
      return "_";
    }
    if (this == LETTERS) {
      return "@";
    }
    if (this == LOWER_LETTERS) {
      return "z";
    }
    if (this == UPPER_LETTERS) {
      return "Z";
    }
    if (this == DIGITS) {
      return "9";
    }
    if (this == HEX_NUMBER) {
      return "#";
    }
    StringBuffer b = new StringBuffer();
    if (!isSingleCharacter()) {
      b.write("{ ");
    }
    for (int i = 0; i < ranges.length; i+=2) {
      if (i > 0) {
        b.write(" ");
      }
      if (ranges[i] < 0) {
        b.write('!');
      }
      if (ranges[i] == ranges[i+1]) {
        b.write(UTF8.toLiteral(ranges[i]));
      } else {
        b.write(UTF8.toLiteral(ranges[i]));
        b.write('-');
        b.write(UTF8.toLiteral(ranges[i+1]));
      }
    }
    if (!isSingleCharacter()) {
      b.write(" }");
    }
    return b.toString();
  }
}