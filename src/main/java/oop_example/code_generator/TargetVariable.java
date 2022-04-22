package oop_example.code_generator;

// represents a variable in the target language
public class TargetVariable {
    public final String name;

    public TargetVariable(final String name) {
        this.name = name;
    }

    public int hashCode() {
        return name.hashCode();
    }

    public boolean equals(final Object other) {
        return (other instanceof TargetVariable &&
                name.equals(((TargetVariable)other).name));
    }

    public String toString() {
        return "TargetVariable(" + name + ")";
    }
}
