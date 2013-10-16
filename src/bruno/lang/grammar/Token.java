package bruno.lang.grammar;

import java.util.Collections;
import java.util.List;

public final class Token {

	public final String name;
	public final int start;
	public final int end;
	public final List<Token> nexts;
	
	public Token(String name, int start, int end) {
		this(name, start, end, Collections.<Token>emptyList());
	}
	
	public Token(String name, int start, int end, List<Token> nexts) {
		super();
		this.name = name;
		this.start = start;
		this.end = end;
		this.nexts = nexts;
	}

	public Token addNext(Token next) {
		if (next != null) {
			nexts.add(next);
		}
		return this;
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		toString(b, "");
		return b.toString();
	}
	
	private void toString(StringBuilder b, String indent) {
		b.append(indent);
		b.append(name).append(' ').append(start).append(':').append(end).append('\n');
		indent += " ";
		for (Token c : nexts) {
			c.toString(b, indent);
		}
	}
}
