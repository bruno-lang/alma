package bruno.lang.grammar;

import java.nio.ByteBuffer;

public class BParseTree {

	private final ByteBuffer lang;
	private final int[] rules;
	private final int[] starts;
	private final int[] ends;
	private final int[] levels;
	
	private final int[] indexStack = new int[50];
	
	private int level = -1;
	private int top = -1;
	
	public BParseTree(ByteBuffer lang, int length) {
		super();
		this.lang = lang;
		this.rules = new int[length];
		this.starts = new int[length];
		this.ends = new int[length];
		this.levels = new int[length];
	}

	public void push(int rule, int start) {
		starts[++top] = start;
		ends[top] = start;
		rules[top] = rule;
		level++;
		levels[top] = level;
		indexStack[level] = top;
	}
	
	public int end(int index) {
		return ends[index];
	}
	
	public int start(int index) {
		return starts[index];
	}
	
	public int level(int index) {
		return levels[index];
	}
	
	public int rule(int index) {
		return rules[index];
	}
	
	public int end() {
		return ends[0];
	}
	
	public int count() {
		return top+1;
	}

	public void pop() {
		top = indexStack[level]-1;
		level--;
	}

	public void done(int end) {
		ends[indexStack[level]] = end;
		level--;
	}
	
	public void erase(int position) {
		while (top > 0 && ends[top] > position) {
			top--;
		}
	}
	
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		for (int i = 0; i <= top; i++) {
			// indent
			for (int g = 0; g < levels[i]; g++) {
				b.append(' ');
			}
			// name
			int l = lang.get(rules[i]+1)-2;
			int j = rules[i] + 4;
			for (int h = l; h > 0; h--) {
				b.append((char)lang.get(j++));
			}
			b.append(' ').append(starts[i]).append('-').append(ends[i]).append('\n');
		}
		return b.toString();
	}
}
