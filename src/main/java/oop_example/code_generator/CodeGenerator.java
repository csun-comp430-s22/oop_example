package oop_example.code_generator;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.Iterator;

import java.io.PrintStream;

import oop_example.typechecker.Typechecker;
import oop_example.typechecker.TypeErrorException;
import oop_example.parser.*;

public class CodeGenerator {
    public static final String SELF_NAME = "self";
    public static final String MAKE_OBJECT_HELPER =
        "function makeObject(vtable, constructor, ...params) {\n" +
        "let self = {};\n" +
        "self.vtable = vtable;\n" +
        "constructor.apply(self, params);\n" +
        "return self;\n" +
        "}\n";

    public final Program program;
    public final PrintStream output;
    
    public final Map<ClassName, ClassDef> classes;
    public final Map<ClassName, Map<MethodName, MethodDef>> methods;
    public final Map<ClassName, VTable> vtables;

    public CodeGenerator(final Program program,
                         final PrintStream output) throws TypeErrorException {
        this.program = program;
        this.output = output;
        classes = Typechecker.makeClassMap(program.classes);
        methods = Typechecker.makeMethodMap(classes);
        vtables = new HashMap<ClassName, VTable>();
        for (final ClassName className : classes.keySet()) {
            makeVTableForClass(className);
        }
    }

    public static FunctionName nameMangleFunctionName(final ClassName className,
                                                      final MethodName methodName) {
        return new FunctionName(className.name + "_" + methodName.name);
    }

    public static FunctionName nameMangleConstructorName(final ClassName className) {
        return new FunctionName(className.name + "_constructor");
    }
    
    private VTable makeVTableForClass(final ClassName className) throws TypeErrorException {
        VTable vtable = vtables.get(className);
        // save vtables as we create them, and only compute if needed
        if (vtable == null) {
            if (className.name.equals(Typechecker.BASE_CLASS_NAME)) {
                // object's vtable is empty
                vtable = new VTable(className);
            } else {
                // some class with a parent class
                // get a copy of the parent's vtable, and extend off of that
                final ClassDef classDef = Typechecker.getClass(className, classes);
                vtable = makeVTableForClass(classDef.extendsClassName).copy(className);
                for (final MethodDef methodDef : classDef.methods) {
                    vtable.addOrUpdateMethod(methodDef.methodName);
                }
            }
            vtables.put(className, vtable);
        }
        return vtable;
    }

    public VTable getVTable(final ClassName className) {
        final VTable vtable = vtables.get(className);
        assert(vtable != null);
        return vtable;
    }
    
    public void writeIntLiteralExp(final IntLiteralExp exp) {
        output.print(exp.value);
    }

    public void writeVariableExp(final VariableExp exp,
                                 final Set<Variable> localVariables) {
        // local variables work as-is
        // the only non-local variables are instance variables, which
        // must always be accessed through self
        final Variable variable = exp.variable;
        if (localVariables.contains(variable)) {
            output.print(SELF_NAME);
            output.print(".");
        }
        output.print(variable.name);
    }

    public void writeBoolLiteralExp(final BoolLiteralExp exp) {
        output.print(exp.value);
    }

    public void writeOp(final Op op) throws CodeGeneratorException {
        if (op instanceof PlusOp) {
            output.print("+");
        } else if (op instanceof LessThanOp) {
            output.print("<");
        } else if (op instanceof EqualsOp) {
            output.print("==");
        } else {
            throw new CodeGeneratorException("Unhandled op: " + op.toString());
        }
    }
    
    public void writeOpExp(final OpExp exp,
                           final Set<Variable> localVariables) throws CodeGeneratorException {
        output.print("(");
        writeExp(exp.left, localVariables);
        output.print(" ");
        writeOp(exp.op);
        output.print(" ");
        writeExp(exp.right, localVariables);
        output.print(")");
    }

    // comma-separated
    public void writeExps(final List<Exp> exps,
                          final Set<Variable> localVariables) throws CodeGeneratorException {
        final int numExps = exps.size();
        // intentionally using an iterator for access, because it could
        // be a linked list
        final Iterator<Exp> iterator = exps.iterator();
        for (int index = 1; iterator.hasNext() && index < numExps; index++) {
            writeExp(iterator.next(), localVariables);
            output.print(", ");
        }
        if (iterator.hasNext()) {
            writeExp(iterator.next(), localVariables);
        }
    }
    
