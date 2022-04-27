package oop_example.parser;

import java.util.List;
import java.util.ArrayList;

import oop_example.lexer.*;

public class Parser {
    public final Token[] tokens;

    public Parser(final Token[] tokens) {
        this.tokens = tokens;
    }

    public Token getToken(final int position) throws ParseErrorException {
        if (position < 0 || position >= tokens.length) {
            throw new ParseErrorException("No token at position: " + position);
        } else {
            return tokens[position];
        }
    }

    public void assertTokenHereIs(final int position,
                                  final Token expected) throws ParseErrorException {
        final Token received = getToken(position);
        if (!received.equals(expected)) {
            throw new ParseErrorException("Expected token " + expected +
                                          "; received: " + received);
        }
    }

    public ParseResult<ClassName> parseClassName(final int position) throws ParseErrorException {
        final Token token = getToken(position);
        if (token instanceof IdentifierToken) {
            final String name = ((IdentifierToken)token).identifier;
            return new ParseResult<ClassName>(new ClassName(name), position + 1);
        } else {
            throw new ParseErrorException("Expected class name; received: " + token);
        }
    }

    public ParseResult<MethodName> parseMethodName(final int position) throws ParseErrorException {
        final Token token = getToken(position);
        if (token instanceof IdentifierToken) {
            final String name = ((IdentifierToken)token).identifier;
            return new ParseResult<MethodName>(new MethodName(name), position + 1);
        } else {
            throw new ParseErrorException("Expected method name; received: " + token);
        }
    }

    public ParseResult<Variable> parseVariable(final int position) throws ParseErrorException {
        final Token token = getToken(position);
        if (token instanceof IdentifierToken) {
            final String name = ((IdentifierToken)token).identifier;
            return new ParseResult<Variable>(new Variable(name), position + 1);
        } else {
            throw new ParseErrorException("Expected variable name; received: " + token);
        }
    }

    // type ::= int | bool | void | classname
    public ParseResult<Type> parseType(int position) throws ParseErrorException {
        final Token token = getToken(position);
        Type type = null;
        if (token instanceof IntToken) {
            type = new IntType();
            position++;
        } else if (token instanceof BoolToken) {
            type = new BoolType();
            position++;
        } else if (token instanceof VoidToken) {
            type = new VoidType();
            position++;
        } else {
            final ParseResult<ClassName> className = parseClassName(position);
            position = className.position;
            type = new ClassNameType(className.result);
        }

        return new ParseResult<Type>(type, position);
    }

    // primary_exp ::= i | x | true | false | this | `(` exp `)` | new classname `(` comma_exp `)`
    public ParseResult<Exp> parsePrimaryExp(int position) throws ParseErrorException {
        final Token token = getToken(position++);
        Exp exp = null;
        if (token instanceof IntegerToken) {
            exp = new IntLiteralExp(((IntegerToken)token).value);
        } else if (token instanceof IdentifierToken) {
            exp = new VariableExp(new Variable(((IdentifierToken)token).identifier));
        } else if (token instanceof TrueToken) {
            exp = new BoolLiteralExp(true);
        } else if (token instanceof FalseToken) {
            exp = new BoolLiteralExp(false);
        } else if (token instanceof ThisToken) {
            exp = new ThisExp();
        } else if (token instanceof LeftParenToken) {
            final ParseResult<Exp> nested = parseExp(position);
            assertTokenHereIs(nested.position, new RightParenToken());
            exp = nested.result;
            position = nested.position + 1;
        } else if (token instanceof NewToken) {
            final ParseResult<ClassName> className = parseClassName(position);
            assertTokenHereIs(className.position, new LeftParenToken());
            final ParseResult<List<Exp>> params = parseCommaExp(className.position + 1);
            position = params.position;
            assertTokenHereIs(position++, new RightParenToken());
            exp = new NewExp(className.result, params.result);
        } else {
            throw new ParseErrorException("Expected primary expression; received: " + token +
                                          " at position: " + position);
        }

        return new ParseResult<Exp>(exp, position);
    }

