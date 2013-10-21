package bruno.lang.grammar;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

public class TestTokeniser {

	@Test
	public void thatBrunoLangCanBeTokenised() throws IOException {
		Token root = Tokeniser.tokenise("etc/bruno.grammar");
		assertEquals(6402, root.end);
	}
	
	@Test
	public void thatGrammarGrammarCanBeTokenised() throws IOException {
		Token root = Tokeniser.tokenise("etc/grammar.grammar");
		assertEquals(866, root.end);
	}
}
