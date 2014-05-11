lingukit
========

### What is lingukit?
Lingukit is a programming language independent concept to define and process formal grammar using data and solvers.
You get the lexer/parser and parse-tree for a new language by defining a grammar. The analyer and emitter is up to you to implement based on the parse-tree.

It has been designed and developed in the context of the bruno programming system for what reason the first available implementation is in Java but it a key property of the concept that it is programming language independent and can be implemented similarly in almost every other programming language.

### Aren't there plenty other tools out there?
Yes, I tried AntLR and Xtext and it really isn't fun to use them. I was out for:

- reasonable correlation between grammar and parsing behaviour
- grammar is not complected code (e.g. for AST, analyser or emitter)
- programming language independent
- data driven parsing (data + solvers)
- fine-grained control over parsing and parse tree through the grammar (including whitespace!)
- rapid REPL-like development (no code generation)
- reason about **the created language** not the tool it is build with
- bring back the **fun** in developing languages

### Status
The concept is quite mature and has shown to be complete enough to support the development of bruno without requiring further changes. The Java implementation allows to bootstrap a new language using a grammar declaration file. While the solvers always can get better they pretty much do their job good enough to develop a sophisticated language. 

### Specification
see `spec` folder...
