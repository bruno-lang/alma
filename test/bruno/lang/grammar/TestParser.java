package bruno.lang.grammar;

import static bruno.lang.grammar.Grammar.Rule.completion;
import static bruno.lang.grammar.Grammar.Rule.seq;
import static bruno.lang.grammar.Grammar.Rule.symbol;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.junit.Test;

import bruno.lang.grammar.print.Printer;

public class TestParser {

	@Test
	public void thatBrunoLangCanBeParsed() throws IOException {
		Parsed t = Parsed.parse("etc/bruno.grammar", "grammar", NOA.GRAMMAR);
		Grammar bruno = grammar(t);
		Parsed code = Parsed.parse("etc/example.mod", "file", bruno);
		Processor printer = Printer.rulePrinter(System.out);
		printer.process(code);
		Parsed ns = Parsed.parse("etc/example.ns", "file", bruno);
		printer.process(ns);
	}
	
	@Test
	public void thatGrammarGrammarCanBeParsed() throws IOException {
		Grammar g0 = NOA.GRAMMAR;
		Parsed t1 = Parsed.parse("etc/noa.grammar", "grammar", g0);
		Grammar g1 = grammar(t1);
		Parsed t2 = Parsed.parse("etc/noa.grammar", "grammar", g1);
		System.out.println(g1);
		Printer.rulePrinter(System.out).process(t2);
	}
	
	@Test
	public void thatTerminalHasNoRangeOfZeroLength() throws IOException {
		ParseTree tokens = Parsed.parse("etc/test.grammar", "grammar", NOA.GRAMMAR).tree;
		assertEquals(8, tokens.end());
		assertEquals("terminal", tokens.rule(7).name);
	}
	
	@Test
	public void thatJSONGrammarCanBeParsed() throws IOException {
		Parsed t = Parsed.parse("etc/json.grammar", "grammar", NOA.GRAMMAR);
		Grammar json = grammar(t);
		System.out.println(json);
		Parsed jsont = Parsed.parse("etc/example.json", "json", json);
		Printer.rulePrinter(System.out).process(jsont);
	}
	
	@Test
	public void thatXMLGrammarCanBeParsed() throws IOException {
		Parsed t = Parsed.parse("etc/xml.grammar", "grammar", NOA.GRAMMAR);
		Grammar xml = grammar(t);
		System.out.println(xml);
		Parsed xmlt = Parsed.parse("etc/example.xml", "document", xml);
		Printer.rulePrinter(System.out).process(xmlt);
	}

	@Test
	public void thatCompletionWorks() {
		String input = "% this is the comments text\n% this is another one\n";
		Grammar grammar = COMMENTS;
		ParseTree tokens = Parser.parse(ByteBuffer.wrap(UTF8.bytes(input)), grammar.rule("grammar".intern()));
		assertEquals(5, tokens.count());
		assertEquals(" this is the comments text", input.substring(tokens.start(2), tokens.end(2)));
		assertEquals(" this is another one", input.substring(tokens.start(4), tokens.end(4)));
	}
	
	private static Grammar grammar(Parsed t) {
		return new Grammar(Mechanic.finish(Builder.grammar(t)));		
	}

	/**
	 * A minimal grammar for just comments to test completion feature.
	 */
	static final Grammar COMMENTS = comments();

	private static Grammar comments() {
		return new Grammar(seq(seq(symbol('%'), completion().as("text"), symbol('\n')).as("comment")).plus().as("grammar"));
	}
	
}
