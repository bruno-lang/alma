package bruno.lang.grammar;

/**
 * An abstract data type for a sequence of tokens for a particular
 * {@link Grammar}.
 * 
 * Each token is represented as a single long value that refers to the kind of
 * token (rule's name), the amount of child tokens and the end position in the
 * input stream of the token. The start position of a token is the end position
 * of the previous token. In that regard whitespace becomes a token as well.
 * 
 * Why long arrays? 
 * 1) a very compact memory representation of an token tree
 * 2) use one block of memory, co-location gives efficient read/writes
 * 3) the most often performed computations actually became very cheap ones
 * 
 * @author jan
 */
public final class Tokens {

	private static final long CHILDREN_MASK = 0x00FF0000;
	private static final long END_MASK = 0x0000FFFF;

	/**
	 * 16bit rule-id / 16bit children / 32bit end index 
	 */
	private final long[] tokens = new long[1024];
	private int current=1;
	
	public int length() {
		return current;
	}
	
	public int children(int index) {
		return (int) ((tokens[++index] >> 16) & CHILDREN_MASK);
	}
	
	/**
	 * @return amount of bytes
	 */
	public int bytes(int index) {
		return endPosition(index) - startPosition(index); // we use an "incomplete" token holding the start so length becomes computable for the "last" token as well.
	}
	
	/**
	 * @return start byte position (inclusive)
	 */
	public int startPosition(int index) {
		return endPosition(--index) ;
	}
	
	/**
	 * @return end byte position (exclusive)
	 */
	public int endPosition(int index) {
		return (int)(tokens[++index] & END_MASK);
	}
	
	/**
	 * This is used when the symbol itself isn't a named rule (a node on its own)
	 */
	public void symbol() {
		tokens[current]++; // as the end index is on the lower 32bits we just increment the end position
	}
	
	public void whitespace(int endPosition) {
		tokens[++current] = endPosition;
	}
	
	/**
	 * In case a path turned out to not match the tokens so far are "trashed" by
	 * going back to the position before the path. The next path tried will than
	 * override the obsolete tokens.
	 */
	public void reset(int index) {
		current = index;
	}
	
	//TODO make this usable as if one works with a tree of tokens 
}
