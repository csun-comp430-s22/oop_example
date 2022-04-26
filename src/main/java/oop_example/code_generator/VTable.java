package oop_example.code_generator;

import oop_example.parser.MethodName;
import oop_example.parser.ClassName;

import java.io.PrintWriter;
import java.io.IOException;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

public class VTable {
    public final ClassName className;
    private final List<FunctionName> functions;
    private final Map<MethodName, Integer> methodToIndex;

    public VTable(final ClassName className,
                  final List<FunctionName> functions,
                  final Map<MethodName, Integer> methodToIndex) {
        this.className = className;
        this.functions = functions;
        this.methodToIndex = methodToIndex;
    }
    
    // creates a new empty VTable
    public VTable(final ClassName className) {
        this(className,
             new ArrayList<FunctionName>(),
             new HashMap<MethodName, Integer>());
    }

    public int indexOfMethod(final MethodName forMethod) {
        final Integer retval = methodToIndex.get(forMethod);
        assert(retval != null);
        return retval.intValue();
    }
    
    public void addOrUpdateMethod(final MethodName forMethod) {
        final Integer index = methodToIndex.get(forMethod);
        final FunctionName functionName =
            CodeGenerator.nameMangleFunctionName(className, forMethod);
        if (index == null) {
            // new method - put it at the end
            functions.add(functionName);
            methodToIndex.put(forMethod, new Integer(functions.size() - 1));
        } else {
            // existing method - update it
            functions.set(index.intValue(), functionName);
        }
    }

    public VTable copy(final ClassName className) {
        final List<FunctionName> copyFunctions = new ArrayList<FunctionName>();
        final Map<MethodName, Integer> copyMethods = new HashMap<MethodName, Integer>();
        copyFunctions.addAll(functions);
        copyMethods.putAll(methodToIndex);
        return new VTable(className, copyFunctions, copyMethods);
    }

    public TargetVariable targetVariable() {
        return new TargetVariable("vtable_" + className.name);
    }
    
    public void writeTable(final PrintWriter output) throws IOException {
        output.print("let ");
        output.print(targetVariable().name);
        output.print(" = [");
        final int numFunctions = functions.size();
        final Iterator<FunctionName> iterator = functions.iterator();
        for (int index = 1; iterator.hasNext() && index < numFunctions; index++) {
            output.print(iterator.next().name);
            output.print(", ");
        }
        if (iterator.hasNext()) {
            output.print(iterator.next().name);
        }
        output.println("];");
    }
}
