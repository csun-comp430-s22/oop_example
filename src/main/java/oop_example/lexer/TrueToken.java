package oop_example.lexer;

public class TrueToken implements Token {
    public int hashCode() { return 0; }
    public boolean equals(final Object other) {
        return other instanceof TrueToken;
    }
    public String toString() {
        return "TrueToken";
    }
}
