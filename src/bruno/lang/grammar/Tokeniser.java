package bruno.lang.grammar;

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
		int t = tokenise(r, ByteBuffer.wrap(input), 0, true, tokens);
		//TODO verify and visualize errors
		return tokens;
	}
	
	private static int tokenise(Rule rule, ByteBuffer input, int position, boolean gobbleWhitespace, Tokens tokens) {
		switch (rule.type) {
		case CHARACTER:
			return character(rule, input, position);
		case TERMINAL:
			return terminal(rule, input, position); 
		case TOKEN:
			return token(rule, input, position, tokens);
		case ITERATION:
			return iteration(rule, input, position, gobbleWhitespace, tokens);
		case SEQUENCE:
			return sequence(rule, input, position, gobbleWhitespace, tokens);
		case SELECTION:
			return selection(rule, input, position, gobbleWhitespace, tokens);
		case CAPTURE:
			return capture(rule, input, position, gobbleWhitespace, tokens);
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
			boolean gobbleWhitespace, Tokens tokens) {
		tokens.push(rule, position);
		int end = tokenise(rule.elements[0], input, position, gobbleWhitespace, tokens);
		if (end > position) {
			tokens.done(end);
		} else {
			tokens.pop();
		}
		return end;
	}

	private static int selection(Rule rule, ByteBuffer input, int position,
			boolean gobbleWhitespace, Tokens tokens) {
		for (Rule r : rule.elements) {
			int end = tokenise(r, input, position, gobbleWhitespace, tokens);
			if (end >= 0) {
				return end;
			}
		}
		return -1;
	}

	private static int sequence(Rule rule, ByteBuffer input, int position,
			boolean gobbleWhitespace, Tokens tokens) {
		int end = position;
		for (int i = 0; i < rule.elements.length; i++) {
			end = scanWhitespace(input, end, gobbleWhitespace);
			Rule r = rule.elements[i];
			int endPosition = tokenise(r, input, end, gobbleWhitespace || !r.tokenish, tokens);
			if (endPosition == -1) {
				return -1;
			}
			end = endPosition;
		}
		return end;
	}

	private static int iteration(Rule rule, ByteBuffer input, int position,
			boolean gobbleWhitespace, Tokens tokens) {
		int end = position;
		int c = 0;
		while (c < rule.occur.max) {
			int endPosition = tokenise(rule.elements[0], input, end, gobbleWhitespace, tokens);
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

	private static int token(Rule rule, ByteBuffer input, int position,
			Tokens tokens) {
		return tokenise(rule.elements[0], input, position, false, tokens);
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
	
	private static int scanWhitespace(ByteBuffer input, int index, boolean gobbleWhitespace) {
		while (gobbleWhitespace && index < input.limit() && Character.isWhitespace(input.get(index))) {
			index++;
		}
		return index;
	}
	
	public static Tokens tokenise(String filename) throws IOException {
		Tokeniser t = new Tokeniser(BNF.GRAMMAR);
		return t.tokenise("grammar", readFile(filename));
	}

	static byte[] readFile(String path) throws IOException {
		return Files.readAllBytes(Paths.get(path));
	}

}
