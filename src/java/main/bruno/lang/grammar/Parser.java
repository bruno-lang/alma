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
			t = parseRule(start, input, 0, tree);
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
	
	private static int parseRule(Rule rule, ByteBuffer input, int position, ParseTree tree) {
		switch (rule.type) {
		case LITERAL:
			return parseLiteral(rule, input, position);
		case TERMINAL:
			return parseTerminal(rule, input, position);
		case PATTERN:
			return parsePattern(rule, input, position);
		case ITERATION:
			return parseIteration(rule, input, position, tree);
		case SEQUENCE:
			return parseSequence(rule, input, position, tree);
		case SELECTION:
			return parseSelection(rule, input, position, tree);
		case COMPLETION:
			return parseCompletion(rule, input, position, tree);
		case CAPTURE:
			return parseCapture(rule, input, position, tree);
		default:
			throw new IllegalArgumentException("`"+rule+"` has no proper type: "+rule.type);
		}
	}
	
	private static int mismatch(int position) {
		return -position-1;
	}

	private static int parseCompletion(Rule rule, ByteBuffer input, int position, ParseTree tree) {
		final int l = input.limit();
		while (position < l) {
			int end = parseRule(rule.elements[0], input, position, tree);
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

	private static int parseLiteral(Rule rule, ByteBuffer input, int position) {
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

	private static int parseCapture(Rule rule, ByteBuffer input, int position, ParseTree tree) {
		tree.push(rule, position);
		int end = parseRule(rule.elements[0], input, position, tree);
		if (end > position) {
			tree.done(end);
		} else {
			tree.pop();
		}
		return end;
	}

	private static int parseSelection(Rule rule, ByteBuffer input, int position, ParseTree tree) {
		int end = mismatch(position);
		for (Rule r : rule.elements) {
			int endPosition = parseRule(r, input, position, tree);
			if (endPosition >= 0) {
				return endPosition;
			}
			end = Math.min(end, endPosition);
		}
		tree.erase(position);
		return end;
	}

	private static int parseSequence(Rule rule, ByteBuffer input, int position, ParseTree tree) {
		int end = position;
		for (int i = 0; i < rule.elements.length; i++) {
			Rule r = rule.elements[i];
			int endPosition = parseRule(r, input, end, tree);
			if (endPosition < 0) {
				if (rule.distinctFromIndex <= i) {
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

	private static int parseIteration(Rule rule, ByteBuffer input, int position, ParseTree tree) {
		int end = position;
		int c = 0;
		while (c < rule.occur.max) {
			int endPosition = parseRule(rule.elements[0], input, end, tree);
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

	private static int parseTerminal(Rule rule, ByteBuffer input, int position) {
		if (position >= input.limit())
			return mismatch(position);
		if (rule.terminal.contains(input, position)) {
			return position + UTF8.byteCount(input, position);
		}
		return mismatch(position);
	}
	
	private static int parsePattern(Rule rule, ByteBuffer input, int position) {
		final int l = rule.pattern.length(input, position);
		return l < 0 ? mismatch(position) : position + l;
	}

}
