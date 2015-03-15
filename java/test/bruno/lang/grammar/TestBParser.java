package bruno.lang.grammar;

import static java.nio.ByteBuffer.wrap;
import static org.junit.Assert.assertEquals;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.junit.Test;

public class TestBParser {

	@Test
	public void justOneLiteral() {
		String data = "aabbcc";
		String lang =
				w("v1")+
				w("#-aabbcc")+
				w("'-"+i(32))
		;
		int pN = BParser.parse(0, wrap(data.getBytes()), 64, wrap(lang.getBytes()));
		assertEquals(6, pN);
	}
	
	@Test
	public void repeatingOneLiteral() {
		String data = "abcabcabcabc";
		String lang =
				w("v1")+
				w("#-abc")+
				w("'-"+i(32))+
				w("*-"+i(64)+i(0)+i(5))
		;
		int pN = BParser.parse(0, wrap(data.getBytes()), 96, wrap(lang.getBytes()));
		assertEquals(12, pN);
	}
	
	@Test
	public void sequenceOfTwoLiterals() {
		String data = "abcdef";
		ByteBuffer lang = ByteBuffer.allocate(194)
				.put(w("v1").getBytes())
				.put(w("#-abc").getBytes())    // 32
				.put(w("#-def").getBytes())    // 64
				.put("'-".getBytes()).putShort((short) 32).put(zeros(28)) // 96
				.put("'-".getBytes()).putShort((short) 64).put(zeros(28)) // 128
				.put("&-".getBytes()).putShort((short) 96).putShort((short) 128).put(zeros(26));
		;
		int pN = BParser.parse(0, wrap(data.getBytes()), 160, lang);
		assertEquals(6, pN);
	}
	
	@Test
	public void alternativesOfTwoLiterals() {
		ByteBuffer lang = ByteBuffer.allocate(194)
				.put(w("v1").getBytes())
				.put(w("#-abc").getBytes())    // 32
				.put(w("#-def").getBytes())    // 64
				.put("'-".getBytes()).putShort((short) 32).put(zeros(28)) // 96
				.put("'-".getBytes()).putShort((short) 64).put(zeros(28)) // 128
				.put("|-".getBytes()).putShort((short) 96).putShort((short) 128).put(zeros(26));
		;
		int pN = BParser.parse(0, wrap("abc".getBytes()), 160, lang);
		assertEquals(3, pN);
		pN = BParser.parse(0, wrap("def".getBytes()), 160, lang);
		assertEquals(3, pN);
	}
	
	@Test
	public void charactersetIncludingTwoCharacters() {
		byte[] set = new byte[16];
		addTo(set, 'c');
		addTo(set, '?');
		ByteBuffer lang = ByteBuffer.allocate(64)
				.put(w("v1").getBytes())
				.put("_-".getBytes()).put(set);
		int pN = BParser.parse(0, wrap("c".getBytes()), 32, lang);
		assertEquals(1, pN);
		pN = BParser.parse(0, wrap("?".getBytes()), 32, lang);
		assertEquals(1, pN);
		pN = BParser.parse(0, wrap("b".getBytes()), 32, lang);
		assertEquals(-1, pN);
	}
	
	private static void addTo(byte[] set, int index) {
		set[index/8] |= 1 << (index % 8);
	}
	
	private static byte[] zeros(int length) {
		return new byte[28];
	}

	private static String i(int index) {
		return new String(ByteBuffer.allocate(2).putShort((short) index).array(), 0, 2);
	}
	
	private static String w(String w) {
		return new String(Arrays.copyOf(w.getBytes(), 32));
	}
}
