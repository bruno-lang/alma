package bruno.lang.grammar;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

public class TestTokeniser {

	@Test
	public void thatToyGrammarCanBeTokenised() throws IOException {
		Tokens tokens = Tokeniser.tokenise("etc/toy.grammar");
		assertEquals(53, tokens.end());
	}
	
	@Test
	public void thatBrunoLangCanBeTokenised() throws IOException {
		Tokens tokens = Tokeniser.tokenise("etc/bruno.grammar");
		assertEquals(6402, tokens.end());
		assertEquals(2606, tokens.count());
	}
	
	@Test
	public void thatGrammarGrammarCanBeTokenised() throws IOException {
		Tokens tokens = Tokeniser.tokenise("etc/grammar.grammar");
		assertEquals(930, tokens.end());
	}
	
	@Test
	public void thatTerminalHasNoRangeOfZeroLength() throws IOException {
		Tokens tokens = Tokeniser.tokenise("etc/test.grammar");
		assertEquals(8, tokens.end());
		assertEquals("terminal", tokens.rule(7).name);
	}

}
