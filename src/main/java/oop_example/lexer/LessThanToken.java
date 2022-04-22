package oop_example.lexer;

public class LessThanToken implements Token {
    public int hashCode() { return 4; }
    public boolean equals(final Object other) {
        return other instanceof LessThanToken;
    }
    public String toString() {
        return "LessThanToken";
    }
}
