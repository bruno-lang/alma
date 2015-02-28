package bruno.lang.grammar;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.junit.Test;

import bruno.lang.grammar.print.Printer;
import bruno.lang.grammar.print.Processor;

/**
 * Make some {@link Grammar}s and parse some examples with them. 
 *  
 * @author jan
 */
public class TestAlma {

	@Test
	public void prelude() throws IOException {
		Grammar prelude = Alma.make("examples/prelude.alma");
		System.out.println(prelude);
	}
	
	@Test
	public void json() throws IOException {
		Grammar json = Alma.make("examples/prelude.alma", "examples/json.alma");
		System.out.println(json);
	}
	
	@Test
	public void jsonHuge() throws IOException {
		Grammar json = Alma.make("examples/prelude.alma", "examples/json.alma");
		Parsed jsont = Parsed.parse("../../../huge.json", json, "file");
		assertNotNull(jsont);
	}
	
	@Test
	public void xml() throws IOException {
		Grammar xml = Alma.make("examples/prelude.alma", "examples/xml.alma");
		Parsed xmlt = Parsed.parse("etc/example.xml", xml, "document");
		Printer.rulePrinter(System.out).process(xmlt);
	}
	
	@Test
	public void bruno() throws IOException {
		Grammar bruno = Alma.make("examples/prelude.alma", "examples/bruno.alma");
		Processor printer = Printer.rulePrinter(System.out);
		printer.process(Parsed.parse("etc/example.lib", bruno, "library"));
		printer.process(Parsed.parse("etc/example.ast", bruno, "expr"));		
		printer.process(Parsed.parse("etc/example.mod", bruno, "module"));
	}
	
	@Test
	public void printBruno() throws IOException {
		System.out.println(Alma.make("examples/prelude.alma", "examples/bruno.alma"));
	}
}
