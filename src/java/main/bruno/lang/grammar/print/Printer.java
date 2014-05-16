package bruno.lang.grammar.print;

import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

import bruno.lang.grammar.Grammar.Rule;
import bruno.lang.grammar.ParseTree;
import bruno.lang.grammar.Parsed;

public final class Printer {

	public static Processor rulePrinter(PrintStream out) {
		return new GenericRulePrinter(out);
	}
	
	public static Processor levelPrinter(PrintStream out) {
		return new LevelPrinter(out);
	}
	
	public static final class ParseTreePrinter implements Processor {
		
		private final PrintStream out;

		public ParseTreePrinter(PrintStream out) {
			super();
			this.out = out;
		}

		@Override
		public void process(Parsed t) {
			final ParseTree tokens = t.tree.sequential();
			for (int i = 0; i < tokens.count(); i++) {
				byte[] indent = new byte[Math.abs(tokens.level(i))];
				Arrays.fill(indent, (byte)' ');
				out.append(new String(indent));
				printColor(out, ANSIColor.GREEN, tokens.rule(i).name);
				out.append(' ');
				printColor(t.file, out, ANSIColor.BLUE, tokens.start(i), tokens.end(i));
				out.append('\n');
			}
		}

	}
	
	public static final class RainbowPrinter implements Processor {
		
		private final PrintStream out;
		private int color;

		public RainbowPrinter(PrintStream out) {
			this.out = out;
		}
		
		@Override
		public void process(Parsed t) {
			final ParseTree tokens = t.tree.sequential();
			for (int i = 0; i < tokens.count(); i++) {
				printColor(t.file, out, ANSIColor.rainbow(color++), tokens.start(i), tokens.end(i));
			}
		}
		
	}
	
	public static abstract class ColorPrinter implements Processor {

		abstract void print(ParseTree tokens, ByteBuffer in, int index);
		
		abstract void print(String s);

		@Override
		public void process(Parsed t) {
			ParseTree tokens = t.tree.sequential();
			for (int i = 0; i < tokens.count(); i++) {
				print(tokens, t.file, i);
			}
			print(ANSIColor.BLACK+"\n"+ANSIColor.RESET);
		}
	}
	
	private static final class GenericRulePrinter extends ColorPrinter {

		private final PrintStream out;
		
		GenericRulePrinter(PrintStream out) {
			this.out = out;
		}

		@Override
		public void print(ParseTree tokens, ByteBuffer in, int index) {
			int s = tokens.start(index);
			int e = tokens.end(index);
			if (e == s) {
				return;
			}
			int l = tokens.level(index);
			if (l < 0) {
				printColor(in, out, ANSIColor.BLACK+ANSIColor.BOLD, s, e);
				return;
			}
			Rule r = tokens.rule(index);
			printColorBlock(in, out, ANSIColor.rainbow(r.name.hashCode()/2-1), s, e);
			out.append(ANSIColor.RESET);
		}
		
		@Override
		void print(String s) {
			out.append(s);
		}
	}
	
	private static final class LevelPrinter extends ColorPrinter {
		
		private final PrintStream out;
		
		LevelPrinter(PrintStream out) {
			this.out = out;
		}
		
		@Override
		public void print(ParseTree tokens, ByteBuffer in, int index) {
			int s = tokens.start(index);
			int e = tokens.end(index);
			if (e == s) {
				return;
			}
			int l = tokens.level(index);
			String color = ANSIColor.rainbow(l);
			if (l < 0) {
				printColor(in, out, color, s, e);
				return;
			}
			printColorBlock(in, out, color, s, e);
		}
		
		@Override
		void print(String s) {
			out.append(s);
		}

	}
	
	private static void printColorBlock(ByteBuffer input, PrintStream out, String color, int s, int e) {
		if (s+1 == e) {
			printColor(input, out, color + ANSIColor.BOLD+ ANSIColor.UNDERLINED, s, e);
		} else {
			printColor(input, out, color+ANSIColor.BOLD, s, s+1);
			printColor(input, out, color, s+1, e-1);
			printColor(input, out, color+ANSIColor.UNDERLINED, e-1, e);
		}
	}

	private static void printColor(ByteBuffer input, PrintStream out, String color, int start, int end) {
		if (start < end) {
			printColor(out, color, string(input, start, end));
		}
	}

	private static void printColor(PrintStream out, String color, String text) {
		final int l = text.length();
		int lf = text.indexOf('\n');
		int s = 0;
		while (lf >= 0 || s < l) {
			int e = lf < 0 ? l : lf+1;
			out.append(color);
			out.append(text, s, e);
			s = e;
			lf = text.indexOf('\n', e);
			out.append(ANSIColor.RESET);
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
