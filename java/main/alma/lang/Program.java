package alma.lang;

import static java.lang.Character.isUpperCase;
import static java.lang.Math.min;
import static java.util.Arrays.copyOfRange;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public final class Program {

	/**
	 * the de-sugared original program (mostly for debugging purposes) 
	 */
	private final byte[] prog0;
	
	private final byte[] prog;
	
	/**
	 * The begin index of the named blocks.
	 */
	private final int[] indices;
	
	private final String[] names;

	public static Program compileFile(String file) throws IOException {
		return compile(Files.readAllBytes(Paths.get(file)));		
	}
	
	public static Program compile(String prog) {
		return compile(prog.getBytes());
	}

	public static Program compile(byte[] prog) {
		return new Program(prog);
	}

	private Program(byte[] uprog) {
		super();
		this.prog0 = uprog[0] == '$' ? uprog : desugar(uprog);
		Map<String, Integer> names = new LinkedHashMap<>();
		System.out.println(toString());
		this.prog = internalise(prog0, names);
		this.names = names.keySet().toArray(new String[0]);
		this.indices = indicesForNamesIn(prog, names.values());
		System.out.println(toString());
	}

	private static int[] indicesForNamesIn(byte[] prog, Collection<?> names) {
		int[] indices = new int[names.size()];
		for (int i = 0; i < prog.length; i++) { 
			byte op = prog[i];
			if (op == '=') {
				indices[prog[i+1]-1] = i;
				i++;
			} else if (op =='@' || op == '^') { // jump over all instruction arguments because they could contain a byte equal to =
				i++;
			} else if (op == '\'' || op == '"') { //again ignore literals and sets
				while (++i < prog.length && prog[i] != op);
			}
		}
		return indices;
	}
	
	static byte[] internalise(byte[] src, Map<String, Integer> names) {
		byte[] prog = src.clone();
		for (int i = 0; i < prog.length; i++) {
			byte op = prog[i];
			if (op == '=') {
				String name = extractName(prog, i+1);
				names.put(name, names.size()+1);
				if (isUpperCase(name.charAt(0))) {
					prog[i] = ' '; // no capture, just a reference
					//FIXME always put index here but remove it when compressing
				} else {
					prog[i+1] = (byte) names.size();
				}
				i += name.length();
			} else if (op == '\'' || op == '"') {
				while (++i < prog.length && prog[i] != op);
			}
		}
		// replace references (calls) with internal refs (need extra loop as all names must be collected before)
		for (int i = 0; i < prog.length; i++) {
			byte op = prog[i];
			if (op == '\'' || op == '"') { // skip literals and sets
				while (++i < prog.length && prog[i] != op);
			} else if (isName(op) && (i==0 || !isName(prog[i-1]) && prog[i-1] != '=')) {
				String name = extractName(prog, i);
				prog[i++] = '@';
				Integer index = names.get(name);
				if (index == null)
					throw new IllegalArgumentException("Unknown name: "+name);
				prog[i] = index.byteValue();
			}			
		}
		// TODO (in-place) replace multi digit reps with ^c (use WS as filler)
		// last: strip NOOPs (now unneeded)
		byte[] compressed = new byte[Math.max(64, prog.length)];
		int j = 0;
		boolean lastHasArgument = false;
		for (int i = 0; i < prog.length; i++) {
			byte op = prog[i];
			//FIXME we can collect the indexes of = here and remove those that reference Upper case names
			if (lastHasArgument) { 
				compressed[j++] = op; lastHasArgument = false; 
			} else if (!isNoop(op)) {
				compressed[j++] = op;
				lastHasArgument = op == '=' || op == '@' || op == '^'; // ( and | do not have them yet
				if (op == '(' || op == '|') {
					compressed[j++] = ' '; // put a NOOP for now, later holds the length
				} else if (op == '\'' || op == '"') {
					i++;
					while (prog[i] != op) { compressed[j++] = prog[i++]; }
					compressed[j++] = prog[i];
				}
			}
		}
		// length encode block cases
		for (int i = 0; i < j; i++) {
			if (compressed[i] == ' ') {
				compressed[i] = (byte) (end(compressed, i, '(', ')', '|') -i+1);
			}
		}
		return copyOfRange(compressed, 0, j);
	}

	private static String extractName(byte[] prog, int s) {
		int e = s;
		while (isMidName(prog[++e]));
		String name = new String(prog,s,e-s, StandardCharsets.US_ASCII);
		for (int j = s; j < e; j++)
			prog[j] = ' '; // inserting NOOPs for name
		return name;
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
					wi -= len+(ri-1-nameEnd);
					dest[wi++] = '(';
					dest[wi++] = '=';
					for (int i = nameStart; i <= nameEnd; i++)
						dest[wi++] = src[i];
					dest[wi++] = ' ';
					int ruleStart = ri;
					while (isNoop(src[++ruleStart]));
					int ruleEnd = ruleStart;
					if (src[ruleStart] == '(') {
						ruleEnd = end(src, ruleStart+1);
						if (ruleEnd+1 < src.length && src[ruleEnd+1] == '\n') {
							wi = desugar(src, ruleStart+1, ruleEnd, dest, wi);
						} else { 
							ruleEnd = ruleStart; 
						}
					} 
					if (ruleStart == ruleEnd) { // to end of line
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
	
	static boolean isStartOfCase(byte op) {
		return op == '(' || op == '|';
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
		return op >= 'a' && op <= 'z' || op >= 'A' && op <= 'Z';
	}

	public ParseTree parse(String data) {
		return parse(data.getBytes(StandardCharsets.US_ASCII));
	}

	public ParseTree parse(byte[] data) {
		ParseTree tree = new ParseTree(128, names);
		Parser parser = new Parser(prog, indices, data, tree);
		tree.end = parser.parse();
		return tree;
	}

	@Override
	public String toString() {
		if (prog == null)
			return toString(prog0);
		StringBuilder b = new StringBuilder();
		int level = 0;
		boolean literal = false;
		boolean set = false;
		for (int i = 0; i < prog.length; i++) {
			byte op = prog[i];
			b.append((char) op);
			if (!literal && !set) {
				if (op == '@' || op == '=') {
					int id = prog[++i]-1;
					b.append('{').append(indices[id]).append(":").append(names[id]).append('}');
				} else if (op == '(' || op == '|') {
					b.append('{').append(i).append(":").append(prog[++i]).append('}');
				} else if (op == ')' && level == 0) {
					b.append("\n");
				}
				if (op == '(') level++;
				if (op == ')') level--;
			}
			if (!set && op == '\'') { literal = !literal; } 
			if (!literal && op == '"') { set = !set; }
		}
		return b.toString();
	}
	
	public static String toString(byte[] prog) {
		return new String(prog, StandardCharsets.US_ASCII);
	}

	static int end(byte[] prog, int pc0) {
		return end(prog, pc0, '(', ')');
	}
	
	static int end(byte[] prog, int pc0, char begin, char end) {
		return end(prog, pc0, begin, end, end);
	}
	
	static int end(byte[] prog, int pc0, char begin, char end, char exit) {
		int pc = pc0;
		int level = 1;
		while (pc < prog.length) {
			byte op = prog[pc];
			if (op == end) {
				level--;
				if (level == 0)
					return pc;
			} else if (op == begin) {
				level++;
			} else if (op == '\'' || op == '"') {
				while (++pc < prog.length && prog[pc] != op);
			} else if (op == exit && level == 1) {
				return pc;
			}
			pc++;
		}
		return pc;
	}
}
