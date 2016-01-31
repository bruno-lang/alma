package alma.lang;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestVM {

	@Test
	public void simpleLiteralMatch() {
		VM vm = new VM("'abc'", "abc");
		assertEquals(3, vm.eval(0));
	}
	
	@Test
	public void simpleLiteralMismatch() {
		VM vm = new VM("'abc'", "abX");
		assertEquals(-3, vm.eval(0));
	}
	
	@Test
	public void simpleCharacterMatch() {
		VM vm = new VM("#x", "x");
		assertEquals(1, vm.eval(0));
	}
	
	@Test
	public void simpleCharacterMismatch() {
		VM vm = new VM("#x", "y");
		assertEquals(-1, vm.eval(0));
		
		vm = new VM("#x#y", "xA");
		assertEquals(-2, vm.eval(0));
	}
	
	@Test
	public void simpleIndentMatch() {
		VM vm = new VM(",", "\t ");
		assertEquals(2, vm.eval(0));
		
		vm = new VM(",", "X");
		assertEquals(0, vm.eval(0));
	}
	
	@Test
	public void simpleIndentMismatch() {
		VM vm = new VM(";", "X\t ");
		assertEquals(-1, vm.eval(0));
	}

	@Test
	public void simpleWhitespaceMatch() {
		VM vm = new VM(".", "\t \n\r");
		assertEquals(4, vm.eval(0));
		
		vm = new VM(".", "X");
		assertEquals(0, vm.eval(0));
	}
	
	@Test
	public void simpleWhitespaceMismatch() {
		VM vm = new VM(":", "X\n\t ");
		assertEquals(-1, vm.eval(0));
	}
	
	@Test
	public void simpleLinebreakMatch() {
		VM vm = new VM("!", "\t\n ");
		assertEquals(3, vm.eval(0));
		
		vm = new VM("!", "\t ");
		assertEquals(2, vm.eval(0));
	}
	
	@Test
	public void simpleLinebreakMismatch() {
		VM vm = new VM("!", "\t X");
		assertEquals(-3, vm.eval(0));
	}
	
	@Test
	public void simpleLookAheadMatch() {
		VM vm = new VM("'abc'>'def'", "abcdef");
		assertEquals(3, vm.eval(0));
	}
	
	@Test
	public void simpleLookAheadMismatch() {
		VM vm = new VM("'abc'>'def'", "abcdEf");
		assertEquals(-5, vm.eval(0));
	}
	
	@Test
	public void simpleOptionMatch() {
		VM vm = new VM("?'abc'", "abc");
		assertEquals(3, vm.eval(0));
	}
	
	@Test
	public void simpleOptionNoMatch() {
		VM vm = new VM("?'abc'", "aBc");
		assertEquals(0, vm.eval(0));
	}
	
	@Test
	public void basicLiteralSequenceMatch() {
		VM vm = new VM("'abc''def'", "abcdef");
		assertEquals(6, vm.eval(0));
	}
	
	@Test
	public void basicSpacedLiteralSequenceMatch() {
		VM vm = new VM("'abc','def'", "abc def");
		assertEquals(7, vm.eval(0));
	}
	
	@Test
	public void basicLiteralSequenceMismatch() {
		VM vm = new VM("'abc''def'", "abcdEf");
		assertEquals(-5, vm.eval(0));
	}
	
	@Test
	public void basicSpacedLiteralSequenceMismatch() {
		VM vm = new VM("'abc','def'", "abc dEf");
		assertEquals(-6, vm.eval(0));
	}
	
	@Test
	public void basicSubsequenceMatch() {
		VM vm = new VM("'abc'('def')", "abcdef");
		assertEquals(6, vm.eval(0));
	}
	
	@Test
	public void basicSubsequenceMismatch() {
		VM vm = new VM("'abc'('def')", "abcdEf");
		assertEquals(-5, vm.eval(0));
	}
	
	@Test
	public void basicStaircaseMatch() {
		String prog = "(|"+((char)0)+((char)7)+"#a)|"+((char)0)+((char)12)+"#b)#c)";
		assertEquals(1, new VM(prog, "a").eval(0));
		assertEquals(1, new VM(prog, "b").eval(0));
		assertEquals(1, new VM(prog, "c").eval(0));
	}
	
	@Test
	public void basicStaircaseMismatch() {
		String prog = "(|"+((char)0)+((char)7)+"#a)|"+((char)0)+((char)12)+"#b)#c)";
		assertEquals(-1, new VM(prog, "d").eval(0));
	}
	
	@Test
	public void basicOptionMatch() {
		VM vm = new VM("#a(?'bc')#d", "abcd");
		assertEquals(4, vm.eval(0));
	}
	
	@Test
	public void basicOptionNoMatch() {
		VM vm = new VM("#a(?'bc')#d", "ad");
		assertEquals(2, vm.eval(0)); // FIXME the problem is that PC in case of mismatch is not set after the closing )
	}
}
