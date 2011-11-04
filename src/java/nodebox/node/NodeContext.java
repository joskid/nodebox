package nodebox.node;

import nodebox.function.Function;
import nodebox.function.FunctionRepository;
import org.python.google.common.collect.ImmutableMap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public class NodeContext {

    private final double frame;

    public NodeContext() {
        this(1);
    }

    public NodeContext(double frame) {
        this.frame = frame;
    }

    /**
     * Render the network by rendering its rendered child.
     *
     * @param repository The function repository.
     * @param network    The network to render.
     * @return The map with output values keyed by port name.
     * @throws NodeRenderException If processing fails.
     */
    public Map<String, Object> renderNetwork(FunctionRepository repository, Node network) throws NodeRenderException {
        checkNotNull(repository);
        checkNotNull(network);
        return renderChildNode(repository, network.getRenderedChild());
    }

    /**
     * Render the child node.
     * This doesn't calculate child dependencies.
     * On the network, renderNetwork the renderedChild.
     * Note that we pass in the network, not the node to renderNetwork!
     * This is because we can't go up from the node to the network to retrieve the connections.
     *
     * @param repository The function repository.
     * @param node       The node to render.
     * @return The map with output values keyed by port name.
     * @throws NodeRenderException If processing fails.
     */
    public Map<String, Object> renderChildNode(FunctionRepository repository, Node node) throws NodeRenderException {
        checkNotNull(repository);
        checkNotNull(node);
        ImmutableMap.Builder<String, Object> b = ImmutableMap.builder();

        // Get the function.
        String functionName = node.getFunction();
        Function function = repository.getFunction(functionName);

        // Set arguments on the function.
        ArrayList<Object> arguments = new ArrayList<Object>();
        for (Port p : node.getInputs()) {
            arguments.add(p.getValue());
        }

        // Invoke the function.
        Object returnValue;
        try {
            returnValue = function.invoke(arguments.toArray());
        } catch (Exception e) {
            throw new NodeRenderException(node, e);
        }

        // Build the return value map.
        List<Port> outputs = node.getOutputs();
        if (outputs.isEmpty()) {
            return ImmutableMap.of();
        } else if (outputs.size() == 1) {
            Port thePort = node.getOutputs().get(0);
            return ImmutableMap.of(thePort.getName(), returnValue);
        } else {
            checkState(returnValue instanceof Iterable);
            Iterator<Port> outputsIterator = outputs.iterator();
            for (Object value : (Iterable) returnValue) {
                Port p = outputsIterator.next();
                b.put(p.getName(), value);
            }
            return b.build();
        }
    }

    public Object firstOutputOfRender(FunctionRepository repository, Node node) throws NodeRenderException {
        Map<String, Object> values = renderChildNode(repository, node);
        return values.values().iterator().next();
    }

    public double getFrame() {
        return frame;
    }

}
