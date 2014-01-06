package bruno.lang.grammar;


public class ANSI {

	// Foreground color for ANSI black
	static final String BLACK = new String("\033[30m");
	// Background color for ANSI black
	static final String BLACK_B = new String("\033[40m");
	// ANSI blink
	static final String BLINK = new String("\033[5m");
	// Foreground color for ANSI blue
	static final String BLUE = new String("\033[34m");
	// Background color for ANSI blue
	static final String BLUE_B = new String("\033[44m");
	// ANSI bold
	static final String BOLD = new String("\033[1m");
	// Foreground color for ANSI cyan
	static final String CYAN = new String("\033[36m");
	// Background color for ANSI cyan
	static final String CYAN_B = new String("\033[46m");
	// Foreground color for ANSI green
	static final String GREEN = new String("\033[32m");
	// Background color for ANSI green
	static final String GREEN_B = new String("\033[42m");
	// ANSI invisible
	static final String INVISIBLE = new String("\033[8m");
	// Foreground color for ANSI magenta
	static final String MAGENTA = new String("\033[35m");
	// Background color for ANSI magenta
	static final String MAGENTA_B = new String("\033[45m");
	// Foreground color for ANSI red
	static final String RED = new String("\033[31m");
	// Background color for ANSI red
	static final String RED_B = new String("\033[41m");
	// Reset ANSI styles
	static final String RESET = new String("\033[0m");
	// ANSI reversed
	static final String REVERSED = new String("\033[7m");
	// ANSI underlines
	static final String UNDERLINED = new String("\033[4m");
	// Foreground color for ANSI white
	static final String WHITE = new String("\033[37m");
	// Background color for ANSI white
	static final String WHITE_B = new String("\033[47m");
	// Foreground color for ANSI yellow
	static final String YELLOW = new String("\033[33m");
	// Background color for ANSI yellow
	static final String YELLOW_B = new String("\033[43m");
	
	static final String[] RAINBOW = { BLUE, CYAN, GREEN, YELLOW, RED, MAGENTA };
	static final String[] RAINBOW_B = { BLUE_B+WHITE, CYAN_B, GREEN_B, YELLOW_B, RED_B, MAGENTA_B };
	
	public static String rainbow(int no) {
		return RAINBOW[Math.abs(no) % RAINBOW.length];
	}
	
}
