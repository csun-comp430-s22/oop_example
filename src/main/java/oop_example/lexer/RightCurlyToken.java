package oop_example.lexer;

public class RightCurlyToken implements Token {
    public int hashCode() { return 21; }
    public boolean equals(final Object other) {
        return other instanceof RightCurlyToken;
    }
    public String toString() {
        return "RightCurlyToken";
    }
}
