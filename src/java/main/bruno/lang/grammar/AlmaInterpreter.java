package bruno.lang.grammar;

import static bruno.lang.grammar.Grammar.Rule.charset;
import static bruno.lang.grammar.Grammar.Rule.pattern;
import static bruno.lang.grammar.Patterns.MAY_BE_INDENT;
import static bruno.lang.grammar.Patterns.MAY_BE_WS;
import static bruno.lang.grammar.Patterns.MUST_BE_INDENT;
import static bruno.lang.grammar.Patterns.MUST_BE_WRAP;
import static bruno.lang.grammar.Patterns.MUST_BE_WS;
import static java.lang.System.arraycopy;
import static java.util.Arrays.copyOf;

import java.util.NoSuchElementException;

import bruno.lang.grammar.Grammar.Rule;
import bruno.lang.grammar.Grammar.RuleType;

final class AlmaInterpreter {

	static final class Registers {

		// number registers 
		boolean isHigh = false;
		int low = 0;
		int high = 0;
		
		// buffers
		byte[] str = new byte[256]; int str_len = 0;
		byte[] key = new byte[256]; int key_len = 0;

		// composition state
		CharacterSet charset = CharacterSet.EMPTY;
		Rule rule;
		Rule[] seq = new Rule[256]; int seq_len = 0;
		Rule[] alt = new Rule[256]; int alt_len = 0;
		Rule[] idx = new Rule[512]; int idx_len = 0;
		
	}

	public static Grammar make(byte[]... codes) {
		Registers reg = new Registers();
		for (byte[] code : codes) {
			codeMode(code, 0, reg);
		}
		return new Grammar(GrammarBuilder.finish(copyOf(reg.idx, reg.idx_len)));
	}
	
	/* MODES */
	
	public static int codeMode(byte[] code, int pos, Registers reg) {
		while (pos < code.length) {
			byte opcode = code[pos];
			switch (opcode) {
			// no-ops:
			case ' '  : break;
			case '\n' : break; 
			case '\r' : break;
			case '\t' : break;
			// modes:
			case '%' : pos = commentMode(code, pos+1); break; 
			case '\'': pos = literalMode(code, pos+1, reg); pushToSeq(makeLiteral(reg), reg); break;
			case '[' : pos = charsetMode(code, pos+1, reg); pushToSeq(makeCharset(reg), reg); break;
			case '{' : pos = rangeMode(code, pos+1, reg); occur(reg.low, reg.high, reg); resetNumeric(reg); break;
			case '\\': pos = textMode(code, pos+1, reg); pushToSeq(lookup(reg, true), reg); unpack(reg); break;
			case '@' : pos = textMode(code, pos+1, reg); rename(reg); break;
			case '-' : pushToIdx(reg); pos = textMode(code, pos+1, reg); break;
			// repetition:
			case '?' : occur(0, 1, reg); break;
			case '*' : occur(0, Occur.MAX_OCCURANCE, reg); break;
			case '+' : occur(1, Occur.MAX_OCCURANCE, reg); break;
			// whitespace:
			case ',' : pushToSeq(pattern(MAY_BE_INDENT),  reg); break;
			case '.' : pushToSeq(pattern(MAY_BE_WS),      reg); break;
			case ';' : pushToSeq(pattern(MUST_BE_INDENT), reg); break;
			case ':' : pushToSeq(pattern(MUST_BE_WS),     reg); break;
			case '!' : pushToSeq(pattern(MUST_BE_WRAP),   reg); break;
			// groups:
			case '(' : pos = group(code, pos+1, reg); break;
			case ')' : return pos; // done with this sub-group call
			// functions:
			case '~' : pushToSeq(Rule.fill(), reg); break;
			case '<' : pushToSeq(Rule.DECISION, reg); break;
			case '|' : pushToAlt(reg); break;
			case '=' : move(reg); break;
			case '>' : lookahead(reg); break;
			case '^' : excludeCharset(reg); break;
			case '&' : reg.charset = reg.rule.charset; break;
			// illegal:
			default  : 
				if (isAlphanumeric(opcode)) {
					pos = textMode(code, pos, reg);
					pushToSeq(lookup(reg, true), reg);
				} else {
					illegalOp(pos, opcode);
				}
			}
			pos++;
		}
		return pos;
	}

	/**
	 * <pre>[ ... ]</pre> 
	 */
	public static int charsetMode(byte[] code, int pos, Registers reg) {
		pushToSeq(null, reg);
		while (pos < code.length) {
			final byte opcode = code[pos];
			switch (opcode) {
			// no-ops:
			case ' '  : break;
			case '\n' : break; 
			case '\r' : break;
			case '\t' : break;
			// modes:
			case ']'  : return pos;
			case '{'  : pos = rangeMode(code, pos+1, reg);   and(makeRange(reg), reg); break;
			case '\\' : pos = textMode(code, pos+1, reg);    and(charsetOf(lookup(reg, false)), reg); break;
			case '\'' : pos = literalMode(code, pos+1, reg); and(makeSet(reg), reg); break;
			case '%'  : pos = commentMode(code, pos+1); break;
			// functions:
			default   : 
				if (isAlphanumeric(opcode)) {
					and(charsetOf(lookup(new String(new byte[] { opcode }), reg, false)), reg); break;
				} else {
					illegalOp(pos, opcode);
				}
			}
			pos++;
		}
		return pos;
	}
	
