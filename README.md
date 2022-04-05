# Class-Based Object-Oriented Programming Example

This is specifically for typechecking and code generation.
Key language features:

- Object-oriented classes
- Inheritance
- Subtyping

Assumptions:
- `void` is not a special value (Java/C/C++ semantics)
- `Object` is a built-in class with a no-arg constructor

```
x is a variable
i is an integer
classname is a class name
methodname is a method name
type ::= int | bool | void | classname
op ::= + | < | ==
exp ::= i | x | true | false | this | exp op exp | exp.methodname(exp*) | new classname(exp*)
vardec ::= type x
stmt ::= exp; |
         vardec = exp; |
         if (exp) stmt else stmt |
         while (exp) stmt |
         return exp; |
         return; |
         println(exp); |
         { stmt* }
methoddef ::= type methodname(vardec*) stmt
classdef ::= class classname extends classname {
               vardec*; // comma-separated instance variables
               constructor(vardec*) {
                 super(exp*);
                 stmt* // comma-separated
               }
               methoddef*
             }
program ::= classdef* stmt // stmt is the entry point
```
