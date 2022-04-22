package oop_example.lexer;

public class NewToken implements Token {
    public int hashCode() { return 9; }
    public boolean equals(final Object other) {
        return other instanceof NewToken;
    }
    public String toString() {
        return "NewToken";
    }
}
