package nodebox.function;

import clojure.lang.Compiler;
import clojure.lang.*;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import nodebox.util.LoadException;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;

final class ClojureLibrary extends FunctionLibrary {

    private static final Keyword NAME = Keyword.intern("name");
    private static final Keyword FN = Keyword.intern("fn");

    /**
     * Run the Clojure register-nodes function in the library.
     *
     * @param fileName The file name.
     * @return The new Clojure library.
     * @throws LoadException If the script could not be loaded.
     */
    public static ClojureLibrary loadScript(String fileName) throws LoadException {
        Object returnValue;
        try {
            returnValue = Compiler.loadFile(fileName);
        } catch (IOException e) {
            throw new LoadException(fileName, e);
        }
        // We need a Var as the last statement, because we need to retrieve the current namespace.
        if (!(returnValue instanceof Var)) {
            throw new LoadException(fileName,
                    String.format("The last statement does not define a var, but %s.\n" +
                            "Make sure the last line of your script looks like this:\n" +
                            "(def nodes [{:name \"foo\" :fn inc}])",
                            returnValue));
        }
        Var nodesVar = (Var) returnValue;
        Namespace ns = nodesVar.ns;
        String namespace = ns.name.getName();
        Object functionMap = nodesVar.deref();
        checkStructure(functionMap);
        ImmutableMap.Builder<String, Function> builder = ImmutableMap.builder();
        for (Object item : (Iterable) functionMap) {
            Map m = (Map) item;
            String name = (String) m.get(NAME);
            IFn fn = (IFn) m.get(FN);
            Function f = new ClojureFunction(name, fn);
            builder.put(name, f);
        }
        return new ClojureLibrary(namespace, fileName, builder.build());
    }

    private final String namespace;
    private final String fileName;
    private final ImmutableMap<String, Function> functionMap;

    private ClojureLibrary(String namespace, String fileName, ImmutableMap<String, Function> functionMap) {
        this.namespace = namespace;
        this.fileName = fileName;
        this.functionMap = functionMap;
    }

    @Override
    public String getLink() {
        return "clojure:" + fileName;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getFileName() {
        return fileName;
    }

    public Function getFunction(String name) {
        return functionMap.get(name);
    }

    public boolean hasFunction(String name) {
        return functionMap.containsKey(name);
    }

    /**
     * We expect a list of maps, each containing name and fn.
     *
     * @param v The Clojure data structure contained in all-nodes.
     */
    private static void checkStructure(Object v) {
        checkArgument(v instanceof Iterable, "The function map is not a list of maps but a %s", v);
        Iterable iterable = (Iterable) v;
        for (Object item : iterable) {
            checkArgument(item instanceof Map, "The function map is not a list of maps but a %s", v);
            Map m = (Map) item;
            checkArgument(m.containsKey(NAME), "The function map item %s does not contain a name.", m);
            checkArgument(m.containsKey(FN), "The function map item %s does not contain a fn.", m);
            checkArgument(m.get(FN) instanceof IFn, "The function map item %s does not point to a Clojure function.", m.get("name"));
        }
    }

    private static final class ClojureFunction implements Function {

        private final String name;
        private final IFn fn;
        private final ImmutableList<Argument> arguments;

        public ClojureFunction(String name, IFn fn) {
            this.name = name;
            this.fn = fn;
            this.arguments = introspect(fn);
        }

        public String getName() {
            return name;
        }

        public Object invoke(Object... args) throws Exception {
            return fn.applyTo(RT.arrayToList(args));
        }

        public ImmutableList<Argument> getArguments() {
            return arguments;
        }

        private static ImmutableList<Argument> introspect(IFn fn) {
            // Each function is a separate class.
            Class functionClass = fn.getClass();
            Method m = Functions.findMethod(functionClass, "invoke");
            return Functions.introspect(m);
        }

    }
}
