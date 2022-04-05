package oop_example.parser;

import java.util.List;

public class MethodDef {
    public final Type returnType;
    public final MethodName methodName;
    public final List<Vardec> arguments;
    public final Stmt body;

    public MethodDef(final Type returnType,
                     final MethodName methodName,
                     final List<Vardec> arguments,
                     final Stmt body) {
        this.returnType = returnType;
        this.methodName = methodName;
        this.arguments = arguments;
        this.body = body;
    }

    public int hashCode() {
        return (returnType.hashCode() +
                methodName.hashCode() +
                arguments.hashCode() +
                body.hashCode());
    }

    public boolean equals(final Object other) {
        if (other instanceof MethodDef) {
            final MethodDef otherMethod = (MethodDef)other;
            return (returnType.equals(otherMethod.returnType) &&
                    methodName.equals(otherMethod.methodName) &&
                    arguments.equals(otherMethod.arguments) &&
                    body.equals(otherMethod.body));
        } else {
            return false;
        }
    }

    public String toString() {
        return ("MethodDef(" + returnType.toString() + ", " +
                methodName.toString() + ", " +
                arguments.toString() + ", " +
                body.toString() + ")");
    }
}
