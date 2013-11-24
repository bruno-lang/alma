package bruno.lang.grammar;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TestGrammar {

	@Test
	public void thatWhitespaceMatches() {
		assertTrue(Grammar.whitespace.matches((byte) ' '));
		assertTrue(Grammar.whitespace.matches((byte) '\t'));
		assertTrue(Grammar.whitespace.matches((byte) '\n'));
	}
}