	private static CharacterSet charsetOf(Rule rule) {
		if (rule.elements[0].type == RuleType.LITERAL) {
			CharacterSet set = CharacterSet.EMPTY;
			for (byte c : rule.elements[0].literal) {
				set = set.and(CharacterSet.character(c));
			}
			return set;
		}
		return rule.elements[0].charset;
	}

	/**
	 * <pre>{ ... }</pre> 
	 */
	public static int rangeMode(byte[] code, int pos, Registers reg) {
		while (pos < code.length) {
			final byte opcode = code[pos];
			switch (opcode) {
			// no-ops:
			case ' '  : break;
			case '\n' : break; 
			case '\r' : break;
			case '\t' : break;
			// modes:
			case '}'  : return pos;
			case '\'' : pos = literalMode(code, pos+1, reg); toNumber(reg); break;
			case '#'  : pos = hexMode(code, pos+1, reg); break;
			case '*'  : setValue(reg.isHigh ? Occur.MAX_OCCURANCE : 0, reg); break;
			case '0'  :
			case '1'  :
			case '2'  :
			case '3'  :
			case '4'  :
			case '5'  :
			case '6'  :
			case '7'  :
			case '8'  :
			case '9'  : pos = numericMode(code, pos, reg); break;
			// functions:
			case '-': reg.isHigh = !reg.isHigh; break;
			default : illegalOp(pos, opcode);
			}
			pos++;
		}
		return pos;
	}
	
	/**
	 * <pre>' ... '</pre> 
	 */
	public static int literalMode(byte[] code, int pos, Registers reg) {
		while (pos < code.length && code[pos] != '\'') {
			reg.str[reg.str_len++] = code[pos++];
		}
		return pos;
	}

	/**
	 * <pre>
	 * \ ...
	 * @ ...
	 * - ... 
	 * </pre> 
	 */
	public static int textMode(byte[] code, int pos, Registers reg) {
		while (pos < code.length && isAlphanumeric(code[pos])) {
			reg.str[reg.str_len++] = code[pos++];
		}
		return pos-1;
	}
	
	/**
	 * <pre># ... </pre> 
	 */
	public static int hexMode(byte[] code, int pos, Registers reg) {
		while (pos < code.length && isHex(code[pos])) {
			setValue(value(reg) * 16 + Integer.parseInt(new String(new byte[] { code[pos++] }), 16), reg); 
		}
		return pos-1;
	}
	
	/**
	 * <pre>0 ... </pre> 
	 */
	public static int numericMode(byte[] code, int pos, Registers reg) {
		while (pos < code.length && isNumeric(code[pos])) {
			setValue(value(reg) * 10 + (code[pos++] - '0'), reg); 
		}
		return pos-1;
	}
	
	/**
	 * <pre>% ... %</pre> 
	 */
	public static int commentMode(byte[] code, int pos) {
		while (pos < code.length && code[pos] != '%') {
			pos++;
		}
		return pos;
	}
	
	private static boolean isAlphanumeric(byte b) {
		return b >= 'a' && b <= 'z' || b >= 'A' && b <= 'Z' || isNumeric(b) || b == '_';
	}
	
	private static boolean isNumeric(byte b) {
		return b >= '0' && b <= '9';
	}
	
	private static boolean isHex(byte b) {
		return isNumeric(b) || b >= 'A' && b <= 'F'; 
	}
	
	
	/* OPs */

	private static void illegalOp(int pos, byte opcode) {
		throw new IllegalArgumentException("`"+((char)opcode)+"` is not a valid op-code at position: "+pos);
	}

	/**
	 * @return the position after the group
	 */
	private static int group(byte[] code, int pos, Registers reg) {
		pushToSeq(null, reg);
		Rule[] seq = copyOf(reg.seq, reg.seq_len);
		Rule[] alt = copyOf(reg.alt, reg.alt_len);
		reg.alt_len = 0;
		reg.seq_len = 0;
		pos = codeMode(code, pos, reg);
		// pack and restore:
		// 1. push rule to seq, push seq to alt (if needed)
		pushToSeq(null, reg);
		if (reg.alt_len == 0 && reg.seq_len == 0) {
			return pos; // nothing added - fine
		}
		Rule groupSeq = reg.seq_len == 1 ? reg.seq[0] : Rule.seq(copyOf(reg.seq, reg.seq_len));
		arraycopy(seq, 0, reg.seq, 0, seq.length); // restore seq
		reg.seq_len = seq.length;
		if (reg.alt_len == 0) { // just a sequence
			reg.rule = groupSeq;
		} else { // multiple alternatives in the group 
			reg.alt[reg.alt_len++] = groupSeq;
			Rule groupAlt = Rule.alt(copyOf(reg.alt, reg.alt_len));
			reg.rule = groupAlt;
		}
		arraycopy(alt, 0, reg.alt, 0, alt.length); // restore alt
		reg.alt_len = alt.length;
		return pos;
	}

