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
		assertEquals(3, prog.parse("abc").end());
	}
	
	@Test
	public void simpleLiteralMismatch() {
		Program prog = Program.compile("'abc'");
		assertEquals(-3, prog.parse("abX").end());
	}

	@Test
	public void simpleIndentMatch() {
		Program prog = Program.compile(",");
		assertEquals(2, prog.parse("\t ").end());

		prog = Program.compile(",");
		assertEquals(0, prog.parse("X").end());
	}

	@Test
	public void simpleIndentMismatch() {
		Program prog = Program.compile(";");
		assertEquals(-1, prog.parse("X\t ").end());
	}
	
	@Test
	public void simpleSetMatch() {
		Program prog = Program.compile("\"abc\"");
		assertEquals(1, prog.parse("a").end());
		assertEquals(1, prog.parse("b").end());
		assertEquals(1, prog.parse("c").end());
	}
	
	@Test
	public void simpleExclusiveSetMatch() {
		Program prog = Program.compile("\"^abc\"");
		assertEquals(1, prog.parse("d").end());
		assertEquals(1, prog.parse("-").end());
		assertEquals(-1, prog.parse("a").end());
		assertEquals(-1, prog.parse("b").end());
		assertEquals(-1, prog.parse("c").end());
	}
	
	@Test
	public void simpleExclusiveQuotesSetMatch() {
		Program prog = Program.compile("\"^\"");
		assertEquals(-1, prog.parse("\"").end());
		assertEquals(1, prog.parse("a").end());
	}
	
	@Test
	public void simpleBitSetMatch() {
		Program prog = Program.compile("\"&0011xxxx\""); // 0-9:;<=>?
		assertEquals(1, prog.parse("?").end());
		assertEquals(1, prog.parse("0").end());
		assertEquals(1, prog.parse("9").end());
		assertEquals(1, prog.parse("<").end());
		assertEquals(-1, prog.parse("/").end());
		assertEquals(-1, prog.parse("@").end());
	}
	
	@Test
	public void simpleSetMismatch() {
		Program prog = Program.compile("\"abc\"");
		assertEquals(-1, prog.parse("d").end());
	}

	@Test
	public void simpleWhitespaceMatch() {
		Program prog = Program.compile(".");
		assertEquals(4, prog.parse("\t \n\r").end());

		prog = Program.compile(".");
		assertEquals(0, prog.parse("X").end());
	}

	@Test
	public void simpleWhitespaceMismatch() {
		Program prog = Program.compile(":");
		assertEquals(-1, prog.parse("X\n\t ").end());
	}

	@Test
	public void simpleLinebreakMatch() {
		Program prog = Program.compile("!");
		assertEquals(3, prog.parse("\t\n ").end());

		prog = Program.compile("!");
		assertEquals(2, prog.parse("\t ").end());
	}

	@Test
	public void simpleWildcardMatch() {
		Program prog = Program.compile("_");
		assertEquals(1, prog.parse("a").end());
		assertEquals(1, prog.parse(" ").end());
		assertEquals(1, prog.parse(":").end());
		assertEquals(1, prog.parse("^").end());
	}

	@Test
	public void simpleLinebreakMismatch() {
		Program prog = Program.compile("!");
		assertEquals(-3, prog.parse("\t X").end());
	}
	
	@Test
	public void matchesNewline() {
		Program prog = Program.compile("\\");
		assertEquals(2, prog.parse("\r\n").end());
		assertEquals(1, prog.parse("\n").end());
		assertEquals(1, prog.parse("\r").end());
	}

	@Test
	public void simpleLookAheadMatch() {
		Program prog = Program.compile("'abc'>'def'");
		assertEquals(3, prog.parse("abcdef").end());
	}

	@Test
	public void simpleLookAheadMismatch() {
		Program prog = Program.compile("'abc'>'def'");
		assertEquals(-5, prog.parse("abcdEf").end());
	}

	@Test
	public void simpleLockMatch() {
		Program prog = Program.compile("('a'<'b'|'c'*)");
		assertEquals(2, prog.parse("abc").end());
	}

	@Test
	public void simpleLockMismatch() {
		Program prog = Program.compile("('a'<'b'|'c'*)");
		assertNoMatch(prog, -2, "ac");
	}

	@Test
	public void simpleOptionMatch() {
		Program prog = Program.compile("?'abc'");
		assertEquals(3, prog.parse("abc").end());
	}

	@Test
	public void simpleOptionNoMatch() {
		Program prog = Program.compile("?'abc'");
		assertEquals(0, prog.parse("aBc").end());
	}

	@Test
	public void simpleCaptureMatch() {
		Program prog = Program.compile("=x'abc')");
		ParseTree tree = prog.parse("abc");
		assertEquals(3, tree.end());
		assertNode('x', 0, 0, 3, tree, 0);
	}

	@Test
	public void simpleCaptureMismatch() {
		Program prog = Program.compile("=x'aBc')");
		ParseTree tree = prog.parse("abc");
		assertEquals(-2, tree.end());
		assertEquals(0, tree.nodes());
	}

	@Test
	public void simpleFixedRepMatch() {
		Program prog = Program.compile("2'aBc')");
		assertEquals(6, prog.parse("aBcaBc").end());
	}

	@Test
	public void simpleStarRepMatch() {
		Program prog = Program.compile("*'aBc')");
		assertEquals(3, prog.parse("aBc").end());
		assertEquals(6, prog.parse("aBcaBc").end());
		assertEquals(0, prog.parse("").end());
	}

	@Test
	public void simplePlusRepMatch() {
		Program prog = Program.compile("+'aBc')");
		assertEquals(3, prog.parse("aBc").end());
		assertEquals(6, prog.parse("aBcaBc").end());
	}

	@Test
	public void simplePlusRepMismatch() {
		Program prog = Program.compile("+'aBc')");
		assertEquals(-1, prog.parse("").end());
	}

	@Test
	public void simpleOptionRepMatch() {
		Program prog = Program.compile("?'aBc')");
		assertEquals(3, prog.parse("aBc").end());
		assertEquals(3, prog.parse("aBcaBc").end());
		assertEquals(0, prog.parse("").end());
	}

	@Test
	public void simpleRangeRepMatch() {
		Program prog = Program.compile("2+'aBc')");
		assertEquals(6, prog.parse("aBcaBc").end());
		assertEquals(9, prog.parse("aBcaBcaBc").end());
	}

	@Test
	public void simpleRangeRepMismatch() {
		Program prog = Program.compile("2+'aBc')");
		assertEquals(-4, prog.parse("aBc").end());
		assertEquals(-1, prog.parse("").end());
	}
	
	@Test
	public void simpleDigitMatch() {
		Program prog = Program.compile("#");
		assertEquals(1, prog.parse("1").end());
		assertEquals(1, prog.parse("9").end());
	}
	
	@Test
	public void simpleDigitMismatch() {
		Program prog = Program.compile("#");
		assertEquals(-1, prog.parse("a").end());
	}
	
	@Test
	public void basicDigitsMatch() {
		Program prog = Program.compile(" #+ '.' ##? ");
		assertEquals(5, prog.parse("42.05").end());
		assertEquals(2, prog.parse("7.").end());
	}

	@Test
	public void basicMultiCaptureMatch() {
		Program prog = Program.compile("(=x'abc'=y'de'=z'f')");
		ParseTree tree = prog.parse("abcdef");
		assertEquals(6, tree.end());
		assertNode('x', 0, 0, 6, tree, 0);
		assertNode('y', 1, 3, 6, tree, 1);
		assertNode('z', 2, 5, 6, tree, 2);
	}

	@Test
	public void basicMultiCaptureMismatch() {
		Program prog = Program.compile("(=x'abc'=y'de')");
		ParseTree tree = prog.parse("abcDe");
		assertEquals(-4, tree.end());
		assertEquals(0, tree.nodes());
	}

	@Test
	public void basicCaptureLoopMatch() {
		Program prog = Program.compile("(+=x'ab')");
		ParseTree tree = prog.parse("ababab");
		assertEquals(6, tree.end());
		assertEquals(3, tree.nodes());
		assertNode('x', 0, 0, 2, tree, 0);
		assertNode('x', 0, 2, 4, tree, 1);
		assertNode('x', 0, 4, 6, tree, 2);
	}

	@Test
	public void basicLiteralSequenceMatch() {
		Program prog = Program.compile("'abc''def'");
		assertEquals(6, prog.parse("abcdef").end());
	}

	@Test
	public void basicSpacedLiteralSequenceMatch() {
		Program prog = Program.compile("'abc','def'");
		assertEquals(7, prog.parse("abc def").end());
	}

	@Test
	public void basicLiteralSequenceMismatch() {
		Program prog = Program.compile("'abc''def'");
		assertEquals(-5, prog.parse("abcdEf").end());
	}

	@Test
	public void basicSpacedLiteralSequenceMismatch() {
		Program prog = Program.compile("'abc','def'");
		assertEquals(-6, prog.parse("abc dEf").end());
	}

	@Test
	public void basicSubsequenceMatch() {
		Program prog = Program.compile("'abc'('def')");
		assertEquals(6, prog.parse("abcdef").end());
	}

	@Test
	public void basicSubsequenceMismatch() {
		Program prog = Program.compile("'abc'('def')");
		assertEquals(-5, prog.parse("abcdEf").end());
	}

	@Test
	public void basicCasesMatch() {
		Program prog = Program.compile("('a'|'b'|'c')");
		assertEquals(1, prog.parse("a").end());
		assertEquals(1, prog.parse("b").end());
		assertEquals(1, prog.parse("c").end());
	}
	
	@Test
	public void basicCasesRepMatch() {
		Program prog = Program.compile("('a'|'b'|'c')+ ");
		assertEquals(1, prog.parse("a").end());
		assertEquals(1, prog.parse("b").end());
		assertEquals(1, prog.parse("c").end());
		assertEquals(2, prog.parse("aa").end());
		assertEquals(2, prog.parse("bb").end());
		assertEquals(3, prog.parse("ccc").end());
		assertEquals(-1, prog.parse("").end());
		assertEquals(1, prog.parse("abc").end());
	}

	@Test
	public void basicCasesMismatch() {
		Program prog = Program.compile("('a'|'b'|'c')");
		assertEquals(-1, prog.parse("d").end());
	}

	@Test
	public void modestCasesRepMatch() {
		Program prog = Program.compile("(2'a'|1'b'|3'c')");
		assertEquals(2, prog.parse("aa").end());
		assertEquals(1, prog.parse("b").end());
		assertEquals(3, prog.parse("ccc").end());
		// but also
		assertEquals(1, prog.parse("bb").end());
	}

	@Test
	public void modestStaircaseRepMismatch() {
		Program prog = Program.compile("(2'a'|'b'|3'c')");
		assertEquals(-1, prog.parse("").end());
		assertEquals(-1, prog.parse("a").end());
		assertEquals(-3, prog.parse("cc").end());
	}

	@Test
	public void modestNestedRepMatch() {
		Program prog = Program.compile("(+'abc'(*'de'(2'f')))");
		assertEquals(3, prog.parse("abc").end());
		assertEquals(3, prog.parse("abcde").end());
		assertEquals(7, prog.parse("abcdeff").end());
		assertEquals(9, prog.parse("abcabcabc").end());
		assertEquals(10, prog.parse("abcdeffabc").end());
		assertEquals(14, prog.parse("abcdeffabcdeff").end());
	}

	@Test
	public void modestNestedRepMismatch() {
		Program prog = Program.compile("(+'abc'(*'de'(2'f')))");
		assertEquals(-1, prog.parse("").end());
		assertEquals(-3, prog.parse("ab").end());
	}

	@Test
	public void nestedBlocksCaptureMatch() {
		Program prog = Program.compile("(=a'a'(=b'b'(=c'c')'d')'e')");
		ParseTree tree = prog.parse("abcde");
		assertEquals(5, tree.end());
		assertEquals(3, tree.nodes());
		assertNode('a', 0, 0, 5, tree, 0);
		assertNode('b', 1, 1, 4, tree, 1);
		assertNode('c', 2, 2, 3, tree, 2);
	}
	
	@Test
	public void nestedBlocksCaptureWithLoopsMatch() {
		Program prog = Program.compile("(=a'a'(+=b'b'(*=c'c')'d')'e')");
		ParseTree tree = prog.parse("abdbde");
		assertEquals(6, tree.end());
		assertEquals(3, tree.nodes());
		assertNode('a', 0, 0, 6, tree, 0);
		assertNode('b', 1, 1, 3, tree, 1);
		assertNode('b', 1, 3, 5, tree, 2);
		
		tree = prog.parse("abccdbcde");
		assertEquals(9, tree.end());
		assertEquals(6, tree.nodes());
	}
	
	@Test
	public void nestedBlocksMultiCaptureWithLoopsMatch() {
		Program prog = Program.compile("(+=a'a'(*=b'b'(*=c'c')=d'd')'e')");
		ParseTree tree = prog.parse("ae");;
		assertEquals(2, tree.end());
		assertEquals(1, tree.nodes());
		assertNode('a', 0, 0, 2, tree, 0);
		
		tree = prog.parse("abdbcde");
		assertEquals(7, tree.end());
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
		ParseTree tree = prog.parse("abcd");
		assertEquals(-5, tree.end());
		assertEquals(0, tree.nodes());
		
		tree = prog.parse("abc");
		assertEquals(-4, tree.end());
		assertEquals(0, tree.nodes());
		
		tree = prog.parse("ab");
		assertEquals(-3, tree.end());
		assertEquals(0, tree.nodes());
		
		tree = prog.parse("a");
		assertEquals(-2, tree.end());
		assertEquals(0, tree.nodes());
	}

	@Test
	public void basicOptionMatch() {
		Program prog = Program.compile("'a'(?'bc')'d'");
		assertEquals(4, prog.parse("abcd").end());
	}

	@Test
	public void basicOptionNoMatch() {
		Program prog = Program.compile("'a'(?'bc')'d'");
		assertEquals(2, prog.parse("ad").end());
	}
	
	@Test
	public void basicFillMatch() {
		Program prog = Program.compile("'a'~'d'");
		assertEquals(2, prog.parse("ad").end());
		assertEquals(3, prog.parse("axd").end());
		assertEquals(4, prog.parse("axxd").end());
		assertEquals(6, prog.parse("axxxxd").end());
	}
	
	@Test
	public void basicFillMatchQuotes() {
		Program prog = Program.compile("'\"' ~ '\"'");
		assertEquals(2, prog.parse("\"\"").end());
		assertEquals(3, prog.parse("\"a\"").end());		
		assertEquals(4, prog.parse("\"ab\"").end());
	}
	
	/**
	 * Shows how to capture the fill by using a look-ahead for the literal afterwards.
	 */
	@Test
	public void fillCaptureLookAheadMatch() {
		Program prog = Program.compile("'<'(=x~>'>')'>'<");
		ParseTree tree = prog.parse("<foo>");
		assertEquals(5, tree.end());
		assertEquals(1, tree.nodes());
		assertNode('x', 0, 1, 4, tree, 0);
	}
	
	@Test
	public void anyMatch() {
		Program prog = Program.compile("_'a'_'b'");
		assertEquals(4, prog.parse("xaxb").end());
		assertEquals(4, prog.parse("'a*b").end());
	}
	
	@Test
	public void simpleRefMatch() {
		Program prog = Program.compile("b c) (=b #) (=c .)");
		ParseTree tree = prog.parse("9 ");
		assertEquals(2, tree.end());
		assertNode('b', 0, 0, 1, tree, 0);
		assertNode('c', 0, 1, 2, tree, 1);
	}
	
	@Test
	public void exampleQuotedString() {
		Program prog = Program.compile("'\"' (*('\\'_|\"^\")) '\"'");
		assertEquals(2, prog.parse("\"\"").end());
		assertEquals(5, prog.parse("\"abc\"").end());
	}

	private static void assertNoMatch(Program prog, int iError, String data) {
		try {
			prog.parse(data);
			fail("Not a parse error:"+prog);
		} catch (NoMatch e) {
			assertEquals(iError, e.iError);
		}
	}

	private static void assertNode(char name, int level, int start, int end, ParseTree tree, int index) {
		assertEquals("Type ID ", name, tree.rule(index).charAt(0));
		assertEquals("Start ", start, tree.start(index));
		assertEquals("Level ", level, tree.level(index));
		assertEquals("End", end, tree.end(index));
	}
}
