package oop_example.lexer;

public class EqualsToken implements Token {
    public int hashCode() { return 5; }
    public boolean equals(final Object other) {
        return other instanceof EqualsToken;
    }
    public String toString() {
        return "EqualsToken";
    }
}
