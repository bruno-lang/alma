package alma.lang;

import static java.lang.System.arraycopy;
import static java.util.Arrays.copyOf;

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
	
	/**
	 * Assignments of type "name = ..." are desugared by rewriting the src
	 * program first as just a short section has to be moved/changed that is
	 * guaranteed to fit into same space.
	 */
	private static void desugarAssignments(byte[] src) {
		// name = (
		// j      k
		// name = ....\n
		// j          k
		int i = 0;
		while (i+1 < src.length) {
			i++;
			if (isRec(src[i]) && isNoop(src[i-1]) && isNoop(src[i+1])) {
				int j = i-1;
				while (j >= 0 && isNoop(src[j])) j--;
				while (j >= 0 && isName(src[j])) j--;
				j++;
				arraycopy(src, j, src, j+2, i-j-1);
				int k = i+2;
				while (k < src.length && isNoop(src[k])) k++;
				if (isBlockStart(src[k])) {
					src[j] = src[k];
					src[k] = ' ';
				} else { // to end of line
					while (k < src.length && src[k] != '\n') k++;
					src[j] = '(';
					// TODO optimize: when \n is followed by more WS once can just replace the \n with ) and skip copying 
					// or when the \n is preceded by 2 WS one can put the ) there
					arraycopy(src, i+2, src, i+1, k-i-2);
					src[k-1] = ')';
				}
				src[j+1] = '=';
			}
		}
	}

	/**
	 * Basic strategy: scan left to right, identify and move sections when sugar
	 * is encountered. This mostly depends on WS.
	 */
	public static byte[] desugar(byte[] src) {
		desugarAssignments(src);
		byte[] dest = new byte[src.length*2]; //TODO use shared long array to desugar that is copied to new dest array at the end
		int iS = 0; // index src
		int iD = 0; // index dest
		int iND = -1; // index after last noop in dest
		while (iS < src.length) {
			byte op = src[iS++];
			dest[iD++] = op;
			if (isNoop(op)) {
				iD--; // will override noop with next
				iND = iD;
				if (iS > 5 && isName(src[iS-2])) { // 5 because it needs at least 5 chars be allow the case
					int iNS = iS-3;
					while (isName(src[iNS])) iNS--;
					if (iNS > 3 && isRec(src[iNS]) && isBlockEnd(src[iNS-1])) {
						int ln = iS-iNS-2;
						int iSD = src[iNS-1] == ')'
								? afterPrevious('(', ')', dest, iD-ln-2)
								: afterPrevious('[', ']', dest, iD-ln-2);
						arraycopy(dest, iSD, dest, iSD+ln+1, iD-iSD);
						dest[iSD] = '=';
						arraycopy(src, iNS+1, dest, iSD+1, ln);
					}
				}
			} else if (isLooping(op)) {
				if (iS < src.length && isNoop(src[iS]) && iND >= 0) { // sugar
					// are there loopings to the left?
					int ll = 1;
					while (isLooping(src[iS-ll])) ll++;
					arraycopy(dest, iND, dest, iND+1+ll, iD-iND-ll+2); // move
					dest[iND] = '(';
					dest[iND+1] = ' '; //NOOP for now, later: (byte) (iD-iND);
					arraycopy(src, iS-ll+1, dest, iND+2, ll-1);
					iD+=2;
					dest[iD++] = ')';
					//TODO detect range and replace properly?
				}
			}
			//TODO make room for block length
		}
		return copyOf(dest, iD);
	}
	
	private static int afterPrevious(char target, char inverse, byte[] prog, int pc) {
		int c = 1;
		while (c > 0 && pc > 0) {
			pc--;
			if (prog[pc] == target) { c--; }
			else if (prog[pc] == inverse) c++;
		}
		return pc+1;
	}

	private static boolean isRec(byte op) {
		return op == '=';
	}
	
	private static boolean isBlockEnd(byte op) {
		return op == ')' || op == ']';
	}
	
	private static boolean isBlockStart(byte op) {
		return op == '(' || op == '[';
	}
	
	private static boolean isSugar(byte op) {
		return isRec(op) || isLooping(op);
	}

	private static boolean isLooping(byte op) {
		return op == '*' || op == '+' || op == '?' || op >= '1' && op <= '9' || op == '-';
	}

	private static boolean isNoop(byte op) {
		return op == ' ' || op == '\t' || op == '\n';
	}

	private static boolean isName(byte op) {
		return op >= 'a' && op <= 'z' || op >= 'A' && op <= 'Z' || op == '-';
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
}
