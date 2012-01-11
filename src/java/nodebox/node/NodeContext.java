package nodebox.node;

import com.google.common.base.Objects;
import com.google.common.collect.*;
import nodebox.function.Function;
import nodebox.function.FunctionRepository;

import java.util.*;

import static com.google.common.base.Preconditions.*;

public class NodeContext {

    /**
     * This is used as the key for the results map.
     * <p/>
     * We can't just use the Port, since Port instances are re-used across nodes. Using the node / port combination
     * is unique.
     */
    public static final class NodePort {
        private final Node node;
        private final Port port;

        public static NodePort of(Node node, Port port) {
            return new NodePort(node, port);
        }

        public static NodePort of(Node node, String portName) {
            return new NodePort(node, node.getOutput(portName));
        }

        private NodePort(Node node, Port port) {
            checkNotNull(node);
            checkNotNull(port);
            this.node = node;
            this.port = port;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            NodePort nodePort = (NodePort) o;

            if (!node.equals(nodePort.node)) return false;
            if (!port.equals(nodePort.port)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(node, port);
        }
    }

    private final FunctionRepository repository;
    private final double frame;
    private final Map<NodePort, List<Object>> results = new HashMap<NodePort, List<Object>>();
    private final Set<Node> renderedNodes = new HashSet<Node>();

    public NodeContext(FunctionRepository repository) {
        this(repository, 1);
    }

    public NodeContext(FunctionRepository repository, double frame) {
        checkNotNull(repository);
        this.repository = repository;
        this.frame = frame;
    }

    public Map<NodePort, List<Object>> getResultsMap() {
        return results;
    }

    public List<Object> getResults(Node node, String outputPort) {
        checkArgument(node.hasOutput(outputPort), "Node %s does not have an output port named %s.", node, outputPort);
        return results.get(NodePort.of(node, outputPort));
    }

