package bruno.lang.grammar;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Test;

import bruno.lang.grammar.print.Printer;
import bruno.lang.grammar.print.Processor;

public class TestAlmaInterpreter {

	@Test
	public void prelude() throws IOException {
		byte[] code = Files.readAllBytes(Paths.get("examples/prelude.alma"));
		Grammar prelude = AlmaInterpreter.make(code);
		System.out.println(prelude);
	}
	
	@Test
	public void json() throws IOException {
		byte[] prelude = Files.readAllBytes(Paths.get("examples/prelude.alma"));
		byte[] code = Files.readAllBytes(Paths.get("examples/json.alma"));
		Grammar json = AlmaInterpreter.make(prelude, code);
		System.out.println(json);
	}
	
	@Test
	public void bruno() throws IOException {
		byte[] prelude = Files.readAllBytes(Paths.get("examples/prelude.alma"));
		byte[] code = Files.readAllBytes(Paths.get("examples/bruno.alma"));
		Grammar bruno = AlmaInterpreter.make(prelude, code);
		Processor printer = Printer.rulePrinter(System.out);
		printer.process(Parsed.parse("etc/example.lib", bruno, "library"));
		printer.process(Parsed.parse("etc/example.ast", bruno, "expr"));		
		printer.process(Parsed.parse("etc/example.mod", bruno, "module"));
	}
	
	@Test
	public void brunoGrammar() throws IOException {
		byte[] prelude = Files.readAllBytes(Paths.get("examples/prelude.alma"));
		byte[] code = Files.readAllBytes(Paths.get("examples/bruno.alma"));
		Grammar bruno = AlmaInterpreter.make(prelude, code);
		System.out.println(bruno);
	}
}
