# Lingukit

_make understandable parsers easily_

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
The following describes the `lingukit` language that maps almost 1:1 to the 8+3 
underlying instructions of the parser's _machine language_.

Each instruction is executed in the context of the current input position and 
results in a new _matched_ input position or a _mismatch_.

### Matching Bytes
`lingukit` is designed to match bytes (of UTF8 characters) but the principle
can be applied to any _basic unit_.

##### 0 Literal
Tests if input at current position literally matches a constant of one or more 
bytes given as a string literal with single quotes `'`.

		'what we are looking for'

Pseudocode:

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

Pseudocode:

		if (character-set contains (input code-point at position))
			continue at position + length(code-point at position)
		else
			mismatch at position

### Matching Structure
##### 2 Sequence
A sequence of instructions is - surprise - given by writing the instructions 
one after another (separated by whitespace where ambiguous otherwise). 

		'a' 'b' 'c'

Parentheses can be used to group sequences for nesting structures.

		('a' 'b') 'c'

If one instruction in a sequences results in a mismatch the sequence results in 
a mismatch as well.

Pseudocode:

		cursor = position
		foreach instruction in sequence
			cursor = instruction exec (input, cursor) 
			if (is-mismatch(cursor))
				mismatch at position
		continue at cursor

##### 3 Iteration
Executes an instruction several times (between a minimum and a maximum). The
iteration count is directly appended to the repeated instruction using 
`x{min-max}`. 

		'a'x1-2 'b'x4 ('c' 'd')x3 {'0'-'9'}x2-4

Pseudocode:

		match = position;
		do maximum times
			cursor = instruction exec (input, match)
			if (is-mismatch(cursor))
				if (done less than minimum times)
					mismatch at position
				continue at match
			match = cursor
		continue at match

##### 4 Selection
##### 5 Completion
##### 6 Reference

### Capturing Matches

##### 7 Capture

### Optional

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

Pseudocode:

		length = pattern length-of-match(input, position)
		if (length >= 0)
			continue at postion + length
		else
			mismatch at position

Patterns are mostly a performance optimisations as almost all could similarly 
be modeled using combinations of other instructions. 
Different parsers might support different sets of named patterns so they should
be used with caution. For the same reason RegExes should not be included as 
different platforms have different support and interpretation of regular 
expressions.

##### 9 Decision

##### 10 Lookahead


## Syntactic Sugar 

## Q & A


[^1] Damian Conway - [Everything You Know About Regexes Is Wrong](http://www.infoq.com/presentations/regex)
