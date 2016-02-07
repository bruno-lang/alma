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
					src[j]   = src[k];
					src[j+1] = '=';
					src[k]   = ' ';
				} else { // to end of line
					while (k < src.length-1 && src[k] != '\n') k++;
					src[j]   = '(';
					src[j+1] = '=';
					src[k]   = ')';
				}
			}
		}
	}

	/**
	 * Basic strategy: scan left to right, identify and move sections when sugar
	 * is encountered. This mostly depends on WS.
	 */
	public static byte[] desugar(byte[] src) {
		//TODO resolve names first so that letters do no longer appear except for recordings
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
			} else if (isLooping(op)) {
				if (iS < src.length && isNoop(src[iS]) && iND >= 0) { // sugar
					// are there loopings to the left?
					int ll = 1;
					while (isLooping(src[iS-ll])) ll++; //FIXME iS is after FIRST looping!
					if (isBlockEnd(src[iS-2])) {
						int iBD = afterPrevious('(', ')', dest, iD-3);
						arraycopy(dest, iBD, dest, iBD+ll, iD-iBD);
						arraycopy(src, iS, dest, iBD, ll);
					} else {
						arraycopy(dest, iND, dest, iND+1+ll, iD-iND-ll+2); // move
						dest[iND] = '(';
						dest[iND+1] = ' '; //NOOP for now, later: (byte) (iD-iND);
						arraycopy(src, iS-ll+1, dest, iND+2, ll-1); //FIXME most likely this is wrong as iS still points after FIRST looping
						iD+=2;
						dest[iD++] = ')';
					}
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
