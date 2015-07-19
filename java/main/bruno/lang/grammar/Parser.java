package bruno.lang.grammar;

import static bruno.lang.grammar.Grammar.Whitespace.MAY_BE_INDENT;
import static bruno.lang.grammar.Grammar.Whitespace.MAY_BE_WS;
import static java.lang.Character.isWhitespace;
import static java.lang.Math.min;

import java.nio.ByteBuffer;

import bruno.lang.grammar.Grammar.Rule;

/**
 * A universal parser that can parse any language given a starting {@link Rule}.
 * 
 * <pre>
 * p  = current position (in input)
 * p0 = starting position
 * pN = position after rule evaluation
 * pE = maximum position (end of input)
 * </pre>
 * 
 * @author jan
 */
public final class Parser {

	public static int parse(Rule start, ParseTree target, ByteBuffer input) {
		return parseRule(start, target, input, 0);
	}
	
	/**
	 * In contrast to {@link #parseRule(Rule, ParseTree, ByteBuffer, int)} (that
	 * has to match at the current position) this method searches forward in the
	 * input to match the rule.
	 */
	public static int scan(Rule rule, ParseTree tree, ByteBuffer input, int p0) {
		final int pE = input.limit();
		int p = p0;
		while (true) { 
			if (p >= pE)
				return mismatch(pE);
			int pN = parseRule(rule, tree, input, p);
			if (pN > 0)
				return p;
			p++;
		}
	}
	
	private static int parseRule(Rule rule, ParseTree tree, ByteBuffer input, int p0) {
		switch (rule.type) {
		case LITERAL:
			return parseLiteral(rule, input, p0);
		case CHARACTER_SET:
			return parseCharacterSet(rule, input, p0);
		case WHITESPACE:
			return parseWhitespace(rule, input, p0);
		case REPETITION:
			return parseRepetition(rule, tree, input, p0);
		case SEQUENCE:
			return parseSequence(rule, tree, input, p0);
		case CASCADE:
			return parseCascade(rule, tree, input, p0);
		case CAPTURE:
			return parseCapture(rule, tree, input, p0);
		default:
			throw new IllegalArgumentException("`"+rule+"` has non-runtime type: "+rule.type);
		}
	}
	
	private static int mismatch(int position) {
		return -position-1;
	}

	private static int parseLiteral(Rule rule, ByteBuffer input, int p0) {
		final byte[] literal = rule.literal;
		final int pE = input.limit();
		for (int i = 0; i < literal.length; i++) {
			if (p0 >= pE)
				return mismatch(pE);
			if (input.get(p0) != literal[i])
				return mismatch(p0);
			p0++;
		}
		return p0;
	}

	private static int parseCapture(Rule rule, ParseTree tree, ByteBuffer input, int p0) {
		tree.push(rule, p0);
		int pN = parseRule(rule.elements[0], tree, input, p0);
		if (pN > p0) {
			tree.done(pN);
		} else {
			tree.pop();
		}
		return pN;
	}

	private static int parseCascade(Rule rule, ParseTree tree, ByteBuffer input, int p0) {
		int p = mismatch(p0);
		for (Rule r : rule.elements) {
			int pN = parseRule(r, tree, input, p0);
			if (pN >= 0) {
				return pN;
			}
			p = Math.min(p, pN);
		}
		tree.erase(p0);
		return p;
	}

	private static int parseSequence(Rule rule, ParseTree tree, ByteBuffer input, int p0) {
		final int elems = rule.elements.length;
		boolean decided = false;
		int p = p0;
		int pL = Integer.MAX_VALUE;
		for (int i = 0; i < elems; i++) {
			Rule r = rule.elements[i];
			switch (r.type) {
			case DECISION:
				decided = true; break;
			case LOOKAHEAD:
				pL = p; break; // the end of the previous rule is the result
			case FILL:
				p = scan(rule.elements[i+1], tree, input, p); break;
			default:
				int pN = p < 0 ? p : parseRule(r, tree, input, p);
				if (pN < 0) {
					if (decided) {
						tree.erase(p);
						throw new ParseException(input, p, pN, tree);
					}
					tree.erase(p0);
					return pN;
				}
				p = pN;
			}
		}
		return min(p, pL);
	}

	private static int parseRepetition(Rule rule, ParseTree tree, ByteBuffer input, int p0) {
		int p = p0;
		int c = 0;
		while (c < rule.occur.max) {
			int pN = parseRule(rule.elements[0], tree, input, p);
			if (pN < 0) {
				tree.erase(p);
				if (c < rule.occur.min) {
					return pN;
				}
				return p;
			} else {
				p = pN;
				c++;
			}
		}
		return p;
	}

	private static int parseCharacterSet(Rule rule, ByteBuffer input, int p0) {
		if (p0 >= input.limit())
			return mismatch(p0);
		if (rule.charset.contains(input, p0)) {
			return p0 + UTF8.byteCount(input, p0);
		}
		return mismatch(p0);
	}
	
	private static int parseWhitespace(Rule rule, ByteBuffer input, int p0) {
		final int pE = input.limit();
		int p = p0;
		switch (rule.ws) {
		default:
		case MAY_BE_INDENT:
		case MUST_BE_INDENT:
			while (p < pE && isIndent(input.get(p))) { p++; }
			return p > p0 || rule.ws == MAY_BE_INDENT ? p : mismatch(p0);
		case MAY_BE_WS:
		case MUST_BE_WS:
			while (p < pE &&	isWhitespace(input.get(p))) { p++; }
			return p > p0 || rule.ws == MAY_BE_WS ? p : mismatch(p0);
		case MUST_BE_WRAP:
			while (p  < pE && isIndent(input.get(p))) { p++; }
			if (p >= pE) {
				return p; // end of input is also treated as wrap
			}
			final int w = p;
			while (p < pE && isWrap(input.get(p))) { p++; }
			if (w == p) {
				return mismatch(p0);
			}
			while (p < pE && isIndent(input.get(p))) { p++; }
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