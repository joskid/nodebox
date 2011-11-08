package nodebox.function;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import nodebox.util.LoadException;
import org.python.core.*;
import org.python.util.PythonInterpreter;

public class PythonLibrary extends FunctionLibrary {

    /**
     * Run the Python register-nodes function in the library.
     *
     * @param namespace The name space in which the library resides.
     * @param fileName The file name.
     * @return The new Python library.
     * @throws LoadException If the script could not be loaded.
     */
    public static PythonLibrary loadScript(String namespace, String fileName) throws LoadException {
        PythonInterpreter interpreter = new PythonInterpreter();
        try {
            interpreter.execfile(fileName);
        } catch (PyException e) {
            throw new LoadException(fileName, e);
        }
        PyStringMap map = (PyStringMap) interpreter.getLocals();

        ImmutableMap.Builder<String, Function> builder = ImmutableMap.builder();

        for (Object key : map.keys()) {
            Object o = map.get(Py.java2py(key));
            if (o instanceof PyFunction) {
                String name = (String) key;
                Function f = new PythonFunction(name, (PyFunction) o);
                builder.put(name, f);
            }
        }
        return new PythonLibrary(namespace, fileName, builder.build());
    }

    private final String namespace;
    private final String fileName;
    private final ImmutableMap<String, Function> functionMap;

    private PythonLibrary(String namespace, String fileName, ImmutableMap<String, Function> functionMap) {
        this.namespace = namespace;
        this.fileName = fileName;
        this.functionMap = functionMap;
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

    private static final class PythonFunction implements Function {

        private final String name;
        private final PyFunction fn;

        public PythonFunction(String name, PyFunction fn) {
            this.name = name;
            this.fn = fn;
        }

        public String getName() {
            return name;
        }

        public Object invoke(Object... args) throws Exception {
            PyObject[] pyArgs = new PyObject[args.length];
            for (int i = 0; i < args.length; i++)
                pyArgs[i] = Py.java2py(args[i]);

            PyObject pyResult = fn.__call__(pyArgs);
            if (pyResult == null)
                return null;
            // todo: number conversions should be handled higher up in the code, and not at the Jython level.
            if (pyResult instanceof PyLong || pyResult instanceof PyInteger)
                return pyResult.__tojava__(Long.class);

            Object result = pyResult.__tojava__(Object.class);
            if (result == Py.NoConversion)
                throw new RuntimeException("Cannot convert Python object " + pyResult + " to java.");
            return result;
        }

        public ImmutableList<Argument> getArguments() {
            // todo: check if keeping a list of arguments makes sense in a python environment.
            return ImmutableList.of();
        }
    }
}
