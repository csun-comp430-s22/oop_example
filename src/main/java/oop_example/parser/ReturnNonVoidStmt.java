package oop_example.parser;

public class ReturnNonVoidStmt implements Stmt {
    public final Exp exp;

    public ReturnNonVoidStmt(final Exp exp) {
        this.exp = exp;
    }

    public int hashCode() { return exp.hashCode(); }

    public boolean equals(final Object other) {
        return (other instanceof ReturnNonVoidStmt &&
                exp.equals(((ReturnNonVoidStmt)other).exp));
    }

    public String toString() {
        return "ReturnNonVoidStmt(" + exp.toString() + ")";
    }
}
