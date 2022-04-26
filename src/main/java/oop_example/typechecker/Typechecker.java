package oop_example.typechecker;

import oop_example.parser.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

// typechecks: well-typed: no type errors
// doesn't typecheck: ill-typed: some number of type errors (>0)

public class Typechecker {
    public static final String BASE_CLASS_NAME = "Object";
    
    // Things to track:
    // 1.) Variables in scope, and their types
    // 2.) Classes available, parameters constructors take, methods they have, what their parent
    //     class is.
    //
    // Sorts of queries we want to make to class information:
    // 1. Is this a valid class?
    // 2. For this class, what are the argument types for the constructor?
    // 3. Does this class support a given method?  If so, what are the parameter
    //    types for the method?
    //    - Need to take inheritance into account
    // 4. Is this given class a subclass of another class?
    // 5. Does our class hierarchy form a tree?

    public final Map<ClassName, ClassDef> classes;

    // includes inherited methods
    public final Map<ClassName, Map<MethodName, MethodDef>> methods;

    public final Program program;

    // throws an exception if the class doesn't exist
    // returns null if it's Object
    public static ClassDef getClass(final ClassName className,
                                    final Map<ClassName, ClassDef> classes) throws TypeErrorException {
        if (className.name.equals(BASE_CLASS_NAME)) {
            return null;
        } else {
            final ClassDef classDef = classes.get(className);
            if (classDef == null) {
                throw new TypeErrorException("no such class: " + className);
            } else {
                return classDef;
            }
        }
    }

    public ClassDef getClass(final ClassName className) throws TypeErrorException {
        return getClass(className, classes);
    }
    
    // gets the parent class definition for the class with the given name
    // Throws an exception if the class doesn't exist, or if its parent
    // doesn't exist.  Returns null if the parent is Object.
    public static ClassDef getParent(final ClassName className,
                                     final Map<ClassName, ClassDef> classes) throws TypeErrorException {
        final ClassDef classDef = getClass(className, classes);
        return getClass(classDef.extendsClassName, classes);
    }

    public ClassDef getParent(final ClassName className) throws TypeErrorException {
        return getParent(className, classes);
    }
    
    public static void assertInheritanceNonCyclicalForClass(final ClassDef classDef,
                                                            final Map<ClassName, ClassDef> classes) throws TypeErrorException {
        final Set<ClassName> seenClasses = new HashSet<ClassName>();
        seenClasses.add(classDef.className);
        ClassDef parentClassDef = getParent(classDef.className, classes);
        while (parentClassDef != null) {
            final ClassName parentClassName = parentClassDef.className;
            if (seenClasses.contains(parentClassName)) {
                throw new TypeErrorException("cyclic inheritance involving: " + parentClassName);
            }
            seenClasses.add(parentClassName);
            parentClassDef = getParent(parentClassName, classes);
        }
    }
        
    public static void assertInheritanceNonCyclical(final Map<ClassName, ClassDef> classes) throws TypeErrorException {
        for (final ClassDef classDef : classes.values()) {
            assertInheritanceNonCyclicalForClass(classDef, classes);
        }
    }

    // includes inherited methods
    // duplicates are not permitted within the same class, but it's ok to override a superclass' method
    public static Map<MethodName, MethodDef> methodsForClass(final ClassName className,
                                                             final Map<ClassName, ClassDef> classes) throws TypeErrorException {
        final ClassDef classDef = getClass(className, classes);
        if (classDef == null) {
            return new HashMap<MethodName, MethodDef>();
        } else {
            final Map<MethodName, MethodDef> retval = methodsForClass(classDef.extendsClassName, classes);
            final Set<MethodName> methodsOnThisClass = new HashSet<MethodName>();
            for (final MethodDef methodDef : classDef.methods) {
                final MethodName methodName = methodDef.methodName;
                if (methodsOnThisClass.contains(methodName)) {
                    throw new TypeErrorException("duplicate method: " + methodName);
                }
                methodsOnThisClass.add(methodName);
                retval.put(methodName, methodDef);
            }
            return retval;
        }
    }

    public static Map<ClassName, Map<MethodName, MethodDef>> makeMethodMap(final Map<ClassName, ClassDef> classes) throws TypeErrorException {
        final Map<ClassName, Map<MethodName, MethodDef>> retval = new HashMap<ClassName, Map<MethodName, MethodDef>>();
        for (final ClassName className : classes.keySet()) {
            retval.put(className, methodsForClass(className, classes));
        }
        return retval;
    }
    
