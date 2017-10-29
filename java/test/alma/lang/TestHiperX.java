package alma.lang;

import static alma.lang.Parser.mismatch;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TestHiperX {

	@Test
	public void matchNumberExamples() {
		// dates
		assertFullMatch("####/##/##", "2017/10/24");
		assertFullMatch("####{-/}##{-/}##", "2017/10/24");
		assertFullMatch("####{-/}##{-/}##", "2017-10-24");

		// time
		assertFullMatch("##:##:##", "12:35:45");
		assertFullMatch("##{.:}##{.:}##", "12:35:45");
		assertFullMatch("##{.:}##{.:}##", "12.35.45");

		// integers
		assertFullMatch("#+", "1");
		assertFullMatch("#+", "12");
		assertFullMatch("#+", "12345678900543");
		// with dividers
		assertFullMatch("#+[,###]+", "12");
		assertFullMatch("#+[,###]+", "12000");
		assertFullMatch("#+[,###]+", "12,000");
		assertFullMatch("#+[,###]+", "12,345,456");

		// floating point
		assertFullMatch("#+[.#+]", "1");
		assertFullMatch("#+[.#+]", "1.0");
		assertFullMatch("#+[.#+]", "13.45");
	}

	@Test
	public void matchQuotedStrings() {
		assertFullMatch("\"~\"", "\"abcd\"");
		assertFullMatch("\"\"\"~(\"\"\")", "\"\"\"ab\"c\"d\"\"\"");
		// or single quoted for better readability of the example
		assertFullMatch("'~'", "'abcd'");
		assertFullMatch("'''~(''')", "'''ab'c'd'''");
		// also by using exclusive sets
		assertFullMatch("'{^'}+'", "'abcd and d'");
	}

	@Test
	public void matchIdentifiers() {
		assertFullMatch("{a-z}+[{-_}{a-zA-Z0-9}+]+", "a");
		assertFullMatch("{a-z}+[{-_}{a-zA-Z0-9}+]+", "a-b");
		assertFullMatch("{a-z}+[{-_}{a-zA-Z0-9}+]+", "aa_b0");
		assertFullMatch("{a-z}+[[{-_}]{a-zA-Z0-9}+]+", "a");
		assertFullMatch("{a-z}+[[{-_}]{a-zA-Z0-9}+]+", "aCamalCase");
	}

	@Test
	public void matchLiteral() {
		assertFullMatch("abcdefäöå", "abcdefäöå");
	}

	@Test
	public void matchLiteralPlus() {
		assertFullMatch("a+", "a");
		assertFullMatch("a+", "aa");
		assertFullMatch("a+", "aaa");
		assertFullMatch("a+b", "ab");
		assertFullMatch("a+b", "aab");
	}

	@Test
	public void mismatchLiteralPlus() {
		assertNoMatchAt("a+x", "ay", 1);
		assertNoMatchAt("a+x", "aay", 2);
		assertNoMatchAt("a+x", "aaay", 3);
	}

	@Test
	public void matchLiteralAndSet() {
		assertFullMatch("ab{cd}", "abc");
		assertFullMatch("ab{cd}", "abd");
	}

	@Test
	public void matchLiteralAndSetPlus() {
		assertFullMatch("ab{cd}+", "abcd");
		assertFullMatch("ab{cde}+", "abedc");
		assertFullMatch("ab{cd}+e", "abde");
	}

	@Test
	public void matchExclusiveSet() {
		assertFullMatch("{^cd}", "a");
		assertFullMatch("{^cd}", "b");
		assertFullMatch("{^cd}", "e");
		assertFullMatch("{^c}", "e");
		assertFullMatch("{^-}", "x");
	}

	@Test
	public void matchExclusiveSetRange() {
		assertFullMatch("{^b-fgx-y}", "-");
		assertFullMatch("{^b-fgx-y}", "a");
		assertFullMatch("{^b-fgx-y}", "h");
		assertFullMatch("{^b-fgx-y}", "i");
		assertFullMatch("{^b-fgx-y}", "z");
		assertFullMatch("{^b-fgx-y}", "w");
		assertFullMatch("{^b-fgx-y}", "*");
		assertFullMatch("{^b-fgx-y}", "^");
		assertFullMatch("{^b-fgx-y}", "'");
	}

	@Test
	public void matchExclusiveSetRangePlus() {
		assertFullMatch("{^b-fgx-y }+", "ahijklmnopqrstuvwz");
		assertFullMatch("{^b-fgx-y }+", "1234567890");
		assertFullMatch("{^b-fgx-y }+", "+-*/~^'&|@#$");
		assertFullMatch("{^b-fgx-y }+", "<>[]{}");
	}

	@Test
	public void matchSetRangeSquareBrackets() {
		assertFullMatch("{[}{^]}+{]}", "[foo]");
		assertFullMatch("{[}{^]}+{]}", "[1]");
		assertFullMatch("{[}{^]}+{]}", "[x++]");
	}

	@Test
	public void mismatchExclusiveSetRange() {
		assertNoMatchAt("{^b-fgx-y}", "b", 0);
		assertNoMatchAt("{^b-fgx-y}", "c", 0);
		assertNoMatchAt("{^b-fgx-y}", "f", 0);
		assertNoMatchAt("{^b-fgx-y}", "g", 0);
		assertNoMatchAt("{^b-fgx-y}", "x", 0);
		assertNoMatchAt("{^b-fgx-y}", "y", 0);
	}

	@Test
	public void mismatchExclusiveSet() {
		assertNoMatchAt("{^cde}", "c", 0);
		assertNoMatchAt("{^cde}", "d", 0);
		assertNoMatchAt("{^cde}", "e", 0);
		assertNoMatchAt("{^c}", "c", 0);
		assertNoMatchAt("{^-}", "-", 0);
	}

	@Test
	public void matchOption() {
		assertFullMatch("[abc]", "abc");
		assertMatchUpTo("[abc]", "xbc", 0);
		assertMatchUpTo("[abc]", "abx", 0);
		assertFullMatch("[abc]x", "x");
		assertFullMatch("[abc]x", "abcx");
	}

	@Test
	public void matchOptionPlus() {
		assertFullMatch("[ab]+c", "abc");
		assertFullMatch("[ab]+c", "ababc");
		assertFullMatch("[ab]+c", "c");
	}

	@Test
	public void mismatchOptionPlus() {
		assertNoMatchAt("[ab]+c", "abe", 2);
		assertNoMatchAt("[ab]+c", "e", 0);
	}

	@Test
	public void matchGroup() {
		assertFullMatch("(ab)", "ab");
		assertFullMatch("(abc)", "abc");
		assertFullMatch("(#.#)", "1.1");
	}

	@Test
	public void mismatchGroup() {
		assertNoMatchAt("(ab)", "ax", 1);
		assertNoMatchAt("(abc)", "abx", 2);
		assertNoMatchAt("(abc)", "xbc", 0);
	}

	@Test
	public void matchGroupNested() {
		assertFullMatch("(a(b(c)))", "abc");
		assertFullMatch("(ax(bx(cx)))", "axbxcx");
	}

	@Test
	public void matchGroupNestedPlus() {
		assertFullMatch("(a(b(c)+)+)+", "abc");
		assertFullMatch("(a(b(c)+)+)+", "abcabc");
		assertFullMatch("(a(b(c)+)+)+", "abccbc");
		assertFullMatch("(a(b+(cd)+)+)+", "abbcdcdabcd");
	}

	@Test
	public void matchSetRange() {
		assertFullMatch("{a-z}", "g");
		assertFullMatch("{a-z}", "a");
		assertFullMatch("{a-z}", "z");
	}

	@Test
	public void matchSetRangePlus() {
		assertFullMatch("{a-z}+", "abc");
		assertFullMatch("{A-Za-z}+", "zA");
		assertFullMatch("{A-Za-z0-9}+", "bD7aZ");
		assertFullMatch("{A-Za-z<0-9>}+", "<bD7aZ>");
		assertFullMatch("{a-zA-Z }+.", "The quick brown fox jumps over the lazy dog.");
	}

	@Test
	public void mismatchSetRange() {
		assertNoMatchAt("{a-z}", "G", 0);
		assertNoMatchAt("{a-z}", "`", 0);
		assertNoMatchAt("{a-z}", "{", 0);
		assertNoMatchAt("{a-df-z}", "e", 0);
		assertNoMatchAt("{a-df-z}+X", "aeX", 1);
	}
	@Test
	public void mismatchLiteralAndSetPlus() {
		assertNoMatchAt("ab{cd}+", "abb", 2);
		assertNoMatchAt("ab{cd}+e", "abcb", 3);
		assertNoMatchAt("ab{cd}+e", "abdcb", 4);
	}

	@Test
	public void matchDigitPattern() {
		assertFullMatch("#", "0");
		assertFullMatch("#", "9");
		assertFullMatch("#", "5");
		assertFullMatch("##:##:##", "12:59:45");
		assertFullMatch("####/##/##", "2010/12/31");
	}

	@Test
	public void matchDigitPlus() {
		assertFullMatch("#+", "1");
		assertFullMatch("#+", "12");
		assertFullMatch("#+", "123");
		assertFullMatch("#+", "1234567890");
		assertFullMatch("x#+y", "x1234567890y");
	}

	@Test
	public void mismatchDigitPlus() {
		assertNoMatchAt("#+", "a", 0);
		assertNoMatchAt("#+x", "0y", 1);
		assertNoMatchAt("#+x", "01y", 2);
	}

	@Test
	public void mismatchDigitPattern() {
		assertNoMatchAt("#", "a", 0);
		assertNoMatchAt("x#", "xa", 1);
		assertNoMatchAt("#y", "0t", 1);
	}

	@Test
	public void matchLetterPattern() {
		assertFullMatch("@", "a");
		assertFullMatch("@", "A");
		assertFullMatch("@", "z");
		assertFullMatch("@", "Z");
		assertFullMatch("@", "F");
		assertFullMatch("@", "g");
		assertFullMatch("@@@", "Ace");
	}

	@Test
	public void misatchLetterPattern() {
		assertNoMatchAt("@", "@", 0);
		assertNoMatchAt("@", "[", 0);
		assertNoMatchAt("@", "`", 0);
		assertNoMatchAt("@", "{", 0);
		assertNoMatchAt("@", "0", 0);
	}

	@Test
	public void matchFillLiteral() {
		assertFullMatch("a~b", "ab");
		assertFullMatch("a~b", "axb");
		assertFullMatch("a~b", "axxb");
		assertFullMatch("a~b", "axyzb");
	}

	@Test
	public void matchFillDigit() {
		assertFullMatch("a~#", "a0");
		assertFullMatch("a~#", "axx1");
		assertFullMatch("a~#", "ayyy9");
		assertFullMatch("a~#.#", "ayyy9.1");
	}

	@Test
	public void mismatchFillDigit() {
		assertNoMatchAt("a~#", "aa", 1);
	}

	@Test
	public void matchFillSet() {
		assertFullMatch("a~{b-z}", "a0b");
		assertFullMatch("a~{b-z}", "a11z");
	}

	@Test
	public void mismatchFill() {
		assertNoMatchAt("a~b", "ax", 1);
		assertNoMatchAt("a~b", "axy", 1);
	}

	@Test
	public void matchFillGroup() {
		assertFullMatch("a~(bc)", "abc");
		assertFullMatch("a~(bc)", "abdbc");
		assertFullMatch("a~(bc)", "acxbc");
	}

	@Test
	public void matchNestedOption() {
		assertFullMatch("a[b[c]]d", "ad");
		assertFullMatch("a[b[c]]d", "abd");
		assertFullMatch("a[b[c]]d", "abcd");
	}

	@Test
	public void matchNestedOptionPlus() {
		assertFullMatch("a[b+[c]+]d", "ad");
		assertFullMatch("a[b+[c]+]d", "abd");
		assertFullMatch("a[b+[c]+]d", "abcd");
		assertFullMatch("a[b+[c]+]d", "abbd");
		assertFullMatch("a[b+[c]+]d", "abbcd");
		assertFullMatch("a[b+[c]+]d", "abccd");
		assertFullMatch("a[b+[c]+]d", "abbccd");
	}

	@Test
	public void matchNonWhitespaceSetPlus() {
		assertFullMatch("^+", "azAZ:-,;*()[]{}?!<>|&%@");
	}

	@Test
	public void mismatchNonWhitespaceSet() {
		assertNoMatchAt("^", " ", 0);
		assertNoMatchAt("^", "\n", 0);
		assertNoMatchAt("^", "\t", 0);
		assertNoMatchAt("^", "\r", 0);
	}

	@Test
	public void matchAnyByteSet() {
		assertFullMatch("______", "aAzZ09");
		assertFullMatch("______", "<>[]{}");
		assertFullMatch("______", "!?-:.,");
		assertFullMatch("______", "+-*/^=");
		assertFullMatch("______", "&|%#@~");
		assertFullMatch("____",   " \t\n\r");
	}

	private static void assertNoMatchAt(String pattern, String data, int pos) {
		int[] res = match("`"+pattern+"`", data);
		assertEquals(mismatch(pos), res[1]);
	}

	private static void assertMatchUpTo(String pattern, String data, int pos) {
		int[] res = match("`"+pattern+"`", data);
		assertEquals(pos, res[1]);
	}


	private static void assertFullMatch(String pattern, String data) {
		String mark = " "; // we add this to both pattern and data to make sure we have a full match even when data is processed before end of pattern is reached
		pattern = "`"+pattern+mark+"`";
		data += mark;
		int[] res = match(pattern, data);
		assertTrue(res[0] > 0);
		assertEquals(data.getBytes(UTF_8).length, res[1]);
		assertEquals(pattern+data, pattern.getBytes(UTF_8).length-1, res[0]); // -1 because of the end mark ` that is not processed
		// if a pattern end with groups or repetitions their positions might not be passed when data is fully processed.
		// how do we know the full pattern matched? we add a literal at the end to both pattern and data (see mark above)
	}

	private static int[] match(String pattern, String data) {
		long res = HiperX.match(pattern.getBytes(UTF_8), 0, data.getBytes(UTF_8), 0);
		return new int[] { (int)(res >> 32), (int)res };
	}
}