    public void writeMethodCallExp(final MethodCallExp exp,
                                   final Set<Variable> localVariables) throws CodeGeneratorException {
        assert(exp.targetType != null);
        final VTable vtable = getVTable(exp.targetType.className);
        writeExp(exp.target, localVariables);
        output.print(".vtable[");
        output.print(vtable.indexOfMethod(exp.methodName));
        output.print("](");
        writeExps(exp.params, localVariables);
        output.print(")");
    }

    public void writeNewExp(final NewExp newExp,
                            final Set<Variable> localVariables) throws CodeGeneratorException {
        final VTable vtable = getVTable(newExp.className);
        output.print("makeObject(");
        output.print(vtable.targetVariable().name);
        output.print(", ");
        output.print(nameMangleConstructorName(newExp.className).name);
        if (!newExp.params.isEmpty()) {
            output.print(", ");
            writeExps(newExp.params, localVariables);
        }
        output.print(")");
    }
        
    public void writeExp(final Exp exp,
                         final Set<Variable> localVariables) throws CodeGeneratorException {
        if (exp instanceof IntLiteralExp) {
            writeIntLiteralExp((IntLiteralExp)exp);
        } else if (exp instanceof VariableExp) {
            writeVariableExp((VariableExp)exp, localVariables);
        } else if (exp instanceof BoolLiteralExp) {
            writeBoolLiteralExp((BoolLiteralExp)exp);
        } else if (exp instanceof ThisExp) {
            output.print(SELF_NAME);
        } else if (exp instanceof OpExp) {
            writeOpExp((OpExp)exp, localVariables);
        } else if (exp instanceof MethodCallExp) {
            writeMethodCallExp((MethodCallExp)exp, localVariables);
        } else if (exp instanceof NewExp) {
            writeNewExp((NewExp)exp, localVariables);
        } else {
            throw new CodeGeneratorException("Unhandled expression: " + exp);
        }
    }

    public static Set<Variable> addVariable(final Set<Variable> variables,
                                            final Variable variable) {
        final Set<Variable> retval = new HashSet<Variable>();
        retval.addAll(variables);
        retval.add(variable);
        return retval;
    }

    public Set<Variable> writeExpStmt(final ExpStmt stmt,
                                      final Set<Variable> localVariables) throws CodeGeneratorException {
        writeExp(stmt.exp, localVariables);
        output.println(";");
        return localVariables;
    }

    public Set<Variable> writeVariableInitializationStmt(final VariableInitializationStmt stmt,
                                                         final Set<Variable> localVariables) throws CodeGeneratorException {
        final Variable variable = stmt.vardec.variable;
        output.print("let ");
        output.print(variable.name);
        output.print(" = ");
        writeExp(stmt.exp, localVariables);
        output.println(";");
        return addVariable(localVariables, variable);
    }

    // JavaScript does not allow for two variables to be introduced in the same scope
    // with the same name.  However, this language allows it.  In order to resolve this,
    // each statement is executed in an ever deeper scope.
    public void writeStmtsInNestedScopes(final Iterator<Stmt> stmts,
                                         Set<Variable> localVariables) throws CodeGeneratorException {
        if (stmts.hasNext()) {
            localVariables = writeStmt(stmts.next(), localVariables);
            output.print("{");
            writeStmtsInNestedScopes(stmts, localVariables);
            output.print("}");
        }
    }

    public Set<Variable> writeIfStmt(final IfStmt stmt,
                                     final Set<Variable> localVariables) throws CodeGeneratorException {
        output.print("if (");
        writeExp(stmt.guard, localVariables);
        output.print(") {");
        writeStmt(stmt.ifTrue, localVariables);
        output.println("} else {");
        writeStmt(stmt.ifFalse, localVariables);
        output.println("}");
        return localVariables;
    }

    public Set<Variable> writeWhileStmt(final WhileStmt stmt,
                                        final Set<Variable> localVariables) throws CodeGeneratorException {
        output.print("while (");
        writeExp(stmt.guard, localVariables);
        output.print(") {");
        writeStmt(stmt.body, localVariables);
        output.println("}");
        return localVariables;
    }

    public Set<Variable> writeReturnNonVoidStmt(final ReturnNonVoidStmt stmt,
                                                final Set<Variable> localVariables) throws CodeGeneratorException {
        output.print("return ");
        writeExp(stmt.exp, localVariables);
        output.println(";");
        return localVariables;
    }

    public Set<Variable> writeReturnVoidStmt(final ReturnVoidStmt stmt,
                                             final Set<Variable> localVariables) {
        output.println("return;");
        return localVariables;
    }

    public Set<Variable> writePrintlnStmt(final PrintlnStmt stmt,
                                          final Set<Variable> localVariables) throws CodeGeneratorException {
        output.print("console.log(");
        writeExp(stmt.exp, localVariables);
        output.println(");");
        return localVariables;
    }
    