    // also makes sure inheritance hierarchies aren't cyclical
    public static Map<ClassName, ClassDef> makeClassMap(final List<ClassDef> classes) throws TypeErrorException {
        final Map<ClassName, ClassDef> retval = new HashMap<ClassName, ClassDef>();
        for (final ClassDef classDef : classes) {
            final ClassName className = classDef.className;
            if (retval.containsKey(classDef.className)) {
                throw new TypeErrorException("Duplicate class name: " + className);
            }
        }

        assertInheritanceNonCyclical(retval);

        return retval;
    }
    
    // recommended: ClassName -> All Methods on the Class
    // recommended: ClassName -> ParentClass
    public Typechecker(final Program program) throws TypeErrorException {
        this.program = program;
        classes = makeClassMap(program.classes);
        methods = makeMethodMap(classes);
    }

    public Type typeofVariable(final VariableExp exp,
                               final Map<Variable, Type> typeEnvironment) throws TypeErrorException {
        final Type mapType = typeEnvironment.get(exp.variable);
        if (mapType == null) {
            throw new TypeErrorException("Used variable not in scope: " + exp.variable.name);
        } else {
            return mapType;
        }
    }

    public Type typeofThis(final ClassName classWeAreIn) throws TypeErrorException {
        if (classWeAreIn == null) {
            throw new TypeErrorException("this used in the entry point");
        } else {
            return new ClassNameType(classWeAreIn);
        }
    }

    public Type typeofOp(final OpExp exp,
                         final Map<Variable, Type> typeEnvironment,
                         final ClassName classWeAreIn) throws TypeErrorException {
        final Type leftType = typeof(exp.left, typeEnvironment, classWeAreIn);
        final Type rightType = typeof(exp.right, typeEnvironment, classWeAreIn);
        // (leftType, exp.op, rightType) match {
        //   case (IntType, PlusOp, IntType) => IntType
        //   case (IntType, LessThanOp | EqualsOp, IntType) => Booltype
        //   case _ => throw new TypeErrorException("Operator mismatch")
        // }
        if (exp.op instanceof PlusOp) {
            if (leftType instanceof IntType && rightType instanceof IntType) {
                return new IntType();
            } else {
                throw new TypeErrorException("Operand type mismatch for +");
            }
        } else if (exp.op instanceof LessThanOp) {
            if (leftType instanceof IntType && rightType instanceof IntType) {
                return new BoolType();
            } else {
                throw new TypeErrorException("Operand type mismatch for <");
            }
        } else if (exp.op instanceof EqualsOp) {
            if (leftType instanceof IntType && rightType instanceof IntType) {
                return new BoolType();
            } else {
                throw new TypeErrorException("Operand type mismatch for ==");
            }
        } else {
            throw new TypeErrorException("Unsupported operation: " + exp.op);
        }
    }

    public MethodDef getMethodDef(final ClassName className,
                                  final MethodName methodName) throws TypeErrorException {
        final Map<MethodName, MethodDef> methodMap = methods.get(className);
        if (methodMap == null) {
            throw new TypeErrorException("Unknown class name: " + className);
        } else {
            final MethodDef methodDef = methodMap.get(methodName);
            if (methodDef == null) {
                throw new TypeErrorException("Unknown method name " + methodName + " for class " + className);
            } else {
                return methodDef;
            }
        }
    }
    
    public Type expectedReturnTypeForClassAndMethod(final ClassName className,
                                                    final MethodName methodName) throws TypeErrorException {
        return getMethodDef(className, methodName).returnType;
    }

    // Doesn't handle access modifiers right now; would be to know which class we
    // are calling from.
    //
    // class Base extends Object {
    //   public void basePublic() {}
    //   protected void baseProtected() {}
    //   private void basePrivate() {}
    // }
    // class Sub extends Base {
    //   public void foobar() {
    //     this.basePublic();  // should be ok
    //     this.baseProtected(); // should be ok
    //     this.basePrivate(); // should give an error
    //   }
    // }
    // class SomeOtherClass extends Object {
    //   public void test() {
    //     Sub sub = new Sub();
    //     sub.basePublic(); // should be ok
    //     sub.baseProtected(); // should give an error
    //     sub.basePrivate(); // should give an error
    //   }
    // }
    //
    // for every class:
    //   - Methods on that class
    //   - Methods on the parent of that class
    public List<Type> expectedParameterTypesForClassAndMethod(final ClassName className,
                                                              final MethodName methodName)
        throws TypeErrorException {
        final MethodDef methodDef = getMethodDef(className, methodName);
        final List<Type> retval = new ArrayList<Type>();
        for (final Vardec vardec : methodDef.arguments) {
            retval.add(vardec.type);
        }
        return retval;
    }

