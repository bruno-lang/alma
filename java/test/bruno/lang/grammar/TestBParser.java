package bruno.lang.grammar;

import static java.nio.ByteBuffer.wrap;
import static org.junit.Assert.assertEquals;

import java.nio.ByteBuffer;

import org.junit.Test;

public class TestBParser {

	@Test
	public void justOneLiteral() {
		String data = "aabbcc";
		ByteBuffer lang = lang(
				binary("aabbcc"),
				literal(0));
		int pN = BParser.parse(0, wrap(data.getBytes()), 32, lang);
		assertEquals(6, pN);
	}
	
	@Test
	public void repeatingOneLiteral() {
		String data = "abcabcabcabc";
		ByteBuffer lang = lang(
				binary("abc"),
				literal(0),
				repetition(1, 0, 5));
		int pN = BParser.parse(0, wrap(data.getBytes()), 64, lang);
		assertEquals(12, pN);
	}
	
	@Test
	public void sequenceOfTwoLiterals() {
		String data = "abcdef";
		ByteBuffer lang = lang(
				binary("abc"),
				binary("def"),
				literal(0),
				literal(1),
				sequence(2,3));
		int pN = BParser.parse(0, wrap(data.getBytes()), 128, lang);
		assertEquals(6, pN);
	}
	
	@Test
	public void alternativesOfTwoLiterals() {
		ByteBuffer lang = lang(
				binary("abc"),
				binary("def"),
				literal(0),
				literal(1),
				options(2, 3));
		int pN = BParser.parse(0, wrap("abc".getBytes()), 128, lang);
		assertEquals(3, pN);
		pN = BParser.parse(0, wrap("def".getBytes()), 128, lang);
		assertEquals(3, pN);
	}
	
	@Test
	public void charactersetIncludingTwoCharacters() {
		ByteBuffer lang = lang(
				characterset('c', '?'));				
		int pN = BParser.parse(0, wrap("c".getBytes()), 0, lang);
		assertEquals(1, pN);
		pN = BParser.parse(0, wrap("?".getBytes()), 0, lang);
		assertEquals(1, pN);
		pN = BParser.parse(0, wrap("b".getBytes()), 0, lang);
		assertEquals(-1, pN);
	}

	@Test
	public void whitespaceMayBeIndent() {
		String data = " \t\n";
		ByteBuffer lang = lang(
				whitespace(','));
		int pN = BParser.parse(0, wrap(data.getBytes()), 0, lang);
		assertEquals(2, pN);
	}

	@Test
	public void whitespaceMustBeIndent() {
		String data = " \t\n";
		ByteBuffer lang = lang(
				whitespace(';'));
		int pN = BParser.parse(0, wrap(data.getBytes()), 0, lang);
		assertEquals(2, pN);
	}
	
	@Test
	public void whitespaceMayBeWS() {
		String data = " \t\n";
		ByteBuffer lang = lang(
				whitespace('.'));
		int pN = BParser.parse(0, wrap(data.getBytes()), 0, lang);
		assertEquals(3, pN);
	}
	
	@Test
	public void whitespaceMustBeWS() {
		String data = " \t\n";
		ByteBuffer lang = lang(
				whitespace(':'));
		int pN = BParser.parse(0, wrap(data.getBytes()), 0, lang);
		assertEquals(3, pN);
	}
	
	@Test
	public void whitespaceMustBeLineWrap() {
		String data = " \t\n";
		ByteBuffer lang = lang(
				whitespace('!'));
		int pN = BParser.parse(0, wrap(data.getBytes()), 0, lang);
		assertEquals(3, pN);
	}
	
	@Test
	public void sequenceLiteralWSRepetitionOfCharacterset() {
		String data = "keyword  x~zz~";
		ByteBuffer lang = lang(
				binary("keyword"),
				literal(0),
				characterset('x', 'z', '0', '~'),
				repetition(2, 0, 10),
				sequence(1, ',', 3));
		int pN = BParser.parse(0, wrap(data.getBytes()), 128, lang);
		assertEquals(14, pN);
	}
	
	private static ByteBuffer lang(byte[]...words) {
		ByteBuffer b = ByteBuffer.allocate(words.length*32);
		for (byte[] w : words) {
			b.put(w);
		}
		return b;
	}
	
	private static byte[] literal(int index) {
		byte[] b = new byte[32];
		b[0] = '\'';
		ByteBuffer.wrap(b).putShort(2, (short) index);
		return b;
	}
	
	private static  byte[] binary(String s) {
		byte[] b = new byte[32];
		b[0] = '#';
		b[1] = (byte) s.length();
		for (int i = 0; i < s.length(); i++) {
			b[i+2] = (byte) s.charAt(i);
		}
		return b;
	}
	
	private static byte[] repetition(int index, int min, int max) {
		byte[] b = new byte[32];
		b[0] = '*';
		ByteBuffer.wrap(b).putShort(2, (short) index).putShort(4, (short) min).putShort(6, (short) max);
		return b;
	}
	
	private static byte[] sequence(Object... elements) {
		byte[] b = new byte[32];
		b[0] = '&';
		b[1] = (byte) elements.length;
		ByteBuffer buf = ByteBuffer.wrap(b);
		int i = 2;
		for (Object e : elements) {
			if (e instanceof Number) {
				buf.putShort(i, ((Number)e).shortValue());
			} else {
				buf.put(i, (byte) ((Character)e).charValue());
			}
			i+=2;
		}
		return b;
	}
	
	private static byte[] options(Object... elements) {
		byte[] b = new byte[32];
		b[0] = '|';
		b[1] = (byte) elements.length;
		ByteBuffer buf = ByteBuffer.wrap(b);
		int i = 2;
		for (Object e : elements) {
			if (e instanceof Number) {
				buf.putShort(i, ((Number)e).shortValue());
			} else {
				buf.put(i, (byte) ((Character)e).charValue());
			}
			i+=2;
		}
		return b;
	}

	private static byte[] characterset(char... members) {
		byte[] set = new byte[32];
		set[0] = '_';
		set[1] = (byte)8;
		for (char c : members) {
			set[2+c/8] |= 1 << (c % 8);
		}
		return set;
	}
	
	private static byte[] whitespace(char op) {
		byte[] b = new byte[32];
		b[0] = (byte) op;
		return b;
	}
}
