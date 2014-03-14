package bruno.lang.grammar;

import static bruno.lang.grammar.Grammar.Rule.literal;
import static bruno.lang.grammar.Grammar.Rule.ref;
import static bruno.lang.grammar.Grammar.Rule.selection;
import static bruno.lang.grammar.Grammar.Rule.seq;
import static bruno.lang.grammar.Grammar.Rule.symbol;
import static bruno.lang.grammar.Grammar.Rule.terminal;
import static bruno.lang.grammar.Occur.occur;
import static bruno.lang.grammar.Terminals.in;
import static bruno.lang.grammar.Terminals.not;
import static bruno.lang.grammar.Terminals.or;
import static bruno.lang.grammar.Terminals.range;
import bruno.lang.grammar.Grammar.Rule;

/**
 * The name <code>grano</code> is derived from grammar notation.
 * 
 * @author jan
 */
public final class Grano {

	static final Terminal
		DIGIT = range('0', '9'),
		HEX = or(DIGIT, range('A', 'F')),
		LETTER = or(range('a', 'z'), range('A','Z')),
		//TODO move above to terminals
		
		NOT = in('^', '!')
		;
	
	static final Rule
		tGap = terminal(Terminals.gap),
		tIndent = terminal(Terminals.indent),
		apo = literal('\''),
		
		name = seq(literal('-').qmark(), literal('\\').qmark(), terminal(or(LETTER, DIGIT, in('_', '-'))).plus()).as("name"),
		capture = seq(literal(':'), name.as("alias")).qmark().as("capture"),
		ref = seq(name, capture).as("ref"),

		wildcard = literal('.').as("wildcard"),
		atom = seq(apo, terminal(Terminals.wildcard), apo).as("atom"),
		hexcode = seq(symbol("\\u"), terminal(HEX).occurs(occur(4, 8))).as("hexcode"), 
		literal = selection(hexcode, atom).as("literal"),
		range = seq(literal, tGap, literal('-'), tGap, literal).as("range"),
		letter = literal('@').as("letter"),
		digit = literal('#').as("digit"),
		hex = literal('X').as("hex"),
		not = terminal(NOT).as("not"),
		whitespace = literal('_').as("whitespace"),
		gap = literal(',').as("gap"),
		pad = literal('~').as("pad"),
		indent = symbol(">>").as("indent"),

		_t = symbol("\\t").as("\\t"),
		_n = symbol("\\n").as("\\n"),
		_r = symbol("\\r").as("\\r"),
		_s = symbol("\\s").as("\\s"),
		shortname = selection(_s, _t, _n, _r).as("shortname"),

		clazz = seq(symbol("\\u{"), terminal(LETTER).plus(), literal('}')).as("class"),
		figure = seq(not.qmark(), selection(wildcard, letter, hex, digit, clazz, range, literal, whitespace, shortname)).as("figure"),
		figure1 = selection(figure, ref).as("-figure1"),
		figures = seq(literal('{'), tGap, seq(figure1, seq(tGap, figure1).star()) , tGap, literal('}'), capture).as("figures"),
		terminal = selection(figure, figures, gap, pad, indent).as("terminal"),

		symbol = seq(apo, terminal(not('\'')).occurs(occur(2, Occur.plus.max)), apo).as("symbol"),

		num = terminal(DIGIT).plus().as("num"),
		star = literal('*').as("star"),
		plus = literal('+').as("plus"),
		qmark = literal('?').as("qmark"),
		occurrence = selection(seq(literal('x').qmark(), num.as("min"), terminal(in('-', '+')).as("to").qmark(), num.as("max").qmark()), qmark, star, plus).as("occurrence"),
		
		option = seq(literal('['), tGap, ref("selection"), tGap, literal(']'), capture).as("option"),
		group = seq(literal('('), tGap, ref("selection"), tGap, literal(')'), capture).as("group"),
		completion = symbol("..").as("completion"),
		element = seq(selection(completion, group, option, symbol, terminal, literal, ref), occurrence.qmark()).as("element"),
		
		sequence = seq(element, seq(tIndent, element).star()).as("sequence"),
		selection = seq(sequence, seq(tGap, literal('|'), tIndent, sequence).star()).as("selection"),
		
		rule = seq(name, tGap, selection(literal('='), seq(literal(':'), literal(':').qmark(), literal('=').qmark())), tGap, selection, literal(';').qmark()).as("rule"),
		comment = seq(literal('%'), terminal(not('\n')).plus().as("text")).as("comment"),
		member = selection(comment, rule).as("member"), 
		grammar = seq(member, seq(tGap, member).star()).as("grammar") 
		;

	static final Grammar GRAMMAR = new Grammar(grammar);
}
