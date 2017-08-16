package alma.lang;

import static java.util.Arrays.copyOfRange;

public final class Program {

	private byte[] prog;

	public static Program compile(String prog) {
		return compile(prog.getBytes());
	}

	public static Program compile(byte[] prog) {
		return new Program(prog);
	}

	private Program(byte[] prog) {
		super();
		this.prog = desugar(prog);
	}

	static byte[] desugar(byte[] prog) {
		byte[] dest = new byte[prog.length+prog.length/3];
		return copyOfRange(dest, 0, desugar(prog, 0, prog.length, dest, 0));
		
		// 0. step: strip comments
		
		// 1. step: split into a list of blocks and their names (must be 1. to be able to resolve backwards refs later)
		// the single outer most block is always at index zero (named or not)
//		List<byte[]> blocks = new ArrayList<byte[]>();
//		Map<String, Integer> names = new LinkedHashMap<String, Integer>();
		//FIXME names can be used anywhere within a block blocks 
		// 

		// for each named block
		// 2. step: de-sugar repetition including [ => (?
		// keep all WS, insert new ( )
		
		// 3. step: (in-place) replace multi digit reps with ^c (use WS as filler)
		
		// 3.5 : index names (blocks starting with a name would be considered "global" and thereby referable)
		
		// 4. step: replace references (calls) with internal refs
		
		// 5. step: strip unneeded no-ops (some are needed as name delimiters unless names are replaced with internal instructions)
		
	}

	static int desugar(byte[] src, int ri, int re, byte[] dest, int wi) {
		while (ri < re) {
			byte op = src[ri++];
			dest[wi++] = op; // this is the default - we might override it in special cases
			if (op == '%') { // comments
				wi--; // take back '%' in dest
				while (ri < re && !isEndOfComment(src[ri])) ri++;
				if (ri < re && src[ri] == '%') ri++;
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

	public int parse(String data) {
		return parse(data.getBytes());
	}

	public int parse(byte[] data) {
		return parse(data, new ParseTree(data.length/2));
	}

	public int parse(byte[] data, ParseTree tree) {
		Parser parser = new Parser(prog, data, tree);
		int end = parser.parse();
		return end;
	}

	@Override
	public String toString() {
		return new String(prog);
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
				while (prog[++pcEnd] != '\'');
			}
			pcEnd++;
		}
		return pcEnd-1;
	}
}
