package oop_example.lexer;

public class ReturnToken implements Token {
    public int hashCode() { return 18; }
    public boolean equals(final Object other) {
        return other instanceof ReturnToken;
    }
    public String toString() {
        return "ReturnToken";
    }
}
