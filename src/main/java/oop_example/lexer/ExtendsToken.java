package oop_example.lexer;

public class ExtendsToken implements Token {
    public int hashCode() { return 23; }
    public boolean equals(final Object other) {
        return other instanceof ExtendsToken;
    }
    public String toString() {
        return "ExtendsToken";
    }
}
