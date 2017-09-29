package alma.lang;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;


public class TestParserExamples {

	@Test
	public void test() throws IOException {
		Program prog = Program.compileFile("alma/json.a2");
		assertTrue(prog.parse("{\"name\":null}").end() > 0);
		assertTrue(prog.parse("null").end() > 0);
		assertTrue(prog.parse("true").end() > 0);
		assertTrue(prog.parse("false").end() > 0);
		assertTrue(prog.parse("\"string\"").end() > 0);
		assertTrue(prog.parse("1").end() > 0);
		assertTrue(prog.parse("1.04").end() > 0);
		assertTrue(prog.parse("[true]").end() > 0);
		assertTrue(prog.parse("[null, false]").end() > 0);
		assertTrue(prog.parse("{\"name\": [1., null, \"foo\"], \"name2\": 23}").end() > 0);
	}
}
