package oop_example.parser;

public class ReturnVoidStmt implements Stmt {
    public int hashCode() { return 0; }
    public boolean equals(final Object other) {
        return other instanceof ReturnVoidStmt;
    }
    public String toString() {
        return "ReturnVoidStmt";
    }
}
