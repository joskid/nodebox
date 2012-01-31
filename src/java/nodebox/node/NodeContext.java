package nodebox.node;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import nodebox.function.Function;
import nodebox.function.FunctionRepository;

import java.util.*;

import static com.google.common.base.Preconditions.*;

public class NodeContext {

    private final FunctionRepository repository;
    private final double frame;
    private final Map<Node, List<Object>> outputValuesMap = new HashMap<Node, List<Object>>();
    private final Map<NodePort, List<Object>> inputValuesMap = new HashMap<NodePort, List<Object>>();
    private final Set<Node> renderedNodes = new HashSet<Node>();

    public NodeContext(FunctionRepository repository) {
        this(repository, 1);
    }

    public NodeContext(FunctionRepository repository, double frame) {
        checkNotNull(repository);
        this.repository = repository;
        this.frame = frame;
    }

    public Map<Node, List<Object>> getResultsMap() {
        return outputValuesMap;
    }

    public List<Object> getResults(Node node) {
        return outputValuesMap.get(node);
    }

    /**
     * Render the network by rendering its rendered child.
     *
     * @param network The network to render.
     * @throws NodeRenderException If processing fails.
     */
    public void renderNetwork(Node network) throws NodeRenderException {
        checkNotNull(network);
        if (network.getRenderedChild() == null) {
            throw new NodeRenderException(network, "No child node to render.");
        } else {
            renderChild(network, network.getRenderedChild());
        }
    }

    /**
     * Render the child in the network.
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
                List<Object> result = outputValuesMap.get(outputNode);
                // Check if the result is null. This can happen if there is a cycle in the network.
                if (result != null) {
                    inputValuesMap.put(NodePort.of(child, c.getInputPort()), result);
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
     * @return The list of rendered values.
     * @throws NodeRenderException If processing fails.
     */
    public List<Object> renderNode(Node node) throws NodeRenderException {
        checkNotNull(node);
        checkState(!outputValuesMap.containsKey(node), "Node %s already has a rendered value.", node);

        // Get the function.
        String functionName = node.getFunction();
        Function function = repository.getFunction(functionName);

        // Get the input values.
        ArrayList<ValueOrList> inputValues = new ArrayList<ValueOrList>();
        for (Port p : node.getInputs()) {
            NodePort np = NodePort.of(node, p);
            if (inputValuesMap.containsKey(np)) {
                inputValues.add(ValueOrList.ofList(inputValuesMap.get(np)));
            } else {
                // Because other values return the shortest list, this doesn't work.
                inputValues.add(ValueOrList.ofValue(p.getValue()));
            }
        }

        // Invoke the node function.
        List<Object> results;
        if (node.isListAware()) {
            results = renderListAwareNode(node, function, inputValues);

        } else {
            results = renderListUnawareNode(node, function, inputValues);
        }
        outputValuesMap.put(node, results);
        return results;
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
     * @return The list of results.
     */
    private List<Object> renderListAwareNode(Node node, Function function, List<ValueOrList> inputValues) {
        List<Object> arguments = inputValuesToArguments(inputValues);
        Object returnValues = invokeFunction(node, function, arguments);
        checkState(returnValues instanceof Iterable, "Return value of list-aware function needs to be a List.");
        return ImmutableList.copyOf((Iterable<? extends Object>) returnValues);
    }

    private List<Object> inputValuesToArguments(List<ValueOrList> inputValues) {
        List<Object> arguments = new ArrayList<Object>(inputValues.size());
        for (ValueOrList v : inputValues) {
            arguments.add(v.value);
        }
        return arguments;
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
     * @return The list of return values.
     */
    private List<Object> renderListUnawareNode(Node node, Function function, List<ValueOrList> inputValues) {
        checkState(!node.isListAware());

        if (node.getInputs().isEmpty()) {
            // Execute the node once if there are no input ports.
            Object returnValue = invokeFunction(node, function, ImmutableList.of());
            return ImmutableList.of(returnValue);
        } else {
            int minimumSize = minimumSize(inputValues);
            if (minimumSize == 0) {
                // If the minimum list size is zero a list of zero elements was passed in to one of the inputs.
                // This means the function doesn't get called, and returns an empty list.
                return ImmutableList.of();
            } else {
                List<Object> results = new LinkedList<Object>();
                int listIndex = 0;
                boolean hasListArgument = false;
                processInputValues:
                while (true) {
                    // Collect arguments by going through the input values.
                    List<Object> arguments = new ArrayList<Object>();
                    for (ValueOrList v : inputValues) {
                        if (v.isList()) {
                            // End when the first list is exhausted.
                            if (listIndex >= v.getList().size()) break processInputValues;
                            arguments.add(v.getList().get(listIndex));
                            hasListArgument = true;
                        } else {
                            arguments.add(v.getValue());
                        }
                    }
                    // Invoke the function.
                    results.add(invokeFunction(node, function, arguments));
                    // If none of the arguments are lists, we're done.
                    if (!hasListArgument) break;
                    // Otherwise increment the list index.
                    listIndex++;
                }
                return results;
            }
        }
    }

    private Object invokeFunction(Node node, Function function, List<? extends Object> arguments) throws NodeRenderException {
        try {
            return function.invoke(arguments.toArray());
        } catch (Exception e) {
            throw new NodeRenderException(node, e);
        }
    }

    public double getFrame() {
        return frame;
    }

    /**
     * Get the minimum amount of elements in the list of lists.
     *
     * @param ll The list of lists.
     * @return The minimum amount of elements.
     */
    private static int minimumSize(List<ValueOrList> ll) {
        if (ll.size() == 0) return 0;
        int minSize = Integer.MAX_VALUE;
        for (ValueOrList v : ll) {
            if (v.isList())
                minSize = Math.min(minSize, v.getList().size());
        }
        // HACK If all lists are infinite (meaning they return a size() of MAX_VALUE),
        // they are all a size of one. Infinite lists are a hack themselves to make
        // sure that non-connected ports do not produce short lists of one element.
        return minSize < Integer.MAX_VALUE ? minSize : 1;
    }

    private static final class ValueOrList {
        private final boolean isList;
        private final Object value;

        private static ValueOrList ofValue(Object value) {
            return new ValueOrList(false, value);
        }

        private static ValueOrList ofList(List list) {
            return new ValueOrList(true, list);
        }

        private ValueOrList(boolean isList, Object value) {
            checkArgument(!isList || value instanceof List);
            this.isList = isList;
            this.value = value;
        }


        private Object getValue() {
            checkState(!isList);
            return value;
        }

        private List getList() {
            checkState(isList);
            return (List) value;
        }

        private boolean isList() {
            return isList;
        }

    }

    /**
     * This is used as the key for the inputValuesMap.
     */
    public static final class NodePort {
        private final Node node;
        private final Port port;

        public static NodePort of(Node node, Port port) {
            return new NodePort(node, port);
        }

        public static NodePort of(Node node, String portName) {
            return new NodePort(node, node.getInput(portName));
        }

        private NodePort(Node node, Port port) {
            checkNotNull(node);
            checkNotNull(port);
            this.node = node;
            this.port = port;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof NodePort)) return false;
            final NodePort other = (NodePort) o;
            return Objects.equal(node, other.node)
                    && Objects.equal(port, other.port);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(node, port);
        }
    }

}
