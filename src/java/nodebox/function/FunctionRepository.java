package nodebox.function;

import com.google.common.collect.ImmutableMap;
import nodebox.node.Node;
import nodebox.node.Port;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Manages a collection of function libraries.
 */
public class FunctionRepository {

    public static FunctionRepository of(FunctionLibrary... libraries) {
        ImmutableMap.Builder<String, FunctionLibrary> builder = ImmutableMap.builder();
        for (FunctionLibrary library : libraries) {
            builder.put(library.getNamespace(), library);
        }
        // The core library is always included.
        builder.put(CoreFunctions.LIBRARY.getNamespace(), CoreFunctions.LIBRARY);
        return new FunctionRepository(builder.build());
    }

    private final ImmutableMap<String, FunctionLibrary> libraryMap;

    private FunctionRepository(ImmutableMap<String, FunctionLibrary> libraryMap) {
        this.libraryMap = libraryMap;
    }

    public Function getFunction(String identifier) {
        String[] functionParts = identifier.split("/");
        checkArgument(functionParts.length == 2, "The function identifier should be in the form 'namespace/function'.");
        String namespace = functionParts[0];
        String functionName = functionParts[1];
        FunctionLibrary library = libraryMap.get(namespace);
        checkArgument(library != null, "Could not find function %s: unknown namespace.", identifier);
        assert library != null; // To avoid a compiler warning.
        checkArgument(library.hasFunction(functionName), "Could not find function %s: unknown function.", identifier);
        return library.getFunction(functionName);
    }

    public boolean hasFunction(String identifier) {
        return getFunction(identifier) != null;
    }

    public Node nodeForFunction(String identifier) {
        Function function = getFunction(identifier);
        Node n = Node.ROOT.withFunction(identifier);
        for (Function.Argument arg : function.getArguments()) {
            n = n.withPortAdded(Port.portForType(arg.getName(), arg.getType()));
        }
        return n;
    }

    public FunctionLibrary getLibrary(String namespace) {
        checkNotNull(namespace);
        checkArgument(libraryMap.containsKey(namespace), "Could not find library %s: unknown namespace.", namespace);
        return libraryMap.get(namespace);
    }

}
