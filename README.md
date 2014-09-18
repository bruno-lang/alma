# lingukit

_easy understandable parsing_

This is _serious_...

1. Pretend you don't know anything about parsing (theory/practice).
2. Imagine a language!
3. Think: How would one read and understand its elements or structure?
4. Picture how you could formalise this to let a machine do what you just did 
   in your mind.

Now, was any of the crazyness you **do** know about parsing relevant in this?

Isn't it amazing how much fun it is to design languages in our imagination
and how frustrating it became to make a machine parse it in the reality of 
_common wisdom_ parsing?

I chose to ignore and forget this wisdom and explore parsing once again.


## A Journey from Grammars to Parse-Trees
We are willing to write a grammar so we can use a parser that gives us 
parse-trees for source files.

		(Grammar -> Parser) -> Source -> ParseTree

That's basically how we used to think of it - because we made the assumption
that we need a specific parser for a specific grammar. While this appears 
_natural_ to us it is a fallacy. It is enough to have **one parser** that given 
a grammar can parse a source in accordance with the grammar.

		Parser -> (Grammar, Source) -> ParseTree

Now we _only_ have to write a grammar that both we as well as the parser do
understand. Although we also need to understand how the parser processes the 
grammar so that we can make it do what we want. This sounds a lot like giving 
instructions - doesn't it?[^1] Such a parser is a _virtual machine_ that given 
_instructions_ in form of a grammar can process our source _programs_ to produce
parse-trees. 
Luckily we are used to programming, to give instructions and reason about their 
implications. This becomes possible as soon as we have learned the language we 
are programming in and the instructions it offers.


## Instructions
The following describes the 8 essential and 3 optional instructions of the 
underlying parser's _machine language_ through the surface syntax `lingukit`,
 that maps almost 1:1 to these _machine instructions_.

A instruction is always executed in the context of the current input position 
and results in a new _matched_ input position or a _mismatch_.

#### Matching Characters
`lingukit` is designed to match bytes (of UTF8 characters) but the principle
can be applied to any _basic unit_.

##### 0 Literal
Tests if input at current position literally matches a constant of one or more 
bytes given as a string literal with single quotes `'`.

		'what we are looking for'

_Pseudo-code:_

		if (input starts-at position == literal)
			continue at position + length(literal)
		else
			mismatch at position

##### 1 Terminal
Tests if input at current position is contained in a set of Unicode code-points.
Sets are given in curly braces by single characters and character ranges:

		{'a' 'b' 'c'} {'a'-'z'}

Sets can also exclude characters using `!` in front of a character or character-
range.

		{!'@'} {!'0'-'9'}

_Pseudo-code:_

		if (character-set contains (input code-point at position))
			continue at position + length(code-point at position)
		else
			mismatch at position

#### Matching _Words_
##### 2 Sequence
A sequence of instructions is - surprise - given by writing the instructions 
one after another (separated by whitespace where ambiguous otherwise). 

		'a' 'b' 'c'

Parentheses can be used to group sequences for nesting structures.

		('a' 'b') 'c'

If one instruction in a sequences results in a mismatch the sequence results in 
a mismatch as well.

_Pseudo-code:_

		cursor = position
		foreach instruction in sequence
			cursor = instruction exec (input, cursor) 
			if (is-mismatch(cursor))
				mismatch at position
		continue at cursor

##### 3 Iteration
Executes an instruction several times (between a minimum and a maximum). The
iteration count is directly appended to the repeated instruction using 
`x{min-max}`. Some examples:

		'a'x1-2 'b'x4 ('c' 'd')x3 {'0'-'9'}x2-4

_Pseudo-code:_

		match = position
		do maximum times
			cursor = instruction exec (input, match)
			if (is-mismatch(cursor))
				if (done less than minimum times)
					mismatch at position
				else 
					continue at match
			else
				match = cursor
		continue at match

##### 4 Selection
Test a sequence of alternatives until the **first match**. If no alternative 
matches the selection is a mismatch. Note that this is not a logical _OR_, the
first matching instruction is continued, the sequence of alternatives is very
important. This is important to be able to reason about what will happen for
a certain input sequence.

Alternatives are separated with the vertical bar `|`.

		'ab' | 'cd'

_Pseudo-code:_

		furthest-mismatch = mismatch at position
		foreach instruction in sequence
			cursor = instruction exec (input, cursor)
			if (is-match(cursor))
				continue at cursor
			else  
				furthest-mismatch = furthest(cursor, furthest-mismatch)
		mismatch at furthest-mismatch

##### 5 Completion
Consumes the input until the completed instruction matches at the current 
position. So instead of describing what to match the input is processed until
a specific end is found matched through any another simple or composed 
instruction.

A completion is indicated by two dots `..` followed by the end instruction. Here
an example to match XML comments:

		'<!--' .. '-->'

_Pseudo-code:_

		end = length(input)
		while (position < end)
			cursor = end-instruction exec (input, position)
			if (is-mismatch(cursor))
				position = increment(position)
			else
				continue at position
		mismatch at end


#### Capturing Matches
Instructions 0-5 control the parsing process by instructing the parser.
The next 2 instructions 6 and 7 are used to a) shape the resulting parse-tree 
and b) allow to form reusable compositions and recursion. 

##### 6 Reference

##### 7 Capture

#### Optional
There are 3 more instructions that are not essential for the concept to work
but that can improve and extend its functionality. 

##### 8 Pattern
Patterns are abstract basic units. The instructions asks a pattern how many 
bytes at the current input position are matching. 

`lingukit` has a fixed set of patterns exclusively used for processing
whitespace but the principle could be applied for any reason. 

* Indent: `>>` = `{' ' \t}*` (may be whitespace on same line)
* Separator `^` = `{' ' \t}+` (must be whitespace on same line)
* Gap: `,` = `_*` (may be whitespace)
* Pad: `~` = `_+` (must be whitespace)
* Wrap: `.` = `>> \n >>` (must be line wrap)

_Pseudo-code:_

		length = pattern length-of-match(input, position)
		if (length >= 0)
			continue at postion + length
		else
			mismatch at position

Patterns are mostly a performance optimisations as almost all could similarly 
be modelled using combinations of other instructions. 

Different parsers might support different sets of named patterns so they should
be used with caution. For the same reason RegExes should not be included as 
different platforms have different support and interpretation of regular 
expressions what would undermine the interoperability of the parser/grammars.

##### 9 Decision

##### 10 Look-ahead


## Syntactic Sugar 

## Q & A


[^1] Damian Conway - [Everything You Know About Regexes Is Wrong](http://www.infoq.com/presentations/regex)
