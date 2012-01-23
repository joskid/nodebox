package nodebox.function;

import com.google.common.base.Objects;
import nodebox.util.LoadException;

/**
 * A collection of functions. This collection is contained in a namespace.
 */
public abstract class FunctionLibrary {

    public static FunctionLibrary load(String fileName) {
        if (fileName.endsWith(".clj")) {
            return ClojureLibrary.loadScript(fileName);
        } else {
            throw new LoadException(fileName, "Unknown function library type.");
        }
    }

    public static FunctionLibrary ofClass(String namespace, Class c, String... methodNames) {
        return JavaLibrary.ofClass(namespace, c, methodNames);
    }

    public abstract String getNamespace();

    public abstract Function getFunction(String name);

    public abstract boolean hasFunction(String name);

    //// Object overrides ////

    @Override
    public int hashCode() {
        return Objects.hashCode(getNamespace());
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof FunctionLibrary)) return false;
        final FunctionLibrary other = (FunctionLibrary) o;
        return Objects.equal(getNamespace(), other.getNamespace());
    }

    @Override
    public String toString() {
        return String.format("<FunctionLibrary %s>", getNamespace());
    }

}
