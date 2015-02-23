package bruno.lang.grammar;

public final class Occur {
	
	public static final int MAX_OCCURANCE = 1000;

	/**
	 * <pre>
	 * { min, max }
	 * </pre>
	 */
	public static Occur occur( int min, int max ) {
		return new Occur(min, max);
	}
	
	public static Occur x(int times) {
		return occur(times, times);
	}
	
	public static final Occur never = occur(0, 0);
	
	
	/**
	 * <pre>
	 * *
	 * </pre>
	 */
	public static final Occur once = occur(1 , 1);
	
	/**
	 * <pre>
	 * *
	 * </pre>
	 */
	public static final Occur star = occur(0 , MAX_OCCURANCE);
	
	/**
	 * <pre>
	 * +
	 * </pre>
	 */
	public static final Occur plus = occur(1 , MAX_OCCURANCE);
	
	/**
	 * <pre>
	 * ?
	 * </pre>
	 */
	public static final Occur qmark = occur(0 , 1);

	public final int min;
	public final int max;

	Occur( int min, int max ) {
		super();
		this.min = min;
		this.max = max;
	}
	
	@Override
	public String toString() {
		if (min == star.min && max == star.max) {
			return "*";
		}
		if (min == plus.min && max == plus.max) {
			return "+";
		}
		if (min == qmark.min && max == qmark.max) {
			return "?";
		}
		if (min == max) {
			return "**"+min;
		}
		if (max == MAX_OCCURANCE) {
			return "**"+min+"+";
		}
		return "**"+min+".."+max+"";
	}

}