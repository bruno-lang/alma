package alma.lang;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

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
	public void mixedStaircaseLoopingMatch() {
		Program prog = Program.compile("[2`a|1`b|3`c]");
		assertEquals(2, prog.parse("aa"));
		assertEquals(1, prog.parse("b"));
		assertEquals(3, prog.parse("ccc"));
	}

	@Test
	public void mixedStaircaseLoopingMismatch() {
		Program prog = Program.compile("[2`a|1`b|3`c]");
		assertEquals(-1, prog.parse( "a")); // too little
		assertEquals(-1, prog.parse("")); 	// nothing?
		assertEquals(-2, prog.parse("bb")); // too much
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
