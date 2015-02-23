package bruno.lang.grammar;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Test;

public class TestAlmaInterpreter {

	@Test
	public void prelude() throws IOException {
		byte[] code = Files.readAllBytes(Paths.get("examples/prelude.alma"));
		Grammar prelude = AlmaInterpreter.interpret(code);
		System.out.println(prelude);
	}
	
	@Test
	public void json() throws IOException {
		byte[] prelude = Files.readAllBytes(Paths.get("examples/prelude.alma"));
		byte[] code = Files.readAllBytes(Paths.get("examples/json.alma"));
		Grammar json = AlmaInterpreter.interpret(prelude, code);
		System.out.println(json);
	}
}