    public void assertEqualOrSubtypeOf(final Type first, final Type second) throws TypeErrorException {
        if (first.equals(second)) {
            return;
        } else if (first instanceof ClassNameType &&
                   second instanceof ClassNameType) {
            final ClassDef parentClassDef = getParent(((ClassNameType)first).className);
            assertEqualOrSubtypeOf(new ClassNameType(parentClassDef.className), second);
        } else {
            throw new TypeErrorException("incompatible types: " + first + ", " + second);
        }
    }
    
    // List<Type> - expected types
    // List<Exp> - received expressions
    public void expressionsOk(final List<Type> expectedTypes,
                              final List<Exp> receivedExpressions,
                              final Map<Variable, Type> typeEnvironment,
                              final ClassName classWeAreIn) throws TypeErrorException {
        if (expectedTypes.size() != receivedExpressions.size()) {
            throw new TypeErrorException("Wrong number of parameters");
        }
        for (int index = 0; index < expectedTypes.size(); index++) {
            final Type paramType = typeof(receivedExpressions.get(index), typeEnvironment, classWeAreIn);
            final Type expectedType = expectedTypes.get(index);
            // myMethod(int, bool, int)
            // myMethod(  2, true,   3)
            //
            // myMethod2(BaseClass)
            // myMethod2(new SubClass())
            assertEqualOrSubtypeOf(paramType, expectedType);
        }
    }
    
    // 1.) target should be a class.
    // 2.) target needs to have the methodname method
    // 3.) need to know the expected parameter types for the method
    //
    // exp.methodname(exp*)
    // target.methodName(params)
    public Type typeofMethodCall(final MethodCallExp exp,
                                 final Map<Variable, Type> typeEnvironment,
                                 final ClassName classWeAreIn) throws TypeErrorException {
        final Type targetType = typeof(exp.target, typeEnvironment, classWeAreIn);
        if (targetType instanceof ClassNameType) {
            final ClassNameType asClassNameType = (ClassNameType)targetType;
            exp.targetType = asClassNameType;
            final ClassName className = asClassNameType.className;
            final List<Type> expectedTypes =
                expectedParameterTypesForClassAndMethod(className, exp.methodName);
            expressionsOk(expectedTypes, exp.params, typeEnvironment, classWeAreIn);
            return expectedReturnTypeForClassAndMethod(className, exp.methodName);
        } else {
            throw new TypeErrorException("Called method on non-class type: " + targetType);
        }
    }

    public List<Type> expectedConstructorTypesForClass(final ClassName className)
        throws TypeErrorException {
        final ClassDef classDef = getClass(className);
        final List<Type> retval = new ArrayList<Type>();
        if (classDef == null) { // Object
            return retval;
        } else {
            for (final Vardec vardec : classDef.constructorArguments) {
                retval.add(vardec.type);
            }
            return retval;
        }
    }
    
    // new classname(exp*)
    // new className(params)
    public Type typeofNew(final NewExp exp,
                          final Map<Variable, Type> typeEnvironment,
                          final ClassName classWeAreIn) throws TypeErrorException {
        // need to know what the constructor arguments for this class are
        final List<Type> expectedTypes = expectedConstructorTypesForClass(exp.className);
        expressionsOk(expectedTypes, exp.params, typeEnvironment, classWeAreIn);
        return new ClassNameType(exp.className);
    }
    
    // classWeAreIn is null if we are in the entry point
    public Type typeof(final Exp exp,
                       final Map<Variable, Type> typeEnvironment,
                       final ClassName classWeAreIn) throws TypeErrorException {
        if (exp instanceof IntLiteralExp) {
            return new IntType();
        } else if (exp instanceof VariableExp) {
            return typeofVariable((VariableExp)exp, typeEnvironment);
        } else if (exp instanceof BoolLiteralExp) {
            return new BoolType();
        } else if (exp instanceof ThisExp) {
            return typeofThis(classWeAreIn);
        } else if (exp instanceof OpExp) {
            return typeofOp((OpExp)exp, typeEnvironment, classWeAreIn);
        } else if (exp instanceof MethodCallExp) {
            return typeofMethodCall((MethodCallExp)exp, typeEnvironment, classWeAreIn);
        } else if (exp instanceof NewExp) {
            return typeofNew((NewExp)exp, typeEnvironment, classWeAreIn);
        } else {
            throw new TypeErrorException("Unrecognized expression: " + exp);
        }
    }

