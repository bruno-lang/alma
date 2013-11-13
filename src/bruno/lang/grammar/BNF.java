package bruno.lang.grammar;

import static bruno.lang.grammar.Grammar.in;
import static bruno.lang.grammar.Grammar.not;
import static bruno.lang.grammar.Grammar.or;
import static bruno.lang.grammar.Grammar.set;
import static bruno.lang.grammar.Grammar.Rule.link;
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
		Rule terminal = token(terminal("'"), terminal(Grammar.any), terminal(not('\'')).star(), terminal("'")).as("terminal");
		Rule range = sequence(terminal, terminal("-"), terminal).as("range");
		Rule name = token(terminal(or(set('0', '9'), set('a','z'), set('A','Z'), in('_', '-', '\''))).plus()).as("name");
		Rule not = token(terminal("!"), link("atom")).as("not");
		Rule any = terminal(".").as("any");
		Rule whitespace = terminal("_").as("whitespace");
		Rule atom = selection(not, any, whitespace, range, terminal, name).as("atom");

		Rule qmark = terminal("?").as("qmark");
		Rule star = terminal("*").as("star");
		Rule plus = terminal("+").as("plus");
		Rule ellipsis  = terminal("..").as("ellipsis");
		Rule digit = terminal(set('0', '9')).as("digit");
		Rule num = digit.plus().as("num");
		Rule minmax = token(terminal("{"), num, terminal(","), num, terminal("}")).as("minmax");
		Rule occurrence = selection(minmax, qmark, star, plus, ellipsis).as("occurrence");
		
		Rule _parts = link("parts");
		Rule token = sequence(terminal("["), _parts, terminal("]")).as("token");
		Rule group = sequence(terminal("("), _parts, terminal(")")).as("group");
		Rule part = selection(group, token, atom).as("part");
		Rule parts = sequence(token(part, occurrence.qmark() ), sequence(terminal("|").as("else").qmark(), _parts).qmark()).as("parts");
		Rule rule = sequence(name,  terminal(":"), parts, terminal(";")).as("rule");
		
		Rule comment = sequence(terminal("%"), terminal(not('\n')).plus().as("text")).as("comment");
		Rule member = selection(comment, rule).as("member");
		Rule grammar = member.plus().as("grammar");
		GRAMMAR = new Grammar(grammar);
	}
}
