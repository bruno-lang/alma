package bruno.lang.grammar;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import bruno.lang.grammar.Grammar.Rule;

public final class Tokeniser {

	public static Tokenised tokenise(String filename) throws IOException {
		Tokeniser t = new Tokeniser(BNF.GRAMMAR);
		RandomAccessFile aFile = new RandomAccessFile(filename, "r");
		FileChannel in = aFile.getChannel();
		MappedByteBuffer buffer = in.map(FileChannel.MapMode.READ_ONLY, 0, in.size());
		try {
			buffer.load();
			Tokens tokens = t.tokenise("grammar", buffer);
			return new Tokenised(buffer, tokens);
		} finally {
			buffer.clear();
			in.close();
			aFile.close();
		}
	}
	
	private final Grammar grammar;

	public Tokeniser(Grammar grammar) {
		super();
		this.grammar = grammar;
	}

	public Tokens tokenise(String start, ByteBuffer input) {
		Rule r = grammar.rule(start.intern());
		Tokens tokens = new Tokens(Math.max(512, input.capacity() / 2));
		try {
			int t = tokenise(r, input, 0, Rule.ANY_WHITESPACE, tokens);
		} catch (Exception e) {
			System.err.println(tokens);
		}
		if (tokens.end() != input.capacity()) {
			System.err.println("Failed to parse:");
			System.out.println(new String(input.array(), tokens.end(), Math.min(60, input.capacity() - tokens.end())));
		}
		//TODO verify and visualize errors
		return tokens;
	}
	
	private static int tokenise(Rule rule, ByteBuffer input, int position, Rule separator, Tokens tokens) {
		switch (rule.type) {
		case LITERAL:
			return literal(rule, input, position);
		case TERMINAL:
			return terminal(rule, input, position); 
		case ITERATION:
			return iteration(rule, input, position, separator, tokens);
		case SEQUENCE:
			return sequence(rule, input, position, separator, tokens);
		case SELECTION:
			return selection(rule, input, position, separator, tokens);
		case COMPLETION:
			return completion(rule, input, position, separator, tokens);
		case CAPTURE:
			return capture(rule, input, position, separator, tokens);
		default:
			throw new IllegalArgumentException("`"+rule+"` has no proper type: "+rule.type);
		}
	}
	
	private static int mismatch(int position) {
		return -position-1;
	}

	private static int completion(Rule rule, ByteBuffer input, int position,
			Rule separator, Tokens tokens) {
		final int l = input.limit();
		while (position < l) {
			int end = tokenise(rule.elements[0], input, position, separator, tokens);
			if (end < 0) {
				position++;
			} else {
				return position;
			}
		}
		return mismatch(l);
	}

	private static int literal(Rule rule, ByteBuffer input, int position) {
		final byte[] literal = rule.literal;
		final int limit = input.limit();
		for (int i = 0; i < literal.length; i++) {
			if (position >= limit)
				return mismatch(limit);
			if (input.get(position) != literal[i])
				return mismatch(position);
			position++;
		}
		return position;
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
		int end = mismatch(position);
		for (Rule r : rule.elements) {
			int endPosition = tokenise(r, input, position, separator, tokens);
			if (endPosition >= 0) {
				return endPosition;
			}
			end = Math.min(end, endPosition);
		}
		return end;
	}

	private static int sequence(Rule rule, ByteBuffer input, int position,
			Rule separator, Tokens tokens) {
		int end = position;
		for (int i = 0; i < rule.elements.length; i++) {
			end = tokenise(rule.separation, input, end, Rule.EMPTY_STRING, tokens);
			Rule r = rule.elements[i];
			int endPosition = tokenise(r, input, end, separator, tokens);
			if (endPosition < 0) {
				return endPosition;
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
			if (endPosition < 0) {
				if (c < rule.occur.min) {
					return endPosition;
				}
				return end;
			} else {
				end = endPosition;
				if (rule.separation != null) {
					end = tokenise(rule.separation, input, end, Rule.EMPTY_STRING, tokens);
				}
				c++;
			}
		}
		return end;
	}

	private static int terminal(Rule rule, ByteBuffer input, int position) {
		if (position >= input.limit())
			return mismatch(position);
		final int l = rule.terminal.matching(input, position);
		return l == 0 ? mismatch(position) : position + l;
	}

}
