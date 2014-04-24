package bruno.lang.grammar;

import java.nio.ByteBuffer;

public interface Pattern {

	int NOT_MACHTING = -1;
	
	int length(ByteBuffer input, int position);
	
}