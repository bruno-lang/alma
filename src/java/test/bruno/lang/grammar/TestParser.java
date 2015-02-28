package bruno.lang.grammar;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.junit.Test;

public class TestParser {

	@Test
	public void lookahead() throws IOException {
		Grammar test = Alma.make("examples/test.alma");				
		Parsed example = Parsed.parse("etc/example.test", test, "start");
		assertEquals("y", example.tree.rule(1).name);
		assertEquals("x", example.tree.rule(3).name);
		assertEquals("y", example.tree.rule(5).name);
		assertEquals("w", example.tree.rule(6).name);
		assertEquals("y", example.tree.rule(8).name);
	}
	
	@Test(timeout=200)
	public void fill() {
		String input = "% this is the comments text\n% this is another one\n";
		Grammar grammar = COMMENTS;
		ParseTree tokens = Parser.parse(ByteBuffer.wrap(UTF8.bytes(input)), grammar.rule("grammar".intern()));
		assertEquals(5, tokens.count());
		assertEquals(" this is the comments text", input.substring(tokens.start(2), tokens.end(2)));
		assertEquals(" this is another one", input.substring(tokens.start(4), tokens.end(4)));
	}
	
	/**
	 * A minimal grammar for just comments to test completion feature.
	 */
	static final Grammar COMMENTS = Alma.make("-grammar=comment+-comment='%'~@text[{10}]-".getBytes());

}
