package bruno.lang.grammar;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public final class UTF8 {

	private static final Charset CHARSET = Charset.forName("UTF-8");

	static final int MAX_CODE_POINT = 0x7FFFFFFF;
	
	static final byte utf8_lenght = (byte) 0b10000000;
	
	static final byte utf8_6bit = (byte) 0b11111100;
	static final byte utf8_5bit = (byte) 0b11111000;
	static final byte utf8_4bit = (byte) 0b11110000;
	static final byte utf8_3bit = (byte) 0b11100000;
	static final byte utf8_2bit = (byte) 0b11000000;
	
	public static byte[] bytes(int codePoint) {
		StringBuilder b = new StringBuilder();
		b.appendCodePoint(codePoint);
		return bytes(b.toString());
	}
	
	public static byte[] bytes(String s) {
		return s.getBytes(CHARSET);
	}
	
	public static int codePoint(byte... bytes) {
		return codePoint(ByteBuffer.wrap(bytes), 0);
	}
	
	public static int codePoint(ByteBuffer input, int position) {
		byte b = input.get(position);
		if (b >= 0)
			return b;
		if ((b & utf8_6bit) == utf8_6bit) {
			return ((b & ~utf8_6bit) << 30) | codePoint(input, position+1, 5);
		}
		if ((b & utf8_5bit) == utf8_5bit) {
			return ((b & ~utf8_5bit) << 24) | codePoint(input, position+1, 4);
		}
		if ((b & utf8_4bit) == utf8_4bit) {
			return ((b & ~utf8_4bit) << 18) | codePoint(input, position+1, 3);
		}
		if ((b & utf8_3bit) == utf8_3bit) {
			return ((b & ~utf8_3bit) << 12) | codePoint(input, position+1, 2);
		}
		return ((b & ~utf8_2bit) << 6) | codePoint(input, position+1, 1);
	}
	
	private static int codePoint(ByteBuffer input, int position, int followupBytes) {
		int cp = 0;
		for (int i = 0; i < followupBytes; i++) {
			cp = cp << 6;
			cp |= input.get(position+i) & 0b00111111;
		}
		return cp;
	}

	public static int byteCount(ByteBuffer input, int position) {
		byte b = input.get(position);
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

	public static String toLiteral(int codePoint) {
		StringBuilder b = new StringBuilder();
		codePoint = Math.abs(codePoint);
		if (codePoint > 31 && codePoint < 256) {
			b.append('\'');
			b.append((char)codePoint);
			b.append('\'');
		} else if (codePoint == '\t') {
			b.append("\\t");
		} else if (codePoint == '\n') {
			b.append("\\n");
		} else if (codePoint == '\r') {
			b.append("\\r");
		} else {
			b.append("U+");
			b.append(String.format("%04x", codePoint));
		}
		return b.toString();
	}

	public static int characters(byte[] bytes) {
		ByteBuffer b = ByteBuffer.wrap(bytes);
		int l = 0;
		int pos = 0;
		while (pos < bytes.length) {
			pos += byteCount(b, pos);
			l++;
		}
		return l;
	}

}
