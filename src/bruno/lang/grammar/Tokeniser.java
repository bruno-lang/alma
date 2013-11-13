package bruno.lang.grammar;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import bruno.lang.grammar.Grammar.Rule;

public class Tokeniser {

	private final Grammar grammar;

	public Tokeniser(Grammar grammar) {
		super();
		this.grammar = grammar;
	}
	
	public Tokens tokenise(String start, String input) {
		Rule r = grammar.rule(start.intern());
		Tokens tokens = new Tokens(8000);
		int t = tokenise(r, ByteBuffer.wrap(input.getBytes()), 0, true, tokens);
		return tokens;
	}
	
	private static int tokenise(Rule rule, ByteBuffer input, int position, boolean gobbleWhitespace, Tokens tokens) {
		switch (rule.type) {
		case SYMBOL:
			return symbol(rule, input, position); 
		case TOKEN:
			return token(rule, input, position, tokens);
		case ITERATION:
			return iteration(rule, input, position, gobbleWhitespace, tokens);
		case SEQUENCE:
			return sequence(rule, input, position, gobbleWhitespace, tokens);
		case SELECTION:
			return selection(rule, input, position, gobbleWhitespace, tokens);
		case CAPTURE:
			tokens.push(rule, position);
			int end = tokenise(rule.elements[0], input, position, gobbleWhitespace, tokens);
			if (end >= 0) {
				tokens.done(end);
				return end;
			}
			tokens.pop();
			return -1;
		default:
			throw new IllegalArgumentException("`"+rule+"` has no proper type: "+rule.type);
		}
	}

	private static int selection(Rule rule, ByteBuffer input, int position,
			boolean gobbleWhitespace, Tokens tokens) {
		for (Rule r : rule.elements) {
			int endPosition = tokenise(r, input, position, gobbleWhitespace, tokens);
			if (endPosition >= 0) {
				return endPosition;
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
		int endPosition = tokenise(rule.elements[0], input, position, false, tokens);
		if (endPosition >= 0) {
			return endPosition;
		}
		return -1;
	}

	private static int symbol(Rule rule, ByteBuffer input, int position) {
		if (position >= input.limit())
			return -1;
		byte c = input.get(position);
		if (!rule.symbol.matches(c)) {
			return -1;
		}
		return position+1;
	}
	
	private static int scanWhitespace(ByteBuffer input, int index, boolean gobbleWhitespace) {
		while (gobbleWhitespace && index < input.limit() && Character.isWhitespace(input.get(index))) {
			index++;
		}
		return index;
	}
	
	public static Tokens tokenise(String filename) throws IOException {
		Tokeniser t = new Tokeniser(BNF.GRAMMAR);
		String file = readFile(filename, Charset.forName("UTF-8"));
		return t.tokenise("grammar", file);
	}

	static String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return encoding.decode(ByteBuffer.wrap(encoded)).toString();
	}

	public static void main(String[] args) throws IOException {
		Tokeniser t = new Tokeniser(BNF.GRAMMAR);
		String file = readFile("etc/grammar.grammar", Charset.forName("UTF-8"));
		Tokens root = t.tokenise("grammar", file);
		System.out.println(root);
		System.out.println(file.length() +" / "+ root.end());
		
	}
}
