package nodebox.node;

import nodebox.function.Function;
import nodebox.function.FunctionRepository;

import java.util.ArrayList;

public class NodeContext {

    private final FunctionRepository repository;
    private final double frame = 1;

    public NodeContext(FunctionRepository repository) {
        this.repository = repository;
    }

    public Object render(Node node) {
        // Get the function.
        String functionName = node.getFunction();
        Function function = repository.getFunction(functionName);

        // Set arguments on the function.
        ArrayList<Object> arguments = new ArrayList<Object>();
        for (Port p : node.getPorts()) {
            arguments.add(p.getValue());
        }

        // Invoke the function.
        try {
            return function.invoke(arguments.toArray());
        } catch (Exception e) {
            throw new NodeRenderException(node, e);
        }
    }

    public double getFrame() {
        return frame;
    }
}
