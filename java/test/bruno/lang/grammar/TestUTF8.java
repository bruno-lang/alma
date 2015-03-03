package bruno.lang.grammar;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestUTF8 {

	@Test
	public void that4ByteCodePointCalculationIsCorrect() {
		int cp = UTF8.codePoint((byte) 0b11110000, (byte) 0b10100100, (byte) 0b10101101, (byte) 0b10100010);
		assertEquals("24b62", String.format("%05x", cp));
	}
	
	@Test
	public void that3ByteCodePointCalculationIsCorrect() {
		int cp = UTF8.codePoint((byte) 0b11100010, (byte) 0b10000010, (byte) 0b10101100);
		assertEquals("20ac", String.format("%04x", cp));
	}
	
	@Test
	public void that2ByteCodePointCalculationIsCorrect() {
		int cp = UTF8.codePoint((byte) 0b11000010, (byte) 0b10100010);
		assertEquals("00a2", String.format("%04x", cp));
	}
	
	@Test
	public void that1ByteCodePointCalculationIsCorrect() {
		int cp = UTF8.codePoint((byte) 0b00100100);
		assertEquals("0024", String.format("%04x", cp));
	}
}
