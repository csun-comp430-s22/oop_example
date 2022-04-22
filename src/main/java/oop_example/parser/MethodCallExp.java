package oop_example.parser;

import java.util.List;

public class MethodCallExp implements Exp {
    public final Exp target;
    public ClassNameType targetType; // filled in by the typechecker
    public final MethodName methodName;
    public final List<Exp> params;

    public MethodCallExp(final Exp target,
                         final MethodName methodName,
                         final List<Exp> params) {
        this.target = target;
        targetType = null;
        this.methodName = methodName;
        this.params = params;
    }

    public int hashCode() {
        return (target.hashCode() +
                ((targetType == null) ? 0 : targetType.hashCode()) +
                methodName.hashCode() +
                params.hashCode());
    }

    public boolean equals(final Object other) {
        if (other instanceof MethodCallExp) {
            final MethodCallExp call = (MethodCallExp)other;
            return (target.equals(call.target) &&
                    ((targetType == null && call.targetType == null) ||
                     (targetType != null && call.targetType != null &&
                      targetType.equals(call.targetType))) &&
                    methodName.equals(call.methodName) &&
                    params.equals(call.params));
        } else {
            return false;
        }
    }

    public String toString() {
        return ("MethodCallExp(" + target.toString() + ", " +
                targetType + ", " +
                methodName.toString() + ", " +
                params.toString() + ")");
    }
}
