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
import bruno.lang.grammar.Grammar.Rule;

/**
 * An extended variant of the BNF grammar.
 * 
 * @author jan
 *
 */
public final class BNF {

	static final Grammar GRAMMAR;
	
	static {
		Rule terminal = token(literal("'"), terminal(Grammar.any), terminal(not('\'')).star(), literal("'")).as("terminal");
		Rule range = sequence(terminal, literal("-"), terminal).as("range");
		Rule name = token(terminal(or(set('0', '9'), set('a','z'), set('A','Z'), in('_', '-', '\''))).plus()).as("name");
		Rule not = token(literal("!"), link("atom")).as("not");
		Rule any = literal(".").as("any");
		Rule whitespace = literal("_").as("whitespace");
		Rule eol = literal("$").as("eol");
		Rule atom = selection(not, any, whitespace, eol, range, terminal, name).as("atom");

		Rule qmark = literal("?").as("qmark");
		Rule star = literal("*").as("star");
		Rule plus = literal("+").as("plus");
		Rule ellipsis  = literal("..").as("ellipsis");
		Rule digit = terminal(set('0', '9')).as("digit");
		Rule num = digit.plus().as("num");
		Rule minmax = token(literal("{"), num, literal(","), num, literal("}")).as("minmax");
		Rule occurrence = selection(minmax, qmark, star, plus, ellipsis).as("occurrence");
		
		Rule _parts = link("parts");
		Rule token = sequence(literal("["), _parts, literal("]")).as("token");
		Rule group = sequence(literal("("), _parts, literal(")")).as("group");
		Rule part = selection(group, token, atom).as("part");
		//TODO way to describe that 2 rules in a sequence should stick together (control the separator)
		//Rule parts = sequence(token(part, occurrence.qmark() ), sequence(string("|").as("else").qmark(), _parts).qmark()).as("parts");
		Rule parts = sequence(part, occurrence.qmark(), sequence(literal("|").as("else").qmark(), _parts).qmark()).as("parts");
		Rule rule = sequence(name,  literal(":"), parts, literal(";")).as("rule");
		
		Rule comment = sequence(literal("%"), terminal(not('\n')).plus().as("text")).as("comment");
		Rule member = selection(comment, rule).as("member");
		Rule grammar = member.plus().as("grammar");
		GRAMMAR = new Grammar(grammar);
	}
}
