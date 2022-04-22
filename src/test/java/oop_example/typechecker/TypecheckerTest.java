package oop_example.typechecker;

import oop_example.parser.*;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class TypecheckerTest {
    public static Typechecker emptyTypechecker() throws TypeErrorException {
        return new Typechecker(new Program(new ArrayList<ClassDef>(),
                                           new ExpStmt(new IntLiteralExp(0))));
    }
    
    @Test
    public void testVariableInScope() throws TypeErrorException {
        final Type expectedType = new IntType();
        final Map<Variable, Type> typeEnvironment = new HashMap<Variable, Type>();
        typeEnvironment.put(new Variable("x"), new IntType());
        
        final Type receivedType =
            emptyTypechecker().typeofVariable(new VariableExp(new Variable("x")),
                                              typeEnvironment);
        assertEquals(expectedType, receivedType);
    }
    
    @Test(expected = TypeErrorException.class)
    public void testVariableOutOfScope() throws TypeErrorException {
        final Map<Variable, Type> typeEnvironment = new HashMap<Variable, Type>();
        emptyTypechecker().typeofVariable(new VariableExp(new Variable("x")),
                                          typeEnvironment);
    }

    @Test
    public void testThisInClass() throws TypeErrorException {
        assertEquals(new ClassNameType(new ClassName("foo")),
                     emptyTypechecker().typeofThis(new ClassName("foo")));
    }

    @Test(expected = TypeErrorException.class)
    public void testThisNotInClass() throws TypeErrorException {
        emptyTypechecker().typeofThis(null);
    }
}
