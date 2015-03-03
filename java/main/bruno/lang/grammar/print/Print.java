package bruno.lang.grammar.print;

import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

import bruno.lang.grammar.Grammar.Rule;
import bruno.lang.grammar.ParseTree;
import bruno.lang.grammar.IndexOverlayedFile;

public final class Print {

	public static Highlighter highlighter(PrintStream out) {
		return new Highlighter(out);
	}
	
	public static LevelPrinter levelPrinter(PrintStream out) {
		return new LevelPrinter(out);
	}
	
	public static final class ParseTreePrinter {
		
		private final PrintStream out;

		public ParseTreePrinter(PrintStream out) {
			super();
			this.out = out;
		}

		public void print(IndexOverlayedFile f) {
			final ParseTree tree = f.indexOverlay.sequential();
			for (int i = 0; i < tree.count(); i++) {
				byte[] indent = new byte[Math.abs(tree.level(i))];
				Arrays.fill(indent, (byte)' ');
				out.append(new String(indent));
				printColor(out, ANSIColor.GREEN, tree.rule(i).name);
				out.append(' ');
				printColor(f.file, out, ANSIColor.BLUE, tree.start(i), tree.end(i));
				out.append('\n');
			}
		}

	}
	
	public static final class RainbowPrinter {
		
		private final PrintStream out;
		private int color;

		public RainbowPrinter(PrintStream out) {
			this.out = out;
		}
		
		public void print(IndexOverlayedFile f) {
			final ParseTree tree = f.indexOverlay.sequential();
			for (int i = 0; i < tree.count(); i++) {
				printColor(f.file, out, ANSIColor.rainbow(color++), tree.start(i), tree.end(i));
			}
		}
		
	}
	
	public static abstract class ColorPrinter {

		abstract void print(ParseTree tree, ByteBuffer in, int index);
		
		abstract void print(String s);

		public void print(IndexOverlayedFile f) {
			ParseTree tree = f.indexOverlay.sequential();
			for (int i = 0; i < tree.count(); i++) {
				print(tree, f.file, i);
			}
			print(ANSIColor.BLACK+"\n"+ANSIColor.RESET);
		}
	}
	
	public static final class Highlighter extends ColorPrinter {

		private final PrintStream out;
		
		Highlighter(PrintStream out) {
			this.out = out;
		}

		@Override
		public void print(ParseTree tree, ByteBuffer input, int index) {
			int s = tree.start(index);
			int e = tree.end(index);
			if (e == s) {
				return;
			}
			int l = tree.level(index);
			if (l < 0) {
				printColor(input, out, ANSIColor.BLACK+ANSIColor.BOLD, s, e);
				return;
			}
			Rule r = tree.rule(index);
			printColorBlock(input, out, ANSIColor.rainbow(r.name.hashCode()/2-1), s, e);
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
		public void print(ParseTree tree, ByteBuffer input, int index) {
			int s = tree.start(index);
			int e = tree.end(index);
			if (e == s) {
				return;
			}
			int l = tree.level(index);
			String color = ANSIColor.rainbow(l);
			if (l < 0) {
				printColor(input, out, color, s, e);
				return;
			}
			printColorBlock(input, out, color, s, e);
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
		if (color.endsWith(ANSIColor.UNDERLINED) && text.trim().isEmpty()) {
			color = ANSIColor.RESET;
		}
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
