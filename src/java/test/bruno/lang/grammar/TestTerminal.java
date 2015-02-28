package bruno.lang.grammar;

import static bruno.lang.grammar.CharacterSet.range;
import static org.junit.Assert.assertEquals;

import java.nio.ByteBuffer;

import org.junit.Test;

public class TestTerminal {

	@Test
	public void singleAsciiInclusiveRange() {
		CharacterSet t = range('a', 'z');

		assertEquals("[{'a'-'z'}]", t.toString());
		
		assertContainsCharacters(t, "agjuzsldps");
		assertNotContainsCharacters(t, "1@AHGTFÖH");
	}
	
	@Test
	public void singleAsciiExclusiveRange() {
		CharacterSet t = notRange('a', 'z');

		assertEquals("[{'a'-'z'}]^", t.toString());
		
		assertNotContainsCharacters(t, "agjuzsldps");
		assertContainsCharacters(t, "1@AHGTFHÖ");
	}
	
	@Test
	public void multipleAsciiInclusiveRanges() {
		CharacterSet included = range('a', 'z').and(range('A', 'Z')).and(range('0', '9'));
		
		assertEquals("[{'a'-'z'}{'A'-'Z'}{'0'-'9'}]", included.toString());
		
		assertContainsCharacters(included, "azcfgwjs97873AZGHAIKN");
		assertNotContainsCharacters(included, "§$%&/()=@.:-,;#+~^");
	}
	
	@Test
	public void multipleAsciiExclusiveRanges() {
		CharacterSet excluded = notRange('a', 'z').and(notRange('A', 'Z')).and(notRange('0', '9'));
		
		assertEquals("[{'a'-'z'}{'A'-'Z'}{'0'-'9'}]^", excluded.toString());
		
		assertNotContainsCharacters(excluded, "azcfgwjs97873AZGHAIKN");
		assertContainsCharacters(excluded, "§$%&/()=@.:-,;#+~^");
	}
	
	@Test
	public void asciiInclusiveAndExclusiveRanges() {
		CharacterSet mixed = notRange('a', 'z').and(range('c', 'e'));
		
		assertEquals("[{'a'-'z'}]^ [{'c'-'e'}]", mixed.toString());
		
		assertContainsCharacters(mixed, "cde97873AZGHAIKN§$%&/()=@.:-,;#+~^");
		assertNotContainsCharacters(mixed, "abfghijkz");
	}
	
	@Test
	public void asciiInclusiveAndExclusiveRanges2() {
		CharacterSet mixed = notRange('a', 'z').and(notRange('A', 'Z')).and(range('c', 'e'));
		
		assertEquals("[{'a'-'z'}{'A'-'Z'}]^ [{'c'-'e'}]", mixed.toString());
		
		assertContainsCharacters(mixed, "cde97873§$%&/()=@.:-,;#+~^");
		assertNotContainsCharacters(mixed, "abfghijkzAZGHAIKN");
	}
	
	@Test
	public void singleNonAsciiIncludingRange() {
		CharacterSet t = range(500, 800);

		assertEquals("[{#1F4-#320}]", t.toString());
		
		assertContainsCharacters(t, 500,800,555,596,680);
		assertNotContainsCharacters(t, 499,801,999,0);
	}
	
	@Test
	public void singleNonAsciiExcludingRange() {
		CharacterSet t = notRange(500, 800);

		assertEquals("[{#1F4-#320}]^", t.toString());
		
		assertNotContainsCharacters(t, 500,800,555,596,680);
		assertContainsCharacters(t, 499,801,999,0);
	}
	
	@Test
	public void multipleNonAsciiIncludingRange() {
		CharacterSet t = range(500, 800).and(range(1200, 1500));

		assertEquals("[{#1F4-#320}{#4B0-#5DC}]", t.toString());
		
		assertContainsCharacters(t, 500,800,555,596,680,1200,1500,1489,1367);
		assertNotContainsCharacters(t, 499,801,999,0,1199,1501,1816,2578);
	}
	
	@Test
	public void multipleNonAsciiExcludingRange() {
		CharacterSet t = notRange(500, 800).and(notRange(1200, 1500));

		assertEquals("[{#1F4-#320}{#4B0-#5DC}]^", t.toString());
		
		assertNotContainsCharacters(t, 500,800,555,596,680,1200,1500,1489,1367);
		assertContainsCharacters(t, 499,801,999,0,1199,1501,1816,2578);
	}
	
	@Test
	public void nonAsciiInclusiveAndExclusiveRanges() {
		CharacterSet mixed = notRange(500,800).and(range(600,650));
		
		assertEquals("[{#1F4-#320}]^ [{#258-#28A}]", mixed.toString());
		
		assertContainsCharacters(mixed, 600,650,624,801,499,5,999,348);
		assertNotContainsCharacters(mixed, 500,800,599,651,745,567);
	}
	
	@Test
	public void asciiAndNonAsciiWithInclduingAndExcludingRanges() {
		CharacterSet mixed = notRange(500, 800).and(range(600,650)).and(notRange(450, 550)).and(notRange('a', 'z')).and(range('d', 'i')).and(range(120 /*x*/, 200));
		
		assertEquals("[{#1F4-#320}{#1C2-#226}{'a'-'z'}]^ [{#258-#28A}{'d'-'i'}{'x'-#C8}]", mixed.toString());
		
		assertContainsCharacters(mixed, 600,650,624,801,5,999,348,'x','d','i','z',200,201);
		assertNotContainsCharacters(mixed, 500,800,599,651,745,567,'a','c','j','w');
	}

	private void assertNotContainsCharacters(CharacterSet t, String notContained) {
		assertContainsCharacters(t, notContained, false);
	}

	private void assertContainsCharacters(CharacterSet t, String contained) {
		assertContainsCharacters(t, contained, true);
	}
	
	private void assertNotContainsCharacters(CharacterSet t, int... notContained) {
		assertContainsCharacters(t, notContained, false);
	}

	private void assertContainsCharacters(CharacterSet t, int... contained) {
		assertContainsCharacters(t, contained, true);
	}

	private void assertContainsCharacters(CharacterSet t, String characters, boolean contains) {
		for (char c : characters.toCharArray()) {
			assertEquals(String.valueOf(c), contains, t.contains(ByteBuffer.wrap(UTF8.bytes(c)), 0));
		}
	}
	
	private void assertContainsCharacters(CharacterSet t, int[] codePoints, boolean contains) {
		for (int cp : codePoints) {
			byte[] bytes = UTF8.bytes(cp);
			assertEquals(new String(bytes)+"("+cp+")", contains, t.contains(ByteBuffer.wrap(bytes), 0));
		}
	}

	static CharacterSet notRange(int minCodePoint, int maxCodePoint) {
		return CharacterSet.range(minCodePoint, maxCodePoint).not(); 
	}
	
	static CharacterSet notCharacter(int codePoint) {
		return CharacterSet.character(codePoint).not();
	}

}
