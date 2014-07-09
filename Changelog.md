Bruno Language Grammar Changelog
================================

2014-03-26
----------

- A function expression like a switch where the condition doesn't have to repeat the comparision with a constant of the particular case

      \ value == ?
      \ a \= 1
      \ b \= 2
      \   \= 3
      

2014-04-08
----------
Types carry information about certain kinds of data.

- structure of the data (arrays, records, functions)
- value range of the data
- invariants on the data
- available operations on the data (operations, behaviours, notations)
- text and bit representation of data