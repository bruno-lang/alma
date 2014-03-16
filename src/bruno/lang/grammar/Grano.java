package bruno.lang.grammar;

import static bruno.lang.grammar.Grammar.Rule.literal;
import static bruno.lang.grammar.Grammar.Rule.pattern;
import static bruno.lang.grammar.Grammar.Rule.ref;
import static bruno.lang.grammar.Grammar.Rule.selection;
import static bruno.lang.grammar.Grammar.Rule.seq;
import static bruno.lang.grammar.Grammar.Rule.symbol;
import static bruno.lang.grammar.Grammar.Rule.terminal;
import static bruno.lang.grammar.Occur.occur;
import static bruno.lang.grammar.Terminal.DIGIT;
import static bruno.lang.grammar.Terminal.HEX;
import static bruno.lang.grammar.Terminal.LETTER;
import static bruno.lang.grammar.Terminal.character;
import static bruno.lang.grammar.Terminal.notCharacter;
import bruno.lang.grammar.Grammar.Rule;

/**
 * The name <code>grano</code> is derived from grammar notation.
 * 
 * @author jan
 */
public final class Grano {

	//TODO use ; as "must be newline"
	
	static final Rule
		tGap = pattern(Patterns.gap),
		tIndent = pattern(Patterns.indent),
		apo = literal('\''),
		
		name = seq(literal('-').qmark(), literal('\\').qmark(), terminal(LETTER), terminal(LETTER.and(DIGIT).and(character('_')).and(character('-'))).star()).as("name"),
		capture = seq(literal(':'), name.as("alias")).qmark().as("capture"),
		ref = seq(name, capture).as("ref"),

		wildcard = literal('.').as("wildcard"),
		atom = seq(apo, terminal(Terminal.WILDCARD), apo).as("atom"),
		utf8 = seq(symbol("U+"), terminal(HEX).occurs(occur(4, 8))).as("utf8"), 
		literal = selection(utf8, atom).as("literal"),
		range = seq(literal, tGap, literal('-'), tGap, literal).as("range"),
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

		utf8_class = seq(symbol("U+{"), terminal(LETTER).plus(), literal('}')).as("utf8-class"),
		utf8_set = seq(not.qmark(), selection(wildcard, letter, digit, hex, octal, binary, utf8_class, range, literal, whitespace, shortname)).as("utf8-set"),
		
		figure = selection(utf8_set, ref).as("-figure"),
		figures = seq(literal('{'), tGap, seq(figure, seq(tGap, figure).star()) , tGap, literal('}'), capture).as("figures"),
		pattern = selection(gap, pad, indent, separator).as("pattern"),
		terminal = selection(pattern, utf8_set, figures).as("terminal"),

		symbol = seq(apo, terminal(notCharacter('\'')).occurs(occur(2, Occur.plus.max)), apo).as("symbol"),

		num = terminal(DIGIT).plus().as("num"),
		star = literal('*').as("star"),
		plus = literal('+').as("plus"),
		qmark = literal('?').as("qmark"),
		occurrence = selection(seq(literal('x').qmark(), num.as("min"), terminal(character('-').and(character('+'))).as("to").qmark(), num.as("max").qmark()), qmark, star, plus).as("occurrence"),
		
		option = seq(literal('['), tGap, ref("selection"), tGap, literal(']'), capture).as("option"),
		group = seq(literal('('), tGap, ref("selection"), tGap, literal(')'), capture).as("group"),
		completion = symbol("..").as("completion"),
		element = seq(selection(completion, group, option, symbol, terminal, ref), occurrence.qmark()).as("element"),
		
		sequence = seq(element, seq(tIndent, element).star()).as("sequence"),
		selection = seq(sequence, seq(tGap, literal('|'), tIndent, sequence).star()).as("selection"),
		
		rule = seq(name, tGap, selection(literal('='), seq(literal(':'), literal(':').qmark(), literal('=').qmark())), tGap, selection, literal(';').qmark()).as("rule"),
		comment = seq(literal('%'), terminal(notCharacter('\n')).plus().as("text")).as("comment"),
		member = selection(comment, rule).as("member"), 
		grammar = seq(member, seq(tGap, member).star()).as("grammar") 
		;

	static final Grammar GRAMMAR = new Grammar(grammar);

}
