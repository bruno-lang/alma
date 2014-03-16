package bruno.lang.grammar;

import java.nio.ByteBuffer;

import bruno.lang.grammar.Grammar.Rule;

/**
 * A multi-language tokeniser that can tokenise any language given a starting
 * {@link Rule}.
 * 
 * @author jan
 */
public final class Tokeniser {

	public static Tokens tokenise(ByteBuffer input, Rule start) {
		Tokens tokens = new Tokens(Math.max(512, input.capacity()));
		int t = 0;
		try {
			t = tokenise(start, input, 0, tokens);
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
		if (tokens.end() != input.capacity()) {
			int pos = Math.abs(t);
			String msg = "Failed to parse at "+pos+":";
			System.err.println(msg);
			System.err.println(tokens.debug());
			input.position(pos);
			byte[] x = new byte[Math.min(20, input.limit()-pos)];
			input.get(x);
			System.err.println(ANSI.RESET+new String(x)+ANSI.RESET);
			throw new RuntimeException(msg);
		}
		//TODO verify and visualize errors
		return tokens;
	}
	
	private static int tokenise(Rule rule, ByteBuffer input, int position, Tokens tokens) {
		switch (rule.type) {
		case LITERAL:
			return literal(rule, input, position);
		case TERMINAL:
			return terminal(rule, input, position);
		case PATTERN:
			return pattern(rule, input, position);
		case ITERATION:
			return iteration(rule, input, position, tokens);
		case SEQUENCE:
			return sequence(rule, input, position, tokens);
		case SELECTION:
			return selection(rule, input, position, tokens);
		case COMPLETION:
			return completion(rule, input, position, tokens);
		case CAPTURE:
			return capture(rule, input, position, tokens);
		default:
			throw new IllegalArgumentException("`"+rule+"` has no proper type: "+rule.type);
		}
	}
	
	private static int mismatch(int position) {
		return -position-1;
	}

	private static int completion(Rule rule, ByteBuffer input, int position, Tokens tokens) {
		final int l = input.limit();
		while (position < l) {
			int end = tokenise(rule.elements[0], input, position, tokens);
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

	private static int capture(Rule rule, ByteBuffer input, int position, Tokens tokens) {
		tokens.push(rule, position);
		int end = tokenise(rule.elements[0], input, position, tokens);
		if (end > position) {
			tokens.done(end);
		} else {
			tokens.pop();
		}
		return end;
	}

	private static int selection(Rule rule, ByteBuffer input, int position, Tokens tokens) {
		int end = mismatch(position);
		for (Rule r : rule.elements) {
			int endPosition = tokenise(r, input, position, tokens);
			if (endPosition >= 0) {
				return endPosition;
			}
			end = Math.min(end, endPosition);
		}
		return end;
	}

	private static int sequence(Rule rule, ByteBuffer input, int position, Tokens tokens) {
		int end = position;
		for (int i = 0; i < rule.elements.length; i++) {
			Rule r = rule.elements[i];
			end = tokenise(r, input, end, tokens);
			if (end < 0) {
				return end;
			}
		}
		return end;
	}

	private static int iteration(Rule rule, ByteBuffer input, int position, Tokens tokens) {
		int end = position;
		int c = 0;
		while (c < rule.occur.max) {
			int endPosition = tokenise(rule.elements[0], input, end, tokens);
			if (endPosition < 0) {
				if (c < rule.occur.min) {
					return endPosition;
				}
				return end;
			} else {
				end = endPosition;
				c++;
			}
		}
		return end;
	}

	private static int terminal(Rule rule, ByteBuffer input, int position) {
		if (position >= input.limit())
			return mismatch(position);
		if (rule.terminal.contains(input, position)) {
			return position + UTF8.byteLength(input, position);
		}
		return mismatch(position);
	}
	
	private static int pattern(Rule rule, ByteBuffer input, int position) {
		final int l = rule.pattern.length(input, position);
		return l < 0 ? mismatch(position) : position + l;
	}

}
