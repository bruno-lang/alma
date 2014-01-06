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
	
	public void printBy(Printer printer) {
		Tokens tokens = this.tokens.sequential();
		for (int i = 0; i < tokens.count(); i++) {
			printer.print(tokens, file, i);
		}
	}
}
