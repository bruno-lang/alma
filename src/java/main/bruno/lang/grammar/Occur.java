package bruno.lang.grammar;

public final class Occur {
	
	public static final int MAX_OCCURANCE = 1000;
	
	public static final Occur ONCE = occur(1 , 1);

	/**
	 * <pre>
	 * { min - max }
	 * </pre>
	 */
	public static Occur occur( int min, int max ) {
		return new Occur(min, max);
	}
	
	public final int min;
	public final int max;

	Occur( int min, int max ) {
		super();
		this.min = min;
		this.max = max;
	}
	
	@Override
	public String toString() {
		if (min == 0 && max == MAX_OCCURANCE) {
			return "*";
		}
		if (min == 1 && max == MAX_OCCURANCE) {
			return "+";
		}
		if (min == 0 && max == 1) {
			return "?";
		}
		if (min == max) {
			return "{"+min+"}";
		}
		if (max == MAX_OCCURANCE) {
			return "{"+min+"-*}";
		}
		return "{"+min+"-"+max+"}";
	}

}