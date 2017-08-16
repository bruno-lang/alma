package alma.lang;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * Tests all the basic elements of the parser.
 * The test do not use "surface syntax".
 * The rewriting is tested as part of the {@link Program}.
 *
 * @author jan
 */
public class TestParser {

	@Test
	public void simpleLiteralMatch() {
		Program prog = Program.compile("'abc'");
		assertEquals(3, prog.parse("abc"));
	}
	
	@Test
	public void simpleLiteralMismatch() {
		Program prog = Program.compile("'abc'");
		assertEquals(-3, prog.parse("abX"));
	}

	@Test
	public void simpleIndentMatch() {
		Program prog = Program.compile(",");
		assertEquals(2, prog.parse("\t "));

		prog = Program.compile(",");
		assertEquals(0, prog.parse("X"));
	}

	@Test
	public void simpleIndentMismatch() {
		Program prog = Program.compile(";");
		assertEquals(-1, prog.parse("X\t "));
	}

	@Test
	public void simpleWhitespaceMatch() {
		Program prog = Program.compile(".");
		assertEquals(4, prog.parse("\t \n\r"));

		prog = Program.compile(".");
		assertEquals(0, prog.parse("X"));
	}

	@Test
	public void simpleWhitespaceMismatch() {
		Program prog = Program.compile(":");
		assertEquals(-1, prog.parse("X\n\t "));
	}

	@Test
	public void simpleLinebreakMatch() {
		Program prog = Program.compile("!");
		assertEquals(3, prog.parse("\t\n "));

		prog = Program.compile("!");
		assertEquals(2, prog.parse("\t "));
	}

	@Test
	public void simpleWildcardMatch() {
		Program prog = Program.compile("_");
		assertEquals(1, prog.parse("a"));
		assertEquals(1, prog.parse(" "));
		assertEquals(1, prog.parse(":"));
		assertEquals(1, prog.parse("^"));
	}

	@Test
	public void simpleLinebreakMismatch() {
		Program prog = Program.compile("!");
		assertEquals(-3, prog.parse("\t X"));
	}
	
	@Test
	public void matchesNewline() {
		Program prog = Program.compile("\\");
		assertEquals(2, prog.parse("\r\n"));
		assertEquals(1, prog.parse("\n"));
		assertEquals(1, prog.parse("\r"));
	}

	@Test
	public void simpleLookAheadMatch() {
		Program prog = Program.compile("'abc'>'def'");
		assertEquals(3, prog.parse("abcdef"));
	}

	@Test
	public void simpleLookAheadMismatch() {
		Program prog = Program.compile("'abc'>'def'");
		assertEquals(-5, prog.parse("abcdEf"));
	}

	@Test
	public void simpleLockMatch() {
		Program prog = Program.compile("('a'<'b'|'c'*)");
		assertEquals(2, prog.parse("abc"));
	}

	@Test
	public void simpleLockMismatch() {
		Program prog = Program.compile("('a'<'b'|'c'*)");
		assertNoMatch(prog, -2, "ac");
	}

	@Test
	public void simpleOptionMatch() {
		Program prog = Program.compile("?'abc'");
		assertEquals(3, prog.parse("abc"));
	}

	@Test
	public void simpleOptionNoMatch() {
		Program prog = Program.compile("?'abc'");
		assertEquals(0, prog.parse("aBc"));
	}

	@Test
	public void simpleCaptureMatch() {
		Program prog = Program.compile("=x'abc')");
		ParseTree tree = new ParseTree(4);
		assertEquals(3, prog.parse("abc".getBytes(), tree));
		assertNode('x', 0, 0, 3, tree, 0);
	}

	@Test
	public void simpleCaptureMismatch() {
		Program prog = Program.compile("=x'aBc')");
		ParseTree tree = new ParseTree(4);
		assertEquals(-2, prog.parse("abc".getBytes(), tree));
		assertEquals(0, tree.nodes());
	}

	@Test
	public void simpleFixedLoopingMatch() {
		Program prog = Program.compile("2'aBc')");
		assertEquals(6, prog.parse("aBcaBc"));
	}

	@Test
	public void simpleStarLoopingMatch() {
		Program prog = Program.compile("*'aBc')");
		assertEquals(3, prog.parse("aBc"));
		assertEquals(6, prog.parse("aBcaBc"));
		assertEquals(0, prog.parse(""));
	}

	@Test
	public void simplePlusLoopingMatch() {
		Program prog = Program.compile("+'aBc')");
		assertEquals(3, prog.parse("aBc"));
		assertEquals(6, prog.parse("aBcaBc"));
	}

	@Test
	public void simplePlusLoopingMismatch() {
		Program prog = Program.compile("+'aBc')");
		assertEquals(-1, prog.parse(""));
	}

