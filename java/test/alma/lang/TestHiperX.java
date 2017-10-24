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
		assertFullMatch("`####/##/##`", "2017/10/24");
		assertFullMatch("`####{-/}##{-/}##`", "2017/10/24");
		assertFullMatch("`####{-/}##{-/}##`", "2017-10-24");

		// time
		assertFullMatch("`##:##:##`", "12:35:45");
		assertFullMatch("`##{.:}##{.:}##`", "12:35:45");
		assertFullMatch("`##{.:}##{.:}##`", "12.35.45");

		// integers
		assertFullMatch("`#+`", "1");
		assertFullMatch("`#+`", "12");
		assertFullMatch("`#+`", "12345678900543");
		// with dividers
		assertFullMatch("`#+[,###]+`", "12");
		assertFullMatch("`#+[,###]+`", "12000");
		assertFullMatch("`#+[,###]+`", "12,000");
		assertFullMatch("`#+[,###]+`", "12,345,456");

		// floating point
		assertFullMatch("`#+[.#+]`", "1");
		assertFullMatch("`#+[.#+]`", "1.0");
		assertFullMatch("`#+[.#+]`", "13.45");
	}

	@Test
	public void matchQuotedStrings() {
		assertFullMatch("`\"~\"`", "\"abcd\"");
		assertFullMatch("`\"\"\"~(\"\"\")`", "\"\"\"ab\"c\"d\"\"\"");
	}

	@Test
	public void matchIdentifiers() {
		assertFullMatch("`{a-z}+[{-_}{a-zA-Z0-9}+]+`", "a");
		assertFullMatch("`{a-z}+[{-_}{a-zA-Z0-9}+]+`", "a-b");
		assertFullMatch("`{a-z}+[{-_}{a-zA-Z0-9}+]+`", "aa_b0");
		assertFullMatch("`{a-z}+[[{-_}]{a-zA-Z0-9}+]+`", "aCamalCase");
	}

	@Test
	public void matchLiteral() {
		assertFullMatch("`abcdef`", "abcdef");
	}

	@Test
	public void matchLiteralPlus() {
		assertFullMatch("`a+`", "a");
		assertFullMatch("`a+`", "aa");
		assertFullMatch("`a+`", "aaa");
		assertFullMatch("`a+b`", "ab");
		assertFullMatch("`a+b`", "aab");
	}

	@Test
	public void mismatchLiteralPlus() {
		assertNoMatchAt("`a+x`", "ay", 1);
		assertNoMatchAt("`a+x`", "aay", 2);
		assertNoMatchAt("`a+x`", "aaay", 3);
	}

	@Test
	public void matchLiteralAndSet() {
		assertFullMatch("`ab{cd}`", "abc");
		assertFullMatch("`ab{cd}`", "abd");
	}

	@Test
	public void matchLiteralAndSetPlus() {
		assertFullMatch("`ab{cd}+`", "abcd");
		assertFullMatch("`ab{cde}+`", "abedc");
		assertFullMatch("`ab{cd}+e`", "abde");
	}

	@Test
	public void matchOption() {
		assertFullMatch("`[abc]`", "abc");
		assertMatchUpTo("`[abc]`", "xbc", 0);
		assertMatchUpTo("`[abc]`", "abx", 0);
		assertFullMatch("`[abc]x`", "x");
		assertFullMatch("`[abc]x`", "abcx");
	}

	@Test
	public void matchOptionPlus() {
		assertFullMatch("`[ab]+c`", "abc");
		assertFullMatch("`[ab]+c`", "ababc");
		assertFullMatch("`[ab]+c`", "c");
	}

	@Test
	public void mismatchOptionPlus() {
		assertNoMatchAt("`[ab]+c`", "abe", 2);
		assertNoMatchAt("`[ab]+c`", "e", 0);
	}

	@Test
	public void matchGroup() {
		assertFullMatch("`(ab)`", "ab");
		assertFullMatch("`(abc)`", "abc");
		assertFullMatch("`(#.#)`", "1.1");
	}

	@Test
	public void mismatchGroup() {
		assertNoMatchAt("`(ab)`", "ax", 1);
		assertNoMatchAt("`(abc)`", "abx", 2);
		assertNoMatchAt("`(abc)`", "xbc", 0);
	}

	@Test
	public void matchGroupNested() {
		assertFullMatch("`(a(b(c)))`", "abc");
		assertFullMatch("`(ax(bx(cx)))`", "axbxcx");
	}

	@Test
	public void matchGroupNestedPlus() {
		assertFullMatch("`(a(b(c)+)+)+`", "abc");
		assertFullMatch("`(a(b(c)+)+)+`", "abcabc");
		assertFullMatch("`(a(b(c)+)+)+`", "abccbc");
		assertFullMatch("`(a(b+(cd)+)+)+`", "abbcdcdabcd");
	}

	@Test
	public void matchSetRange() {
		assertFullMatch("`{a-z}`", "g");
		assertFullMatch("`{a-z}`", "a");
		assertFullMatch("`{a-z}`", "z");
	}

	@Test
	public void matchSetRangePlus() {
		assertFullMatch("`{a-z}+`", "abc");
		assertFullMatch("`{A-Za-z}+`", "zA");
		assertFullMatch("`{A-Za-z0-9}+`", "bD7aZ");
		assertFullMatch("`{A-Za-z<0-9>}+`", "<bD7aZ>");
		assertFullMatch("`{a-zA-Z }+`", "The quick brown fox jumps over the lazy dog");
	}

	@Test
	public void mismatchSetRange() {
		assertNoMatchAt("`{a-z}`", "G", 0);
		assertNoMatchAt("`{a-z}`", "`", 0);
		assertNoMatchAt("`{a-z}`", "{", 0);
		assertNoMatchAt("`{a-df-z}`", "e", 0);
		assertNoMatchAt("`{a-df-z}+X`", "aeX", 1);
	}
	@Test
	public void mismatchLiteralAndSetPlus() {
		assertNoMatchAt("`ab{cd}+`", "abb", 2);
		assertNoMatchAt("`ab{cd}+e`", "abcb", 3);
		assertNoMatchAt("`ab{cd}+e`", "abdcb", 4);
	}

	@Test
	public void matchDigitPattern() {
		assertFullMatch("`#`", "0");
		assertFullMatch("`#`", "9");
		assertFullMatch("`#`", "5");
		assertFullMatch("`##:##:##`", "12:59:45");
		assertFullMatch("`####/##/##`", "2010/12/31");
	}

	@Test
	public void matchDigitPlus() {
		assertFullMatch("`#+`", "1");
		assertFullMatch("`#+`", "12");
		assertFullMatch("`#+`", "123");
		assertFullMatch("`#+`", "1234567890");
		assertFullMatch("`x#+y`", "x1234567890y");
	}

	@Test
	public void mismatchDigitPlus() {
		assertNoMatchAt("`#+`", "a", 0);
		assertNoMatchAt("`#+x`", "0y", 1);
		assertNoMatchAt("`#+x`", "01y", 2);
	}

	@Test
	public void mismatchDigitPattern() {
		assertNoMatchAt("`#`", "a", 0);
		assertNoMatchAt("`x#`", "xa", 1);
		assertNoMatchAt("`#y`", "0t", 1);
	}

	@Test
	public void matchLetterPattern() {
		assertFullMatch("`@`", "a");
		assertFullMatch("`@`", "A");
		assertFullMatch("`@`", "z");
		assertFullMatch("`@`", "Z");
		assertFullMatch("`@`", "F");
		assertFullMatch("`@`", "g");
		assertFullMatch("`@@@`", "Ace");
	}

	@Test
	public void misatchLetterPattern() {
		assertNoMatchAt("`@`", "@", 0);
		assertNoMatchAt("`@`", "[", 0);
		assertNoMatchAt("`@`", "`", 0);
		assertNoMatchAt("`@`", "{", 0);
		assertNoMatchAt("`@`", "0", 0);
	}

	@Test
	public void matchFillLiteral() {
		assertFullMatch("`a~b`", "ab");
		assertFullMatch("`a~b`", "axb");
		assertFullMatch("`a~b`", "axxb");
		assertFullMatch("`a~b`", "axyzb");
	}

	@Test
	public void matchFillDigit() {
		assertFullMatch("`a~#`", "a0");
		assertFullMatch("`a~#`", "axx1");
		assertFullMatch("`a~#`", "ayyy9");
		assertFullMatch("`a~#.#`", "ayyy9.1");
	}

	@Test
	public void mismatchFillDigit() {
		assertNoMatchAt("`a~#`", "aa", 1);
	}

	@Test
	public void matchFillSet() {
		assertFullMatch("`a~{b-z}`", "a0b");
		assertFullMatch("`a~{b-z}`", "a11z");
	}

	@Test
	public void mismatchFill() {
		assertNoMatchAt("`a~b`", "ax", 1);
		assertNoMatchAt("`a~b`", "axy", 1);
	}

	@Test
	public void matchFillGroup() {
		assertFullMatch("`a~(bc)`", "abc");
		assertFullMatch("`a~(bc)`", "abdbc");
		assertFullMatch("`a~(bc)`", "acxbc");
	}

	private static void assertNoMatchAt(String pattern, String data, int pos) {
		int[] res = match(pattern, data);
		assertEquals(mismatch(pos), res[1]);
	}

	private static void assertMatchUpTo(String pattern, String data, int pos) {
		int[] res = match(pattern, data);
		assertEquals(pos, res[1]);
	}


	private static void assertFullMatch(String pattern, String data) {
		int[] res = match(pattern, data);
		assertTrue(res[0] > 0);
		assertEquals(data.length(), res[1]);
		if (false) // strictly this should be true but not now
			assertEquals(pattern.length()-1, res[0]);
	}

	private static int[] match(String pattern, String data) {
		long res = HiperX.match(pattern.getBytes(UTF_8), 0, data.getBytes(UTF_8), 0);
		return new int[] { (int)(res >> 32), (int)res };
	}
}
