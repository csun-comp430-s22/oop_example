package oop_example.lexer;

public class LeftParenToken implements Token {
    public int hashCode() { return 7; }
    public boolean equals(final Object other) {
        return other instanceof LeftParenToken;
    }
    public String toString() {
        return "LeftParenToken";
    }
}
