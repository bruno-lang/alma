package bruno.lang.grammar;

import java.nio.ByteBuffer;

public final class Tokenised {

	public final ByteBuffer file;
	public final Tokens tokens;
	
	public Tokenised(ByteBuffer file, Tokens tokens) {
		super();
		this.file = file;
		this.tokens = tokens;
	}
	
	public String text(int index) {
		int s = tokens.start(index);
		int e = tokens.end(index);
		int l = e-s;
		byte[] dst = new byte[l];
		file.position(s);
		file.get(dst);
		return new String(dst);
	}
}
