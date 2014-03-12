package bruno.lang.grammar;

import java.nio.ByteBuffer;

public interface Terminal {

	int length(ByteBuffer input, int position);
}