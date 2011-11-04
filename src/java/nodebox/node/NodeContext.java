package nodebox.node;

import nodebox.function.Function;
import nodebox.function.FunctionRepository;

import java.util.*;

import static com.google.common.base.Preconditions.*;

public class NodeContext {

    private final FunctionRepository repository;
    private final double frame;
    private final Map<Port, Object> results = new HashMap<Port, Object>();
    private final Set<Node> renderedNodes = new HashSet<Node>();

    public NodeContext(FunctionRepository repository) {
        this(repository, 1);
    }

    public NodeContext(FunctionRepository repository, double frame) {
        checkNotNull(repository);
        this.repository = repository;
        this.frame = frame;
    }

    public Map<Port, Object> getResults() {
        return results;
    }

    public Object getResult(Node node, String outputPort) {
        return results.get(node.getOutput(outputPort));
    }

    public Object getResult(Port port) {
        return results.get(port);
    }

    /**
     * Render the network by rendering its rendered child.
     *
     * @param network The network to render.
     * @throws NodeRenderException If processing fails.
     */
    public void renderNetwork(Node network) throws NodeRenderException {
        checkNotNull(network);
        renderChild(network, network.getRenderedChild());
    }

    /**
     * Render the child by rendering its rendered child.
     *
     * @param network The network to render.
     * @param child   The child node to render.
     * @throws NodeRenderException If processing fails.
     */
    public void renderChild(Node network, Node child) throws NodeRenderException {
        checkNotNull(network);
        checkNotNull(child);
        checkArgument(network.hasChild(child));

        // Check if child was already rendered.
        if (renderedNodes.contains(child)) return;
        renderedNodes.add(child);

        // Process dependencies
        for (Connection c : network.getConnections()) {
            if (c.getInputNode().equals(child.getName())) {
                Node outputNode = network.getChild(c.getOutputNode());
                renderChild(network, outputNode);
                Port outputPort = outputNode.getOutput(c.getOutputPort());
                Port inputPort = child.getInput(c.getInputPort());
                Object result = results.get(outputPort);
                // Check if the result is null. This can happen if there is a cycle in the network.
                if (result != null) {
                    results.put(inputPort, result);
                }
            }
        }

        renderNode(child);
    }

    /**
     * Render a single node.
     * This doesn't evaluate child dependencies.
     * On the network, renderNetwork the renderedChild.
     * Note that we pass in the network, not the node to renderNetwork!
     * This is because we can't go up from the node to the network to retrieve the connections.
     *
     * @param node The node to render.
     * @throws NodeRenderException If processing fails.
     */
    public void renderNode(Node node) throws NodeRenderException {
        checkNotNull(node);

        // Get the function.
        String functionName = node.getFunction();
        Function function = repository.getFunction(functionName);

        // Set arguments on the function.
        ArrayList<Object> arguments = new ArrayList<Object>();
        for (Port p : node.getInputs()) {
            // Check if the argument is available in the results.
            // This happens if we have calculated a dependency.
            Object argumentValue;
            if (results.containsKey(p)) {
                argumentValue = results.get(p);
            } else {
                argumentValue = p.getValue();
            }
            arguments.add(argumentValue);
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
            // Do nothing.
        } else if (outputs.size() == 1) {
            Port thePort = node.getOutputs().get(0);
            results.put(thePort, returnValue);
        } else {
            checkState(returnValue instanceof Iterable);
            Iterator<Port> outputsIterator = outputs.iterator();
            for (Object value : (Iterable) returnValue) {
                Port p = outputsIterator.next();
                results.put(p, value);
            }
        }
    }

    public Object renderPort(Node node, String outputPort) throws NodeRenderException {
        renderNode(node);
        return results.get(node.getOutput(outputPort));
    }

    public double getFrame() {
        return frame;
    }

}
