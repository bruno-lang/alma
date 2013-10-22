package bruno.lang.grammar;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import bruno.lang.grammar.Grammar.Rule;
import bruno.lang.grammar.Grammar.RuleType;

public class Tokeniser {

	private final Grammar grammar;

	public Tokeniser(Grammar grammar) {
		super();
		this.grammar = grammar;
	}
	
	public Token tokenise(String start, String input) {
		Rule r = grammar.rule(start.intern());
		Token t = tokenise(r, ByteBuffer.wrap(input.getBytes()), 0, true, new Tokens());
		return t;
	}
	
	private Token tokenise(Rule rule, ByteBuffer input, int index, boolean gobbleWhitespace, Tokens tokens) {
		RuleType type = rule.type;
		String name = rule.name;
		if (name.isEmpty()) {
			name = ":"+rule.type.code;
		}
		if (type == RuleType.SYMBOL) {
			if (index >= input.limit())
				return null;
			//System.out.println(input.charAt(index)+"\t"+rule.symbol);
			byte c = input.get(index);
			boolean matches = rule.symbol.matches(c);
			return matches ? new Token(name, index, index+1) : null; 
		}
		//System.out.println(rule);
		if (type == RuleType.TOKEN) {
			Token child = tokenise(rule.elements[0], input, index, false, tokens);
			if (child != null) {
				return new Token(name, index, child.end, Collections.singletonList(child));
			}
			return child;
		}
		if (type == RuleType.ITERATION) {
			int end = index;
			int c = 0;
			List<Token> rep = new ArrayList<>();
			while (c < rule.occur.max) {
				Token t = tokenise(rule.elements[0], input, end, gobbleWhitespace, tokens);
				if (t == null) {
					if (c < rule.occur.min) {
						return null;
					}
					return new Token(name, index, end, rep);
				} else {
					rep.add(t);
					end = t.end;
					c++;
				}
			}
			if (c < rule.occur.min) {
				return null;
			}
			return new Token(name, index, end, rep);
		}
		if (type == RuleType.SEQUENCE) {
			int end = index;
			List<Token> seq = new ArrayList<>();
			for (int i = 0; i < rule.elements.length; i++) {
				end = scanWhitespace(input, end, gobbleWhitespace);
				Rule r = rule.elements[i];
				Token t = tokenise(r, input, end, gobbleWhitespace || !r.tokenish, tokens);
				if (t == null) {
					return null;
				}
				seq.add(t);
				end = t.end;
			}
			return new Token(name, index, end, seq);
		}
		if (type == RuleType.SELECTION) {
			for (Rule r : rule.elements) {
				Token t = tokenise(r, input, index, gobbleWhitespace, tokens);
				if (t != null) {
					return new Token(name, index, t.end, Collections.singletonList(t));
				}
			}
			return null;
		}
		throw new IllegalArgumentException(rule+" has no proper type");
	}
	
	public int scanWhitespace(ByteBuffer input, int index, boolean gobbleWhitespace) {
		while (gobbleWhitespace && index < input.limit() && Character.isWhitespace(input.get(index))) {
			index++;
		}
		return index;
	}
	
	public static Token tokenise(String filename) throws IOException {
		Tokeniser t = new Tokeniser(BNF.GRAMMAR);
		String file = readFile(filename, Charset.forName("UTF-8"));
		return t.tokenise("grammar", file);
	}

	static String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return encoding.decode(ByteBuffer.wrap(encoded)).toString();
	}

	public static void main(String[] args) throws IOException {
		Tokeniser t = new Tokeniser(BNF.GRAMMAR);
		String file = readFile("etc/grammar.grammar", Charset.forName("UTF-8"));
		Token root = t.tokenise("grammar", file);
		System.out.println(root);
		System.out.println(file.length() +" / "+ root.end);
		
	}
}
