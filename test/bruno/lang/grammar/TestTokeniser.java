package bruno.lang.grammar;

import static bruno.lang.grammar.Grammar.Rule.completion;
import static bruno.lang.grammar.Grammar.Rule.literal;
import static bruno.lang.grammar.Grammar.Rule.sequence;
import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import bruno.lang.grammar.Grammar.Rule;

public class TestTokeniser {

	@Test
	public void thatToyGrammarCanBeTokenised() throws IOException {
		Tokens tokens = Tokeniser.tokenise("etc/toy.grammar");
		assertEquals(90, tokens.end());
	}
	
	@Test
	public void thatBrunoLangCanBeTokenised() throws IOException {
		Tokens tokens = Tokeniser.tokenise("etc/bruno.grammar");
		assertEquals(6254, tokens.end());
		assertEquals(2305, tokens.count());
	}
	
	@Test
	public void thatGrammarGrammarCanBeTokenised() throws IOException {
		Tokens tokens = Tokeniser.tokenise("etc/grammar.grammar");
		assertEquals(982, tokens.end());
	}
	
	@Test
	public void thatTerminalHasNoRangeOfZeroLength() throws IOException {
		Tokens tokens = Tokeniser.tokenise("etc/test.grammar");
		assertEquals(8, tokens.end());
		assertEquals("terminal", tokens.rule(8).name);
	}
	
	@Test
	public void thatJSONGrammarCanBeTokenised() throws IOException {
		Tokens tokens = Tokeniser.tokenise("etc/json.grammar");
		assertEquals(362, tokens.end());
	}
	
	@Test
	public void thatXMLGrammarCanBeTokenised() throws IOException {
		Tokens tokens = Tokeniser.tokenise("etc/xml.grammar");
		assertEquals(338, tokens.end());
	}

	@Test
	public void thatCompletionWorks() {
		Tokeniser t = new Tokeniser(COMMENTS);
		String input = "% this is the comments text\n";
		Tokens tokens = t.tokenise("comment", input.getBytes());
		assertEquals(2, tokens.count());
		assertEquals(" this is the comments text", input.substring(tokens.start(1), tokens.end(1)));
	}
	
	static final Grammar COMMENTS = comments();

	private static Grammar comments() {
		return new Grammar(sequence(literal('%'), completion(literal('\n')).as("text"), literal('\n')).separate(Rule.EMPTY_STRING).as("comment"));
	} 
}
