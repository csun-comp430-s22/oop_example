package oop_example.lexer;

public class PlusToken implements Token {
    public int hashCode() { return 3; }
    public boolean equals(final Object other) {
        return other instanceof PlusToken;
    }
    public String toString() {
        return "PlusToken";
    }
}
