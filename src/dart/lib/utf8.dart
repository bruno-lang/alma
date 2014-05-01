part of lingukit;

class UTF8 {
  
  static const int MAX_CODE_POINT = 0x7FFFFFFF;
  
  static const int byte_mask = 0xFF;
  static const int utf8_lenght = 0x80;
  
  static const int utf8_6bit = 0xFC;
  static const int utf8_5bit = 0xF8;
  static const int utf8_4bit = 0xF0;
  static const int utf8_3bit = 0xE0;
  static const int utf8_2bit = 0xC0;
  
  static Uint8List bytesOf(int codePoint) {
    var l = byteCount(codePoint);
    if (l == 1)
      return [codePoint];
    Uint8List res = new Uint8List(l);
    for (int i = 0; i < l; i++) {
      res[l-i-1] = codePoint & byte_mask;
      codePoint = codePoint >> 8;
    }
    return res;
  }
  
  static Uint8List bytes(String s) {
    return new Uint8List.fromList(Encoding.UTF8.encode(s));
  }
  
  static int codePoint0(List<int> bytes) {
    return codePointAt(bytes, 0);
  }
  
  static int codePointAt(List<int> bytes, int position) {
    int b = bytes[position];
    if (byteCount(b) == 1)
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
    return Encoding.UTF8.decode(bytes);
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
      String n = codePoint.toString();
      for (int i = 0; i < 4-n.length; i++) {
        b.write("0");
      }
      b.write(n);
    }
    return b.toString();
  }

  static int characters(List<int> bytes) {
    int l = 0;
    int pos = 0;
    while (pos < bytes.length) {
      pos += byteCount(bytes[pos]);
      l++;
    }
    return l;
  }

  static int byteCount(int byte) {
    if (byte < 128)  
      return 1;
    if ((byte & utf8_6bit) == utf8_6bit) { 
      return 6;
    }
    if ((byte & utf8_5bit) == utf8_5bit) { 
      return 5;
    }
    if ((byte & utf8_4bit) == utf8_4bit) { 
      return 4;
    }
    if ((byte & utf8_3bit) == utf8_3bit) { 
      return 3;
    }
    return 2;
  }
  
  static bool isWhitespace(int codePoint) {
    if (codePoint < 128) {
      return codePoint == 32 || (codePoint >= 9 && codePoint <= 13);
    }
    if (codePoint == 0x00a0) {
      return true;
    }
    if (codePoint < 0x1680) {
      return false;
    }
    return codePoint == 0x1680 || codePoint == 0x180e || codePoint == 0x2028 || codePoint == 0x2029 || codePoint == 0x202f 
        || codePoint == 0x205f || codePoint == 0x3000 || (codePoint >= 0x2000 && codePoint <= 0x200a); 
  }
}