    public List<Object> getResults(Node node, Port port) {
        return results.get(new NodePort(node, port));
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
                List<Object> result = results.get(NodePort.of(outputNode, outputPort));
                // Check if the result is null. This can happen if there is a cycle in the network.
                if (result != null) {
                    results.put(NodePort.of(child, inputPort), result);
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

        // Get the input values.
        ArrayList<List> inputValues = new ArrayList<List>();
        for (Port p : node.getInputs()) {
            if (results.containsKey(NodePort.of(node, p))) {
                inputValues.add(results.get(NodePort.of(node, p)));
            } else {
                inputValues.add(ImmutableList.of(p.getValue()));
            }
        }

        // Invoke the node function.
        if (node.isListAware()) {
            renderListAwareNode(node, function, inputValues);
        } else {
            renderListUnawareNode(node, function, inputValues);
        }
    }

    /**
     * Render a list-aware node.
     * <p/>
     * List-aware nodes know that the inputs and outputs of a node are always lists,
     * and operate on this list. The function is called with each argument as a list.
     * The function itself takes care of all looping and returns a list.
     * <p/>
     * This is useful for nodes that reduce the list, ie. filter / sum nodes.
     *
     * @param node        The node to render.
     * @param function    The node's function implementation.
     * @param inputValues A list of all values for the input ports.
     */
    private void renderListAwareNode(Node node, Function function, List<List> inputValues) {
        Object returnValues = invokeFunction(node, function, inputValues);
        checkState(returnValues instanceof Iterable, "Return value of list-aware function needs to be a List.");

        List<Port> outputs = node.getOutputs();
        checkState(outputs.size() == 1, "Only one output port is supported.");
        Port thePort = node.getOutputs().get(0);

        NodePort np = NodePort.of(node, thePort);
        checkState(!results.containsKey(np), "This value should not have been set yet.");
        results.put(np, ImmutableList.copyOf((Iterable<? extends Object>) returnValues));
    }

    private int listMin(List<List> ll) {
        if (ll.size() == 0) return 0;
        int minSpan = Integer.MAX_VALUE;
        for (List values : ll) {
            minSpan = Math.min(minSpan, values.size());
        }
        checkState(minSpan < Integer.MAX_VALUE);
        return minSpan;
    }

    /**
     * Render a node that is not list-aware.
     * <p/>
     * The function processes the input values one by one, and returns.
     * This function takes care of looping through all executions.
     * The list matching strategy defines how multiple inputs are combined.
     *
     * @param node        The node to render.
     * @param function    The node's function implementation.
     * @param inputValues A list of all values for the input ports.
     */
    private void renderListUnawareNode(Node node, Function function, List<List> inputValues) {
        // Find list matching strategy.
        // TODO: Implement. Currently we choose shortest list.
        int minSpan = listMin(inputValues);

        List<Port> outputs = node.getOutputs();
        for (Port output : outputs) {
            checkState(!results.containsKey(NodePort.of(node, output)), "This value should not have been set yet.");
        }
        if (minSpan == 0) {
            // Execute the node once if there are no input nodes.
            Map<Port, Object> returnValues = invokeListUnawareFunction(node, function, ImmutableList.of());
            for (Map.Entry<Port, Object> returnValue : returnValues.entrySet()) {
                results.put(NodePort.of(node, returnValue.getKey()), ImmutableList.of(returnValue.getValue()));
            }
        } else {
            Multimap<Port, Object> resultsMultimap = LinkedListMultimap.create();
            for (int i = 0; i < minSpan; i++) {
                ArrayList<Object> arguments = new ArrayList<Object>();
                for (List values : inputValues) {
                    Object arg = values.get(i);
                    arguments.add(arg);
                }
                Map<Port, Object> returnValues = invokeListUnawareFunction(node, function, arguments);
                for (Map.Entry<Port, Object> returnValue : returnValues.entrySet()) {
                    resultsMultimap.put(returnValue.getKey(), returnValue.getValue());
                }
            }
            for (Port port : resultsMultimap.keySet()) {
                NodePort np = NodePort.of(node, port);
                checkState(!results.containsKey(np), "This value should not have been set yet.");
                results.put(NodePort.of(node, port), ImmutableList.copyOf(resultsMultimap.get(port)));
            }
        }
    }

    private Map<Port, Object> invokeListUnawareFunction(Node node, Function function, List<Object> arguments) throws NodeRenderException {
        Object returnValue = invokeFunction(node, function, arguments);
        List<Port> outputs = node.getOutputs();
        if (outputs.size() == 0) {
            // If the node has no outputs, we ignore the return value.
            return ImmutableMap.of();
        } else if (outputs.size() == 1) {
            // If the node only has one output port, we assume the function returns the value as-is.
            // The return value can still be a list: we don't really care what comes out of the function.
            Port firstOutput = outputs.get(0);
            return ImmutableMap.of(firstOutput, returnValue);
        } else {
            // If the node has multiple output ports, we assume the function returns an Iterable with 
            // the same size as the number of ports.
            checkState(returnValue instanceof Iterable, "The return value of %s needs to be a list with %s elements.", function, outputs.size());
            ImmutableList<Object> returnValues = ImmutableList.copyOf((Iterable<? extends Object>) returnValue);
            checkState(returnValues.size() == outputs.size(), "The return value of %s needs to be a list with %s elements.", function, outputs.size());
            ImmutableMap.Builder<Port, Object> b = ImmutableMap.builder();
            for (int i = 0; i < outputs.size(); i++) {
                b.put(outputs.get(i), returnValues.get(i));
            }
            return b.build();
        }
    }

    private Object invokeFunction(Node node, Function function, List<? extends Object> arguments) throws NodeRenderException {
        try {
            return function.invoke(arguments.toArray());
        } catch (Exception e) {
            throw new NodeRenderException(node, e);
        }
    }

    public List<Object> renderPort(Node node, String outputPort) throws NodeRenderException {
        renderNode(node);
        return results.get(NodePort.of(node, outputPort));
    }

    public double getFrame() {
        return frame;
    }

}
