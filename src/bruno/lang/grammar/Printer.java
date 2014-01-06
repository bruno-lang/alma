package bruno.lang.grammar;

import java.io.PrintStream;
import java.nio.ByteBuffer;

public class Printer {

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
	
	public static void print(Tokens tokens, ByteBuffer input, PrintStream out) {
		tokens = tokens.sequential();
		for (int i = 0; i < tokens.count(); i++) {
			printRainbow(tokens, input, out, i);
		}
	}
	
	private static void printRainbow(Tokens tokens, ByteBuffer input, PrintStream out, int index) {
		int s = tokens.start(index);
		int e = tokens.end(index);
		if (e == s) {
			return;
		}
		int level = tokens.level(index);
		String color = RAINBOW[Math.abs(level) % RAINBOW.length];
		if (level < 0) {
			printColor(input, out, color, s, e);
			return;
		}
		if (s+1 == e) {
			printColor(input, out, color + BOLD+ UNDERLINED, s, e);
		} else {
			printColor(input, out, color+BOLD, s, s+1);
			printColor(input, out, color, s+1, e-1);
			printColor(input, out, color+UNDERLINED, e-1, e);
		}
	}

	private static void printColor(ByteBuffer input, PrintStream out, String color, int start, int end) {
		if (start < end) {
			String text = string(input, start, end);
			out.append(color);
			out.append(text);
			out.append(RESET);
		}
	}
	

	private static String string(ByteBuffer input, int start, int end) {
		byte[] p = new byte[end-start];
		int j = 0;
		for (int i = start; i < end; i++) {
			p[j++] = input.get(i);
		}
		return new String(p);
	}
}
