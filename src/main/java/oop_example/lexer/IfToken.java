package oop_example.lexer;

public class IfToken implements Token {
    public int hashCode() { return 15; }
    public boolean equals(final Object other) {
        return other instanceof IfToken;
    }
    public String toString() {
        return "IfToken";
    }
}