    // dot_exp ::= primary_exp (`.` methodname `(` comma_exp `)`)*
    public ParseResult<Exp> parseDotExp(final int position) throws ParseErrorException {
        ParseResult<Exp> retval = parsePrimaryExp(position);
        boolean shouldRun = true;

        while (shouldRun) {
            try {
                assertTokenHereIs(retval.position, new DotToken());
                final ParseResult<MethodName> methodName = parseMethodName(retval.position + 1);
                assertTokenHereIs(methodName.position, new LeftParenToken());
                final ParseResult<List<Exp>> params = parseCommaExp(methodName.position + 1);
                assertTokenHereIs(params.position, new RightParenToken());
                retval = new ParseResult<Exp>(new MethodCallExp(retval.result,
                                                                methodName.result,
                                                                params.result),
                                              params.position + 1);
            } catch (final ParseErrorException e) {
                shouldRun = false;
            }
        }

        return retval;
    }

    // additive_exp ::= dot_exp (`+` dot_exp)*
    public ParseResult<Exp> parseAdditiveExp(final int position) throws ParseErrorException {
        ParseResult<Exp> retval = parseDotExp(position);
        boolean shouldRun = true;

        while (shouldRun) {
            try {
                assertTokenHereIs(retval.position, new PlusToken());
                final ParseResult<Exp> right = parseDotExp(retval.position + 1);
                retval = new ParseResult<Exp>(new OpExp(retval.result,
                                                        new PlusOp(),
                                                        right.result),
                                              right.position);
            } catch (final ParseErrorException e) {
                shouldRun = false;
            }
        }

        return retval;
    }

    // less_than_exp ::= additive_exp (`<` additive_exp)*
    public ParseResult<Exp> parseLessThanExp(final int position) throws ParseErrorException {
        ParseResult<Exp> retval = parseAdditiveExp(position);
        boolean shouldRun = true;

        while (shouldRun) {
            try {
                assertTokenHereIs(retval.position, new LessThanToken());
                final ParseResult<Exp> right = parseAdditiveExp(retval.position + 1);
                retval = new ParseResult<Exp>(new OpExp(retval.result,
                                                        new LessThanOp(),
                                                        right.result),
                                              right.position);
            } catch (final ParseErrorException e) {
                shouldRun = false;
            }
        }

        return retval;
    }

    // equals_exp ::= less_than_exp (`==` less_than_exp)*
    public ParseResult<Exp> parseEqualsExp(final int position) throws ParseErrorException {
        ParseResult<Exp> retval = parseLessThanExp(position);
        boolean shouldRun = true;

        while (shouldRun) {
            try {
                assertTokenHereIs(retval.position, new EqualsToken());
                final ParseResult<Exp> right = parseLessThanExp(retval.position + 1);
                retval = new ParseResult<Exp>(new OpExp(retval.result,
                                                        new EqualsOp(),
                                                        right.result),
                                              right.position);
            } catch (final ParseErrorException e) {
                shouldRun = false;
            }
        }

        return retval;
    }

    // comma_exp ::= [equals_exp (`,` equals_exp)*]
    public ParseResult<List<Exp>> parseCommaExp(int position) throws ParseErrorException {
        final List<Exp> exps = new ArrayList<Exp>();

        try {
            ParseResult<Exp> currentExp = parseExp(position);
            exps.add(currentExp.result);
            position = currentExp.position;
            boolean shouldRun = true;
            while (shouldRun) {
                try {
                    assertTokenHereIs(position, new CommaToken());
                    currentExp = parseExp(currentExp.position + 1);
                    exps.add(currentExp.result);
                    position = currentExp.position;
                } catch (final ParseErrorException e) {
                    shouldRun = false;
                }
            }
        } catch (final ParseErrorException e) {}

        return new ParseResult<List<Exp>>(exps, position);
    }

