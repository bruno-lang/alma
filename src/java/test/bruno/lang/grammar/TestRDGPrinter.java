package bruno.lang.grammar;

import java.io.IOException;

import org.junit.Test;

import bruno.lang.grammar.print.RDGPrinter;

public class TestRDGPrinter {

	@Test
	public void printingJSON() throws IOException {
		Parsed t = Parsed.parse("examples/json.grammar", Lingukit.GRAMMAR, "grammar");
		Grammar json = grammar(t);
		new RDGPrinter(System.out).print(json);
	}
	
	private static Grammar grammar(Parsed t) {
		return new Grammar(Linguist.finish(Builder.buildGrammar(t)));		
	}
}