	@Test
	public void simpleOptionLoopingMatch() {
		Program prog = Program.compile("?'aBc')");
		assertEquals(3, prog.parse("aBc"));
		assertEquals(3, prog.parse("aBcaBc"));
		assertEquals(0, prog.parse(""));
	}

	@Test
	public void simpleRangeLoopingMatch() {
		Program prog = Program.compile("2+'aBc')");
		assertEquals(6, prog.parse("aBcaBc"));
		assertEquals(9, prog.parse("aBcaBcaBc"));
	}

	@Test
	public void simpleRangeLoopingMismatch() {
		Program prog = Program.compile("2+'aBc')");
		assertEquals(-4, prog.parse("aBc"));
		assertEquals(-1, prog.parse(""));
	}

	@Test
	public void basicMultiCaptureMatch() {
		Program prog = Program.compile("(=x'abc'=y'de'=z'f')");
		ParseTree tree = new ParseTree(4);
		assertEquals(6, prog.parse("abcdef".getBytes(), tree));
		assertNode('x', 0, 0, 6, tree, 0);
		assertNode('y', 1, 3, 6, tree, 1);
		assertNode('z', 2, 5, 6, tree, 2);
	}

	@Test
	public void basicMultiCaptureMismatch() {
		Program prog = Program.compile("(=x'abc'=y'de')");
		ParseTree tree = new ParseTree(4);
		assertEquals(-4, prog.parse("abcDe".getBytes(), tree));
		assertEquals(0, tree.nodes());
	}

	@Test
	public void basicCaptureLoopMatch() {
		Program prog = Program.compile("(+=x'ab')");
		ParseTree tree = new ParseTree(4);
		assertEquals(6, prog.parse("ababab".getBytes(), tree));
		assertEquals(3, tree.nodes());
		assertNode('x', 0, 0, 2, tree, 0);
		assertNode('x', 0, 2, 4, tree, 1);
		assertNode('x', 0, 4, 6, tree, 2);
	}

	@Test
	public void basicLiteralSequenceMatch() {
		Program prog = Program.compile("'abc''def'");
		assertEquals(6, prog.parse("abcdef"));
	}

	@Test
	public void basicSpacedLiteralSequenceMatch() {
		Program prog = Program.compile("'abc','def'");
		assertEquals(7, prog.parse("abc def"));
	}

	@Test
	public void basicLiteralSequenceMismatch() {
		Program prog = Program.compile("'abc''def'");
		assertEquals(-5, prog.parse("abcdEf"));
	}

	@Test
	public void basicSpacedLiteralSequenceMismatch() {
		Program prog = Program.compile("'abc','def'");
		assertEquals(-6, prog.parse("abc dEf"));
	}

	@Test
	public void basicSubsequenceMatch() {
		Program prog = Program.compile("'abc'('def')");
		assertEquals(6, prog.parse("abcdef"));
	}

	@Test
	public void basicSubsequenceMismatch() {
		Program prog = Program.compile("'abc'('def')");
		assertEquals(-5, prog.parse("abcdEf"));
	}

	@Test
	public void basicStaircaseMatch() {
		Program prog = Program.compile("('a'|'b'|'c')");
		assertEquals(1, prog.parse("a"));
		assertEquals(1, prog.parse("b"));
		assertEquals(1, prog.parse("c"));
	}

	@Test
	public void basicStaircaseMismatch() {
		Program prog = Program.compile("('a'|'b'|'c')");
		assertEquals(-1, prog.parse("d"));
	}

	@Test
	public void modestStaircaseLoopingMatch() {
		Program prog = Program.compile("(2'a'|'b'|3'c')");
		assertEquals(2, prog.parse("aa"));
		assertEquals(1, prog.parse("b"));
		assertEquals(3, prog.parse("ccc"));
		// but also
		assertEquals(1, prog.parse("bb"));
	}

	@Test
	public void modestStaircaseLoopingMismatch() {
		Program prog = Program.compile("(2'a'|'b'|3'c')");
		assertEquals(-1, prog.parse(""));
		assertEquals(-1, prog.parse("a"));
		assertEquals(-3, prog.parse("cc"));
	}

	@Test
	public void modestNestedLoopingMatch() {
		Program prog = Program.compile("(+'abc'(*'de'(2'f')))");
		assertEquals(3, prog.parse("abc"));
		assertEquals(3, prog.parse("abcde"));
		assertEquals(7, prog.parse("abcdeff"));
		assertEquals(9, prog.parse("abcabcabc"));
		assertEquals(10, prog.parse("abcdeffabc"));
		assertEquals(14, prog.parse("abcdeffabcdeff"));
	}

	@Test
	public void modestNestedLoopingMismatch() {
		Program prog = Program.compile("(+'abc'(*'de'(2'f')))");
		assertEquals(-1, prog.parse(""));
		assertEquals(-3, prog.parse("ab"));
	}