    // exp ::= comma_exp
    public ParseResult<Exp> parseExp(final int position) throws ParseErrorException {
        return parseEqualsExp(position);
    }

    // vardec ::= type x
    public ParseResult<Vardec> parseVardec(final int position) throws ParseErrorException {
        final ParseResult<Type> type = parseType(position);
        final ParseResult<Variable> variable = parseVariable(type.position);
        return new ParseResult<Vardec>(new Vardec(type.result, variable.result),
                                       variable.position);
    }

    public ParseResult<List<Stmt>> parseStmts(int position) throws ParseErrorException {
        final List<Stmt> stmts = new ArrayList<Stmt>();
        boolean shouldRun = true;
        while (shouldRun) {
            try {
                final ParseResult<Stmt> stmt = parseStmt(position);
                stmts.add(stmt.result);
                position = stmt.position;
            } catch (final ParseErrorException e) {
                shouldRun = false;
            }
        }

        return new ParseResult<List<Stmt>>(stmts, position);
    }
    
    // stmt ::=
    //      if (exp) stmt else stmt |
    //      while (exp) stmt |
    //      return exp; |
    //      return; |
    //      println(exp); |
    //      { stmt* } |
    //      vardec = exp; |
    //      exp;
    // try expression statements last, as we don't know we (should) have one until we
    // finish parsing it.  Similar situation for vardecs
    public ParseResult<Stmt> parseStmt(final int position) throws ParseErrorException {
        final Token token = getToken(position);
        if (token instanceof IfToken) {
            assertTokenHereIs(position + 1, new LeftParenToken());
            final ParseResult<Exp> guard = parseExp(position + 2);
            assertTokenHereIs(guard.position, new RightParenToken());
            final ParseResult<Stmt> ifTrue = parseStmt(guard.position + 1);
            assertTokenHereIs(ifTrue.position, new ElseToken());
            final ParseResult<Stmt> ifFalse = parseStmt(ifTrue.position + 1);
            return new ParseResult<Stmt>(new IfStmt(guard.result, ifTrue.result, ifFalse.result),
                                         ifFalse.position);
        } else if (token instanceof WhileToken) {
            assertTokenHereIs(position + 1, new LeftParenToken());
            final ParseResult<Exp> guard = parseExp(position + 2);
            assertTokenHereIs(guard.position, new RightParenToken());
            final ParseResult<Stmt> body = parseStmt(guard.position + 1);
            return new ParseResult<Stmt>(new WhileStmt(guard.result, body.result),
                                         body.position);
        } else if (token instanceof ReturnToken) {
            if (getToken(position + 1) instanceof SemicolonToken) {
                return new ParseResult<Stmt>(new ReturnVoidStmt(), position + 2);
            } else {
                final ParseResult<Exp> exp = parseExp(position + 1);
                assertTokenHereIs(exp.position, new SemicolonToken());
                return new ParseResult<Stmt>(new ReturnNonVoidStmt(exp.result),
                                             exp.position + 1);
            }
        } else if (token instanceof PrintlnToken) {
            assertTokenHereIs(position + 1, new LeftParenToken());
            final ParseResult<Exp> exp = parseExp(position + 2);
            assertTokenHereIs(exp.position, new RightParenToken());
            assertTokenHereIs(exp.position + 1, new SemicolonToken());
            return new ParseResult<Stmt>(new PrintlnStmt(exp.result),
                                         exp.position + 2);
        } else if (token instanceof LeftCurlyToken) {
            final ParseResult<List<Stmt>> stmts = parseStmts(position + 1);
            assertTokenHereIs(stmts.position, new RightCurlyToken());
            return new ParseResult<Stmt>(new BlockStmt(stmts.result),
                                         stmts.position + 1);
        } else {
            // try variable declaration...
            try {
                final ParseResult<Vardec> vardec = parseVardec(position);
                assertTokenHereIs(vardec.position, new AssignToken());
                final ParseResult<Exp> exp = parseExp(vardec.position + 1);
                assertTokenHereIs(exp.position, new SemicolonToken());
                return new ParseResult<Stmt>(new VariableInitializationStmt(vardec.result,
                                                                            exp.result),
                                             exp.position + 1);
            } catch (final ParseErrorException e1) {
                // ...then expression statements if that didn't work...
                try {
                    final ParseResult<Exp> exp = parseExp(position);
                    assertTokenHereIs(exp.position, new SemicolonToken());
                    return new ParseResult<Stmt>(new ExpStmt(exp.result),
                                                 exp.position + 1);
                } catch (final ParseErrorException e2) {
                    // ...and finally assignment if that didn't work
                    final ParseResult<Variable> variable = parseVariable(position);
                    assertTokenHereIs(variable.position, new AssignToken());
                    final ParseResult<Exp> exp = parseExp(variable.position + 1);
                    assertTokenHereIs(exp.position, new SemicolonToken());
                    return new ParseResult<Stmt>(new AssignStmt(variable.result,
                                                                exp.result),
                                                 exp.position + 1);
                }
            }
        }
    }

