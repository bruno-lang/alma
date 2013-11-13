package bruno.lang.grammar;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

public class TestTokeniser {

	@Test
	public void thatToyGrammarCanBeTokenised() throws IOException {
		Tokens tree = Tokeniser.tokenise("etc/toy.grammar");
		assertEquals(41, tree.end());
	}
	
	@Test
	public void thatBrunoLangCanBeTokenised() throws IOException {
		Tokens tree = Tokeniser.tokenise("etc/bruno.grammar");
		assertEquals(6402, tree.end());
	}
	
	@Test
	public void thatGrammarGrammarCanBeTokenised() throws IOException {
		Tokens tree = Tokeniser.tokenise("etc/grammar.grammar");
		assertEquals(908, tree.end());
	}
}