    public Set<Variable> writeBlockStmt(final BlockStmt stmt,
                                        final Set<Variable> localVariables) throws CodeGeneratorException {
        output.print("{");
        writeStmtsInNestedScopes(stmt.body.iterator(), localVariables);
        output.print("}");
        return localVariables;
    }

    // returns new set of variables in scope
    public Set<Variable> writeStmt(final Stmt stmt,
                                   final Set<Variable> localVariables) throws CodeGeneratorException {
        if (stmt instanceof ExpStmt) {
            return writeExpStmt((ExpStmt)stmt, localVariables);
        } else if (stmt instanceof VariableInitializationStmt) {
            return writeVariableInitializationStmt((VariableInitializationStmt)stmt, localVariables);
        } else if (stmt instanceof IfStmt) {
            return writeIfStmt((IfStmt)stmt, localVariables);
        } else if (stmt instanceof WhileStmt) {
            return writeWhileStmt((WhileStmt)stmt, localVariables);
        } else if (stmt instanceof ReturnNonVoidStmt) {
            return writeReturnNonVoidStmt((ReturnNonVoidStmt)stmt, localVariables);
        } else if (stmt instanceof ReturnVoidStmt) {
            return writeReturnVoidStmt((ReturnVoidStmt)stmt, localVariables);
        } else if (stmt instanceof PrintlnStmt) {
            return writePrintlnStmt((PrintlnStmt)stmt, localVariables);
        } else if (stmt instanceof BlockStmt) {
            return writeBlockStmt((BlockStmt)stmt, localVariables);
        } else {
            throw new CodeGeneratorException("Unhandled statement: " + stmt.toString());
        }
    }

    // writes a comma-separated list
    public void writeFormalParams(final List<Vardec> vardecs) {
        final int numParams = vardecs.size();
        final Iterator<Vardec> iterator = vardecs.iterator();
        for (int index = 1; iterator.hasNext() && index < numParams; index++) {
            output.print(iterator.next().variable.name);
            output.print(", ");
        }
        if (iterator.hasNext()) {
            output.print(iterator.next().variable.name);
        }
    }

    public static Set<Variable> initialLocalVariables(final List<Vardec> vardecs) {
        final Set<Variable> retval = new HashSet<Variable>();
        for (final Vardec vardec : vardecs) {
            retval.add(vardec.variable);
        }
        return retval;
    }
    
    public void writeMethod(final ClassName forClass,
                            final MethodDef methodDef) throws CodeGeneratorException {
        output.print("function ");
        output.print(nameMangleFunctionName(forClass, methodDef.methodName).name);
        output.print("(");
        writeFormalParams(methodDef.arguments);
        output.println(") {");
        writeStmt(methodDef.body,
                  initialLocalVariables(methodDef.arguments));
        output.println("}");
    }

    public void writeConstructor(final ClassDef classDef) throws CodeGeneratorException {
        // header
        output.print("function ");
        output.print(nameMangleConstructorName(classDef.className));
        output.print("(");
        output.print(SELF_NAME);
        if (!classDef.constructorArguments.isEmpty()) {
            output.print(", ");
            writeFormalParams(classDef.constructorArguments);
        }
        output.println(") {");

        // call to super
        final Set<Variable> localVariables =
            initialLocalVariables(classDef.constructorArguments);
        output.print(nameMangleConstructorName(classDef.extendsClassName));
        output.print("(");
        output.print(SELF_NAME);
        if (!classDef.superParams.isEmpty()) {
            output.print(", ");
            writeExps(classDef.superParams,
                      localVariables);
        }
        output.println(");");

        // body
        writeStmtsInNestedScopes(classDef.constructorBody.iterator(),
                                 localVariables);
    }
    
    public void writeClass(final ClassDef classDef) throws CodeGeneratorException {
        writeConstructor(classDef);
        for (final MethodDef methodDef : classDef.methods) {
            writeMethod(classDef.className, methodDef);
        }
    }

    public void writeMakeObject() {
        output.println(MAKE_OBJECT_HELPER);
    }

    public void generateCode() throws CodeGeneratorException {
        // makeObject helper
        writeMakeObject();
        
        // write out vtables
        for (final VTable vtable : vtables.values()) {
            vtable.writeTable(output);
        }

        // write out everything for each class
        for (final ClassDef classDef : program.classes) {
            writeClass(classDef);
        }

        // write out entry point
        writeStmt(program.entryPoint, new HashSet<Variable>());
    }
}
