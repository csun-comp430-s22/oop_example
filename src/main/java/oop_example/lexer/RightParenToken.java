package oop_example.lexer;

public class RightParenToken implements Token {
    public int hashCode() { return 8; }
    public boolean equals(final Object other) {
        return other instanceof RightParenToken;
    }
    public String toString() {
        return "RightParenToken";
    }
}