    // vardecs_comma ::= [vardec (`,` vardec)*]
    public ParseResult<List<Vardec>> parseVardecsComma(int position) throws ParseErrorException {
        final List<Vardec> vardecs = new ArrayList<Vardec>();

        try {
            ParseResult<Vardec> vardec = parseVardec(position);
            vardecs.add(vardec.result);
            position = vardec.position;
            boolean shouldRun = true;
            while (shouldRun) {
                try {
                    assertTokenHereIs(position, new CommaToken());
                    vardec = parseVardec(position + 1);
                    vardecs.add(vardec.result);
                    position = vardec.position;
                } catch (final ParseErrorException e) {
                    shouldRun = false;
                }
            }
        } catch (final ParseErrorException e) {}

        return new ParseResult<List<Vardec>>(vardecs, position);
    }

    // vardecs_semicolon ::= (vardec `;`)*
    public ParseResult<List<Vardec>> parseVardecsSemicolon(int position) throws ParseErrorException {
        final List<Vardec> vardecs = new ArrayList<Vardec>();
        boolean shouldRun = true;
        while (shouldRun) {
            try {
                final ParseResult<Vardec> vardec = parseVardec(position);
                assertTokenHereIs(vardec.position, new SemicolonToken());
                vardecs.add(vardec.result);
                position = vardec.position + 1;
            } catch (final ParseErrorException e) {
                shouldRun = false;
            }
        }

        return new ParseResult<List<Vardec>>(vardecs, position);
    }

    // methoddef ::= type methodname(vardecs_comma) stmt
    public ParseResult<MethodDef> parseMethodDef(final int position) throws ParseErrorException {
        final ParseResult<Type> type = parseType(position);
        final ParseResult<MethodName> methodName = parseMethodName(type.position);
        assertTokenHereIs(methodName.position, new LeftParenToken());
        final ParseResult<List<Vardec>> arguments = parseVardecsComma(methodName.position + 1);
        assertTokenHereIs(arguments.position, new RightParenToken());
        final ParseResult<Stmt> body = parseStmt(arguments.position + 1);
        return new ParseResult<MethodDef>(new MethodDef(type.result,
                                                        methodName.result,
                                                        arguments.result,
                                                        body.result),
                                          body.position);
    }

    public ParseResult<List<MethodDef>> parseMethodDefs(int position) throws ParseErrorException {
        final List<MethodDef> methodDefs = new ArrayList<MethodDef>();
        boolean shouldRun = true;
        while (shouldRun) {
            try {
                final ParseResult<MethodDef> methodDef = parseMethodDef(position);
                methodDefs.add(methodDef.result);
                position = methodDef.position;
            } catch (final ParseErrorException e) {
                shouldRun = false;
            }
        }

        return new ParseResult<List<MethodDef>>(methodDefs, position);
    }
    