    public static Map<Variable, Type> addToMap(final Map<Variable, Type> map,
                                               final Variable variable,
                                               final Type type) {
        final Map<Variable, Type> result = new HashMap<Variable, Type>();
        result.putAll(map);
        result.put(variable, type);
        return result;
    }

    public Map<Variable, Type> isWellTypedVar(final VariableInitializationStmt stmt,
                                              final Map<Variable, Type> typeEnvironment,
                                              final ClassName classWeAreIn) throws TypeErrorException {
        final Type expType = typeof(stmt.exp, typeEnvironment, classWeAreIn);
        assertEqualOrSubtypeOf(expType, stmt.vardec.type);
        return addToMap(typeEnvironment, stmt.vardec.variable, stmt.vardec.type);
    }

    public Map<Variable, Type> isWellTypedIf(final IfStmt stmt,
                                             final Map<Variable, Type> typeEnvironment,
                                             final ClassName classWeAreIn,
                                             final Type functionReturnType) throws TypeErrorException {
        if (typeof(stmt.guard, typeEnvironment, classWeAreIn) instanceof BoolType) {
            isWellTypedStmt(stmt.ifTrue, typeEnvironment, classWeAreIn, functionReturnType);
            isWellTypedStmt(stmt.ifFalse, typeEnvironment, classWeAreIn, functionReturnType);
            return typeEnvironment;
        } else {
            throw new TypeErrorException("guard of if is not a boolean: " + stmt);
        }
    }

    public Map<Variable, Type> isWellTypedWhile(final WhileStmt stmt,
                                                final Map<Variable, Type> typeEnvironment,
                                                final ClassName classWeAreIn,
                                                final Type functionReturnType) throws TypeErrorException {
        if (typeof(stmt.guard, typeEnvironment, classWeAreIn) instanceof BoolType) {
            isWellTypedStmt(stmt.body, typeEnvironment, classWeAreIn, functionReturnType);
            return typeEnvironment;
        } else {
            throw new TypeErrorException("guard on while is not a boolean: " + stmt);
        }
    }

    public Map<Variable, Type> isWellTypedBlock(final BlockStmt stmt,
                                                Map<Variable, Type> typeEnvironment,
                                                final ClassName classWeAreIn,
                                                final Type functionReturnType) throws TypeErrorException {
        for (final Stmt bodyStmt : stmt.body) {
            typeEnvironment = isWellTypedStmt(bodyStmt, typeEnvironment, classWeAreIn, functionReturnType);
        }
        return typeEnvironment;
    }
    
    // return exp;
    public Map<Variable, Type> isWellTypedReturnNonVoid(final ReturnNonVoidStmt stmt,
                                                        final Map<Variable, Type> typeEnvironment,
                                                        final ClassName classWeAreIn,
                                                        final Type functionReturnType) throws TypeErrorException {
        if (functionReturnType == null) {
            throw new TypeErrorException("return in program entry point");
        } else {
            final Type receivedType = typeof(stmt.exp, typeEnvironment, classWeAreIn);
            assertEqualOrSubtypeOf(receivedType, functionReturnType);
            return typeEnvironment;
        }
    }

    public Map<Variable, Type> isWellTypedReturnVoid(final Map<Variable, Type> typeEnvironment,
                                                     final ClassName classWeAreIn,
                                                     final Type functionReturnType) throws TypeErrorException {
        if (functionReturnType == null) {
            throw new TypeErrorException("return in program entry point");
        } else if (!functionReturnType.equals(new VoidType())) {
            throw new TypeErrorException("return of void in non-void context");
        } else {
            return typeEnvironment;
        }
    }
    
    // bool x = true;
    // while (true) {
    //   int x = 17;
    //   break;
    // }
    public Map<Variable, Type> isWellTypedStmt(final Stmt stmt,
                                               final Map<Variable, Type> typeEnvironment,
                                               final ClassName classWeAreIn,
                                               final Type functionReturnType) throws TypeErrorException {
        if (stmt instanceof ExpStmt) {
            typeof(((ExpStmt)stmt).exp, typeEnvironment, classWeAreIn);
            return typeEnvironment;
        } else if (stmt instanceof VariableInitializationStmt) {
            return isWellTypedVar((VariableInitializationStmt)stmt, typeEnvironment, classWeAreIn);
        } else if (stmt instanceof IfStmt) {
            return isWellTypedIf((IfStmt)stmt, typeEnvironment, classWeAreIn, functionReturnType);
        } else if (stmt instanceof WhileStmt) {
            return isWellTypedWhile((WhileStmt)stmt, typeEnvironment, classWeAreIn, functionReturnType);
        } else if (stmt instanceof ReturnNonVoidStmt) {
            return isWellTypedReturnNonVoid((ReturnNonVoidStmt)stmt, typeEnvironment, classWeAreIn, functionReturnType);
        } else if (stmt instanceof ReturnVoidStmt) {
            return isWellTypedReturnVoid(typeEnvironment, classWeAreIn, functionReturnType);
        } else if (stmt instanceof PrintlnStmt) {
            typeof(((PrintlnStmt)stmt).exp, typeEnvironment, classWeAreIn);
            return typeEnvironment;
        } else if (stmt instanceof BlockStmt) {
            return isWellTypedBlock((BlockStmt)stmt, typeEnvironment, classWeAreIn, functionReturnType);
        } else {
            throw new TypeErrorException("Unsupported statement: " + stmt);
        }
    }

