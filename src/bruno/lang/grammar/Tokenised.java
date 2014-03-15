package bruno.lang.grammar;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

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

	public static Tokenised tokenise(String src, String start, Grammar grammar)
			throws FileNotFoundException, IOException {
		RandomAccessFile aFile = new RandomAccessFile(src, "r");
		FileChannel in = aFile.getChannel();
		MappedByteBuffer buffer = in.map(FileChannel.MapMode.READ_ONLY, 0, in.size());
		try {
			buffer.load();
			Tokens tokens = Tokeniser.tokenise(buffer, grammar.rule(start.intern()));
			return new Tokenised(buffer, tokens);
		} finally {
			buffer.clear();
			in.close();
			aFile.close();
		}
	}
}
