package oop_example.parser;

public class IfStmt implements Stmt {
    public final Exp guard;
    public final Stmt ifTrue;
    public final Stmt ifFalse;

    public IfStmt(final Exp guard,
                  final Stmt ifTrue,
                  final Stmt ifFalse) {
        this.guard = guard;
        this.ifTrue = ifTrue;
        this.ifFalse = ifFalse;
    }

    public int hashCode() {
        return (guard.hashCode() +
                ifTrue.hashCode() +
                ifFalse.hashCode());
    }

    public boolean equals(final Object other) {
        if (other instanceof IfStmt) {
            final IfStmt asIf = (IfStmt)other;
            return (guard.equals(asIf.guard) &&
                    ifTrue.equals(asIf.ifTrue) &&
                    ifFalse.equals(asIf.ifFalse));
        } else {
            return false;
        }
    }

    public String toString() {
        return ("IfStmt(" + guard.toString() + ", " +
                ifTrue.toString() + ", " +
                ifFalse.toString() + ")");
    }
}
