package bruno.lang.grammar;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public final class Parsed {

	public final ByteBuffer file;
	public final ParseTree tree;
	
	public Parsed(ByteBuffer file, ParseTree tree) {
		super();
		this.file = file;
		this.tree = tree;
	}
	
	public String text(int index) {
		int s = tree.start(index);
		int e = tree.end(index);
		int l = e-s;
		byte[] dst = new byte[l];
		file.position(s);
		file.get(dst);
		return new String(dst);
	}

	public static Parsed parse(String src, String start, Grammar grammar)
			throws FileNotFoundException, IOException {
		RandomAccessFile aFile = new RandomAccessFile(src, "r");
		FileChannel in = aFile.getChannel();
		MappedByteBuffer buffer = in.map(FileChannel.MapMode.READ_ONLY, 0, in.size());
		try {
			buffer.load();
			ParseTree tree = Parser.parse(buffer, grammar.rule(start.intern()));
			return new Parsed(buffer, tree);
		} finally {
			buffer.clear();
			in.close();
			aFile.close();
		}
	}
}
