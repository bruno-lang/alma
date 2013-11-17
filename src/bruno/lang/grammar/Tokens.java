package bruno.lang.grammar;

import java.io.PrintStream;
import java.util.Arrays;

import bruno.lang.grammar.Grammar.Rule;


/**
 * An abstract data type for a sequence of tokens for a particular
 * {@link Grammar}.
 * 
 * Each token is represented as a single long value that refers to the kind of
 * token (rule's name), the amount of child tokens and the end position in the
 * input stream of the token. The start position of a token is the end position
 * of the previous token. In that regard whitespace becomes a token as well.
 * 
 * Why long arrays? 
 * 1) a very compact memory representation of an token tree
 * 2) use one block of memory, co-location gives efficient read/writes
 * 3) the most often performed computations actually became very cheap ones
 * 
 * @author jan
 */
public final class Tokens {

	private final Rule[] rules;
	private final int[] starts;
	private final int[] ends;
	private final int[] levels;
	
	private final int[] indexStack = new int[50];
	
	private int level = -1;
	private int top = -1;
	
	public Tokens(int length) {
		super();
		this.rules = new Rule[length];
		this.starts = new int[length];
		this.ends = new int[length];
		this.levels = new int[length];
	}

	public void push(Rule rule, int start) {
		starts[++top] = start;
		ends[top] = start;
		rules[top] = rule;
		level++;
		levels[top] = level;
		indexStack[level] = top;
	}
	
	public int end() {
		return ends[0];
	}
	
	public int count() {
		return top;
	}

	public void pop() {
		top = indexStack[level]-1;
		level--;
	}

	public void done(int end) {
		ends[indexStack[level]] = end;
		level--;
	}
	
	@Override
	public String toString() {
		if (top < 0)
			return "(empty)";
		StringBuilder b = new StringBuilder();
		for (int i = 0; i <= top; i++) {
			toString(b, "", i);
		}
		return b.toString();
	}
	
	private void toString(StringBuilder b, String indent, int index) {
		char[] ind = new char[levels[index]];
		Arrays.fill(ind, ' ');
		b.append(ind).append(rules[index].name).append(' ').append(starts[index]).append(':').append(ends[index]).append('\n');
	}
	
	public void print(int position, PrintStream out) {
		int index = tokenIndexFor(position);
		StringBuilder b = new StringBuilder();
		toString(b, "", index);
		out.println(b);
	}

	private int tokenIndexFor(int position) {
		int index = Math.abs(Arrays.binarySearch(starts, 0, top, position));
		while (index > 0 && starts[index] > position) {
			index--;
		}
		return index;
	}
}
