package nodebox.function;

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

}
