package alma.lang;

import static java.util.Arrays.copyOfRange;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public final class Program {

	/**
	 * the de-sugared original program (mostly for debugging purposes) 
	 */
	private final byte[] prog;
	
	/**
	 * The begin index of the named blocks.
	 */
	private final int[] begins;
	
	private final String[] names;

	public static Program compile(String prog) {
		return compile(prog.getBytes());
	}

	public static Program compile(byte[] prog) {
		return new Program(prog);
	}

	private Program(byte[] uprog) {
		super();
		this.prog = uprog[0] == '$' ? uprog : desugar(uprog);
		Map<String, Integer> names = new LinkedHashMap<>();
		for (int i = 0; i < prog.length; i++) {
			byte op = prog[i];
			if (op == '=') {
				int s = i+1;
				int e = s;
				while (isMidName(prog[++e]));
				String name = new String(prog,s,e-s, StandardCharsets.US_ASCII);
				names.put(name, i-1); // pointing to the ( for now
				prog[s] = (byte) names.size();
				for (int j = s+1; j < e; j++)
					prog[j] = ' '; // inserting NOOPs for rest of the name
				i = e-1;
			} else if (op == '\'' || op == '"') {
				while (++i < prog.length && prog[i] != op);
			}
		}
		// TODO (in-place) replace multi digit reps with ^c (use WS as filler)
		// TODO replace references (calls) with internal refs
		// TODO strip unneeded no-ops (some are needed as name delimiters unless names are replaced with internal instructions)
		this.names = names.keySet().toArray(new String[0]);
		this.begins = new int[names.size()];
		int i = 0;
		for (Integer begin : names.values()) {
			this.begins[i++] = begin;
		}
		System.out.println(toString());
		System.out.println(names);
	}

	static byte[] desugar(byte[] prog) {
		byte[] dest = new byte[prog.length+Math.max(10,prog.length/3)];
		return copyOfRange(dest, 0, desugar(prog, 0, prog.length, dest, 0));
	}

	static int desugar(byte[] src, int ri, int re, byte[] dest, int wi) {
		while (ri < re) {
			byte op = src[ri++];
			dest[wi++] = op; // this is the default - we might override it in special cases
			if (op == '%') { // comments
				wi--; // take back '%' in dest
				while (ri < re && !isEndOfComment(src[ri])) ri++;
				if (ri < re && src[ri] == '%') ri++;
			} else if (op == '\'' || op == '"') { // we need to transfer literals and sets "as is"
				while (ri < re && src[ri] != op) 
					dest[wi++] = src[ri++]; // skip literal/set
				dest[wi++] = src[ri++];     // and the " or '
			} else if (isNoop(op)) {
				int nextNoop = ri;
				while (nextNoop < re && !isNoop(src[nextNoop])) nextNoop++;
				int beforeRep = nextNoop-1;
				if (isRep(src[beforeRep])) {
					while (isRep(src[--beforeRep]));
					if (beforeRep >= 0 && src[beforeRep] != ')') { // if its a block don't touch it
						dest[wi-1] = '(';
						wi = copyRange(src, beforeRep+1, nextNoop, dest, wi);
						wi = desugar(src, ri, beforeRep+1, dest, wi);
						dest[wi++] = ')';
						ri = nextNoop; // this way this NOOP is evaluated also as next start of sugar
					} 
				} 
			} else if (op == '(') { 
				int closingBracket = end(src, ri);
				int afterRep = closingBracket+1;
				if (afterRep < re && isRep(src[afterRep])) {
					while (++afterRep < re && isRep(src[afterRep]));
					if (afterRep < re && isNoop(src[afterRep])) {
						dest[wi-1] = '(';
						wi = copyRange(src, closingBracket+1, afterRep, dest, wi);
						wi = desugar(src, ri, closingBracket, dest, wi);
						dest[wi++] = ')';
						ri = afterRep; // NOOP could start of sugar again
					} 
				} 
			} else if (op == '[') {
				int closingBracket = end(src, ri, '[', ']');
				dest[wi-1] = '(';
				dest[wi++] = '?';
				wi = desugar(src, ri, closingBracket, dest, wi);
				dest[wi++] = ')';
				ri = closingBracket+1;
			} else if (op == '=' && isNoop(src[ri]) && ri > 1 && isNoop(src[ri-2])) {
				int nameEnd = ri-1;
				while (nameEnd >= 0 && isNoop(src[--nameEnd]));
				if (nameEnd >= 0 && isName(src[nameEnd])) {
					int nameStart = nameEnd;
					while (nameStart > 0 && isMidName(src[nameStart-1])) nameStart--;
					int len = nameEnd-nameStart+1;
					wi -= len+2;
					dest[wi++] = '(';
					dest[wi++] = '=';
					for (int i = nameStart; i <= nameEnd; i++)
						dest[wi++] = src[i];
					dest[wi++] = ' ';
					int ruleStart = ri;
					while (isNoop(src[++ruleStart]));
					int ruleEnd = ruleStart;
					if (src[ruleStart] == '{') {
						while (ruleEnd < re && src[ruleEnd] != '}') ruleEnd++;
						wi = desugar(src, ruleStart+1, ruleEnd, dest, wi);
					} else { // to end of line
						while (ruleEnd < re && src[ruleEnd] != '\n') ruleEnd++;
						wi = desugar(src, ruleStart, ruleEnd, dest, wi);
					}
					dest[wi++] = ')';
					ri = ruleEnd+1;
				} 
			} else if (isName(op) && (ri >= re || !isMidName(src[ri])) && (ri <= 1 || !isMidName(src[ri-2]))) {
				dest[wi++] = ' '; // part of @ internalisation, we make sure all single letter names have a space afterwards so @x has space in dest array 
			}
		}
		return wi;
	}

	private static int copyRange(byte[] src, int from, int to, byte[] dest,	int wi) {
		for (int i = from; i < to; i++) {
			byte op = src[i];
			if (op !=  '{' && op != '}') {
				dest[wi++] = op;
			}
		}
		return wi;
	}
	
	private static boolean isEndOfComment(byte op) {
		return op == '%' || op == '\n';
	}
	
	private static boolean isRep(byte op) {
		return op == '*' || op == '+' || op == '?' || op >= '1' && op <= '9' || op == '-' || op == '{' || op == '}'; 
	}

	private static boolean isNoop(byte op) {
		return op == ' ' || op == '\t' || op == '\r' || op == '\n';
	}

	private static boolean isMidName(byte op) {
		return isName(op) || op == '-' || op == '_';
	}
	
	private static boolean isName(byte op) {
		return op >= 'a' && op <= 'z' || op >= 'A' && op <= 'Z' || op == '#' || op == '$';
	}

	public ParseTree parse(String data) {
		return parse(data.getBytes(StandardCharsets.US_ASCII));
	}

	public ParseTree parse(byte[] data) {
		ParseTree tree = new ParseTree(128, names);
		Parser parser = new Parser(prog, data, tree);
		tree.end = parser.parse();
		return tree;
	}

	@Override
	public String toString() {
		return new String(prog, StandardCharsets.US_ASCII);
	}

	static int end(byte[] prog, int pc0) {
		return end(prog, pc0, '(', ')');
	}
	
	static int end(byte[] prog, int pc0, char begin, char end) {
		int pcEnd = pc0;
		int level = 1;
		while (pcEnd < prog.length) {
			byte op = prog[pcEnd];
			if (op == end) {
				level--;
				if (level == 0)
					return pcEnd;
			} else if (op == begin) {
				level++;
			} else if (op == '\'') {
				while (pcEnd < end && prog[++pcEnd] != '\'');
			}
			pcEnd++;
		}
		return pcEnd-1;
	}
}
