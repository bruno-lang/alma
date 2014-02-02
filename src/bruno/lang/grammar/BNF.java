package bruno.lang.grammar;

import static bruno.lang.grammar.Grammar.in;
import static bruno.lang.grammar.Grammar.not;
import static bruno.lang.grammar.Grammar.or;
import static bruno.lang.grammar.Grammar.set;
import static bruno.lang.grammar.Grammar.Rule.link;
import static bruno.lang.grammar.Grammar.Rule.literal;
import static bruno.lang.grammar.Grammar.Rule.selection;
import static bruno.lang.grammar.Grammar.Rule.sequence;
import static bruno.lang.grammar.Grammar.Rule.terminal;
import static bruno.lang.grammar.Grammar.Rule.token;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

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
		Rule terminal = token(literal("'"), terminal(Grammar.any), terminal(not('\'')).star(), literal("'")).as("terminal");
		Rule range = sequence(terminal, literal("-"), terminal).as("range");
		Rule name = token(terminal(or(set('0', '9'), set('a','z'), set('A','Z'), in('_', '-', '\''))).plus()).as("name");
		Rule not = token(literal("!"), link("atom")).as("not");
		Rule any = literal(".").as("any");
		Rule whitespace = literal("_").as("whitespace");
		Rule eol = literal("$").as("eol");
		Rule atom = selection(not, any, whitespace, eol, range, terminal, name).as("atom");

		Rule qmark = literal("?").as("qmark");
		Rule star = literal("*").as("star");
		Rule plus = literal("+").as("plus");
		Rule ellipsis  = literal("..").as("ellipsis");
		Rule digit = terminal(set('0', '9')).as("-digit");
		Rule num = digit.plus().as("num");
		Rule minmax = token(literal("{"), num, literal(","), num, literal("}")).as("minmax");
		Rule occurrence = selection(minmax, qmark, star, plus).as("occurrence");
		
		Rule group = sequence(literal("("), link("selection"), literal(")")).as("group");
		Rule terminals = sequence(literal('['), sequence(terminal).plus(), literal(']')).as("terminals");
		Rule indent = terminal(in(' ', '\t')).star();
		Rule part = selection(ellipsis, token(selection(group, terminals, atom), occurrence.qmark())).as("part");
		Rule parts = part.plus().separate(indent).as("parts");
		Rule selection = sequence(parts, sequence(literal('|'), parts).star()).as("selection"); 
		Rule separation = sequence(literal('['), name.qmark(), literal(']')).as("separation");
		Rule colon = literal(":");
		Rule equal = literal('=');
		Rule definedAs = selection(equal, sequence(colon, colon.qmark(), equal.qmark()));
		Rule rule = sequence(separation.qmark(), name,  definedAs, selection, literal(";").qmark()).as("rule");
		
		Rule comment = sequence(literal("%"), terminal(not('\n')).plus().as("text")).as("comment");
		Rule member = selection(comment, rule).as("member");
		Rule grammar = member.plus().as("grammar");
		GRAMMAR = new Grammar(grammar);
	}

	public static Tokenised tokenise(String filename) throws IOException {
		RandomAccessFile aFile = new RandomAccessFile(filename, "r");
		FileChannel in = aFile.getChannel();
		MappedByteBuffer buffer = in.map(FileChannel.MapMode.READ_ONLY, 0, in.size());
		try {
			buffer.load();
			Tokens tokens = Tokeniser.tokenise(buffer, GRAMMAR.rule("grammar".intern()));
			return new Tokenised(buffer, tokens);
		} finally {
			buffer.clear();
			in.close();
			aFile.close();
		}
	}
}
