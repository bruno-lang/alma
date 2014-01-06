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
	
}