    // classdef ::= class classname extends classname {
    //            vardecs_semicolon
    //            constructor(vardecs_comma) {
    //              super(comma_exp);
    //              stmt*
    //            }
    //            methoddef*
    //          }
    public ParseResult<ClassDef> parseClassDef(final int position) throws ParseErrorException {
        // header
        assertTokenHereIs(position, new ClassToken());
        final ParseResult<ClassName> className = parseClassName(position + 1);
        assertTokenHereIs(className.position, new ExtendsToken());
        final ParseResult<ClassName> extendsClassName = parseClassName(className.position + 1);
        assertTokenHereIs(extendsClassName.position, new LeftCurlyToken());

        // instance variables
        final ParseResult<List<Vardec>> instanceVariables =
            parseVardecsSemicolon(extendsClassName.position + 1);

        // constructor header
        assertTokenHereIs(instanceVariables.position, new ConstructorToken());
        assertTokenHereIs(instanceVariables.position + 1, new LeftParenToken());
        final ParseResult<List<Vardec>> constructorArguments =
            parseVardecsComma(instanceVariables.position + 2);
        assertTokenHereIs(constructorArguments.position, new RightParenToken());
        assertTokenHereIs(constructorArguments.position + 1, new LeftCurlyToken());
        
        // constructor body
        assertTokenHereIs(constructorArguments.position + 2, new SuperToken());
        assertTokenHereIs(constructorArguments.position + 3, new LeftParenToken());
        final ParseResult<List<Exp>> superParams =
            parseCommaExp(constructorArguments.position + 4);
        assertTokenHereIs(superParams.position, new RightParenToken());
        assertTokenHereIs(superParams.position + 1, new SemicolonToken());
        final ParseResult<List<Stmt>> constructorBody =
            parseStmts(superParams.position + 2);
        assertTokenHereIs(constructorBody.position, new RightCurlyToken());

        // methods
        final ParseResult<List<MethodDef>> methodDefs =
            parseMethodDefs(constructorBody.position + 1);
        assertTokenHereIs(methodDefs.position, new RightCurlyToken());

        return new ParseResult<ClassDef>(new ClassDef(className.result,
                                                      extendsClassName.result,
                                                      instanceVariables.result,
                                                      constructorArguments.result,
                                                      superParams.result,
                                                      constructorBody.result,
                                                      methodDefs.result),
                                         methodDefs.position + 1);
    }

    public ParseResult<List<ClassDef>> parseClassDefs(int position) throws ParseErrorException {
        final List<ClassDef> classDefs = new ArrayList<ClassDef>();
        boolean shouldRun = true;

        while (shouldRun) {
            try {
                final ParseResult<ClassDef> classDef = parseClassDef(position);
                classDefs.add(classDef.result);
                position = classDef.position;
            } catch (final ParseErrorException e) {
                shouldRun = false;
            }
        }

        return new ParseResult<List<ClassDef>>(classDefs, position);
    }

    // program ::= classdef* stmt // stmt is the entry point
    public ParseResult<Program> parseProgram(final int position) throws ParseErrorException {
        final ParseResult<List<ClassDef>> classDefs = parseClassDefs(position);
        final ParseResult<Stmt> entryPoint = parseStmt(classDefs.position);
        return new ParseResult<Program>(new Program(classDefs.result, entryPoint.result),
                                        entryPoint.position);
    }

    public Program parseProgram() throws ParseErrorException {
        final ParseResult<Program> program = parseProgram(0);
        if (program.position == tokens.length) {
            return program.result;
        } else {
            throw new ParseErrorException("remaining tokens at end");
        }
    }

    public static Program parse(final Token[] tokens) throws ParseErrorException {
        return new Parser(tokens).parseProgram();
    }
}
