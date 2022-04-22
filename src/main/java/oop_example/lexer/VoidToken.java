package oop_example.lexer;

public class VoidToken implements Token {
    public int hashCode() { return 12; }
    public boolean equals(final Object other) {
        return other instanceof VoidToken;
    }
    public String toString() {
        return "VoidToken";
    }
}
