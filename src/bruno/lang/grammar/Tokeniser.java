package bruno.lang.grammar;

import static bruno.lang.grammar.Grammar.in;
import static bruno.lang.grammar.Grammar.not;
import static bruno.lang.grammar.Grammar.or;
import static bruno.lang.grammar.Grammar.set;
import static bruno.lang.grammar.Grammar.Rule.decision;
import static bruno.lang.grammar.Grammar.Rule.group;
import static bruno.lang.grammar.Grammar.Rule.link;
import static bruno.lang.grammar.Grammar.Rule.rule;
import static bruno.lang.grammar.Grammar.Rule.terminal;
import static bruno.lang.grammar.Grammar.Rule.token;

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

	static Grammar grammarGrammar;
	
	static {
		Rule terminal = token("terminal", terminal("'"), terminal(Grammar.any), terminal(not('\'')).star(), terminal("'"));
		Rule range = rule("range", terminal, terminal("-"), terminal);
		Rule name = token("name", terminal(or(set('0', '9'), set('a','z'), set('A','Z'), in('_', '-', '\''))).plus());
		Rule not = token("not", terminal("!"), link("atom"));
		Rule any = token("any", terminal("."));
		Rule atom = decision("atom", not, any, range, terminal, name);

		Rule qmark = token("qmark", terminal("?"));
		Rule star = token("star", terminal("*"));
		Rule plus = token("plus", terminal("+"));
		Rule ellipsis  = token("ellipsis", terminal(".."));
		Rule digit = token("digit", terminal(set('0', '9')));
		Rule num = token("num", digit.plus());
		Rule minmax = token("minmax", terminal("{"), num, terminal(","), num, terminal("}") );
		Rule occurrence = decision("occurrence", minmax, qmark, star, plus, ellipsis);
		
		Rule _parts = link("parts");
		Rule token = rule("token", terminal("["), _parts, terminal("]"));
		Rule group = rule("group", terminal("("), _parts, terminal(")"));
		Rule part = decision("part", group, token, atom);
		Rule parts = rule("parts", part, occurrence.qmark(), group(terminal("|").qmark(), _parts).qmark());
		Rule rule = rule("rule", name,  terminal(":"), parts, terminal(";"));
		
		Rule comment = rule("comment", terminal("%"), terminal(not('\n')).plus());
		Rule member = decision("member", comment, rule);
		Rule grammar = rule("grammar", member.plus());
		grammarGrammar = new Grammar(grammar, member, comment, rule, name, parts, atom, digit, terminal,
				range, token, group, qmark, star, plus, num, minmax,
				occurrence, not, part, any);
	}
	
	private final Grammar grammar;

	public Tokeniser(Grammar grammar) {
		super();
		this.grammar = grammar;
	}
	
	public Token tokenise(String start, String input) {
		Rule r = grammar.rule(start.intern());
		Token t = tokenise(r, input, 0, true);
		return t;
	}

	public Token tokenise(Rule rule, String input, int index, boolean gobbleWhitespace) {
		RuleType type = rule.type;
		String name = rule.name;
		if (name.isEmpty()) {
			name = ":"+rule.type.code;
		}
		//System.out.println(index +" "+input.charAt(index));
		if (type == RuleType.SYMBOL) {
			if (index >= input.length())
				return null;
			char c = input.charAt(index);
			boolean matches = rule.symbol.matches(Grammar.toByte(c));
			return matches ? new Token(name, index, index+1) : null; 
		}
		if (type == RuleType.TOKEN) {
			Token child = tokenise(rule.elements[0], input, index, false);
			if (child != null) {
				return new Token(name, index, child.end, Collections.singletonList(child));
			}
			return child;
		}
		if (type == RuleType.REPETITION) {
			int end = index;
			int c = 0;
			List<Token> rep = new ArrayList<>();
			while (c < rule.occur.max) {
				Token t = tokenise(rule.elements[0], input, end, gobbleWhitespace);
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
				Token t = tokenise(r, input, end, gobbleWhitespace);
				if (t == null) {
					return null;
				}
				seq.add(t);
				end = t.end;
			}
			return new Token(name, index, end, seq);
		}
		if (type == RuleType.DECISION) {
			for (Rule r : rule.elements) {
				Token t = tokenise(r, input, index, gobbleWhitespace);
				if (t != null) {
					return new Token(name, index, t.end, Collections.singletonList(t));
				}
			}
			return null;
		}
		throw new IllegalArgumentException(rule+" has no proper type");
	}
	
	public int scanWhitespace(String input, int index, boolean gobbleWhitespace) {
		while (gobbleWhitespace && index < input.length() && Character.isWhitespace(input.charAt(index))) {
			index++;
		}
		return index;
	}
	
	public static void main(String[] args) throws IOException {
		Tokeniser t = new Tokeniser(grammarGrammar);
		
		String file = readFile("etc/grammar.grammar", Charset.forName("UTF-8"));
		Token root = t.tokenise("grammar", file);
		System.out.println("=============================");
		System.out.println(root);
		System.out.println(file.length());
	}

	static String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return encoding.decode(ByteBuffer.wrap(encoded)).toString();
	}

}
