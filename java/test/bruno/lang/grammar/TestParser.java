package bruno.lang.grammar;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.junit.Test;

public class TestParser {

	@Test
	public void lookahead() throws IOException {
		Grammar test = Alma.make("alma/test.alma");				
		IndexOverlayedFile example = IndexOverlayedFile.read("_data/example.test", test, "start");
		assertEquals("y", example.indexOverlay.rule(1).name);
		assertEquals("x", example.indexOverlay.rule(3).name);
		assertEquals("y", example.indexOverlay.rule(5).name);
		assertEquals("w", example.indexOverlay.rule(6).name);
		assertEquals("y", example.indexOverlay.rule(8).name);
	}
	
	@Test(timeout=200)
	public void fill() {
		String input = "% this is the comments text\n% this is another one\n";
		Grammar grammar = COMMENTS;
		ParseTree tree = new ParseTree(50); 
		Parser.parse(ByteBuffer.wrap(UTF8.bytes(input)), grammar.rule("grammar".intern()), tree);
		assertEquals(5, tree.count());
		assertEquals(" this is the comments text", input.substring(tree.start(2), tree.end(2)));
		assertEquals(" this is another one", input.substring(tree.start(4), tree.end(4)));
	}
	
	/**
	 * A minimal grammar for just comments to test completion feature.
	 */
	static final Grammar COMMENTS = Alma.make("-grammar=comment+-comment='%'~@text[{10}]-".getBytes());

}
