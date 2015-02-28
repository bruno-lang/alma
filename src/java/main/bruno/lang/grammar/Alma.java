package bruno.lang.grammar;

import static bruno.lang.grammar.Grammar.Rule.pattern;
import static bruno.lang.grammar.Grammar.Rule.include;
import static bruno.lang.grammar.Grammar.Rule.alt;
import static bruno.lang.grammar.Grammar.Rule.seq;
import static bruno.lang.grammar.Grammar.Rule.string;
import static bruno.lang.grammar.Grammar.Rule.symbol;
import static bruno.lang.grammar.Grammar.Rule.charset;
import static bruno.lang.grammar.Occur.occur;
import static bruno.lang.grammar.Patterns.MAY_BE_INDENT;
import static bruno.lang.grammar.Patterns.MAY_BE_WS;
import static bruno.lang.grammar.Patterns.MUST_BE_WRAP;
import static bruno.lang.grammar.CharacterSet.DIGITS;
import static bruno.lang.grammar.CharacterSet.HEX_NUMBER;
import static bruno.lang.grammar.CharacterSet.LETTERS;
import static bruno.lang.grammar.CharacterSet.character;
import static bruno.lang.grammar.CharacterSet.notCharacter;
import bruno.lang.grammar.Grammar.Rule;

/**
 * Bootstrap {@link Grammar} of ALMA.
 * 
 * @author jan
 */
public final class Alma {

	static final Rule
		g = pattern(MAY_BE_WS),
		i = pattern(MAY_BE_INDENT),
		w = pattern(MUST_BE_WRAP),
		a = symbol('\'');

	static final Rule
		name_ = seq(symbol('-').qmark(), symbol('\\').qmark(), charset(LETTERS), charset(LETTERS.and(DIGITS).and(character('_')).and(character('-'))).star()).is("name"),
		capture_ = seq(symbol(':'), name_.as("alias")).qmark().is("capture"),
		ref_ = seq(name_, capture_).is("ref"),

		wildcard_ = symbol('$').is("wildcard"),
		symbol_ = seq(a, charset(CharacterSet.WILDCARD), a).is("symbol"),
		code_point_ = seq(string("U+"), charset(HEX_NUMBER).occurs(occur(4, 8))).is("code-point"), 
		literal_ = alt(code_point_, symbol_).is("literal"),
		range_ = seq(literal_, g, symbol('-'), g, literal_).is("range"),
		letter_ = symbol('@').is("letter"),
		upper_ = symbol('Z').is("upper"),
		lower_ = symbol('z').is("lower"),
		digit_ = symbol('9').is("digit"),
		hex_ = symbol('#').is("hex"),
		octal_ = symbol('7').is("octal"),
		binary_ = symbol('1').is("binary"),
		not_ = symbol('-').is("not"),
		whitespace_ = symbol('_').is("whitespace"),
		gap_ = symbol(',').is("gap"),
		pad_ = symbol(';').is("pad"),
		wrap_ = symbol('.').is("wrap"),
		indent_ = string(">").is("indent"),
		separator_ = string(">>").is("separator"),

		tab_ = string("\\t").is("tab"),
		lf_ = string("\\n").is("lf"),
		cr_ = string("\\r").is("cr"),
		shortname_ = alt(tab_, lf_, cr_).is("shortname"),

		category_ = seq(string("U+{"), charset(LETTERS).plus(), symbol('}')).is("category"),
		ranges_ = alt(wildcard_, letter_, upper_, lower_, digit_, hex_, octal_, binary_, category_, range_, literal_, whitespace_, shortname_).is("ranges"),

		figure_ = alt(ranges_, name_).is("figure"),
		figures_ = seq(not_.qmark(), symbol('{'), g, seq(alt(ranges_, name_), seq(g, alt(ranges_, name_)).star()) , g, symbol('}'), capture_).is("figures"),
		pattern_ = alt(gap_, pad_, indent_, separator_, wrap_).is("pattern"),
		terminal_ = alt(pattern_, ranges_, figures_).is("terminal"),

		string_ = seq(a, charset(notCharacter('\'')).occurs(occur(2, Occur.plus.max)), a).is("string"),

		num_ = charset(DIGITS).plus().is("num"),
		star_ = symbol('*').is("star"),
		plus_ = symbol('+').is("plus"),
		qmark_ = symbol('?').is("qmark"),
		quantity_ = seq(num_.as("min"), alt(string(".."), charset(character('-').and(character('+')))).as("to").qmark(), num_.as("max").qmark()).is("quantity"),
		occurrence_ = alt(qmark_, plus_, seq(alt(symbol('x'), string("**")), alt(quantity_, include("element"))), star_).is("occurrence"),

		option_ = seq(symbol('['), g, include("selection"), g, symbol(']'), capture_).is("option"),
		group_ = seq(symbol('('), g, include("selection"), g, symbol(')'), capture_).is("group"),
		completion_ = seq(string(".."), capture_).is("completion"),
		decision_ = symbol('!').is("decision"),
		lookahead_ = seq(string("~("),g, include("selection"), g, symbol(')')).is("lookahead"),
		element_ = seq(alt(decision_, completion_, group_, option_, lookahead_, string_, terminal_, ref_), occurrence_.qmark()).is("element"),

		sequence_ = seq(element_, seq(i, element_).star()).is("sequence"),
		selection_ = seq(sequence_, seq(g, symbol('|'), i, sequence_).star()).is("selection"),

		rule_ = seq(name_, g, alt(symbol('='), seq(symbol(':'), symbol(':').qmark(), symbol('=').qmark())), g, selection_, symbol(';').qmark(), w).is("rule"),
		comment_ = seq(symbol('%'), charset(notCharacter('\n')).plus().as("text")).is("comment"),
		member_ = alt(comment_, rule_).is("member"), 
		grammar_ = seq(member_, seq(g, member_).star()).is("grammar") 
		;
	
	static final Grammar GRAMMAR = GrammarBuilder.buildGrammar(grammar_);

}
