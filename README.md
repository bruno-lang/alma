ALMA
====

Instructions
------------

	, ind
	; ind!
	. ws
	: ws!
	! nl!

	_ any
	' many
	` one

	# doxy
	* do
	+ do!
	? do?
	1 do1
	2 do2
	3 do3
	4 do4
	5 do5
	6 do6
	7 do7
	8 do8
	9 do9

	@ ref
	= rec
	
	( nest
	) ret
	[ case
	| brk
	] end

	~ fill
	< lock
	> head

Syntactic Sugar
---------------
` ...? `    => `(?...)`
` ...* `    => `(*...)`
` ...+ `    => `(+...)`
` ...*n-m ` => `(#nm...)`
` ...5 `    => `(5...)`
` ...5+ `   => `(5+...)`

`(...)=name `   => `(=name...)`
`[...]=name `   => `[=name...]`
`name = (...)`  => `(=name...)`
`name = [...]`  => `[=name...]` 
`name = ... \n` => `(=name...)`

Files
-----
```
alma/
+-- _data/        example input files parsed in tests
+-- alma/         several grammars specified in alma language
+-- java/         java implementation
```