	@Test
	public void nestedBlocksCaptureMatch() {
		Program prog = Program.compile("(=a'a'(=b'b'(=c'c')'d')'e')");
		ParseTree tree = new ParseTree(20);
		assertEquals(5, prog.parse("abcde".getBytes(), tree));
		assertEquals(3, tree.nodes());
		assertNode('a', 0, 0, 5, tree, 0);
		assertNode('b', 1, 1, 4, tree, 1);
		assertNode('c', 2, 2, 3, tree, 2);
	}
	
	@Test
	public void nestedBlocksCaptureWithLoopsMatch() {
		Program prog = Program.compile("(=a'a'(+=b'b'(*=c'c')'d')'e')");
		ParseTree tree = new ParseTree(20);
		assertEquals(6, prog.parse("abdbde".getBytes(), tree));
		assertEquals(3, tree.nodes());
		assertNode('a', 0, 0, 6, tree, 0);
		assertNode('b', 1, 1, 3, tree, 1);
		assertNode('b', 1, 3, 5, tree, 2);
		
		tree = new ParseTree(20);
		assertEquals(9, prog.parse("abccdbcde".getBytes(), tree));
		assertEquals(6, tree.nodes());
	}
	
	@Test
	public void nestedBlocksMultiCaptureWithLoopsMatch() {
		Program prog = Program.compile("(+=a'a'(*=b'b'(*=c'c')=d'd')'e')");
		ParseTree tree = new ParseTree(20);
		assertEquals(2, prog.parse("ae".getBytes(), tree));
		assertEquals(1, tree.nodes());
		assertNode('a', 0, 0, 2, tree, 0);
		
		tree = new ParseTree(20);
		assertEquals(7, prog.parse("abdbcde".getBytes(), tree));
		assertEquals(6, tree.nodes());
		assertNode('a', 0, 0, 7, tree, 0);
		assertNode('b', 1, 1, 3, tree, 1);
		assertNode('d', 2, 2, 3, tree, 2);
		assertNode('b', 1, 3, 6, tree, 3);
		assertNode('c', 2, 4, 5, tree, 4);
		assertNode('d', 2, 5, 6, tree, 5);
	}

	@Test
	public void modestNestedCaptureMismatch() {
		Program prog = Program.compile("(=a'a'(=b'b'(=c'c')'d')'e')");
		ParseTree tree = new ParseTree(20);
		assertEquals(-5, prog.parse("abcd".getBytes(), tree));
		assertEquals(0, tree.nodes());
		assertEquals(-4, prog.parse("abc".getBytes(), tree));
		assertEquals(0, tree.nodes());
		assertEquals(-3, prog.parse("ab".getBytes(), tree));
		assertEquals(0, tree.nodes());
		assertEquals(-2, prog.parse("a".getBytes(), tree));
		assertEquals(0, tree.nodes());
	}

	@Test
	public void basicOptionMatch() {
		Program prog = Program.compile("'a'(?'bc')'d'");
		assertEquals(4, prog.parse("abcd"));
	}

	@Test
	public void basicOptionNoMatch() {
		Program prog = Program.compile("'a'(?'bc')'d'");
		assertEquals(2, prog.parse("ad"));
	}
	
	@Test
	public void basicFillMatch() {
		Program prog = Program.compile("'a'~'d'");
		assertEquals(2, prog.parse("ad"));
		assertEquals(3, prog.parse("axd"));
		assertEquals(4, prog.parse("axxd"));
		assertEquals(6, prog.parse("axxxxd"));
	}
	
	/**
	 * Shows how to capture the fill by using a look-ahead for the literal afterwards.
	 */
	@Test
	public void fillCaptureLookAheadMatch() {
		Program prog = Program.compile("'<'(=x~>'>')'>'<");
		ParseTree tree = new ParseTree(20);
		assertEquals(5, prog.parse("<foo>".getBytes(), tree));
		assertEquals(1, tree.nodes());
		assertNode('x', 0, 1, 4, tree, 0);
	}
	
	@Test
	public void anyMatch() {
		Program prog = Program.compile("_'a'_'b'");
		assertEquals(4, prog.parse("xaxb"));
		assertEquals(4, prog.parse("'a*b"));
	}

	private static void assertNoMatch(Program prog, int iError, String data) {
		try {
			prog.parse(data);
			fail("Not a parse error:"+prog);
		} catch (NoMatch e) {
			assertEquals(iError, e.iError);
		}
	}

	private static void assertNode(int id, int level, int start, int end, ParseTree tree, int index) {
		assertEquals("Type ID ", id, tree.id(index));
		assertEquals("Start ", start, tree.start(index));
		assertEquals("Level ", level, tree.level(index));
		assertEquals("End", end, tree.end(index));
	}
}
