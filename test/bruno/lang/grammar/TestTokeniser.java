package bruno.lang.grammar;

import static bruno.lang.grammar.Grammar.Rule.completion;
import static bruno.lang.grammar.Grammar.Rule.literal;
import static bruno.lang.grammar.Grammar.Rule.seq;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.junit.Test;

import bruno.lang.grammar.Grammar.Rule;

public class TestTokeniser {

	@Test
	public void thatBrunoLangCanBeTokenised() throws IOException {
		Tokenised t = Tokenised.tokenise("etc/bruno.grammar", "grammar", Grano.GRAMMAR);
		Grammar bruno = Builder.build(t);
		Tokenised code = Tokenised.tokenise("etc/example.mod", "file", bruno);
		Printer.rulePrinter(System.out).process(code);
	}
	
	@Test
	public void thatGrammarGrammarCanBeTokenised() throws IOException {
		Grammar g0 = Grano.GRAMMAR;
		System.out.println(g0);
		Tokenised t1 = Tokenised.tokenise("etc/grammar.grammar", "grammar", g0);
		new Printer.ParseTreePrinter(System.out).process(t1);
		Grammar g1 = Builder.build(t1);
		System.out.println(g1);
		Tokenised t2 = Tokenised.tokenise("etc/grammar.grammar", "grammar", g1);
		System.out.println(t2.tokens);
		new Printer.ParseTreePrinter(System.out).process(t2);
	}
	
	@Test
	public void thatTerminalHasNoRangeOfZeroLength() throws IOException {
		Tokens tokens = Tokenised.tokenise("etc/test.grammar", "grammar", Grano.GRAMMAR).tokens;
		assertEquals(8, tokens.end());
		assertEquals("terminal", tokens.rule(7).name);
	}
	
	@Test
	public void thatJSONGrammarCanBeTokenised() throws IOException {
		Tokenised t = Tokenised.tokenise("etc/json.grammar", "grammar", Grano.GRAMMAR);
		Grammar json = Builder.build(t);
		System.out.println(json);
		Tokenised jsont = Tokenised.tokenise("etc/example.json", "json", json);
		Printer.rulePrinter(System.out).process(jsont);
		new Printer.ParseTreePrinter(System.out).process(jsont);
	}
	
	@Test
	public void thatXMLGrammarCanBeTokenised() throws IOException {
		Tokenised t = Tokenised.tokenise("etc/xml.grammar", "grammar", Grano.GRAMMAR);
		Grammar xml = Builder.build(t);
		System.out.println(xml);
		Tokenised xmlt = Tokenised.tokenise("etc/example.xml", "document", xml);
		Printer.rulePrinter(System.out).process(xmlt);
		new Printer.ParseTreePrinter(System.out).process(xmlt);
	}

	@Test
	public void thatCompletionWorks() {
		String input = "% this is the comments text\n% this is another one\n";
		Grammar grammar = COMMENTS;
		Tokens tokens = Tokeniser.tokenise(ByteBuffer.wrap(input.getBytes()), grammar.rule("grammar".intern()));
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
		return new Grammar(seq(seq(literal('%'), completion().as("text"), literal('\n')).separate(Rule.EMPTY_STRING).as("comment")).plus().as("grammar"));
	}
	
}
