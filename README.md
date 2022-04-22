# Class-Based Object-Oriented Programming Example

This is specifically for typechecking and code generation.
Key language features:

- Object-oriented classes
- Inheritance
- Subtyping

Assumptions:
- `void` is not a special value (Java/C/C++ semantics)
- `Object` is a built-in class with a no-arg constructor

```java
public class MyClass {
  public MyClass(int x) {}
  public void myMethod() { println(true); }
}

public class Subclass extends MyClass {
  public Subclass(int y) { super(y); }
  //public void myMethod() { println(false); }
}

public class OtherClass { ... }
MyClass temp = new OtherClass(); // new SubClass(7);
temp.myMethod();

public class Foo extends Bar {}
public class Bar extends Foo {}
```

```java
// forgetting to return from a function in C: undefined behavior
public static int doSomething(int x) {
  if (x < 3) {
    return 11;
  } else {

  }
}
```

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

Target language: JavaScript
Every object will be represented by a JavaScript object.
However, we will not make use of prototype-based inheritance.

```
// example code
class BaseClass extends Object {
  int x;
  constructor(int x) {
    super();
    this.x = x;
  }
  int firstMethod(int y) {
    return x + y;
  }
  int secondMethod(int y) {
    return x * y;
  }
}
class SubClass extends Object {
  int y;
  constructor(int x, int y) {
    super(x);
    this.y = y;
  }
  int firstMethod(int z) {
    return x + y + z;
  }
  int thirdMethod(int z) {
    return x - y - z;
  }
}

{
  BaseClass b = new BaseClass(1);
  SubClass sub = new SubClass(2, 3);
  print(b.firstMethod(4));
  print(b.secondMethod(5));
  print(sub.firstMethod(6));
  print(sub.secondMethod(7));
  print(sub.thirdMethod(8));
}
```

```javascript
// translation to JavaScript
function makeObject(vtable, constructor, ...params) {
  let self = {};
  self.vtable = vtable;
  constructor.apply(self, params);
  return self;
}
function Object_constructor(self) {}
function BaseClass_constructor(self, x) {
  Object_constructor(self);
  self.x = x;
}
function BaseClass_firstMethod(self, y) {
  return self.x + y;
}
function BaseClass_secondMethod(self, y) {
  return self.x * y;
}
function SubClass_constructor(self, x, y) {
  BaseClass_constructor(self, x);
  self.y = y;
  return self;
}
function SubClass_firstMethod(self, z) {
  return self.x + self.y + z;
}
function SubClass_thirdMethod(self, z) {
  return self.x - self.y - z;
}
let vtable_BaseClass = [BaseClass_firstMethod,
                        BaseClass_secondMethod];
let vtable_SubClass = [SubClass_firstMethod,
                       BaseClass_secondMethod,
                       SubClass_thirdMethod];

{
  let b = makeObject(vtable_BaseClass, BaseClass_constructor, 1);
  let sub = makeObject(vtable_SubClass, SubClass_constructor, 2, 3);
  console.log(b.vtable[0](b, 4));
  console.log(b.vtable[1](b, 5));
  console.log(sub.vtable[0](sub, 6));
  console.log(sub.vtable[1](sub, 7));
  console.log(sub.vtable[2](sub, 8));
}
```
