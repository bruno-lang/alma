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
		Rule terminal = token(terminal("'"), terminal(Grammar.any), terminal(not('\'')).star(), terminal("'")).named("terminal");
		Rule range = sequence(terminal, terminal("-"), terminal).named("range");
		Rule name = token(terminal(or(set('0', '9'), set('a','z'), set('A','Z'), in('_', '-', '\''))).plus()).named("name");
		Rule not = token(terminal("!"), link("atom")).named("not");
		Rule any = terminal(".").named("any");
		Rule atom = selection(not, any, range, terminal, name).named("atom");

		Rule qmark = terminal("?").named("qmark");
		Rule star = terminal("*").named("star");
		Rule plus = terminal("+").named("plus");
		Rule ellipsis  = terminal("..").named("ellipsis");
		Rule digit = terminal(set('0', '9')).named("digit");
		Rule num = digit.plus().named("num");
		Rule minmax = token(terminal("{"), num, terminal(","), num, terminal("}")).named("minmax");
		Rule occurrence = selection(minmax, qmark, star, plus, ellipsis).named("occurrence");
		
		Rule _parts = link("parts");
		Rule token = sequence(terminal("["), _parts, terminal("]")).named("token");
		Rule group = sequence(terminal("("), _parts, terminal(")")).named("group");
		Rule part = selection(group, token, atom).named("part");
		Rule parts = sequence(token(part, occurrence.qmark() ), sequence(terminal("|").named("else").qmark(), _parts).qmark()).named("parts");
		Rule rule = sequence(name,  terminal(":"), parts, terminal(";")).named("rule");
		
		Rule comment = sequence(terminal("%"), terminal(not('\n')).plus().named("text")).named("comment");
		Rule member = selection(comment, rule).named("member");
		Rule grammar = member.plus().named("grammar");
		GRAMMAR = new Grammar(grammar);
	}
}
