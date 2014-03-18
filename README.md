
NOA FL
======

Not Only Another Formal Language
--------------------------------
                                                      
A general grammar used for the bruno language.

## Syntax
### Rules
The basic unit of a grammar are rules. A grammar is a set of rules (given in no particular order).
A rule has the simplified form:

		<name> : <declaration> ;

The actual syntax allows for several forms of the _is defined as_ operator. A rule can also be written like:

		<name> = <declaration> ;
		<name> ::= <declaration> ;
		<name> :: <declaraion> ;
		<name := <declaration> ;

This allows to adapt to personal or organisational preferences and similarities or distinction with other grammar syntaxes. 
	Also the semicolon `;` at the end of each rule is optional. It can be used in some ambiguous grammars to clearly express the end of a rule (in contrast to further parts of the same).

**Note** that the grammar does not have the classic distinction between terminal and non-terminal rules. This is also due to the fact that white-space is explicitly described as everything else. 

##### Names
A name is any sequence of ASCII letters (both lower and upper), ASCII digits, underscore `_` and dash `-`. In addition a name may start with backslash `\`. Names starting with dash `-` or backslash `\` are never captured. 

### Building Blocks of Rule Declarations
#### Matching Bytes 
The grammar is designed to be used on UTF-8 encoded input. Therefore the basic unit of a _character_ is a `byte`.

The simplest way to match one or more bytes is to literally give a fixed sequence of characters to match.
This is done by enclosing them in single ticks `'` like `'a'` for literally `a` or `'keyword'` for literally `keyword`.
No _escaping_ is supported or necessary - a single tick `'` itself is given similarly `'''`.

		literally-a = 'a'
		literally-keyword = 'keyword'

Non ASCII characters or white-space should instead be given as a uni-code literal in the form `\uXXXX`

To match subsets of characters a few different constructs can be used and combined.

		digit = '0'-'9' % range
		

#### Matching Structure
A rule's patter or structure can be described using the common generic building blocks of information processing:

- iteration
- sequence
- selection
- (plain blocks)

There are neither precedence rules nor different match modes as greedy/non-greedy. The first matching alternative is considered the intended match. 
This fully intentionally limits the way a certain grammar can be expressed but does not disallow to embody any desired behaviour - a grammar just might need to be reshaped by the author. As a result the behaviour of a grammar is far more visible in a grammar's rules.

#### Capturing Matches
By splitting a grammar into named rules also the blocks of the resulting parse-tree are described.

		foo = bar baz

The above rule will capture a tree structure like this

		foo
			bar
			baz

Also individual elements in a rule declaration can be named _inline_:

		range = number:low '-' number:high

The above rule _aliases_ the rule `number` to `low` and `high`. The resulting parse-tree will look like

		range
			low
			high

Lastly rules can be referenced without also capturing them. This is used to reuse patterns that themselves do not describe a complete interesting value.

		number = -digit+

The above rule says that a `number` consists of 1 or more `digit`s but by using minus `-` prefix on the rule's name so this block isn't captured itself. The resulting parse-tree will just contain one _token_ `number`. As a convention also all rules having a name starting with a `\` will not be captured. This is e.g. utilised to declare a rule named `\n` that can be used as an alias to the `\u000A` without capturing such a rule. 

### Short-hands
There is some _syntactic sugar_ that does not add more expressiveness but better readability by giving frequently used patterns a short-hand syntax.

##### White-space
- `_` = `\s` = `{ \t \n \r ' '}`
- `,` = `_*` = `{ \t \n \r ' '}*`
- `~` = `_+` = `{ \t \n \r ' '}+`
- `.` = `>> { \n \r }+ >>`*
- `>>` = `{ \t ' ' }*`

_* this is not fully equivalent as the pattern allows that CR/LF do not occur if the end of the file is reached_

##### Sets of Characters
- `9` = `'0'-'9'`
- `7` = `'0'-'7'`
- `1` = `'0'-'1'`
- `#` = `{ 9 'A'-'F'}` = `{ '0'-'9' 'A'-'F'}`
- `@` = `{ 'a'-'z' 'A'-'Z' }`
- `$` = `{ \u0000-\u7FFFFFFF }` (that is any UTF-8)

##### Occurrence
- `+` = `x1+`
- `*` = `x0+`
- `?` = `x0-1`
- `[<x>]` = `(<x>)?` = `(<x>)x0-1`
		                                                      
