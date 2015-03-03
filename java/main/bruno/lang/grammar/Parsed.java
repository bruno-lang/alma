package bruno.lang.grammar;

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
	
	@Override
	public String toString() {
		return tree.toString();
	}

	public static Parsed parse(String src, Grammar grammar, String start) throws IOException {
		RandomAccessFile aFile = new RandomAccessFile(src, "r");
		FileChannel in = aFile.getChannel();
		MappedByteBuffer buffer = in.map(FileChannel.MapMode.READ_ONLY, 0, in.size());
		ParseTree tree = new ParseTree(Math.max(512, buffer.capacity()));
		try {
			buffer.load();
			Parser.parse(buffer, grammar.rule(start.intern()), tree);
			return new Parsed(buffer, tree);
		} finally {
			in.close();
			aFile.close();
		}
	}
}
