package oop_example.lexer;

public class ElseToken implements Token {
    public int hashCode() { return 16; }
    public boolean equals(final Object other) {
        return other instanceof ElseToken;
    }
    public String toString() {
        return "ElseToken";
    }
}
