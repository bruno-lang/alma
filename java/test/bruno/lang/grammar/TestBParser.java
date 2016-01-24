package bruno.lang.grammar;

import static java.nio.ByteBuffer.wrap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.nio.ByteBuffer;

import org.junit.Test;

public class TestBParser {

	//TODO the next step: make the old Rule based model convert into the new binary one and use that to parse the examples, than create the builder from alma
	
	@Test
	public void justOneLiteral() {
		String data = "aabbcc";
		ByteBuffer lang = lang(
				literal("aabbcc"));
		int pN = BParser.parse(0, wrap(data.getBytes()), 0, lang);
		assertEquals(6, pN);
	}
	
	@Test
	public void repeatingOneLiteral() {
		String data = "abcabcabcabc";
		ByteBuffer lang = lang(
				literal("abc"),
				repetition(0, 0, 5));
		int pN = BParser.parse(0, wrap(data.getBytes()), 32, lang);
		assertEquals(12, pN);
	}
	
	@Test
	public void sequenceOfTwoLiterals() {
		String data = "abcdef";
		ByteBuffer lang = lang(
				literal("abc"),
				literal("def"),
				sequence(0, 1));
		int pN = BParser.parse(0, wrap(data.getBytes()), 64, lang);
		assertEquals(6, pN);
	}
	
	@Test
	public void optionsOfTwoLiterals() {
		ByteBuffer lang = lang(
				literal("abc"),
				literal("def"),
				options(0, 1));
		int pN = BParser.parse(0, wrap("abc".getBytes()), 64, lang);
		assertEquals(3, pN);
		pN = BParser.parse(0, wrap("def".getBytes()), 64, lang);
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
				literal("keyword"),
				characterset('x', 'z', '0', '~'),
				repetition(1, 0, 10),
				sequence(0, ',', 2));
		int pN = BParser.parse(0, wrap(data.getBytes()), 96, lang);
		assertEquals(14, pN);
	}
	
	@Test
	public void sequenceWithDecision() {
		ByteBuffer lang = lang(
				literal("ab"),
				literal("cd"),
				sequence(0, '<', 1)
				);
		assertEquals(4, BParser.parse(0, wrap("abcd".getBytes()), 64, lang));
		try {
			BParser.parse(0, wrap("abdc".getBytes()), 64, lang);
			fail("error expected");
		} catch (BParseException e) {
			assertEquals(-3, e.errorPosition);
		}
	}
	
	@Test
	public void sequenceWithFill() {
		ByteBuffer lang = lang(
				literal("ab"),
				literal("cd"),
				sequence(0, '~', 1)
				);
		assertEquals(4, BParser.parse(0, wrap("abcd".getBytes()), 64, lang));
		assertEquals(6, BParser.parse(0, wrap("abxxcd".getBytes()), 64, lang));
		assertEquals(6, BParser.parse(0, wrap("abdccd".getBytes()), 64, lang));
	}
	
	@Test
	public void sequenceWithLookahead() {
		ByteBuffer lang = lang(
				literal("ab"),
				literal("cd"),
				sequence(0, '>', 1),
				sequence(2, 1)
				);
		assertEquals(2,  BParser.parse(0, wrap("abcd".getBytes()), 64, lang));
		assertEquals(-4, BParser.parse(0, wrap("abd".getBytes()), 64, lang));
		assertEquals(-4, BParser.parse(0, wrap("abccd".getBytes()), 64, lang));
		assertEquals(4,  BParser.parse(0, wrap("abcd".getBytes()), 96, lang));
	}
	
	@Test
	public void optionsWithWhitespace() {
		ByteBuffer lang = lang(
				literal("abc"),
				literal("def"),
				options(0, ';', 1));
		assertEquals(3,  BParser.parse(0, wrap("abc".getBytes()), 64, lang));
		assertEquals(3,  BParser.parse(0, wrap("   ".getBytes()), 64, lang));
		assertEquals(3,  BParser.parse(0, wrap("def".getBytes()), 64, lang));
	}
	
	@Test
	public void captureLiteral() {
		ByteBuffer lang = lang(
				literal("abc"),
				capture(0, "name"));
		BParseTree tree = new BParseTree(lang, 10);
		BParser.parse(0, wrap("abc".getBytes()), 32, lang, tree);
		assertEquals(1, tree.count());
		assertEquals(0, tree.start(0));
		assertEquals(3, tree.end(0));
		assertEquals(32, tree.rule(0));
		assertEquals("name 0-3\n", tree.toString());
	}
	
	@Test
	public void captureNested() {
		ByteBuffer lang = lang(
				literal("abc"),
				capture(0, "name"),
				literal("def"),
				capture(2, "value"),
				sequence(1,3),
				capture(4, "pair")
				);
		BParseTree tree = new BParseTree(lang, 10);
		BParser.parse(0, wrap("abcdef".getBytes()), 160, lang, tree);
		assertEquals(3, tree.count());
		assertEquals("pair 0-6\n name 0-3\n value 3-6\n", tree.toString());
	}

	private static ByteBuffer lang(byte[]...words) {
		ByteBuffer b = ByteBuffer.allocate(words.length*32);
		for (byte[] w : words) {
			b.put(w);
		}
		return b;
	}

	private byte[] capture(int what, String name) {
		byte[] b = new byte[32];
		b[0] = '=';
		b[1] = (byte) (name.length()+2);
		ByteBuffer.wrap(b).putShort(2, (short) what);
		for (int i = 0; i < name.length(); i++) {
			b[i+4] = (byte) name.charAt(i);
		}
		return b;
	}
	
	private static byte[] literal(String s) {
		byte[] b = new byte[32];
		b[0] = '\'';
		b[1] = (byte) s.length();
		for (int i = 0; i < s.length(); i++) {
			b[i+2] = (byte) s.charAt(i);
		}
		return b;
	}
	
	private static byte[] repetition(int index, int min, int max) {
		byte[] b = new byte[32];
		b[0] = '*';
		b[1] = 3;
		ByteBuffer.wrap(b).putShort(2, (short) index).putShort(4, (short) min).putShort(6, (short) max);
		return b;
	}
	
	private static byte[] sequence(Object... elements) {
		byte[] b = new byte[32];
		b[0] = '&';
		return elements(b, elements);
	}
	
	private static byte[] options(Object... elements) {
		byte[] b = new byte[32];
		b[0] = '|';
		return elements(b, elements);
	}
	
	private static byte[] elements(byte[] b, Object... elements) {
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
		set[1] = 8;
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
