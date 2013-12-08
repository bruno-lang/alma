package bruno.lang.grammar;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

public class TestTokeniser {

	@Test
	public void thatToyGrammarCanBeTokenised() throws IOException {
		Tokens tokens = Tokeniser.tokenise("etc/toy.grammar");
		assertEquals(70, tokens.end());
	}
	
	@Test
	public void thatBrunoLangCanBeTokenised() throws IOException {
		Tokens tokens = Tokeniser.tokenise("etc/bruno.grammar");
		assertEquals(6274, tokens.end());
		assertEquals(2470, tokens.count());
	}
	
	@Test
	public void thatGrammarGrammarCanBeTokenised() throws IOException {
		Tokens tokens = Tokeniser.tokenise("etc/grammar.grammar");
		assertEquals(926, tokens.end());
	}
	
	@Test
	public void thatTerminalHasNoRangeOfZeroLength() throws IOException {
		Tokens tokens = Tokeniser.tokenise("etc/test.grammar");
		assertEquals(8, tokens.end());
		assertEquals("terminal", tokens.rule(7).name);
	}

}
