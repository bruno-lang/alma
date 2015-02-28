package bruno.lang.grammar.print;

import java.io.PrintStream;

import bruno.lang.grammar.Grammar;
import bruno.lang.grammar.Grammar.Rule;

public class RDGPrinter {

	private final PrintStream out;

	public RDGPrinter(PrintStream out) {
		super();
		this.out = out;
	}

	public void print(Grammar g) {
		for (Rule rule : g) {
			out.print(rule.name);
			out.print(" ::= ");
			print(rule.elements[0]);
			out.println();
		}
	}

	private void print(Rule rule) {
		switch (rule.type) {
		case LITERAL:
			out.print(rule.toString());
			return;
		case CHARACTER_SET: 
			out.print("[A-Z]"); //TODO print real terminal
			return;
		case CAPTURE:
			// TODO how to visualize the capture name? use name "ws" and add named rules for , and so on
			out.print(rule.name);
			return;
		case REPETITION:
			out.print('(');
			print(rule.elements[0]);
			out.append(')').append(rule.occur.toString());
			return;
		case SEQUENCE:
			out.print('(');
			for (int i = 0; i < rule.elements.length; i++) {
				out.print(' ');
				print(rule.elements[i]);
			}
			out.print(')');
			return;
		case ALTERNATIVES:
			out.print('(');
			for (int i = 0; i < rule.elements.length; i++) {
				if (i > 0 ) {
					out.print(" | ");
				}
				print(rule.elements[i]);
			}
			out.print(')');
			return;
		case PATTERN:
		case LOOKAHEAD:
		case FILL:
		default:
			out.print("'???'");
		}
		
	}
}
