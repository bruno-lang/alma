package bruno.lang.grammar;

import java.nio.ByteBuffer;

import bruno.lang.grammar.Grammar.Rule;

/**
 * A multi-language parser that can parse any language given a starting
 * {@link Rule}.
 * 
 * @author jan
 */
public final class Parser {

	public static ParseTree parse(ByteBuffer input, Rule start) {
		ParseTree tree = new ParseTree(Math.max(512, input.capacity() * 2 / 3));
		int t = 0;
		try {
			t = parse(start, input, 0, tree);
		} catch (ParseException e) { 
			t = e.errorPosition;
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
		if (tree.end() != input.capacity()) {
			int pos = Math.abs(t);
			String msg = "Failed to parse at "+pos+":";
			System.err.println(msg);
			System.err.println(tree.debug());
			input.position(pos);
			//FIXME what if end of file...
			byte[] x = new byte[Math.min(20, input.limit()-pos)];
			input.get(x);
			System.err.println(new String(x));
			throw new RuntimeException(msg);
		}
		//TODO verify and visualize errors
		return tree;
	}
	
	private static int parse(Rule rule, ByteBuffer input, int position, ParseTree tree) {
		switch (rule.type) {
		case LITERAL:
			return literal(rule, input, position);
		case TERMINAL:
			return terminal(rule, input, position);
		case PATTERN:
			return pattern(rule, input, position);
		case ITERATION:
			return iteration(rule, input, position, tree);
		case SEQUENCE:
			return sequence(rule, input, position, tree);
		case SELECTION:
			return selection(rule, input, position, tree);
		case COMPLETION:
			return completion(rule, input, position, tree);
		case CAPTURE:
			return capture(rule, input, position, tree);
		default:
			throw new IllegalArgumentException("`"+rule+"` has no proper type: "+rule.type);
		}
	}
	
	private static int mismatch(int position) {
		return -position-1;
	}

	private static int completion(Rule rule, ByteBuffer input, int position, ParseTree tree) {
		final int l = input.limit();
		while (position < l) {
			int end = parse(rule.elements[0], input, position, tree);
			if (end < 0) {
				position++;
			} else {
				tree.erase(position);
				return position;
			}
		}
		tree.erase(position);
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

	private static int capture(Rule rule, ByteBuffer input, int position, ParseTree tree) {
		tree.push(rule, position);
		int end = parse(rule.elements[0], input, position, tree);
		if (end > position) {
			tree.done(end);
		} else {
			tree.pop();
		}
		return end;
	}

	private static int selection(Rule rule, ByteBuffer input, int position, ParseTree tree) {
		int end = mismatch(position);
		for (Rule r : rule.elements) {
			int endPosition = parse(r, input, position, tree);
			if (endPosition >= 0) {
				return endPosition;
			}
			end = Math.min(end, endPosition);
		}
		tree.erase(position);
		return end;
	}

	private static int sequence(Rule rule, ByteBuffer input, int position, ParseTree tree) {
		int end = position;
		for (int i = 0; i < rule.elements.length; i++) {
			Rule r = rule.elements[i];
			int endPosition = parse(r, input, end, tree);
			if (endPosition < 0) {
				if (rule.determination <= i) {
					tree.erase(end);
					throw new ParseException(end, endPosition);
				}
				tree.erase(position);
				return endPosition;
			}
			end = endPosition;
		}
		return end;
	}

	private static int iteration(Rule rule, ByteBuffer input, int position, ParseTree tree) {
		int end = position;
		int c = 0;
		while (c < rule.occur.max) {
			int endPosition = parse(rule.elements[0], input, end, tree);
			if (endPosition < 0) {
				tree.erase(end);
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
			return position + UTF8.byteCount(input, position);
		}
		return mismatch(position);
	}
	
	private static int pattern(Rule rule, ByteBuffer input, int position) {
		final int l = rule.pattern.length(input, position);
		return l < 0 ? mismatch(position) : position + l;
	}

}
