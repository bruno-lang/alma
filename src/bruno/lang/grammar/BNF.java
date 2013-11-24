package bruno.lang.grammar;

import static bruno.lang.grammar.Grammar.in;
import static bruno.lang.grammar.Grammar.not;
import static bruno.lang.grammar.Grammar.or;
import static bruno.lang.grammar.Grammar.set;
import static bruno.lang.grammar.Grammar.Rule.link;
import static bruno.lang.grammar.Grammar.Rule.selection;
import static bruno.lang.grammar.Grammar.Rule.sequence;
import static bruno.lang.grammar.Grammar.Rule.string;
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
		Rule terminal = token(string("'"), terminal(Grammar.any), terminal(not('\'')).star(), string("'")).as("terminal");
		Rule range = sequence(terminal, string("-"), terminal).as("range");
		Rule name = token(terminal(or(set('0', '9'), set('a','z'), set('A','Z'), in('_', '-', '\''))).plus()).as("name");
		Rule not = token(string("!"), link("atom")).as("not");
		Rule any = string(".").as("any");
		Rule whitespace = string("_").as("whitespace");
		Rule eol = string("$").as("eol");
		Rule atom = selection(not, any, whitespace, eol, range, terminal, name).as("atom");

		Rule qmark = string("?").as("qmark");
		Rule star = string("*").as("star");
		Rule plus = string("+").as("plus");
		Rule ellipsis  = string("..").as("ellipsis");
		Rule digit = terminal(set('0', '9')).as("digit");
		Rule num = digit.plus().as("num");
		Rule minmax = token(string("{"), num, string(","), num, string("}")).as("minmax");
		Rule occurrence = selection(minmax, qmark, star, plus, ellipsis).as("occurrence");
		
		Rule _parts = link("parts");
		Rule token = sequence(string("["), _parts, string("]")).as("token");
		Rule group = sequence(string("("), _parts, string(")")).as("group");
		Rule part = selection(group, token, atom).as("part");
		//TODO way to describe that 2 rules in a sequence should stick together (control the separator)
		//Rule parts = sequence(token(part, occurrence.qmark() ), sequence(string("|").as("else").qmark(), _parts).qmark()).as("parts");
		Rule parts = sequence(part, occurrence.qmark(), sequence(string("|").as("else").qmark(), _parts).qmark()).as("parts");
		Rule rule = sequence(name,  string(":"), parts, string(";")).as("rule");
		
		Rule comment = sequence(string("%"), terminal(not('\n')).plus().as("text")).as("comment");
		Rule member = selection(comment, rule).as("member");
		Rule grammar = member.plus().as("grammar");
		GRAMMAR = new Grammar(grammar);
	}
}
