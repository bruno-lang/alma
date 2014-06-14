package bruno.lang.grammar;

import static bruno.lang.grammar.Grammar.Rule.completion;
import static bruno.lang.grammar.Grammar.Rule.seq;
import static bruno.lang.grammar.Grammar.Rule.symbol;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.junit.Test;

import bruno.lang.grammar.print.Printer;
import bruno.lang.grammar.print.Processor;

public class TestParser {

	@Test
	public void thatBrunoLangCanBeParsed() throws IOException {
		Parsed t = Parsed.parse("etc/bruno.grammar", "grammar", Lingukit.GRAMMAR);
		Grammar bruno = grammar(t);
		Processor printer = Printer.rulePrinter(System.out);
		printer.process(Parsed.parse("etc/example.ns", "namespace", bruno));
		printer.process(Parsed.parse("etc/example.mod", "module", bruno));
	}
	
	@Test
	public void thatBrunoASTCanBeParsed() throws IOException {
		Parsed t = Parsed.parse("etc/bruno.grammar", "grammar", Lingukit.GRAMMAR);
		Grammar bruno = grammar(t);
		Processor printer = Printer.rulePrinter(System.out);
		printer.process(Parsed.parse("etc/example.ast", "namespace", bruno));
		printer.process(Parsed.parse("etc/bruno.mod", "module", bruno));
	}
	
	@Test
	public void thatLingukitGrammarCanBeParsed() throws IOException {
		Grammar g0 = Lingukit.GRAMMAR;
		Parsed t1 = Parsed.parse("etc/lingukit.grammar", "grammar", g0);
		Grammar g1 = grammar(t1);
		Parsed t2 = Parsed.parse("etc/lingukit.grammar", "grammar", g1);
		Printer.rulePrinter(System.out).process(t2);
	}
	
	@Test
	public void thatLookaheadWorks() throws IOException {
		Parsed p = Parsed.parse("etc/test.grammar", "grammar", Lingukit.GRAMMAR);
		Grammar test = grammar(p);
		Parsed example = Parsed.parse("etc/example.test", "start", test);
		assertEquals("y", example.tree.rule(1).name);
		assertEquals("x", example.tree.rule(3).name);
		assertEquals("y", example.tree.rule(5).name);
		assertEquals("w", example.tree.rule(6).name);
		assertEquals("y", example.tree.rule(8).name);
	}
	
	@Test
	public void thatJSONGrammarCanBeParsed() throws IOException {
		Parsed t = Parsed.parse("etc/json.grammar", "grammar", Lingukit.GRAMMAR);
		Grammar json = grammar(t);
		Parsed jsont = Parsed.parse("etc/example.json", "json", json);
		Printer.rulePrinter(System.out).process(jsont);
	}
	
	@Test
	public void thatXMLGrammarCanBeParsed() throws IOException {
		Parsed t = Parsed.parse("etc/xml.grammar", "grammar", Lingukit.GRAMMAR);
		Grammar xml = grammar(t);
		Parsed xmlt = Parsed.parse("etc/example.xml", "document", xml);
		Printer.rulePrinter(System.out).process(xmlt);
	}

	@Test(timeout=200)
	public void thatCompletionWorks() {
		String input = "% this is the comments text\n% this is another one\n";
		Grammar grammar = COMMENTS;
		ParseTree tokens = Parser.parse(ByteBuffer.wrap(UTF8.bytes(input)), grammar.rule("grammar".intern()));
		assertEquals(5, tokens.count());
		assertEquals(" this is the comments text", input.substring(tokens.start(2), tokens.end(2)));
		assertEquals(" this is another one", input.substring(tokens.start(4), tokens.end(4)));
	}
	
	@Test
	public void thatJavaGrammarCanBeParsed() throws IOException {
		Parsed t = Parsed.parse("etc/java.grammar", "grammar", Lingukit.GRAMMAR);
		Grammar java = grammar(t);
		assertParses(java, new File("src/java/main/bruno/lang/grammar/"), ".java");
		assertParses(java, new File("src/java/main/bruno/lang/grammar/print/"), ".java");
		assertParses(java, new File("src/java/test/bruno/lang/grammar/"), ".java");
//		Parsed example = Parsed.parse("/home/jan/proj/silk/src/core/se/jbee/inject/bind/Binder.java", "file", java);
//		Printer.rulePrinter(System.out).process(example);
	}

	private void assertParses(Grammar java, File src, String ending) throws IOException {
		for (File source : src.listFiles()) {
			if (source.getName().endsWith(ending)) {
				Parsed example = Parsed.parse(source.getAbsolutePath(), "file", java);
				assertEquals(source.length(), example.tree.end());
			}
		}
	}	
	
	private static Grammar grammar(Parsed t) {
		return new Grammar(Linguist.finish(Builder.buildGrammar(t)));		
	}

	/**
	 * A minimal grammar for just comments to test completion feature.
	 */
	static final Grammar COMMENTS = comments();

	private static Grammar comments() {
		return new Grammar(seq(seq(symbol('%'), completion().as("text"), symbol('\n')).as("comment")).plus().as("grammar"));
	}
	
}
