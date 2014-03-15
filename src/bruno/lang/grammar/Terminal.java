package bruno.lang.grammar;

import java.nio.ByteBuffer;

public interface Terminal {

	int NOT_MACHTING = -1;
	
	int length(ByteBuffer input, int position);
	
	//TODO? make this Pattern and use fix way of dealing with range based utf8 sets 
}