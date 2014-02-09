package bruno.lang.grammar;

import static bruno.lang.grammar.Grammar.in;
import static bruno.lang.grammar.Grammar.not;
import static bruno.lang.grammar.Grammar.or;
import static bruno.lang.grammar.Grammar.set;
import static bruno.lang.grammar.Grammar.Rule.link;
import static bruno.lang.grammar.Grammar.Rule.literal;
import static bruno.lang.grammar.Grammar.Rule.selection;
import static bruno.lang.grammar.Grammar.Rule.sequence;
import static bruno.lang.grammar.Grammar.Rule.terminal;
import static bruno.lang.grammar.Grammar.Rule.token;

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
		terminal = token(literal("'"), terminal(Grammar.any), terminal(not('\'')).star(), literal("'")).as("terminal"),
		range = sequence(terminal, literal("-"), terminal).as("range"),
		name = token(terminal(or(set('0', '9'), set('a','z'), set('A','Z'), in('_', '-', '\''))).plus()).as("name"),
		not = token(literal("!"), link("atom")).as("not"),
		any = literal(".").as("any"),
		whitespace = literal("_").as("whitespace"),
		eol = literal("$").as("eol"),
		atom = selection(not, any, whitespace, eol, range, terminal, name).as("atom"),

		qmark = literal("?").as("qmark"),
		star = literal("*").as("star"),
		plus = literal("+").as("plus"),
		ellipsis  = literal("..").as("ellipsis"),
		digit = terminal(set('0', '9')).as("-digit"),
		num = digit.plus().as("num"),
		minmax = token(literal("{"), num, literal(","), num, literal("}")).as("minmax"),
		occurrence = selection(minmax, qmark, star, plus).as("occurrence"),
		
		group = sequence(literal("("), link("selection"), literal(")")).as("group"),
		terminals = sequence(literal('['), sequence(terminal).plus(), literal(']')).as("terminals"),
		indent = terminal(in(' ', '\t')).star(),
		elem = selection(ellipsis, token(selection(group, terminals, atom), occurrence.qmark())).as("elem"),
		elems = elem.plus().separate(indent).as("elems"),
		selection = sequence(elems, sequence(literal('|'), elems).star()).as("selection"), 
		separation = sequence(literal('['), name.qmark(), literal(']')).as("separation"),
		colon = literal(":"),
		equal = literal('='),
		definedAs = selection(equal, sequence(colon, colon.qmark(), equal.qmark())),
		rule = sequence(separation.qmark(), name,  definedAs, selection, literal(";").qmark()).as("rule"),
		
		comment = sequence(literal("%"), terminal(not('\n')).plus().as("text")).as("comment"),
		member = selection(comment, rule).as("member"),
		grammar = member.plus().as("grammar");
		
	static final Grammar GRAMMAR = new Grammar(grammar);

	public static Tokenised tokenise(String filename) throws IOException {
		return Tokenised.tokenise(filename, "grammar", GRAMMAR);
	}
}
