package bruno.lang.grammar;

import static bruno.lang.grammar.Grammar.Pattern.MAY_BE_INDENT;
import static bruno.lang.grammar.Grammar.Pattern.MAY_BE_WS;
import static java.lang.Character.isWhitespace;

import java.nio.ByteBuffer;

import bruno.lang.grammar.Grammar.Rule;
import bruno.lang.grammar.Grammar.RuleType;

/**
 * A universal parser that can parse any language given a starting {@link Rule}.
 * 
 * @author jan
 */
public final class Parser {

	public static int parse(ByteBuffer input, Rule start, ParseTree target) {
		return parseRule(start, input, 0, target);
	}
	
	private static int parseRule(Rule rule, ByteBuffer input, int position, ParseTree tree) {
		switch (rule.type) {
		case LITERAL:
			return parseLiteral(rule, input, position);
		case CHARACTER_SET:
			return parseCharacterSet(rule, input, position);
		case PATTERN:
			return parsePattern(rule, input, position);
		case REPETITION:
			return parseRepetition(rule, input, position, tree);
		case SEQUENCE:
			return parseSequence(rule, input, position, tree);
		case ALTERNATIVES:
			return parseAlternatives(rule, input, position, tree);
		case FILL:
			return parseFill(rule, input, position, tree);
		case LOOKAHEAD:
			return parseRule(rule.elements[0], input, position, tree);
		case CAPTURE:
			return parseCapture(rule, input, position, tree);
		default:
			throw new IllegalArgumentException("`"+rule+"` has uses non-runtime type: "+rule.type);
		}
	}
	
	private static int mismatch(int position) {
		return -position-1;
	}

	private static int parseFill(Rule rule, ByteBuffer input, int position, ParseTree tree) {
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

	private static int parseAlternatives(Rule rule, ByteBuffer input, int position, ParseTree tree) {
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
		final int elems = rule.elements.length;
		int end = position;
		int decisionIndex = elems+1;
		for (int i = 0; i < elems; i++) {
			Rule r = rule.elements[i];
			if (r == Rule.DECISION) {
				decisionIndex = i;
			} else {
				int endPosition = parseRule(r, input, end, tree);
				if (endPosition < 0) {
					if (decisionIndex <= i) {
						tree.erase(end);
						throw new ParseException(input, end, endPosition, tree);
					}
					tree.erase(position);
					return endPosition;
				}
				if (r.type == RuleType.LOOKAHEAD) { // we know it is the last rule
					return end; // the end of the previous rule is the result
				}
				end = endPosition;
			}
		}
		return end;
	}

	private static int parseRepetition(Rule rule, ByteBuffer input, int position, ParseTree tree) {
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

	private static int parseCharacterSet(Rule rule, ByteBuffer input, int position) {
		if (position >= input.limit())
			return mismatch(position);
		if (rule.charset.contains(input, position)) {
			return position + UTF8.byteCount(input, position);
		}
		return mismatch(position);
	}
	
	private static int parsePattern(Rule rule, ByteBuffer input, int position) {
		final int l = input.limit();
		int p = position;
		switch (rule.pattern) {
		default:
		case MAY_BE_INDENT:
		case MUST_BE_INDENT:
			while (p < l && isIndent(input.get(p))) { p++; }
			return p > position || rule.pattern == MAY_BE_INDENT ? p : mismatch(position);
		case MAY_BE_WS:
		case MUST_BE_WS:
			while (p < l &&	isWhitespace(input.get(p))) { p++; }
			return p > position || rule.pattern == MAY_BE_WS ? p : mismatch(position);
		case MUST_BE_WRAP:
			while (p  < l && isIndent(input.get(p))) { p++; }
			if (p >= l) {
				return mismatch(position);
			}
			final int w = p;
			while (p < l && isWrap(input.get(p))) { p++; }
			if (w == p) {
				return mismatch(position);
			}
			while (p < l && isIndent(input.get(p))) { p++; }
			return p;
		}
	}
	
	private static boolean isIndent(int b) {
		return b == ' ' || b == '\t';
	}
	
	private static boolean isWrap(int b) {
		return b == '\n' || b == '\r';
	}

}