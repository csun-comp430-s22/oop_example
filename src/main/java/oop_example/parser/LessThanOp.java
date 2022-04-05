package oop_example.parser;

public class LessThanOp implements Op {
    public int hashCode() { return 0; }
    public boolean equals(final Object other) {
        return other instanceof LessThanOp;
    }
    public String toString() {
        return "LessThanOp";
    }
}
