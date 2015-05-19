package bruno.lang.grammar;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.junit.Ignore;
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
		Grammar prelude = Alma.make("alma/prelude.alma");
		System.out.println(prelude);
	}
	
	@Test
	public void json() throws IOException {
		Grammar json = Alma.make("alma/prelude.alma", "alma/json.alma");
		IndexOverlayedFile example = IndexOverlayedFile.read("_data/example.json", json, "file");
		Print.highlighter(System.out).print(example);
	}
	
	/**
	 * just used manually to roughly check performance (file from http://mtgjson.com/)
	 */
	@Test
	@Ignore
	public void jsonHuge() throws IOException {
		Grammar json = Alma.make("alma/prelude.alma", "alma/json.alma");
		IndexOverlayedFile example = IndexOverlayedFile.read("/home/jan/Downloads/AllSets.json", json, "file");
		assertNotNull(example);
	}
	
	@Test
	public void xml() throws IOException {
		Grammar xml = Alma.make("alma/prelude.alma", "alma/xml.alma");
		IndexOverlayedFile example = IndexOverlayedFile.read("_data/example.xml", xml, "document");
		Print.highlighter(System.out).print(example);
	}
	
	@Test
	public void bruno() throws IOException {
		Grammar bruno = Alma.make("alma/prelude.alma", "alma/bruno.alma");
		Highlighter highlighter = Print.highlighter(System.out);
		highlighter.print(IndexOverlayedFile.read("_data/example.lib", bruno, "library"));
		highlighter.print(IndexOverlayedFile.read("_data/example.ast", bruno, "expr"));		
		highlighter.print(IndexOverlayedFile.read("_data/example.bruno", bruno, "module"));
	}
	
	@Test
	public void printBruno() throws IOException {
		System.out.println(Alma.make("alma/prelude.alma", "alma/bruno.alma"));
	}
}
