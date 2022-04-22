package oop_example.lexer;

public class ThisToken implements Token {
    public int hashCode() { return 2; }
    public boolean equals(final Object other) {
        return other instanceof ThisToken;
    }
    public String toString() {
        return "ThisToken";
    }
}
