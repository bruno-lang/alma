part of lingukit;

class UTF8 {
  
  static const int utf8_lenght = 0x80;
  
  static const int utf8_6bit = 0xFC;
  static const int utf8_5bit = 0xF8;
  static const int utf8_4bit = 0xF0;
  static const int utf8_3bit = 0xE0;
  static const int utf8_2bit = 0xC0;
  
  static List<int> bytesOf(int codePoint) {
    return bytes(new String.fromCharCode(codePoint)); //FIXME UTF-16 expected
  }
  
  static List<int> bytes(String s) {
    return IO.UTF8.encode(s);
  }
  
  static int codePoint0(List<int> bytes) {
    return codePointAt(bytes, 0);
  }
  
  static int codePointAt(List<int> bytes, int position) {
    int b = bytes[position];
    if (b >= 0)
      return b;
    if ((b & utf8_6bit) == utf8_6bit) {
      return ((b & ~utf8_6bit) << 30) | codePoint(bytes, position+1, 5);
    }
    if ((b & utf8_5bit) == utf8_5bit) {
      return ((b & ~utf8_5bit) << 24) | codePoint(bytes, position+1, 4);
    }
    if ((b & utf8_4bit) == utf8_4bit) {
      return ((b & ~utf8_4bit) << 18) | codePoint(bytes, position+1, 3);
    }
    if ((b & utf8_3bit) == utf8_3bit) {
      return ((b & ~utf8_3bit) << 12) | codePoint(bytes, position+1, 2);
    }
    return ((b & ~utf8_2bit) << 6) | codePoint(bytes, position+1, 1);
  }
  
  static int codePoint(List<int> bytes, int position, int followupBytes) {
    int cp = 0;
    for (int i = 0; i < followupBytes; i++) {
      cp = cp << 6;
      cp |= bytes[position+i] & 0x3F;
    }
    return cp;
  }
  
  static String string(List<int> bytes) {
    return IO.UTF8.decode(bytes);
  }
  
  static String toLiteral(int codePoint) {
    StringBuffer b = new StringBuffer();
    codePoint = codePoint.abs();
    if (codePoint > 31 && codePoint < 256) {
      b.write('\'');
      b.writeCharCode(codePoint);
      b.write('\'');
    } else if (codePoint == '\t') {
      b.write("\\t");
    } else if (codePoint == '\n') {
      b.write("\\n");
    } else if (codePoint == '\r') {
      b.write("\\r");
    } else {
      b.write("U+");
      b.write(codePoint); //FIXME fixed with of 4 digits
    }
    return b.toString();
  }

  static int characters(List<int> bytes) {
    int l = 0;
    int pos = 0;
    while (pos < bytes.length) {
      pos += byteCount(bytes, pos);
      l++;
    }
    return l;
  }

  static int byteCount(List<int> bytes, int position) {
    int b = bytes[position];
    if (b >= 0)
      return 1;
    if ((b & utf8_6bit) == utf8_6bit) { 
      return 6;
    }
    if ((b & utf8_5bit) == utf8_5bit) { 
      return 5;
    }
    if ((b & utf8_4bit) == utf8_4bit) { 
      return 4;
    }
    if ((b & utf8_3bit) == utf8_3bit) { 
      return 3;
    }
    return 2;
  }
}