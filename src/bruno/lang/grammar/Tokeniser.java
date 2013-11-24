package bruno.lang.grammar;

import static bruno.lang.grammar.Grammar.star;
import static bruno.lang.grammar.Grammar.whitespace;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

import bruno.lang.grammar.Grammar.Rule;

public final class Tokeniser {

	private final Grammar grammar;

	public Tokeniser(Grammar grammar) {
		super();
		this.grammar = grammar;
	}

	public Tokens tokenise(String start, byte[] input) {
		Rule r = grammar.rule(start.intern());
		Tokens tokens = new Tokens(8000);
		int t = tokenise(r, ByteBuffer.wrap(input), 0, GOBBLE_WHITESPACE, tokens);
		//TODO verify and visualize errors
		return tokens;
	}
	
	private static int tokenise(Rule rule, ByteBuffer input, int position, Rule separator, Tokens tokens) {
		switch (rule.type) {
		case CHARACTER:
			return character(rule, input, position);
		case TERMINAL:
			return terminal(rule, input, position); 
		case TOKEN:
			return token(rule, input, position, tokens);
		case ITERATION:
			return iteration(rule, input, position, separator, tokens);
		case SEQUENCE:
			return sequence(rule, input, position, separator, tokens);
		case SELECTION:
			return selection(rule, input, position, separator, tokens);
		case CAPTURE:
			return capture(rule, input, position, separator, tokens);
		default:
			throw new IllegalArgumentException("`"+rule+"` has no proper type: "+rule.type);
		}
	}

	private static int character(Rule rule, ByteBuffer input, int position) {
		if (position >= input.limit())
			return -1;
		byte c = input.get(position);
		if (rule.character != c) {
			return -1;
		}
		return position+1;
	}

	private static int capture(Rule rule, ByteBuffer input, int position,
			Rule separator, Tokens tokens) {
		tokens.push(rule, position);
		int end = tokenise(rule.elements[0], input, position, separator, tokens);
		if (end > position) {
			tokens.done(end);
		} else {
			tokens.pop();
		}
		return end;
	}

	private static int selection(Rule rule, ByteBuffer input, int position,
			Rule separator, Tokens tokens) {
		for (Rule r : rule.elements) {
			int end = tokenise(r, input, position, separator, tokens);
			if (end >= 0) {
				return end;
			}
		}
		return -1;
	}

	private static int sequence(Rule rule, ByteBuffer input, int position,
			Rule separator, Tokens tokens) {
		int end = position;
		for (int i = 0; i < rule.elements.length; i++) {
			end = tokenise(separator, input, end, NOTHING, tokens);
			Rule r = rule.elements[i];
			int endPosition = tokenise(r, input, end, separator, tokens);
			if (endPosition == -1) {
				return -1;
			}
			end = endPosition;
		}
		return end;
	}

	private static int iteration(Rule rule, ByteBuffer input, int position,
			Rule separator, Tokens tokens) {
		int end = position;
		int c = 0;
		while (c < rule.occur.max) {
			int endPosition = tokenise(rule.elements[0], input, end, separator, tokens);
			if (endPosition == -1) {
				if (c < rule.occur.min) {
					return -1;
				}
				return end;
			} else {
				end = endPosition;
				c++;
			}
		}
		return end;
	}

	private static int token(Rule rule, ByteBuffer input, int position, Tokens tokens) {
		return tokenise(rule.elements[0], input, position, NOTHING, tokens);
	}

	private static int terminal(Rule rule, ByteBuffer input, int position) {
		if (position >= input.limit())
			return -1;
		byte c = input.get(position);
		if (!rule.terminal.matches(c)) {
			return -1;
		}
		if (c >= 0)
			return position+1;
		while (input.get(++position) < 0) { ; }
		return position;
	}
	
	static final Rule GOBBLE_WHITESPACE = Rule.terminal(whitespace).occur(star);
	static final Rule NOTHING = Rule.character('$').occur(Grammar.never);
	
	public static Tokens tokenise(String filename) throws IOException {
		Tokeniser t = new Tokeniser(BNF.GRAMMAR);
		return t.tokenise("grammar", readFile(filename));
	}

	static byte[] readFile(String path) throws IOException {
		return Files.readAllBytes(Paths.get(path));
	}

}
