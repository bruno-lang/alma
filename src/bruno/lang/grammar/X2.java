package bruno.lang.grammar;

import static bruno.lang.grammar.Grammar.in;
import static bruno.lang.grammar.Grammar.occur;
import static bruno.lang.grammar.Grammar.or;
import static bruno.lang.grammar.Grammar.set;
import static bruno.lang.grammar.Grammar.x;
import static bruno.lang.grammar.Grammar.Rule.completion;
import static bruno.lang.grammar.Grammar.Rule.literal;
import static bruno.lang.grammar.Grammar.Rule.ref;
import static bruno.lang.grammar.Grammar.Rule.selection;
import static bruno.lang.grammar.Grammar.Rule.seq;
import static bruno.lang.grammar.Grammar.Rule.symbol;
import static bruno.lang.grammar.Grammar.Rule.terminal;
import bruno.lang.grammar.Grammar.Rule;
import bruno.lang.grammar.Grammar.Terminal;

public class X2 {

	static final Terminal
		DIGIT = set('0', '9'),
		LETTER = or(set('a', 'z'), set('A','Z')),
		NOT = in('^', '!')
		;
	
	//TODO bitmask unicode sets (to cover a-Z0-9-_ in one mask)
	
	static final Rule
		c = terminal(Grammar.comma),
		
		name = seq(literal('-').qmark(), literal('\\').qmark(), terminal(or(LETTER, DIGIT, in('_', '-'))).plus()).as("name"),
		ref = seq(name, seq(literal(':'), name.as("literal").qmark())).as("ref"),

		wildcard = literal('.').as("wildcard"),
		atom = seq(literal('\''), terminal(Grammar.any), literal('\'')).as("atom"),
		hexcode = seq(symbol("\\u"), terminal(DIGIT).occurs(x(4))).as("hexcode"), 
		literal = selection(hexcode, atom).as("literal"),
		range = seq(literal, c, terminal(in('-', '+')), c, literal).as("range"),
		letter = literal('@').as("letter"),
		digit = literal('#').as("digit"),
		not = terminal(NOT).as("not"),
		whitespace = literal('_').as("whitespace"),
		gap = literal(',').as("gap"),
		separation = literal('~').as("separation"),

		_t = literal('\t').as("\\t"),
		_n = literal('\n').as("\\n"),
		_r = literal('\r').as("\\r"),
		_s = terminal(in('\r', '\n', '\t', ' ')).as("\\s"),
		shortname = selection(_s, _t, _n, _r).as("shortname"),

		clazz = seq(symbol("\\u&"), terminal(LETTER).plus()).as("class"),
		figure = seq(not.qmark(), selection(wildcard, letter, digit, clazz, range, literal, whitespace, shortname, ref)).as("figure"),
		figures = seq(literal('{'), c, seq(figure, seq(c, figure).star()) , c, literal('}')).as("figures"),
		terminal = selection(figure, figures, gap, separation).as("terminal"),

		symbol = seq(literal('\''), terminal(Grammar.any).occurs(occur(2, Grammar.plus.max)) ,literal('\'')).as("symbol"),

		num = terminal(DIGIT).plus().as("num"),
		star = literal('*').as("star"),
		plus = literal('+').as("plus"),
		qmark = literal('?').as("qmark"),
		occurrence = selection(seq(literal('x').qmark(), num.as("low"), literal('-').qmark(), num.as("high").qmark()), qmark, star, plus).as("occurrence"),
		
		option = seq(literal('['), c, ref("selection"), c, literal(']')).as("option"),
		group = seq(literal('('), c, ref("selection"), c, literal(')')).as("group"),
		completion = symbol(".."),
		element = seq(selection(completion, group, option, literal, symbol, ref, terminal), occurrence.qmark()).as("element"),
		sequence = seq(element, seq(terminal(in(' ', '\t')).star(), element).star()).as("sequence"),
		selection = seq(sequence, seq(c, literal('|'), c, sequence).star()).as("selection"),
		
		rule = seq(name, c, selection(literal('='), seq(literal(':'), literal(':').qmark(), literal('=').qmark())), c, selection, c, literal(';').qmark()).as("rule"),
		comment = seq(literal('%'), completion(), literal('\n')).as("comment"),
		member = selection(comment, rule).as("member"), 
		grammar = seq(member, seq(c, member).star()).as("grammar") 
		;

	static final Grammar GRAMMAR = new Grammar(grammar);
}
