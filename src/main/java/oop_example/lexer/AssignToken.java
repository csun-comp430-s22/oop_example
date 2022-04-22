package oop_example.lexer;

public class AssignToken implements Token {
    public int hashCode() { return 14; }
    public boolean equals(final Object other) {
        return other instanceof AssignToken;
    }
    public String toString() {
        return "AssignToken";
    }
}
