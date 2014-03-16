package bruno.lang.grammar;

import static bruno.lang.grammar.Terminal.notRange;
import static bruno.lang.grammar.Terminal.range;
import static org.junit.Assert.assertEquals;

import java.nio.ByteBuffer;

import org.junit.Test;

public class TestTerminal {

	@Test
	public void wildcardIncludesAnyCodePoint() {
		assertContainsCharacters(Terminal.WILDCARD, '?');
	}
	
	@Test
	public void singleAsciiInclusiveRange() {
		Terminal t = range('a', 'z');

		assertEquals("{ 'a'-'z' }", t.toString());
		
		assertContainsCharacters(t, "agjuzsldps");
		assertNotContainsCharacters(t, "1@AHGTFÖH");
	}
	
	@Test
	public void singleAsciiExclusiveRange() {
		Terminal t = notRange('a', 'z');

		assertEquals("{ !'a'-'z' }", t.toString());
		
		assertNotContainsCharacters(t, "agjuzsldps");
		assertContainsCharacters(t, "1@AHGTFHÖ");
	}
	
	@Test
	public void multipleAsciiInclusiveRanges() {
		Terminal included = range('a', 'z').and(range('A', 'Z')).and(range('0', '9'));
		
		assertEquals("{ 'a'-'z' 'A'-'Z' '0'-'9' }", included.toString());
		
		assertContainsCharacters(included, "azcfgwjs97873AZGHAIKN");
		assertNotContainsCharacters(included, "§$%&/()=@.:-,;#+~^");
	}
	
	@Test
	public void multipleAsciiExclusiveRanges() {
		Terminal excluded = notRange('a', 'z').and(notRange('A', 'Z')).and(notRange('0', '9'));
		
		assertEquals("{ !'a'-'z' !'A'-'Z' !'0'-'9' }", excluded.toString());
		
		assertNotContainsCharacters(excluded, "azcfgwjs97873AZGHAIKN");
		assertContainsCharacters(excluded, "§$%&/()=@.:-,;#+~^");
	}
	
	@Test
	public void asciiInclusiveAndExclusiveRanges() {
		Terminal mixed = notRange('a', 'z').and(range('c', 'e'));
		
		assertEquals("{ !'a'-'z' 'c'-'e' }", mixed.toString());
		
		assertContainsCharacters(mixed, "cde97873AZGHAIKN§$%&/()=@.:-,;#+~^");
		assertNotContainsCharacters(mixed, "abfghijkz");
	}
	
	@Test
	public void asciiInclusiveAndExclusiveRanges2() {
		Terminal mixed = notRange('a', 'z').and(notRange('A', 'Z')).and(range('c', 'e'));
		
		assertEquals("{ !'a'-'z' !'A'-'Z' 'c'-'e' }", mixed.toString());
		
		assertContainsCharacters(mixed, "cde97873§$%&/()=@.:-,;#+~^");
		assertNotContainsCharacters(mixed, "abfghijkzAZGHAIKN");
	}
	
	@Test
	public void singleNonAsciiIncludingRange() {
		Terminal t = range(500, 800);

		assertEquals("{ U+01f4-U+0320 }", t.toString());
		
		assertContainsCharacters(t, 500,800,555,596,680);
		assertNotContainsCharacters(t, 499,801,999,0);
	}
	
	@Test
	public void singleNonAsciiExcludingRange() {
		Terminal t = notRange(500, 800);

		assertEquals("{ !U+01f4-U+0320 }", t.toString());
		
		assertNotContainsCharacters(t, 500,800,555,596,680);
		assertContainsCharacters(t, 499,801,999,0);
	}
	
	@Test
	public void multipleNonAsciiIncludingRange() {
		Terminal t = range(500, 800).and(range(1200, 1500));

		assertEquals("{ U+01f4-U+0320 U+04b0-U+05dc }", t.toString());
		
		assertContainsCharacters(t, 500,800,555,596,680,1200,1500,1489,1367);
		assertNotContainsCharacters(t, 499,801,999,0,1199,1501,1816,2578);
	}
	
	@Test
	public void multipleNonAsciiExcludingRange() {
		Terminal t = notRange(500, 800).and(notRange(1200, 1500));

		assertEquals("{ !U+01f4-U+0320 !U+04b0-U+05dc }", t.toString());
		
		assertNotContainsCharacters(t, 500,800,555,596,680,1200,1500,1489,1367);
		assertContainsCharacters(t, 499,801,999,0,1199,1501,1816,2578);
	}
	
	@Test
	public void nonAsciiInclusiveAndExclusiveRanges() {
		Terminal mixed = notRange(500,800).and(range(600,650));
		
		assertEquals("{ !U+01f4-U+0320 U+0258-U+028a }", mixed.toString());
		
		assertContainsCharacters(mixed, 600,650,624,801,499,5,999,348);
		assertNotContainsCharacters(mixed, 500,800,599,651,745,567);
	}
	
	@Test
	public void asciiAndNonAsciiWithInclduingAndExcludingRanges() {
		Terminal mixed = notRange(500, 800).and(range(600,650)).and(notRange(450, 550)).and(notRange('a', 'z')).and(range('d', 'i')).and(range(120 /*x*/, 200));
		
		assertEquals("{ !U+01f4-U+0320 !U+01c2-U+0226 !'a'-'z' U+0258-U+028a 'd'-'i' 'x'-'È' }", mixed.toString());
		
		assertContainsCharacters(mixed, 600,650,624,801,5,999,348,'x','d','i','z',200,201);
		assertNotContainsCharacters(mixed, 500,800,599,651,745,567,'a','c','j','w');
	}

	private void assertNotContainsCharacters(Terminal t, String notContained) {
		assertContainsCharacters(t, notContained, false);
	}

	private void assertContainsCharacters(Terminal t, String contained) {
		assertContainsCharacters(t, contained, true);
	}
	
	private void assertNotContainsCharacters(Terminal t, int... notContained) {
		assertContainsCharacters(t, notContained, false);
	}

	private void assertContainsCharacters(Terminal t, int... contained) {
		assertContainsCharacters(t, contained, true);
	}

	private void assertContainsCharacters(Terminal t, String characters, boolean contains) {
		for (char c : characters.toCharArray()) {
			assertEquals(String.valueOf(c), contains, t.contains(ByteBuffer.wrap(String.valueOf(c).getBytes()), 0));
		}
	}
	
	private void assertContainsCharacters(Terminal t, int[] codePoints, boolean contains) {
		for (int cp : codePoints) {
			StringBuilder b = new StringBuilder();
			b.appendCodePoint(cp);
			String c = b.toString();
			assertEquals(c+"("+UTF8.codePoint(c.getBytes())+")", contains, t.contains(ByteBuffer.wrap(c.getBytes()), 0));
		}
	}

}