	private static void pushToIdx(Registers reg) {
		if (reg.key_len == 0)
			return;
		pushToAlt(reg);
		Rule r = Rule.alt(copyOf(reg.alt, reg.alt_len));
		if (r.elements.length == 1 && r.type == RuleType.ALTERNATIVES) { // unfold alternative of length 1
			r = r.elements[0];
		}
		if (r.elements.length == 1 && r.type == RuleType.SEQUENCE) { // unfold sequence of length 1
			r = r.elements[0];
		}
		String name = new String(reg.key, 0, reg.key_len);
		reg.idx[reg.idx_len++] = r.is(name);
		reg.key_len = 0;
		reg.seq_len = 0;
		reg.alt_len = 0;
	}

	private static void pushToAlt(Registers reg) {
		pushToSeq(null, reg); // complete current sequence
		reg.alt[reg.alt_len++] = reg.seq_len == 1 ? reg.seq[0] : Rule.seq(copyOf(reg.seq, reg.seq_len));
		reg.seq_len = 0;
	}	
	
	private static void lookahead(Registers reg) {
		reg.rule = Rule.lookahead(reg.rule);
	}
	
	private static void resetNumeric(Registers reg) {
		reg.low = 0;
		reg.high = 0;
		reg.isHigh = false;
	}
	
	private static void move(Registers reg) {
		reg.key_len = reg.str_len;
		System.arraycopy(reg.str, 0, reg.key, 0, reg.key_len); 
		reg.str_len = 0;
	}
	
	private static void rename(Registers reg) {
		reg.rule = reg.rule.as(new String(copyOf(reg.str, reg.str_len)));
		reg.str_len = 0;
	}
	
	private static void unpack(Registers reg) {
		if (reg.rule.type == RuleType.INCLUDE) {
			reg.rule = reg.rule.subst();
		} else {
			reg.rule = reg.rule.elements[0];
		}
	}
	
	private static Rule makeLiteral(Registers reg) {
		Rule r = Rule.literal(copyOf(reg.str, reg.str_len)); 
		reg.str_len = 0;
		return r;
	}
	
	private static Rule makeCharset(Registers reg) {
		Rule r = charset(reg.charset); 
		reg.charset = CharacterSet.EMPTY;
		return r;
	}
	
	private static CharacterSet makeRange(Registers reg) {
		int low = reg.low;
		int high = reg.high;
		if (high < low) {
			high = low;
		}
		CharacterSet range = CharacterSet.range(low, high);
		resetNumeric(reg);
		return range;
	}

	private static CharacterSet makeSet(Registers reg) {
		CharacterSet t = CharacterSet.EMPTY;
		for (int i = 0; i < reg.str_len; i++) {
			t = t.and(CharacterSet.character(reg.str[i]));
		}
		reg.str_len = 0;
		return t;
	}
	
	private static void occur(int min, int max, Registers reg) {
		reg.rule = reg.rule.occurs(Occur.occur(min, max));
	}
	
	/**
	 * appends current rule to current sequence and makes the argument rule the
	 * new current rule.
	 */
	private static void pushToSeq(Rule rule, Registers reg) {
		if (reg.rule != null) {
			reg.seq[reg.seq_len++] = reg.rule;
		}
		reg.rule = rule;
	}	
	
	private static Rule lookup(Registers reg, boolean referenceIfUnknown) {
		String name = new String(copyOf(reg.str, reg.str_len));
		reg.str_len = 0;
		return lookup(name, reg, referenceIfUnknown);
	}

	private static Rule lookup(String name, Registers reg, boolean include) {
		for (int i = 0; i < reg.idx_len; i++) {
			Rule r = reg.idx[i];
			if (name.equals(r.name)) {
				return r;
			}
		}
		if (include) {
			return Rule.include(name);
		}
		throw new NoSuchElementException("`"+name+"`");
	}
	
	private static void excludeCharset(Registers reg) {
		Rule r = reg.rule;
		if (r.type == RuleType.CHARACTER_SET) {
			reg.rule = Rule.charset(r.charset.not());
		} else if (r.type == RuleType.PATTERN) {
			reg.rule = Rule.pattern(Patterns.not(r.pattern));
		}
	}

	private static void and(CharacterSet other, Registers reg) {
		reg.charset = reg.charset.and(other);
	}
	
	private static void toNumber(Registers reg) {
		setValue(reg.str[0], reg); 
		reg.str_len = 0;
	}
	
	private static void setValue(int i, Registers reg) {
		if (reg.isHigh) { reg.high = i; } else { reg.low = i; }
	}
	
	private static int value(Registers reg) {
		return reg.isHigh ? reg.high : reg.low;
	}
	
}