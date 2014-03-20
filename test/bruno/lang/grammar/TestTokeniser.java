package bruno.lang.grammar;

import static bruno.lang.grammar.Grammar.Rule.completion;
import static bruno.lang.grammar.Grammar.Rule.seq;
import static bruno.lang.grammar.Grammar.Rule.symbol;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.junit.Test;

public class TestTokeniser {

	@Test
	public void thatBrunoLangCanBeTokenised() throws IOException {
		Tokenised t = Tokenised.tokenise("etc/bruno.grammar", "grammar", NOA.GRAMMAR);
		Grammar bruno = grammar(t);
		Tokenised code = Tokenised.tokenise("etc/example.mod", "file", bruno);
		Printer.rulePrinter(System.out).process(code);
	}
	
	@Test
	public void thatGrammarGrammarCanBeTokenised() throws IOException {
		Grammar g0 = NOA.GRAMMAR;
		Tokenised t1 = Tokenised.tokenise("etc/noa.grammar", "grammar", g0);
		Grammar g1 = grammar(t1);
		Tokenised t2 = Tokenised.tokenise("etc/noa.grammar", "grammar", g1);
		System.out.println(g1);
		Printer.rulePrinter(System.out).process(t2);
	}
	
	@Test
	public void thatTerminalHasNoRangeOfZeroLength() throws IOException {
		Tokens tokens = Tokenised.tokenise("etc/test.grammar", "grammar", NOA.GRAMMAR).tokens;
		assertEquals(8, tokens.end());
		assertEquals("terminal", tokens.rule(7).name);
	}
	
	@Test
	public void thatJSONGrammarCanBeTokenised() throws IOException {
		Tokenised t = Tokenised.tokenise("etc/json.grammar", "grammar", NOA.GRAMMAR);
		Grammar json = grammar(t);
		System.out.println(json);
		Tokenised jsont = Tokenised.tokenise("etc/example.json", "json", json);
		Printer.rulePrinter(System.out).process(jsont);
	}
	
	@Test
	public void thatXMLGrammarCanBeTokenised() throws IOException {
		Tokenised t = Tokenised.tokenise("etc/xml.grammar", "grammar", NOA.GRAMMAR);
		Grammar xml = grammar(t);
		System.out.println(xml);
		Tokenised xmlt = Tokenised.tokenise("etc/example.xml", "document", xml);
		Printer.rulePrinter(System.out).process(xmlt);
	}

	@Test
	public void thatCompletionWorks() {
		String input = "% this is the comments text\n% this is another one\n";
		Grammar grammar = COMMENTS;
		Tokens tokens = GTokeniser.tokenise(ByteBuffer.wrap(input.getBytes()), grammar.rule("grammar".intern()));
		assertEquals(5, tokens.count());
		assertEquals(" this is the comments text", input.substring(tokens.start(2), tokens.end(2)));
		assertEquals(" this is another one", input.substring(tokens.start(4), tokens.end(4)));
	}
	
	private static Grammar grammar(Tokenised t) {
		return new Grammar(GMechanic.finish(GBuilder.grammar(t)));		
	}

	/**
	 * A minimal grammar for just comments to test completion feature.
	 */
	static final Grammar COMMENTS = comments();

	private static Grammar comments() {
		return new Grammar(seq(seq(symbol('%'), completion().as("text"), symbol('\n')).as("comment")).plus().as("grammar"));
	}
	
}
