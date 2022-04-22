package oop_example.lexer;

public class IntToken implements Token {
    public int hashCode() { return 10; }
    public boolean equals(final Object other) {
        return other instanceof IntToken;
    }
    public String toString() {
        return "IntToken";
    }
}
