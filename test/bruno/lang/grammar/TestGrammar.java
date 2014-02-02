package bruno.lang.grammar;

import static org.junit.Assert.assertEquals;

import java.nio.ByteBuffer;

import org.junit.Test;

public class TestGrammar {

	@Test
	public void thatWhitespaceMatches() {
		assertEquals(1, Grammar.whitespace.length(ByteBuffer.wrap(" ".getBytes()), 0));
		assertEquals(1, Grammar.whitespace.length(ByteBuffer.wrap("\t".getBytes()), 0));
		assertEquals(1, Grammar.whitespace.length(ByteBuffer.wrap("\n".getBytes()), 0));
	}
}
