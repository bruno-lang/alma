package bruno.lang.grammar;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public final class IndexOverlayedFile {

	public final ByteBuffer file;
	public final ParseTree indexOverlay;
	
	public IndexOverlayedFile(ByteBuffer file, ParseTree tree) {
		super();
		this.file = file;
		this.indexOverlay = tree;
	}
	
	@Override
	public String toString() {
		return indexOverlay.toString();
	}

	public static IndexOverlayedFile read(String src, Grammar grammar, String start) throws IOException {
		RandomAccessFile aFile = new RandomAccessFile(src, "r");
		FileChannel in = aFile.getChannel();
		MappedByteBuffer buffer = in.map(FileChannel.MapMode.READ_ONLY, 0, in.size());
		ParseTree tree = new ParseTree(Math.max(512, buffer.capacity()));
		try {
			buffer.load();
			Parser.parse(grammar.rule(start.intern()), tree, buffer);
			if (tree.end() < in.size()) {
				throw new ParseException(buffer, tree.end(), tree.end(), tree);
			}
			return new IndexOverlayedFile(buffer, tree);
		} finally {
			in.close();
			aFile.close();
		}
	}
}
