package bruno.lang.grammar;

import static bruno.lang.grammar.Grammar.Rule.completion;
import static bruno.lang.grammar.Grammar.Rule.literal;
import static bruno.lang.grammar.Grammar.Rule.sequence;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.junit.Test;

import bruno.lang.grammar.Grammar.Rule;

public class TestTokeniser {

	@Test
	public void thatToyGrammarCanBeTokenised() throws IOException {
		Tokens tokens = BNF.tokenise("etc/toy.grammar").tokens;
		assertEquals(90, tokens.end());
	}
	
	@Test
	public void thatBrunoLangCanBeTokenised() throws IOException {
		Tokenised t = BNF.tokenise("etc/bruno.grammar");
		Tokens tokens = t.tokens;
		assertEquals(6109, tokens.end());
		assertEquals(2304, tokens.count());
		Printer.rulePrinter(System.out).process(t);
	}
	
	@Test
	public void thatGrammarGrammarCanBeTokenised() throws IOException {
		Tokenised t = BNF.tokenise("etc/grammar.grammar");
		assertEquals(983, t.tokens.end());
		Grammar g = GrammerCompiler.compile(t);
		System.out.println(g);
	}
	
	@Test
	public void thatTerminalHasNoRangeOfZeroLength() throws IOException {
		Tokens tokens = BNF.tokenise("etc/test.grammar").tokens;
		assertEquals(8, tokens.end());
		assertEquals("terminal", tokens.rule(8).name);
	}
	
	@Test
	public void thatJSONGrammarCanBeTokenised() throws IOException {
		Tokenised t = BNF.tokenise("etc/json.grammar");
		assertEquals(368, t.tokens.end());
	}
	
	@Test
	public void thatXMLGrammarCanBeTokenised() throws IOException {
		Tokenised t = BNF.tokenise("etc/xml.grammar");
		assertEquals(337, t.tokens.end());
	}

	@Test
	public void thatCompletionWorks() {
		String input = "% this is the comments text\n% this is another one\n";
		Tokens tokens = Tokeniser.tokenise(ByteBuffer.wrap(input.getBytes()), COMMENTS.rule("grammar".intern()));
		assertEquals(5, tokens.count());
		assertEquals(" this is the comments text", input.substring(tokens.start(2), tokens.end(2)));
		assertEquals(" this is another one", input.substring(tokens.start(4), tokens.end(4)));
	}

	/**
	 * A minimal grammar for just comments to test completion feature working as
	 * it is not needed for the {@link BNF} grammar.
	 */
	static final Grammar COMMENTS = comments();

	private static Grammar comments() {
		return new Grammar(sequence(sequence(literal('%'), completion(literal('\n')).as("text"), literal('\n')).separate(Rule.EMPTY_STRING).as("comment")).plus().as("grammar"));
	} 
}
