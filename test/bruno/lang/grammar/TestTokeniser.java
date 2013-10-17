package bruno.lang.grammar;

import java.io.IOException;

import org.junit.Test;

public class TestTokeniser {

	@Test
	public void thatBrunoLangCanBeTokenised() throws IOException {
		Token root = Tokeniser.tokenise("etc/bruno.grammar");
		System.out.println("=============================");
		System.out.println(root);
	}
	
	@Test
	public void thatGrammarGrammarCanBeTokenised() throws IOException {
		Token root = Tokeniser.tokenise("etc/grammar.grammar");
		System.out.println("=============================");
		System.out.println(root);
	}
}
