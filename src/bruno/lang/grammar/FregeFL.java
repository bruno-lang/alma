package bruno.lang.grammar;

import static bruno.lang.grammar.Grammar.Rule.symbol;
import static bruno.lang.grammar.Grammar.Rule.pattern;
import static bruno.lang.grammar.Grammar.Rule.ref;
import static bruno.lang.grammar.Grammar.Rule.selection;
import static bruno.lang.grammar.Grammar.Rule.seq;
import static bruno.lang.grammar.Grammar.Rule.string;
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
		a = symbol('\''),
		
		name = seq(symbol('-').qmark(), symbol('\\').qmark(), terminal(LETTERS), terminal(LETTERS.and(DIGITS).and(character('_')).and(character('-'))).star()).as("name"),
		capture = seq(symbol(':'), name.as("alias")).qmark().as("capture"),
		ref = seq(name, capture).as("ref"),

		wildcard = symbol('.').as("wildcard"),
		symbol = seq(a, terminal(Terminal.WILDCARD), a).as("symbol"),
		code_point = seq(string("U+"), terminal(HEX_NUMBER).occurs(occur(4, 8))).as("code-point"), 
		literal = selection(code_point, symbol).as("literal"),
		range = seq(literal, g, symbol('-'), g, literal).as("range"),
		letter = symbol('@').as("letter"),
		digit = symbol('#').as("digit"),
		hex = symbol('&').as("hex"),
		octal = symbol('8').as("octal"),
		binary = symbol('1').as("binary"),
		not = symbol('!').as("not"),
		whitespace = symbol('_').as("whitespace"),
		gap = symbol(',').as("gap"),
		pad = symbol('~').as("pad"),
		indent = string(">>").as("indent"),
		separator = symbol('^').as("separator"),

		tab = string("\\t").as("tab"),
		lf = string("\\n").as("lf"),
		cr = string("\\r").as("cr"),
		shortname = selection(tab, lf, cr).as("shortname"),

		category = seq(string("U+{"), terminal(LETTERS).plus(), symbol('}')).as("category"),
		ranges = seq(not.qmark(), selection(wildcard, letter, digit, hex, octal, binary, category, range, literal, whitespace, shortname)).as("ranges"),
		
		figure = selection(ranges, ref).as("-figure"),
		figures = seq(symbol('{'), g, seq(figure, seq(g, figure).star()) , g, symbol('}'), capture).as("figures"),
		pattern = seq(not.qmark(), selection(gap, pad, indent, separator)).as("pattern"),
		terminal = selection(pattern, ranges, figures).as("terminal"),

		string = seq(a, terminal(notCharacter('\'')).occurs(occur(2, Occur.plus.max)), a).as("string"),

		num = terminal(DIGITS).plus().as("num"),
		star = symbol('*').as("star"),
		plus = symbol('+').as("plus"),
		qmark = symbol('?').as("qmark"),
		occurrence = selection(seq(symbol('x').qmark(), num.as("min"), terminal(character('-').and(character('+'))).as("to").qmark(), num.as("max").qmark()), qmark, star, plus).as("occurrence"),
		
		option = seq(symbol('['), g, ref("selection"), g, symbol(']'), capture).as("option"),
		group = seq(symbol('('), g, ref("selection"), g, symbol(')'), capture).as("group"),
		completion = string("..").as("completion"),
		element = seq(selection(completion, group, option, string, terminal, ref), occurrence.qmark()).as("element"),
		
		sequence = seq(element, seq(i, element).star()).as("sequence"),
		selection = seq(sequence, seq(g, symbol('|'), i, sequence).star()).as("selection"),
		
		rule = seq(name, g, selection(symbol('='), seq(symbol(':'), symbol(':').qmark(), symbol('=').qmark())), g, selection, symbol(';').qmark()).as("rule"),
		comment = seq(symbol('%'), terminal(notCharacter('\n')).plus().as("text")).as("comment"),
		member = selection(comment, rule).as("member"), 
		grammar = seq(member, seq(g, member).star()).as("grammar") 
		;

	static final Grammar GRAMMAR = new Grammar(grammar);

}
