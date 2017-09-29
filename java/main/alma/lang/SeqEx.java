package alma.lang;

import static alma.lang.Parser.mismatch;

/**
 * Strictly sequential expression (or pattern).
 * Or more exact a "Sequence Matching Machine".
 * 
 * A form of a simpler "regex".
 *
 * The code is just moved here to not clutter up the {@link Parser} with matching too much.
 */
public final class SeqEx {

	// TODO a "scan", e.g. /"/ would eat all bytes until "
	// should that support sets or sequences or both? and an exclude, like scan for " unless it follows X
	// would be great to replace sets with something easier yet more powerful
	// remember that a scan can be used to make sure the next matches the scan, _ can than eat it
	// to allow to force direct match via scan there must be control over what is allowed to be skipped
	// when masks are used, using 2 longs, high and low, is better than an array since / and % are expensive compared to <<
	
	// we always try to find the SHORTEST possible match for the pattern
	// examples: 
	// '"""'  ~  '"""'   => '"""' /~"""/ '"""'
	// '"' ~ '"'         => '"' /~"/ '"'
	// '' "^'" ''        => '' /~'/ ''
	// '%' < ~ '%'       => '%' < /~%/ '%'
	
	// ~ here just means at this position in the pattern you may scan forward skipping non matches
	// or in other words try to match the following as soon as possible from the current position
	// using ~ at the very end does not eat everything but nothing at it is directly satisfied since we reach the end of the pattern afterwards
	// similar a _ could mean "any one byte"
	// than in fact 'xxx' is just a special case of /xxx/, we literally want to match the exact sequence xxx
	
	// Op        = ['-+/*=><&']   => /{-+//*=><&}/
	// here sets are given using { }, / becomes //
	// ranges are given as a-zA-Z and so forth
	// excluding sets use }xyz{ instead
	// mixed sets are not supported
	
	// Operator  = \Op [\Op '!?|']{0-2} | [\Op '|']{2-3}
	// when a 0-2 should be done one could use [[ ]] to mark a section as optional
	// like  /{-+//*=><&}[{-+//*=><&!?}{-+//*=><&!?}]/
	
	// numerical = Number (Number | [s'^%+-*!?#=~&|<>()[]{}.,;\"`'a]^)*
	// -Number       = ['+-']? \9+ ('_' \9+ | ',' \9\9\9)* ('.' \9+)?
	// this suggests that 0-9 should get a shortcut, # again
	// to match letters with special meaning one could put them into a set, like {+} to match a plus sign
	// and the set can also be used to match { as an empty set does not make sense , the first char after always is literally, so { is {{ } and } is {} }, however if both { and  } should be contained this should work {{}}, so when a { is the first the next is also literal, space can be used to fill without matching something else 
	// we get: /{+-}#+({_,}#+)*[.#+]
	// @ could be A-Za-z
	
	//summary:
	// ~ = skip any number of bytes to make the following match (no backtracking!)
	// _ = any one byte
	// # = {0-9}
	// @ = {a-zA-Z}
	// w = {a-z}
	// W = {A-Z}
	// + = previous position as often as possible
	// * = previous position as often as possible, even 0 times
	// ? = previous position 0-1x
	// (...) = group a sequence for +/* apply to whole sequence
	// [...] = (...) zero or one times
	// {...} = a set of included symbols, all bytes are literally, / is //
	// !{...} = a set of excluded symbols, all bytes are literally, / is //, a set with {} is {}{} (since first is always ignored, and then even the { is literal, just the } isn't)
	// to use any of the special symbols literally wrap them into a set, {~},  
	
	// there is no OR! its strictly a flexible sequence pattern, both flexible in length and in bytes possible at each position

	// using ~ more than once is effectively the same as splitting the pattern before ~ and matching another pattern afterwards
	// all ~ does is to search for the first match of its tail (where a tail ends at the end of the pattern or the next ~)
	
	public static int match(byte[] pattern, int p0, byte[] data, int d0) {
		int pn = p0;
		int dn = d0;
		while (pn < pattern.length) {
			byte op  = pattern[pn++];
			switch (op) {
			// default is matching things literally unless its a special symbol
			case '~': break;
			case '_': dn++; break;
			case '@': break;
			case '#': break;
			case 'w': break;
			case 'W': break;
			case '(': break; // use recursion
			case '[': break;
			case '{': break;
			case '!': break;
			case '/': return dn;
			default: if (op == data[dn]) { dn++; } else { return mismatch(dn); }
			}
		}
		return dn;
	}
	
}
