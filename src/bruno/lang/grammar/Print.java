package bruno.lang.grammar;

import java.io.PrintStream;
import java.nio.ByteBuffer;

import bruno.lang.grammar.Grammar.Rule;

public final class Print {

	public static Printer rulePrinter(PrintStream out) {
		return new RulePrinter(out);
	}
	
	public static Printer levelPrinter(PrintStream out) {
		return new LevelPrinter(out);
	}
	
	private static final class RulePrinter implements Printer {

		private final PrintStream out;
		
		RulePrinter(PrintStream out) {
			super();
			this.out = out;
		}

		@Override
		public void print(Tokens tokens, ByteBuffer in, int index) {
			int s = tokens.start(index);
			int e = tokens.end(index);
			if (e == s) {
				return;
			}
			int l = tokens.level(index);
			if (l < 0) {
				printColor(in, out, ANSI.BLACK+ANSI.BOLD, s, e);
				return;
			}
			Rule r = tokens.rule(index);
			printColorBlock(in, out, ANSI.rainbow(r.id()/2-1), s, e);
		}
	}
	
	private static final class LevelPrinter implements Printer {
		
		private final PrintStream out;
		
		LevelPrinter(PrintStream out) {
			super();
			this.out = out;
		}
		
		@Override
		public void print(Tokens tokens, ByteBuffer in, int index) {
			int s = tokens.start(index);
			int e = tokens.end(index);
			if (e == s) {
				return;
			}
			int l = tokens.level(index);
			String color = ANSI.rainbow(l);
			if (l < 0) {
				printColor(in, out, color, s, e);
				return;
			}
			printColorBlock(in, out, color, s, e);
		}
	}
	
	private static void printColorBlock(ByteBuffer input, PrintStream out, String color, int s, int e) {
		if (s+1 == e) {
			printColor(input, out, color + ANSI.BOLD+ ANSI.UNDERLINED, s, e);
		} else {
			printColor(input, out, color+ANSI.BOLD, s, s+1);
			printColor(input, out, color, s+1, e-1);
			printColor(input, out, color+ANSI.UNDERLINED, e-1, e);
		}
	}

	private static void printColor(ByteBuffer input, PrintStream out, String color, int start, int end) {
		if (start < end) {
			String text = string(input, start, end);
			out.append(color);
			out.append(text);
			out.append(ANSI.RESET);
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
