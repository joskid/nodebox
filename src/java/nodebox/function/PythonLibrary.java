package nodebox.function;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import nodebox.util.LoadException;
import org.python.core.*;
import org.python.util.PythonInterpreter;

public class PythonLibrary extends FunctionLibrary {

    public static PythonLibrary loadScript(String namespace, String fileName) throws LoadException {
        PythonInterpreter interpreter = new PythonInterpreter();
        interpreter.execfile(fileName);
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
            PyObject[] pyargs = new PyObject[args.length];
            for (int i = 0; i < args.length; i++)
                pyargs[i] = Py.java2py(args[i]);
            PyObject pyresult = fn.__call__(pyargs);
            if (pyresult instanceof PyLong || pyresult instanceof PyInteger)
                return pyresult.__tojava__(Long.class);
            return pyresult.__tojava__(Object.class);
        }

        public ImmutableList<Argument> getArguments() {
            return ImmutableList.of();
        }
    }
}
