package oop_example.parser;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import oop_example.lexer.*;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class ParserTest {
    public static Parser mkParser(final Token... tokens) {
        return new Parser(tokens);
    }

    public static List<Exp> mkParams(final Exp... exps) {
        return Arrays.asList(exps);
    }
    
    @Test
    public void testParseInteger() throws ParseErrorException {
        assertEquals(new ParseResult<Exp>(new IntLiteralExp(123), 1),
                     mkParser(new IntegerToken(123)).parseExp(0));
    }

    @Test
    public void testParseTrue() throws ParseErrorException {
        assertEquals(new ParseResult<Exp>(new BoolLiteralExp(true), 1),
                     mkParser(new TrueToken()).parseExp(0));
    }

    @Test
    public void testParseFalse() throws ParseErrorException {
        assertEquals(new ParseResult<Exp>(new BoolLiteralExp(false), 1),
                     mkParser(new FalseToken()).parseExp(0));
    }

    @Test
    public void testParseThis() throws ParseErrorException {
        assertEquals(new ParseResult<Exp>(new ThisExp(), 1),
                     mkParser(new ThisToken()).parseExp(0));
    }

    @Test
    public void testParseVariable() throws ParseErrorException {
        // x
        assertEquals(new ParseResult<Exp>(new VariableExp(new Variable("x")), 1),
                     mkParser(new IdentifierToken("x")).parseExp(0));
    }
    
    @Test
    public void testParsePlusBasic() throws ParseErrorException {
        // 1 + 2
        final ParseResult<Exp> expected =
            new ParseResult<Exp>(new OpExp(new IntLiteralExp(1),
                                           new PlusOp(),
                                           new IntLiteralExp(2)),
                                 3);
        final ParseResult<Exp> received =
            mkParser(new IntegerToken(1),
                     new PlusToken(),
                     new IntegerToken(2)).parseExp(0);
        assertEquals(expected, received);
    }

    @Test
    public void testParsePlusMulti() throws ParseErrorException {
        // 1 + 2 + 3 == (1 + 2) + 3
        final ParseResult<Exp> expected =
            new ParseResult<Exp>(new OpExp(new OpExp(new IntLiteralExp(1),
                                                     new PlusOp(),
                                                     new IntLiteralExp(2)),
                                           new PlusOp(),
                                           new IntLiteralExp(3)),
                                 5);
        final ParseResult<Exp> received =
            mkParser(new IntegerToken(1),
                     new PlusToken(),
                     new IntegerToken(2),
                     new PlusToken(),
                     new IntegerToken(3)).parseExp(0);
        assertEquals(expected, received);
    }

    @Test
    public void testParseLessThanBasic() throws ParseErrorException {
        // 1 < 2
        final ParseResult<Exp> expected =
            new ParseResult<Exp>(new OpExp(new IntLiteralExp(1),
                                           new LessThanOp(),
                                           new IntLiteralExp(2)),
                                 3);
        final ParseResult<Exp> received =
            mkParser(new IntegerToken(1),
                     new LessThanToken(),
                     new IntegerToken(2)).parseExp(0);
        assertEquals(expected, received);
    }

    @Test
    public void testParseLessThanMulti() throws ParseErrorException {
        // 1 < 2 < 3 == (1 < 2) < 3
        final ParseResult<Exp> expected =
            new ParseResult<Exp>(new OpExp(new OpExp(new IntLiteralExp(1),
                                                     new LessThanOp(),
                                                     new IntLiteralExp(2)),
                                           new LessThanOp(),
                                           new IntLiteralExp(3)),
                                 5);
        final ParseResult<Exp> received =
            mkParser(new IntegerToken(1),
                     new LessThanToken(),
                     new IntegerToken(2),
                     new LessThanToken(),
                     new IntegerToken(3)).parseExp(0);
        assertEquals(expected, received);
    }

    @Test
    public void testParseEqualsBasic() throws ParseErrorException {
        // 1 == 2
        final ParseResult<Exp> expected =
            new ParseResult<Exp>(new OpExp(new IntLiteralExp(1),
                                           new EqualsOp(),
                                           new IntLiteralExp(2)),
                                 3);
        final ParseResult<Exp> received =
            mkParser(new IntegerToken(1),
                     new EqualsToken(),
                     new IntegerToken(2)).parseExp(0);
        assertEquals(expected, received);
    }

    @Test
    public void testParseEqualsMulti() throws ParseErrorException {
        // 1 == 2 == 3: (1 == 2) == 3
        final ParseResult<Exp> expected =
            new ParseResult<Exp>(new OpExp(new OpExp(new IntLiteralExp(1),
                                                     new EqualsOp(),
                                                     new IntLiteralExp(2)),
                                           new EqualsOp(),
                                           new IntLiteralExp(3)),
                                 5);
        final ParseResult<Exp> received =
            mkParser(new IntegerToken(1),
                     new EqualsToken(),
                     new IntegerToken(2),
                     new EqualsToken(),
                     new IntegerToken(3)).parseExp(0);
        assertEquals(expected, received);
    }

    @Test
    public void testParseMixed() throws ParseErrorException {
        // 1 + 2 < 3 + 4 == false
        // ((1 + 2) < (3 + 4)) == false
        final Exp onePlusTwo = new OpExp(new IntLiteralExp(1),
                                         new PlusOp(),
                                         new IntLiteralExp(2));
        final Exp threePlusFour = new OpExp(new IntLiteralExp(3),
                                            new PlusOp(),
                                            new IntLiteralExp(4));
        final Exp lessThan = new OpExp(onePlusTwo,
                                       new LessThanOp(),
                                       threePlusFour);
        final ParseResult<Exp> expected =
            new ParseResult<Exp>(new OpExp(lessThan,
                                           new EqualsOp(),
                                           new BoolLiteralExp(false)),
                                 9);
        final ParseResult<Exp> received =
            mkParser(new IntegerToken(1),
                     new PlusToken(),
                     new IntegerToken(2),
                     new LessThanToken(),
                     new IntegerToken(3),
                     new PlusToken(),
                     new IntegerToken(4),
                     new EqualsToken(),
                     new FalseToken()).parseExp(0);
        assertEquals(expected, received);
    }

    @Test
    public void testParseParens() throws ParseErrorException {
        // 1 + (2 + 3)
        final ParseResult<Exp> expected =
            new ParseResult<Exp>(new OpExp(new IntLiteralExp(1),
                                           new PlusOp(),
                                           new OpExp(new IntLiteralExp(2),
                                                     new PlusOp(),
                                                     new IntLiteralExp(3))),
                                 7);
        final ParseResult<Exp> received =
            mkParser(new IntegerToken(1),
                     new PlusToken(),
                     new LeftParenToken(),
                     new IntegerToken(2),
                     new PlusToken(),
                     new IntegerToken(3),
                     new RightParenToken()).parseExp(0);
        assertEquals(expected, received);
    }

    @Test
    public void testMethodCallNoParams() throws ParseErrorException {
        // x.foo()
        final ParseResult<Exp> expected =
            new ParseResult<Exp>(new MethodCallExp(new VariableExp(new Variable("x")),
                                                   new MethodName("foo"),
                                                   mkParams()),
                                 5);
        final ParseResult<Exp> received =
            mkParser(new IdentifierToken("x"),
                     new DotToken(),
                     new IdentifierToken("foo"),
                     new LeftParenToken(),
                     new RightParenToken()).parseExp(0);
        assertEquals(expected, received);
    }

    @Test
    public void testMethodCallNoParamsChained() throws ParseErrorException {
        // x.foo().bar()
        final Exp fooCall = new MethodCallExp(new VariableExp(new Variable("x")),
                                              new MethodName("foo"),
                                              mkParams());
        final Exp barCall = new MethodCallExp(fooCall,
                                              new MethodName("bar"),
                                              mkParams());
        final ParseResult<Exp> expected = new ParseResult<Exp>(barCall, 9);
        final ParseResult<Exp> received =
            mkParser(new IdentifierToken("x"),
                     new DotToken(),
                     new IdentifierToken("foo"),
                     new LeftParenToken(),
                     new RightParenToken(),
                     new DotToken(),
                     new IdentifierToken("bar"),
                     new LeftParenToken(),
                     new RightParenToken()).parseExp(0);
        assertEquals(expected, received);
    }

    @Test
    public void testMethodCallSingleParam() throws ParseErrorException {
        // x.foo(1)
        final Exp fooCall = new MethodCallExp(new VariableExp(new Variable("x")),
                                              new MethodName("foo"),
                                              mkParams(new IntLiteralExp(1)));
        final ParseResult<Exp> expected = new ParseResult<Exp>(fooCall, 6);
        final ParseResult<Exp> received =
            mkParser(new IdentifierToken("x"),
                     new DotToken(),
                     new IdentifierToken("foo"),
                     new LeftParenToken(),
                     new IntegerToken(1),
                     new RightParenToken()).parseExp(0);
        assertEquals(expected, received);
    }

    @Test
    public void testMethodCallMultiParam() throws ParseErrorException {
        // x.foo(1, 2)
        final Exp fooCall = new MethodCallExp(new VariableExp(new Variable("x")),
                                              new MethodName("foo"),
                                              mkParams(new IntLiteralExp(1),
                                                       new IntLiteralExp(2)));
        final ParseResult<Exp> expected = new ParseResult<Exp>(fooCall, 8);
        final ParseResult<Exp> received =
            mkParser(new IdentifierToken("x"),
                     new DotToken(),
                     new IdentifierToken("foo"),
                     new LeftParenToken(),
                     new IntegerToken(1),
                     new CommaToken(),
                     new IntegerToken(2),
                     new RightParenToken()).parseExp(0);
        assertEquals(expected, received);
    }

    @Test
    public void testMethodCallMultiParamChained() throws ParseErrorException {
        // x.foo(1, 2).bar(3, 4)
        final Exp fooCall = new MethodCallExp(new VariableExp(new Variable("x")),
                                              new MethodName("foo"),
                                              mkParams(new IntLiteralExp(1),
                                                       new IntLiteralExp(2)));
        final Exp barCall = new MethodCallExp(fooCall,
                                              new MethodName("bar"),
                                              mkParams(new IntLiteralExp(3),
                                                       new IntLiteralExp(4)));
        final ParseResult<Exp> expected = new ParseResult<Exp>(barCall, 15);
        final ParseResult<Exp> received =
            mkParser(new IdentifierToken("x"),
                     new DotToken(),
                     new IdentifierToken("foo"),
                     new LeftParenToken(),
                     new IntegerToken(1),
                     new CommaToken(),
                     new IntegerToken(2),
                     new RightParenToken(),
                     new DotToken(),
                     new IdentifierToken("bar"),
                     new LeftParenToken(),
                     new IntegerToken(3),
                     new CommaToken(),
                     new IntegerToken(4),
                     new RightParenToken()).parseExp(0);
        assertEquals(expected, received);
    }

    @Test
    public void testNew() throws ParseErrorException {
        // new Foo(1, 2)
        final Exp newFoo = new NewExp(new ClassName("Foo"),
                                      mkParams(new IntLiteralExp(1),
                                               new IntLiteralExp(2)));
        final ParseResult<Exp> expected = new ParseResult<Exp>(newFoo, 7);
        final ParseResult<Exp> received =
            mkParser(new NewToken(),
                     new IdentifierToken("Foo"),
                     new LeftParenToken(),
                     new IntegerToken(1),
                     new CommaToken(),
                     new IntegerToken(2),
                     new RightParenToken()).parseExp(0);
        assertEquals(expected, received);
    }

    @Test
    public void testNewAndMethodCall() throws ParseErrorException {
        // new Foo(1, 2).bar(3, 4)
        final Exp newFoo = new NewExp(new ClassName("Foo"),
                                      mkParams(new IntLiteralExp(1),
                                               new IntLiteralExp(2)));
        final Exp barCall = new MethodCallExp(newFoo,
                                              new MethodName("bar"),
                                              mkParams(new IntLiteralExp(3),
                                                       new IntLiteralExp(4)));
        final ParseResult<Exp> expected = new ParseResult<Exp>(barCall, 14);
        final ParseResult<Exp> received =
            mkParser(new NewToken(),
                     new IdentifierToken("Foo"),
                     new LeftParenToken(),
                     new IntegerToken(1),
                     new CommaToken(),
                     new IntegerToken(2),
                     new RightParenToken(),
                     new DotToken(),
                     new IdentifierToken("bar"),
                     new LeftParenToken(),
                     new IntegerToken(3),
                     new CommaToken(),
                     new IntegerToken(4),
                     new RightParenToken()).parseExp(0);
        assertEquals(expected, received);
    }

    @Test
    public void testMixedPlusAndMethodCall() throws ParseErrorException {
        // x.foo(1, 2) + y.bar(3, 4)
        final Exp fooCall = new MethodCallExp(new VariableExp(new Variable("x")),
                                              new MethodName("foo"),
                                              mkParams(new IntLiteralExp(1),
                                                       new IntLiteralExp(2)));
        final Exp barCall = new MethodCallExp(new VariableExp(new Variable("y")),
                                              new MethodName("bar"),
                                              mkParams(new IntLiteralExp(3),
                                                       new IntLiteralExp(4)));
        final ParseResult<Exp> expected =
            new ParseResult<Exp>(new OpExp(fooCall, new PlusOp(), barCall), 17);
        final ParseResult<Exp> received =
            mkParser(new IdentifierToken("x"),
                     new DotToken(),
                     new IdentifierToken("foo"),
                     new LeftParenToken(),
                     new IntegerToken(1),
                     new CommaToken(),
                     new IntegerToken(2),
                     new RightParenToken(),
                     new PlusToken(),
                     new IdentifierToken("y"),
                     new DotToken(),
                     new IdentifierToken("bar"),
                     new LeftParenToken(),
                     new IntegerToken(3),
                     new CommaToken(),
                     new IntegerToken(4),
                     new RightParenToken()).parseExp(0);
        assertEquals(expected, received);
    }

    @Test
    public void testClassNoMethodsNoInstanceVariables() throws ParseErrorException {
        // class MyClass extends Object {
        //   constructor() { super(); }
        // }
        final ClassDef classDef = new ClassDef(new ClassName("MyClass"),
                                               new ClassName("Object"),
                                               new ArrayList<Vardec>(),
                                               new ArrayList<Vardec>(),
                                               mkParams(),
                                               new ArrayList<Stmt>(),
                                               new ArrayList<MethodDef>());
        final ParseResult<ClassDef> expected =
            new ParseResult<ClassDef>(classDef, 15);
        final ParseResult<ClassDef> received =
            mkParser(new ClassToken(),
                     new IdentifierToken("MyClass"),
                     new ExtendsToken(),
                     new IdentifierToken("Object"),
                     new LeftCurlyToken(),
                     new ConstructorToken(),
                     new LeftParenToken(),
                     new RightParenToken(),
                     new LeftCurlyToken(),
                     new SuperToken(),
                     new LeftParenToken(),
                     new RightParenToken(),
                     new SemicolonToken(),
                     new RightCurlyToken(),
                     new RightCurlyToken()).parseClassDef(0);
        assertEquals(expected, received);
    }

    @Test
    public void testClassOneInstanceVariable() throws ParseErrorException {
        // class MyClass extends Object {
        //   int x;
        //   constructor() { super(); }
        // }
        final ClassDef classDef = new ClassDef(new ClassName("MyClass"),
                                               new ClassName("Object"),
                                               Arrays.asList(new Vardec(new IntType(), new Variable("x"))),
                                               new ArrayList<Vardec>(),
                                               mkParams(),
                                               new ArrayList<Stmt>(),
                                               new ArrayList<MethodDef>());
        final ParseResult<ClassDef> expected =
            new ParseResult<ClassDef>(classDef, 18);
        final ParseResult<ClassDef> received =
            mkParser(new ClassToken(),
                     new IdentifierToken("MyClass"),
                     new ExtendsToken(),
                     new IdentifierToken("Object"),
                     new LeftCurlyToken(),
                     new IntToken(),
                     new IdentifierToken("x"),
                     new SemicolonToken(),
                     new ConstructorToken(),
                     new LeftParenToken(),
                     new RightParenToken(),
                     new LeftCurlyToken(),
                     new SuperToken(),
                     new LeftParenToken(),
                     new RightParenToken(),
                     new SemicolonToken(),
                     new RightCurlyToken(),
                     new RightCurlyToken()).parseClassDef(0);
        assertEquals(expected, received);
    }

    @Test
    public void testClassTwoInstanceVariables() throws ParseErrorException {
        // class MyClass extends Object {
        //   int x;
        //   bool y;
        //   constructor() { super(); }
        // }
        final ClassDef classDef = new ClassDef(new ClassName("MyClass"),
                                               new ClassName("Object"),
                                               Arrays.asList(new Vardec(new IntType(), new Variable("x")),
                                                             new Vardec(new BoolType(), new Variable("y"))),
                                               new ArrayList<Vardec>(),
                                               mkParams(),
                                               new ArrayList<Stmt>(),
                                               new ArrayList<MethodDef>());
        final ParseResult<ClassDef> expected =
            new ParseResult<ClassDef>(classDef, 21);
        final ParseResult<ClassDef> received =
            mkParser(new ClassToken(),
                     new IdentifierToken("MyClass"),
                     new ExtendsToken(),
                     new IdentifierToken("Object"),
                     new LeftCurlyToken(),
                     new IntToken(),
                     new IdentifierToken("x"),
                     new SemicolonToken(),
                     new BoolToken(),
                     new IdentifierToken("y"),
                     new SemicolonToken(),
                     new ConstructorToken(),
                     new LeftParenToken(),
                     new RightParenToken(),
                     new LeftCurlyToken(),
                     new SuperToken(),
                     new LeftParenToken(),
                     new RightParenToken(),
                     new SemicolonToken(),
                     new RightCurlyToken(),
                     new RightCurlyToken()).parseClassDef(0);
        assertEquals(expected, received);
    }

    @Test
    public void testClassOneConstructorParam() throws ParseErrorException {
        // class MyClass extends Object {
        //   constructor(int x) { super(); }
        // }
        final ClassDef classDef = new ClassDef(new ClassName("MyClass"),
                                               new ClassName("Object"),
                                               new ArrayList<Vardec>(),
                                               Arrays.asList(new Vardec(new IntType(), new Variable("x"))),
                                               mkParams(),
                                               new ArrayList<Stmt>(),
                                               new ArrayList<MethodDef>());
        final ParseResult<ClassDef> expected =
            new ParseResult<ClassDef>(classDef, 17);
        final ParseResult<ClassDef> received =
            mkParser(new ClassToken(),
                     new IdentifierToken("MyClass"),
                     new ExtendsToken(),
                     new IdentifierToken("Object"),
                     new LeftCurlyToken(),
                     new ConstructorToken(),
                     new LeftParenToken(),
                     new IntToken(),
                     new IdentifierToken("x"),
                     new RightParenToken(),
                     new LeftCurlyToken(),
                     new SuperToken(),
                     new LeftParenToken(),
                     new RightParenToken(),
                     new SemicolonToken(),
                     new RightCurlyToken(),
                     new RightCurlyToken()).parseClassDef(0);
        assertEquals(expected, received);
    }

    @Test
    public void testClassTwoConstructorParams() throws ParseErrorException {
        // class MyClass extends Object {
        //   constructor(int x, bool y) { super(); }
        // }
        final ClassDef classDef = new ClassDef(new ClassName("MyClass"),
                                               new ClassName("Object"),
                                               new ArrayList<Vardec>(),
                                               Arrays.asList(new Vardec(new IntType(), new Variable("x")),
                                                             new Vardec(new BoolType(), new Variable("y"))),
                                               mkParams(),
                                               new ArrayList<Stmt>(),
                                               new ArrayList<MethodDef>());
        final ParseResult<ClassDef> expected =
            new ParseResult<ClassDef>(classDef, 20);
        final ParseResult<ClassDef> received =
            mkParser(new ClassToken(),
                     new IdentifierToken("MyClass"),
                     new ExtendsToken(),
                     new IdentifierToken("Object"),
                     new LeftCurlyToken(),
                     new ConstructorToken(),
                     new LeftParenToken(),
                     new IntToken(),
                     new IdentifierToken("x"),
                     new CommaToken(),
                     new BoolToken(),
                     new IdentifierToken("y"),
                     new RightParenToken(),
                     new LeftCurlyToken(),
                     new SuperToken(),
                     new LeftParenToken(),
                     new RightParenToken(),
                     new SemicolonToken(),
                     new RightCurlyToken(),
                     new RightCurlyToken()).parseClassDef(0);
        assertEquals(expected, received);
    }

    @Test
    public void testClassSingleMethod() throws ParseErrorException {
        // class MyClass extends Object {
        //   constructor() { super(); }
        //   int myMethod() { return 0; }
        // }
        final List<MethodDef> methods =
            Arrays.asList(new MethodDef(new IntType(),
                                        new MethodName("myMethod"),
                                        new ArrayList<Vardec>(),
                                        new BlockStmt(Arrays.asList(new ReturnNonVoidStmt(new IntLiteralExp(0))))));
        final ClassDef classDef = new ClassDef(new ClassName("MyClass"),
                                               new ClassName("Object"),
                                               new ArrayList<Vardec>(),
                                               new ArrayList<Vardec>(),
                                               mkParams(),
                                               new ArrayList<Stmt>(),
                                               methods);
        final ParseResult<ClassDef> expected =
            new ParseResult<ClassDef>(classDef, 24);
        final ParseResult<ClassDef> received =
            mkParser(new ClassToken(),
                     new IdentifierToken("MyClass"),
                     new ExtendsToken(),
                     new IdentifierToken("Object"),
                     new LeftCurlyToken(),
                     new ConstructorToken(),
                     new LeftParenToken(),
                     new RightParenToken(),
                     new LeftCurlyToken(),
                     new SuperToken(),
                     new LeftParenToken(),
                     new RightParenToken(),
                     new SemicolonToken(),
                     new RightCurlyToken(),
                     new IntToken(),
                     new IdentifierToken("myMethod"),
                     new LeftParenToken(),
                     new RightParenToken(),
                     new LeftCurlyToken(),
                     new ReturnToken(),
                     new IntegerToken(0),
                     new SemicolonToken(),
                     new RightCurlyToken(),
                     new RightCurlyToken()).parseClassDef(0);
        assertEquals(expected, received);
    }

    @Test
    public void testClassSingleMethodWithParam() throws ParseErrorException {
        // class MyClass extends Object {
        //   constructor() { super(); }
        //   int myMethod(int x) { return x; }
        // }
        final List<MethodDef> methods =
            Arrays.asList(new MethodDef(new IntType(),
                                        new MethodName("myMethod"),
                                        Arrays.asList(new Vardec(new IntType(), new Variable("x"))),
                                        new BlockStmt(Arrays.asList(new ReturnNonVoidStmt(new VariableExp(new Variable("x")))))));
        final ClassDef classDef = new ClassDef(new ClassName("MyClass"),
                                               new ClassName("Object"),
                                               new ArrayList<Vardec>(),
                                               new ArrayList<Vardec>(),
                                               mkParams(),
                                               new ArrayList<Stmt>(),
                                               methods);
        final ParseResult<ClassDef> expected =
            new ParseResult<ClassDef>(classDef, 26);
        final ParseResult<ClassDef> received =
            mkParser(new ClassToken(),
                     new IdentifierToken("MyClass"),
                     new ExtendsToken(),
                     new IdentifierToken("Object"),
                     new LeftCurlyToken(),
                     new ConstructorToken(),
                     new LeftParenToken(),
                     new RightParenToken(),
                     new LeftCurlyToken(),
                     new SuperToken(),
                     new LeftParenToken(),
                     new RightParenToken(),
                     new SemicolonToken(),
                     new RightCurlyToken(),
                     new IntToken(),
                     new IdentifierToken("myMethod"),
                     new LeftParenToken(),
                     new IntToken(),
                     new IdentifierToken("x"),
                     new RightParenToken(),
                     new LeftCurlyToken(),
                     new ReturnToken(),
                     new IdentifierToken("x"),
                     new SemicolonToken(),
                     new RightCurlyToken(),
                     new RightCurlyToken()).parseClassDef(0);
        assertEquals(expected, received);
    }

    @Test
    public void testClassNonEmptyConstructor() throws ParseErrorException {
        // class MyClass extends Object {
        //   int x;
        //   constructor(int xParam) {
        //     super();
        //     x = xParam;
        //   }
        // }
        final ClassDef classDef = new ClassDef(new ClassName("MyClass"),
                                               new ClassName("Object"),
                                               Arrays.asList(new Vardec(new IntType(), new Variable("x"))),
                                               Arrays.asList(new Vardec(new IntType(), new Variable("xParam"))),
                                               mkParams(),
                                               Arrays.asList(new AssignStmt(new Variable("x"),
                                                                            new VariableExp(new Variable("xParam")))),
                                               new ArrayList<MethodDef>());
        final ParseResult<ClassDef> expected =
            new ParseResult<ClassDef>(classDef, 24);
        final ParseResult<ClassDef> received =
            mkParser(new ClassToken(),
                     new IdentifierToken("MyClass"),
                     new ExtendsToken(),
                     new IdentifierToken("Object"),
                     new LeftCurlyToken(),
                     new IntToken(),
                     new IdentifierToken("x"),
                     new SemicolonToken(),
                     new ConstructorToken(),
                     new LeftParenToken(),
                     new IntToken(),
                     new IdentifierToken("xParam"),
                     new RightParenToken(),
                     new LeftCurlyToken(),
                     new SuperToken(),
                     new LeftParenToken(),
                     new RightParenToken(),
                     new SemicolonToken(),
                     new IdentifierToken("x"),
                     new AssignToken(),
                     new IdentifierToken("xParam"),
                     new SemicolonToken(),
                     new RightCurlyToken(),
                     new RightCurlyToken()).parseClassDef(0);
        assertEquals(expected, received);
    }            
}
