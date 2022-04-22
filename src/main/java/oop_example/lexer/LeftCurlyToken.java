package oop_example.lexer;

public class LeftCurlyToken implements Token {
    public int hashCode() { return 20; }
    public boolean equals(final Object other) {
        return other instanceof LeftCurlyToken;
    }
    public String toString() {
        return "LeftCurlyToken";
    }
}
