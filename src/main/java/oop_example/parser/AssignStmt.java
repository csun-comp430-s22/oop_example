package oop_example.parser;

public class AssignStmt implements Stmt {
    public final Variable variable;
    public final Exp exp;

    public AssignStmt(final Variable variable,
                      final Exp exp) {
        this.variable = variable;
        this.exp = exp;
    }

    public int hashCode() {
        return variable.hashCode() + exp.hashCode();
    }

    public boolean equals(final Object other) {
        if (other instanceof AssignStmt) {
            final AssignStmt asAssign = (AssignStmt)other;
            return (variable.equals(asAssign.variable) &&
                    exp.equals(asAssign.exp));
        } else {
            return false;
        }
    }

    public String toString() {
        return ("AssignStmt(" + variable.toString() + ", " +
                exp.toString() + ")");
    }
}

        
