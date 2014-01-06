package bruno.lang.grammar;

import java.nio.ByteBuffer;

public interface Printer {

	void print(Tokens tokens, ByteBuffer in, int index);
}
