package oop_example.lexer;

public class ClassToken implements Token {
    public int hashCode() { return 22; }
    public boolean equals(final Object other) {
        return other instanceof ClassToken;
    }
    public String toString() {
        return "ClassToken";
    }
}
