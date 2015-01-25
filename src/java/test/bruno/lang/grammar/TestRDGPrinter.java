package bruno.lang.grammar;

import java.io.IOException;

import org.junit.Test;

import bruno.lang.grammar.print.RDGPrinter;

public class TestRDGPrinter {

	@Test
	public void printingJSON() throws IOException {
		Parsed t = Parsed.parse("examples/json.grammar", Alma.GRAMMAR, "grammar");
		Grammar json = GrammarBuilder.buildGrammar(t);
		new RDGPrinter(System.out).print(json);
	}
	
}