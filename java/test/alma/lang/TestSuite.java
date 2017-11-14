package alma.lang;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith ( Suite.class )
@SuiteClasses ( { TestProgram.class, TestParser.class, TestLex.class } )
public class TestSuite {

}
