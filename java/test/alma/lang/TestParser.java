package alma.lang;

import static org.junit.Assert.assertEquals;

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
	public void simpleCharacterMatch() {
		Program prog = Program.compile("`x");
		assertEquals(1, prog.parse("x"));
	}

	@Test
	public void simpleCharacterMismatch() {
		Program prog = Program.compile("`x");
		assertEquals(-1, prog.parse("y"));

		prog = Program.compile("`x`y");
		assertEquals(-2, prog.parse("xA"));
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
		assertEquals(3, tree.end());
		assertEquals('x', tree.rule(0));
		assertEquals(0, tree.start(0));
	}

	@Test
	public void simpleCaptureMismatch() {
		Program prog = Program.compile("=x'aBc')");
		ParseTree tree = new ParseTree(4);
		assertEquals(-2, prog.parse("abc".getBytes(), tree));
		assertEquals(0, tree.count());
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
		Program prog = Program.compile("=x'abc'=y'de')");
		ParseTree tree = new ParseTree(4);
		assertEquals(5, prog.parse("abcde".getBytes(), tree));
		assertEquals(5, tree.end());
		assertEquals(2, tree.count());
		assertEquals('x', tree.rule(0));
		assertEquals(0, tree.start(0));
		assertEquals('y', tree.rule(1));
		assertEquals(3, tree.start(1));
	}

	@Test
	public void basicMultiCaptureMismatch() {
		Program prog = Program.compile("=x'abc'=y'de')");
		ParseTree tree = new ParseTree(4);
		assertEquals(-4, prog.parse("abcDe".getBytes(), tree));
		assertEquals(0, tree.count());
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
		Program prog = Program.compile("[`a|`b|`c]");
		assertEquals(1, prog.parse("a"));
		assertEquals(1, prog.parse("b"));
		assertEquals(1, prog.parse("c"));
	}

	@Test
	public void basicStaircaseMismatch() {
		Program prog = Program.compile("[`a|`b|`c]");
		assertEquals(-1, prog.parse("d"));
	}

	@Test
	public void modestStaircaseLoopingMatch() {
		Program prog = Program.compile("[2`a|1`b|3`c]");
		assertEquals(2, prog.parse("aa"));
		assertEquals(1, prog.parse("b"));
		assertEquals(3, prog.parse("ccc"));
		// but also
		assertEquals(1, prog.parse("bb"));
	}

	@Test
	public void modestStaircaseLoopingMismatch() {
		Program prog = Program.compile("[2`a|1`b|3`c]");
		assertEquals(-1, prog.parse(""));
		assertEquals(-1, prog.parse("a"));
		assertEquals(-3, prog.parse("cc"));
	}

	@Test
	public void basicOptionMatch() {
		Program prog = Program.compile("`a(?'bc')`d");
		assertEquals(4, prog.parse("abcd"));
	}

	@Test
	public void basicOptionNoMatch() {
		Program prog = Program.compile("`a(?'bc')`d");
		assertEquals(2, prog.parse("ad"));
	}
}
