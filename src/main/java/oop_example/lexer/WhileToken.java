package oop_example.lexer;

public class WhileToken implements Token {
    public int hashCode() { return 17; }
    public boolean equals(final Object other) {
        return other instanceof WhileToken;
    }
    public String toString() {
        return "WhileToken";
    }
}
