package bruno.lang.grammar;

import static bruno.lang.grammar.Grammar.in;
import static bruno.lang.grammar.Grammar.not;
import static bruno.lang.grammar.Grammar.occur;
import static bruno.lang.grammar.Grammar.or;
import static bruno.lang.grammar.Grammar.set;
import static bruno.lang.grammar.Grammar.Rule.literal;
import static bruno.lang.grammar.Grammar.Rule.ref;
import static bruno.lang.grammar.Grammar.Rule.selection;
import static bruno.lang.grammar.Grammar.Rule.seq;
import static bruno.lang.grammar.Grammar.Rule.symbol;
import static bruno.lang.grammar.Grammar.Rule.terminal;
import bruno.lang.grammar.Grammar.Rule;
import bruno.lang.grammar.Grammar.Terminal;

public class X2 {

	//TODO bitmask unicode sets (to cover a-Z0-9-_ in one mask)

	static final Terminal
		DIGIT = set('0', '9'),
		HEX = or(DIGIT, set('A', 'F')),
		LETTER = or(set('a', 'z'), set('A','Z')),
		NOT = in('^', '!')
		;
	
	static final Rule
		c = terminal(Grammar.comma).qmark(),
		pad = terminal(in(' ', '\t')).star(),
		apo = literal('\''),
		
		name = seq(literal('-').qmark(), literal('\\').qmark(), terminal(or(LETTER, DIGIT, in('_', '-'))).plus()).as("name"),
		capture = seq(literal(':'), name.as("alias")).qmark().as("capture"),
		ref = seq(name, capture).as("ref"),

		wildcard = literal('.').as("wildcard"),
		atom = seq(apo, terminal(Grammar.any), apo).as("atom"),
		hexcode = seq(symbol("\\u"), terminal(HEX).occurs(occur(4, 8))).as("hexcode"), 
		literal = selection(hexcode, atom).as("literal"),
		range = seq(literal, c, literal('-'), c, literal).as("range"),
		letter = literal('@').as("letter"),
		digit = literal('#').as("digit"),
		hex = literal('X').as("hex"),
		not = terminal(NOT).as("not"),
		whitespace = literal('_').as("whitespace"),
		gap = literal(',').as("gap"),
		separation = literal('~').as("separation"),
		indent = symbol(">>").as("indent"),

		_t = symbol("\\t").as("\\t"),
		_n = symbol("\\n").as("\\n"),
		_r = symbol("\\r").as("\\r"),
		_s = symbol("\\s").as("\\s"),
		shortname = selection(_s, _t, _n, _r).as("shortname"),

		clazz = seq(symbol("\\u{"), terminal(LETTER).plus(), literal('}')).as("class"),
		figure = seq(not.qmark(), selection(wildcard, letter, hex, digit, clazz, range, literal, whitespace, shortname, ref)).as("figure"),
		figures = seq(literal('{'), c, seq(figure, seq(c, figure).star()) , c, literal('}'), capture).as("figures"),
		terminal = selection(figure, figures, gap, separation, indent).as("terminal"),

		symbol = seq(apo, terminal(not('\'')).occurs(occur(2, Grammar.plus.max)), apo).as("symbol"),

		num = terminal(DIGIT).plus().as("num"),
		star = literal('*').as("star"),
		plus = literal('+').as("plus"),
		qmark = literal('?').as("qmark"),
		occurrence = selection(seq(literal('x').qmark(), num.as("low"), terminal(in('-', '+')).as("to").qmark(), num.as("high").qmark()), qmark, star, plus).as("occurrence"),
		
		option = seq(literal('['), c, ref("selection"), c, literal(']'), capture).as("option"),
		group = seq(literal('('), c, ref("selection"), c, literal(')'), capture).as("group"),
		completion = symbol("..").as("completion"),
		element = seq(selection(completion, group, option, symbol, terminal, literal, ref), occurrence.qmark()).as("element"),
		
		sequence = seq(element, seq(pad, element).star()).as("sequence"),
		selection = seq(sequence, seq(c, literal('|'), pad, sequence).star()).as("selection"),
		
		rule = seq(name, c, selection(literal('='), seq(literal(':'), literal(':').qmark(), literal('=').qmark())), c, selection, literal(';').qmark()).as("rule"),
		comment = seq(literal('%'), terminal(not('\n')).plus().as("text")).as("comment"),
		member = selection(comment, rule).as("member"), 
		grammar = seq(member, seq(c, member).star()).as("grammar") 
		;

	static final Grammar GRAMMAR = new Grammar(grammar);
}
