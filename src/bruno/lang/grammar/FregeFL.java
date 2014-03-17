package bruno.lang.grammar;

import static bruno.lang.grammar.Grammar.Rule.literal;
import static bruno.lang.grammar.Grammar.Rule.pattern;
import static bruno.lang.grammar.Grammar.Rule.ref;
import static bruno.lang.grammar.Grammar.Rule.selection;
import static bruno.lang.grammar.Grammar.Rule.seq;
import static bruno.lang.grammar.Grammar.Rule.symbol;
import static bruno.lang.grammar.Grammar.Rule.terminal;
import static bruno.lang.grammar.Occur.occur;
import static bruno.lang.grammar.Patterns.GAP;
import static bruno.lang.grammar.Patterns.INDENT;
import static bruno.lang.grammar.Terminal.DIGITS;
import static bruno.lang.grammar.Terminal.HEX_NUMBER;
import static bruno.lang.grammar.Terminal.LETTERS;
import static bruno.lang.grammar.Terminal.character;
import static bruno.lang.grammar.Terminal.notCharacter;
import bruno.lang.grammar.Grammar.Rule;

/**
 * The name <code>grano</code> is derived from grammar notation.
 * 
 * @author jan
 */
public final class FregeFL {

	//TODO use ; as "must be newline"
	
	static final Rule
		g = pattern(GAP),
		i = pattern(INDENT),
		a = literal('\''),
		
		name = seq(literal('-').qmark(), literal('\\').qmark(), terminal(LETTERS), terminal(LETTERS.and(DIGITS).and(character('_')).and(character('-'))).star()).as("name"),
		capture = seq(literal(':'), name.as("alias")).qmark().as("capture"),
		ref = seq(name, capture).as("ref"),

		wildcard = literal('.').as("wildcard"),
		atom = seq(a, terminal(Terminal.WILDCARD), a).as("atom"),
		code_point = seq(symbol("U+"), terminal(HEX_NUMBER).occurs(occur(4, 8))).as("code-point"), 
		literal = selection(code_point, atom).as("literal"),
		range = seq(literal, g, literal('-'), g, literal).as("range"),
		letter = literal('@').as("letter"),
		digit = literal('#').as("digit"),
		hex = literal('&').as("hex"),
		octal = literal('8').as("octal"),
		binary = literal('1').as("binary"),
		not = literal('!').as("not"),
		whitespace = literal('_').as("whitespace"),
		gap = literal(',').as("gap"),
		pad = literal('~').as("pad"),
		indent = symbol(">>").as("indent"),
		separator = literal('^').as("separator"),

		tab = symbol("\\t").as("tab"),
		lf = symbol("\\n").as("lf"),
		cr = symbol("\\r").as("cr"),
		shortname = selection(tab, lf, cr).as("shortname"),

		category = seq(symbol("U+{"), terminal(LETTERS).plus(), literal('}')).as("category"),
		ranges = seq(not.qmark(), selection(wildcard, letter, digit, hex, octal, binary, category, range, literal, whitespace, shortname)).as("ranges"),
		
		figure = selection(ranges, ref).as("-figure"),
		figures = seq(literal('{'), g, seq(figure, seq(g, figure).star()) , g, literal('}'), capture).as("figures"),
		pattern = seq(not.qmark(), selection(gap, pad, indent, separator)).as("pattern"),
		terminal = selection(pattern, ranges, figures).as("terminal"),

		symbol = seq(a, terminal(notCharacter('\'')).occurs(occur(2, Occur.plus.max)), a).as("symbol"),

		num = terminal(DIGITS).plus().as("num"),
		star = literal('*').as("star"),
		plus = literal('+').as("plus"),
		qmark = literal('?').as("qmark"),
		occurrence = selection(seq(literal('x').qmark(), num.as("min"), terminal(character('-').and(character('+'))).as("to").qmark(), num.as("max").qmark()), qmark, star, plus).as("occurrence"),
		
		option = seq(literal('['), g, ref("selection"), g, literal(']'), capture).as("option"),
		group = seq(literal('('), g, ref("selection"), g, literal(')'), capture).as("group"),
		completion = symbol("..").as("completion"),
		element = seq(selection(completion, group, option, symbol, terminal, ref), occurrence.qmark()).as("element"),
		
		sequence = seq(element, seq(i, element).star()).as("sequence"),
		selection = seq(sequence, seq(g, literal('|'), i, sequence).star()).as("selection"),
		
		rule = seq(name, g, selection(literal('='), seq(literal(':'), literal(':').qmark(), literal('=').qmark())), g, selection, literal(';').qmark()).as("rule"),
		comment = seq(literal('%'), terminal(notCharacter('\n')).plus().as("text")).as("comment"),
		member = selection(comment, rule).as("member"), 
		grammar = seq(member, seq(g, member).star()).as("grammar") 
		;

	static final Grammar GRAMMAR = new Grammar(grammar);

}
