package bruno.lang.grammar;

import static bruno.lang.grammar.Terminal.notRange;
import static bruno.lang.grammar.Terminal.range;
import static org.junit.Assert.assertEquals;

import java.nio.ByteBuffer;

import org.junit.Test;

public class TestTerminal {

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

	private void assertNotContainsCharacters(Terminal t, String notContained) {
		assertContainsCharacters(t, notContained, false);
	}

	private void assertContainsCharacters(Terminal t, String contained) {
		assertContainsCharacters(t, contained, true);
	}

	private void assertContainsCharacters(Terminal t, String characters,
			boolean contains) {
		for (char c : characters.toCharArray()) {
			assertEquals(String.valueOf(c), contains, t.contains(ByteBuffer.wrap(String.valueOf(c).getBytes()), 0));
		}
	}

}
