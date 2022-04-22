package oop_example.lexer;

public class SemicolonToken implements Token {
    public int hashCode() { return 13; }
    public boolean equals(final Object other) {
        return other instanceof SemicolonToken;
    }
    public String toString() {
        return "SemicolonToken";
    }
}
