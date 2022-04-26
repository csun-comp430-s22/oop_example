package oop_example.parser;

import oop_example.lexer.*;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class ParserTest {
    public static Parser mkParser(final Token... tokens) {
        return new Parser(tokens);
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
}
