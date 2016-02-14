package alma.lang;

import static alma.lang.Program.desugar;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestProgram {


	@Test
	public void loopingSuffixToBlock() {
		assertDesugars(" 'xy'? ", "(?'xy') ");
		assertDesugars(" 'xyz'* ", "(*'xyz') ");
		assertDesugars(" <'xy'+ ", "(+<'xy') ");
		assertDesugars(" 'xy'>5 ", "(5'xy'>) ");
	}

	@Test
	public void loopingDoubleSuffixToBlock() {
		assertDesugars(" 'xy'5+ ", "(5+'xy') ");
		assertDesugars(" 'xyz'5* ", "(5*'xyz') ");
	}

	@Test
	public void noLoopingDoubleSuffixNotToBlock() {
		assertDesugars("'xy'5+ ", "'xy'5+ ");
		assertDesugars(" 'xyz'5*", " 'xyz'5*");
	}

	@Test
	public void loopingRangeSuffixToBlock() {
		assertDesugars(" 'xy'*1-4 ", "(*1-4'xy') ");
	}

	@Test
	public void twiceLoopingSuffixToBlock() {
		assertDesugars(" 'x'+ 'y'* ", "(+'x')(*'y') ");
	}

	@Test
	public void doubleLoopingSuffixOnBlockIntoBlock() {
		assertDesugars(" ('x')1+ ", "(1+'x') ");
		assertDesugars(" ['x']1+ ", "[1+'x'] ");
	}

	@Test
	public void recordingAssignmentWithBlockToBlock() {
		assertDesugars("foo = ( 'xzy')", " (=foo 'xzy')");
		assertDesugars("foo = \t( 'xzy')", " (=foo 'xzy')");
	}

	@Test
	public void recordingAssignmentWithRecoveryBlockToBlock() {
		assertDesugars("foo = ['xzy']", " [=foo 'xzy']");
		assertDesugars("foo = \t['xzy']", " [=foo 'xzy']");
	}

	@Test
	public void recordingAssignmentWithoutBlockToBlock() {
		assertDesugars("foo = 'xzy' ", "(=foo 'xzy')");
		assertDesugars("foo = 'xzy'\n", "(=foo 'xzy')");
	}

	@Test
	public void recordingAssignmentsWithoutBlockToBlock() {
		assertDesugars("foo = 'xzy'\nbar = 'abc'\n", "(=foo 'xzy')(=bar 'abc')");
	}

	@Test
	public void moderateExample() {
		assertDesugars("expr = form (, form)*  ", "(=expr form(*, form) )");
		assertDesugars("expr = form [, form]*  ", "(=expr form[*, form] )");
	}

	@Test
	public void nestedExample() {
		assertDesugars("expr = 'x' ( (form,)+ x)* \n", "(=expr 'x'(*(+form,) x) )");
	}

	private static void assertDesugars(String before, String after) {
		assertEquals(after, new String(desugar(before.getBytes())));
	}
}
