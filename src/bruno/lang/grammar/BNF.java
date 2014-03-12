package bruno.lang.grammar;

import static bruno.lang.grammar.Grammar.Rule.literal;
import static bruno.lang.grammar.Grammar.Rule.ref;
import static bruno.lang.grammar.Grammar.Rule.selection;
import static bruno.lang.grammar.Grammar.Rule.seq;
import static bruno.lang.grammar.Grammar.Rule.symbol;
import static bruno.lang.grammar.Grammar.Rule.terminal;
import static bruno.lang.grammar.Grammar.Rule.token;
import static bruno.lang.grammar.Terminals.in;
import static bruno.lang.grammar.Terminals.not;
import static bruno.lang.grammar.Terminals.or;
import static bruno.lang.grammar.Terminals.set;

import java.io.IOException;

import bruno.lang.grammar.Grammar.Rule;

/**
 * An extended variant of the BNF grammar.
 * 
 * @author jan
 *
 */
public final class BNF {

	static final Rule
		terminal = token(symbol("'"), terminal(Terminals.any), terminal(not('\'')).star(), symbol("'")).as("terminal"),
		range = seq(terminal, symbol("-"), terminal).as("range"),
		name = token(terminal(or(set('0', '9'), set('a','z'), set('A','Z'), in('_', '-', '\''))).plus()).as("name"),
		not = token(symbol("!"), ref("atom")).as("not"),
		any = symbol(".").as("any"),
		whitespace = symbol("_").as("whitespace"),
		eol = symbol("$").as("eol"),
		atom = selection(not, any, whitespace, eol, range, terminal, name).as("atom"),

		qmark = symbol("?").as("qmark"),
		star = symbol("*").as("star"),
		plus = symbol("+").as("plus"),
		ellipsis  = symbol("..").as("ellipsis"),
		digit = terminal(set('0', '9')).as("-digit"),
		num = digit.plus().as("num"),
		minmax = token(symbol("{"), num, symbol(","), num, symbol("}")).as("minmax"),
		occurrence = selection(minmax, qmark, star, plus).as("occurrence"),
		
		group = seq(symbol("("), ref("selection"), symbol(")")).as("group"),
		terminals = seq(literal('['), seq(terminal).plus(), literal(']')).as("terminals"),
		indent = terminal(in(' ', '\t')).star(),
		elem = selection(ellipsis, token(selection(group, terminals, atom), occurrence.qmark())).as("elem"),
		elems = elem.plus().separate(indent).as("elems"),
		selection = seq(elems, seq(literal('|'), elems).star()).as("selection"), 
		separation = seq(literal('['), name.qmark(), literal(']')).as("separation"),
		colon = symbol(":"),
		equal = literal('='),
		definedAs = selection(equal, seq(colon, colon.qmark(), equal.qmark())),
		rule = seq(separation.qmark(), name,  definedAs, selection, symbol(";").qmark()).as("rule"),
		
		comment = seq(symbol("%"), terminal(not('\n')).plus().as("text")).as("comment"),
		member = selection(comment, rule).as("member"),
		grammar = member.plus().as("grammar");
		
	static final Grammar GRAMMAR = new Grammar(grammar);

	public static Tokenised tokenise(String filename) throws IOException {
		return Tokenised.tokenise(filename, "grammar", GRAMMAR);
	}
	
	//TODO
	//- more exact difference between single char literals and longer literals
	//- new syntax for separation, e.g.: {foo bar}:indent or {foo bar}
	//- occur as: #0-2
	// have special rules named \* for special characters \t \s \n (names must be allowed to start with \)
	//- any tab: ~ 
	//- any whitespace: _ 
	//- space: ' '
	//- remove $ syntax
	//- add unicode: \u0000 ; use this to define \r \n \t simply as alias rule (e.g. \t = \u0009)  
	
	/*
	 * Separation
	 * 
	 * '{' selection { '}' [':' ( group | optional ) ]}
	 */
}