    // methoddef ::= type methodname(vardec*) stmt
    public void isWellTypedMethodDef(final MethodDef method,
                                     Map<Variable, Type> typeEnvironment, // instance variables
                                     final ClassName classWeAreIn) throws TypeErrorException {
        // starting type environment: just instance variables
        final Set<Variable> variablesInMethod = new HashSet<Variable>();
        for (final Vardec vardec : method.arguments) {
            final Variable variable = vardec.variable;
            if (variablesInMethod.contains(variable)) {
                throw new TypeErrorException("Duplicate variable in method definition: " + variable);
            }
            variablesInMethod.add(variable);
            // odd semantics: last variable declaration shadows prior one
            typeEnvironment = addToMap(typeEnvironment, variable, vardec.type);
        }
        
        isWellTypedStmt(method.body,
                        typeEnvironment, // instance variables + parameters
                        classWeAreIn,
                        method.returnType);
    }

    // classdef ::= class classname extends classname {
    //            vardec*; // comma-separated instance variables
    //            constructor(vardec*) {
    //              super(exp*);
    //              stmt* // comma-separated
    //            }
    //            methoddef*
    //          }

    // puts all instance variables in scope for the class
    // includes parent classes
    // throws exception if there are any duplicate names in the chain
    public Map<Variable, Type> baseTypeEnvironmentForClass(final ClassName className) throws TypeErrorException {
        final ClassDef classDef = getClass(className);
        if (classDef == null) {
            return new HashMap<Variable, Type>();
        } else {
            final Map<Variable, Type> retval = baseTypeEnvironmentForClass(classDef.extendsClassName);
            for (final Vardec instanceVariable : classDef.instanceVariables) {
                final Variable variable = instanceVariable.variable;
                if (retval.containsKey(variable)) {
                    throw new TypeErrorException("Duplicate instance variable (possibly inherited): " + variable);
                }
                retval.put(variable, instanceVariable.type);
            }
            return retval;
        }
    }
    
    // -Check constructor
    // -Check methods
    public void isWellTypedClassDef(final ClassDef classDef) throws TypeErrorException {
        final Map<Variable, Type> typeEnvironment = baseTypeEnvironmentForClass(classDef.className);
        
        // check constructor
        Map<Variable, Type> constructorTypeEnvironment = typeEnvironment;
        final Set<Variable> variablesInConstructor = new HashSet<Variable>();
        for (final Vardec vardec : classDef.constructorArguments) {
            final Variable variable = vardec.variable;
            if (variablesInConstructor.contains(variable)) {
                throw new TypeErrorException("Duplicate variable in constructor param: " + variable);
            }
            variablesInConstructor.add(variable);
            constructorTypeEnvironment = addToMap(constructorTypeEnvironment, variable, vardec.type);
        }
        // check call to super
        expressionsOk(expectedConstructorTypesForClass(classDef.extendsClassName),
                      classDef.superParams,
                      constructorTypeEnvironment,
                      classDef.className);
        isWellTypedBlock(new BlockStmt(classDef.constructorBody),
                         constructorTypeEnvironment,
                         classDef.className,
                         new VoidType());

        // check methods
        for (final MethodDef method : classDef.methods) {
            isWellTypedMethodDef(method,
                                 typeEnvironment,
                                 classDef.className);
        }
    }

    // program ::= classdef* stmt
    public void isWellTypedProgram() throws TypeErrorException {
        for (final ClassDef classDef : program.classes) {
            isWellTypedClassDef(classDef);
        }

        isWellTypedStmt(program.entryPoint,
                        new HashMap<Variable, Type>(),
                        null,
                        null);
    }

    public static void typecheck(final Program program) throws TypeErrorException {
        new Typechecker(program).isWellTypedProgram();
    }
}
