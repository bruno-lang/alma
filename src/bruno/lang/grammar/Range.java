package bruno.lang.grammar;

public final class Range {
	
	public final boolean not;
	public final int minCodePoint;
	public final int maxCodePoint;
	
	public Range(boolean not, int minCodePoint, int maxCodePoint) {
		super();
		this.not = not;
		this.minCodePoint = minCodePoint;
		this.maxCodePoint = maxCodePoint;
	}
	
	// use ranges to construct a terminal having an int[] where the first 4 are a 128 bit wide bitmask for ASCII, all following are 2 int pairs using negative numbers to model exclusive ranges
	
	// combine ranges: excluded must be ANDed, inclusive must be ORed. 
	// Mask:
	// 1. is there any negative range? start from FFFF FFFF FFFF FFFF else 0000 0000 0000 0000
	// 2. set all exclusive ranges to 0
	// 3. set all inclusive ranges to 1
	// Pairs:
	// sort pairs by range start (negative/exclusive first since we have to check them all)
	// when checking: as soon as actual is within a exclusive range one has to check if there isn't a including range.
	
	// OR: build masks and sets in sequence of appearance? -> more powerful but confusing - than it might be better to allow to subtract ranges/utf8-sets from another 
}
