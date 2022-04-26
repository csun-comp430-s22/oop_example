package oop_example.lexer;

import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.fail;
import org.junit.Test;

public class TokenizerTest {
    // if expected is null, it means we expect an exception
    public static void assertTokenizes(final String input,
                                       final Token[] expected)
        throws TokenizerException {
        if (expected == null) {
            Token[] tokens = null;
            try {
                tokens = Tokenizer.tokenize(input);
            } catch (final TokenizerException e) {
            }

            if (tokens != null) {
                fail("Input should not have tokenized, but instead tokenized as: " +
                     tokens.toString());
            }
        } else {
            final Token[] tokens = Tokenizer.tokenize(input);
            assertArrayEquals(expected, tokens);
        }
    }

    @Test
    public void testReservedWord() throws TokenizerException {
        assertTokenizes("int", new Token[]{ new IntToken() });
    }

    @Test
    public void testReservedWordWithWhitespace() throws TokenizerException {
        assertTokenizes(" int ", new Token[]{ new IntToken() });
    }

    @Test
    public void testSymbol() throws TokenizerException {
        assertTokenizes("+", new Token[]{ new PlusToken() });
    }
    
    @Test
    public void testSymbolWithWhitespace() throws TokenizerException {
        assertTokenizes(" + ", new Token[]{ new PlusToken() });
    }

    @Test
    public void testEqualVsAssign() throws TokenizerException {
        assertTokenizes("= == = == ==",
                        new Token[]{
                            new AssignToken(),
                            new EqualsToken(),
                            new AssignToken(),
                            new EqualsToken(),
                            new EqualsToken()
                        });
    }

    @Test
    public void testIdentifier() throws TokenizerException {
        assertTokenizes("foo", new Token[] { new IdentifierToken("foo") });
    }

    @Test
    public void testIdentifierWithWhitespace() throws TokenizerException {
        assertTokenizes(" foo ", new Token[] { new IdentifierToken("foo") });
    }

    @Test
    public void testSmallClass() throws TokenizerException {
        final String test =
            "class MyClass extends Object {\n" +
            "  int x;\n" +
            "  constructor(int x) {\n" +
            "    super();\n" +
            "    this.x = x;\n" +
            "  }\n" +
            "}\n";
        

        final Token[] expected = new Token[]{
            new ClassToken(),
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
            new IdentifierToken("x"),
            new RightParenToken(),
            new LeftCurlyToken(),
            new SuperToken(),
            new LeftParenToken(),
            new RightParenToken(),
            new SemicolonToken(),
            new ThisToken(),
            new DotToken(),
            new IdentifierToken("x"),
            new AssignToken(),
            new IdentifierToken("x"),
            new SemicolonToken(),
            new RightCurlyToken(),
            new RightCurlyToken()
        };
        
        assertTokenizes(test, expected);
    }
}
