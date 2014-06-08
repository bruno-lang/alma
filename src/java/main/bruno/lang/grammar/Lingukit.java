package bruno.lang.grammar;

import static bruno.lang.grammar.Grammar.Rule.pattern;
import static bruno.lang.grammar.Grammar.Rule.ref;
import static bruno.lang.grammar.Grammar.Rule.selection;
import static bruno.lang.grammar.Grammar.Rule.seq;
import static bruno.lang.grammar.Grammar.Rule.string;
import static bruno.lang.grammar.Grammar.Rule.symbol;
import static bruno.lang.grammar.Grammar.Rule.terminal;
import static bruno.lang.grammar.Linguist.finish;
import static bruno.lang.grammar.Linguist.namedRules;
import static bruno.lang.grammar.Occur.occur;
import static bruno.lang.grammar.Patterns.GAP;
import static bruno.lang.grammar.Patterns.INDENT;
import static bruno.lang.grammar.Patterns.WRAP;
import static bruno.lang.grammar.Terminal.DIGITS;
import static bruno.lang.grammar.Terminal.HEX_NUMBER;
import static bruno.lang.grammar.Terminal.LETTERS;
import static bruno.lang.grammar.Terminal.character;
import static bruno.lang.grammar.Terminal.notCharacter;
import bruno.lang.grammar.Grammar.Rule;

/**
 * Bootstrap {@link Grammar} of Lingukit.
 * 
 * @author jan
 */
public final class Lingukit {

	static final Rule
		g = pattern(GAP),
		i = pattern(INDENT),
		w = pattern(WRAP),
		a = symbol('\'');

	static final Rule
		name_ = seq(symbol('-').qmark(), symbol('\\').qmark(), terminal(LETTERS), terminal(LETTERS.and(DIGITS).and(character('_')).and(character('-'))).star()).as("name"),
		capture_ = seq(symbol(':'), name_.as("alias")).qmark().as("capture"),
		ref_ = seq(name_, capture_).as("ref"),

		wildcard_ = symbol('$').as("wildcard"),
		symbol_ = seq(a, terminal(Terminal.WILDCARD), a).as("symbol"),
		code_point_ = seq(string("U+"), terminal(HEX_NUMBER).occurs(occur(4, 8))).as("code-point"), 
		literal_ = selection(code_point_, symbol_).as("literal"),
		range_ = seq(literal_, g, symbol('-'), g, literal_).as("range"),
		letter_ = symbol('@').as("letter"),
		upper_ = symbol('Z').as("upper"),
		lower_ = symbol('z').as("lower"),
		digit_ = symbol('9').as("digit"),
		hex_ = symbol('#').as("hex"),
		octal_ = symbol('7').as("octal"),
		binary_ = symbol('1').as("binary"),
		not_ = symbol('!').as("not"),
		whitespace_ = symbol('_').as("whitespace"),
		gap_ = symbol(',').as("gap"),
		pad_ = symbol('~').as("pad"),
		wrap_ = symbol('.').as("wrap"),
		indent_ = string(">>").as("indent"),
		separator_ = symbol('^').as("separator"),

		tab_ = string("\\t").as("tab"),
		lf_ = string("\\n").as("lf"),
		cr_ = string("\\r").as("cr"),
		shortname_ = selection(tab_, lf_, cr_).as("shortname"),

		category_ = seq(string("U+{"), terminal(LETTERS).plus(), symbol('}')).as("category"),
		ranges_ = seq(not_.qmark(), selection(wildcard_, letter_, upper_, lower_, digit_, hex_, octal_, binary_, category_, range_, literal_, whitespace_, shortname_)).as("ranges"),

		figure_ = selection(ranges_, name_).as("-figure"),
		figures_ = seq(symbol('{'), g, seq(figure_, seq(g, figure_).star()) , g, symbol('}'), capture_).as("figures"),
		pattern_ = seq(not_.qmark(), selection(gap_, pad_, indent_, separator_, wrap_)).as("pattern"),
		terminal_ = selection(pattern_, ranges_, figures_).as("terminal"),

		string_ = seq(a, terminal(notCharacter('\'')).occurs(occur(2, Occur.plus.max)), a).as("string"),

		num_ = terminal(DIGITS).plus().as("num"),
		star_ = symbol('*').as("star"),
		plus_ = symbol('+').as("plus"),
		qmark_ = symbol('?').as("qmark"),
		occurrence_ = selection(seq(symbol('x').qmark(), num_.as("min"), terminal(character('-').and(character('+'))).as("to").qmark(), num_.as("max").qmark()), qmark_, star_, plus_).as("occurrence"),

		option_ = seq(symbol('['), g, ref("selection"), g, symbol(']'), capture_).as("option"),
		group_ = seq(symbol('('), g, ref("selection"), g, symbol(')'), capture_).as("group"),
		completion_ = seq(string(".."), capture_).as("completion"),
		distinction_ = symbol('<').as("distinction"),
		lookahead_ = seq(string(">("),g, ref("selection"), g, symbol(')')).as("lookahead"),
		element_ = seq(selection(distinction_, completion_, group_, option_, lookahead_, string_, terminal_, ref_), occurrence_.qmark()).as("element"),

		sequence_ = seq(element_, seq(i, element_).star()).as("sequence"),
		selection_ = seq(sequence_, seq(g, symbol('|'), i, sequence_).star()).as("selection"),

		rule_ = seq(name_, g, selection(symbol('='), seq(symbol(':'), symbol(':').qmark(), symbol('=').qmark())), g, selection_, symbol(';').qmark(), w).as("rule"),
		comment_ = seq(symbol('%'), terminal(notCharacter('\n')).plus().as("text")).as("comment"),
		member_ = selection(comment_, rule_).as("member"), 
		grammar_ = seq(member_, seq(g, member_).star()).as("grammar") 
		;
	
	static final Grammar GRAMMAR = new Grammar(finish(namedRules(grammar_)));

}
