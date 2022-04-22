package oop_example.lexer;

public class BoolToken implements Token {
    public int hashCode() { return 11; }
    public boolean equals(final Object other) {
        return other instanceof BoolToken;
    }
    public String toString() {
        return "BoolToken";
    }
}
