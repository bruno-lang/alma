package bruno.lang.grammar;

import java.io.IOException;

import org.junit.Test;

import bruno.lang.grammar.print.RDGPrinter;

public class TestRDGPrinter {

	@Test
	public void printingJSON() throws IOException {
		Grammar json = Alma.make("examples/prelude.alma", "examples/json.alma");
		new RDGPrinter(System.out).print(json);
	}
	
}