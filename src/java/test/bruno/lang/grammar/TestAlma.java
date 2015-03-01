package bruno.lang.grammar;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.junit.Test;

import bruno.lang.grammar.print.Print;
import bruno.lang.grammar.print.Print.Highlighter;

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
		Parsed jsont = Parsed.parse("etc/example.json", json, "file");
		Print.highlighter(System.out).print(jsont);
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
		Print.highlighter(System.out).print(xmlt);
	}
	
	@Test
	public void bruno() throws IOException {
		Grammar bruno = Alma.make("examples/prelude.alma", "examples/bruno.alma");
		Highlighter highlighter = Print.highlighter(System.out);
		highlighter.print(Parsed.parse("etc/example.lib", bruno, "library"));
		highlighter.print(Parsed.parse("etc/example.ast", bruno, "expr"));		
		highlighter.print(Parsed.parse("etc/example.mod", bruno, "module"));
	}
	
	@Test
	public void printBruno() throws IOException {
		System.out.println(Alma.make("examples/prelude.alma", "examples/bruno.alma"));
	}
}
