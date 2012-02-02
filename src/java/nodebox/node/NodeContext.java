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
    private final Map<Node, Iterable<?>> outputValuesMap = new HashMap<Node, Iterable<?>>();
    private final Map<NodePort, Iterable<?>> inputValuesMap = new HashMap<NodePort, Iterable<?>>();
    private final Set<Node> renderedNodes = new HashSet<Node>();

    public NodeContext(FunctionRepository repository) {
        this(repository, 1);
    }

    public NodeContext(FunctionRepository repository, double frame) {
        checkNotNull(repository);
        this.repository = repository;
        this.frame = frame;
    }

    public Map<Node, Iterable<?>> getResultsMap() {
        return outputValuesMap;
    }

    public Iterable<?> getResults(Node node) {
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
        if (network.getRenderedChild() != null) {
            renderChild(network, network.getRenderedChild());
        }
    }

    /**
     * Render the child in the network.
     *
     * @param network The network to render.
     * @param child   The child node to render.
     * @return The list of rendered values.
     * @throws NodeRenderException If processing fails.
     */
    public Iterable<?> renderChild(Node network, Node child) throws NodeRenderException {
        checkNotNull(network);
        checkNotNull(child);
        checkArgument(network.hasChild(child));

        // Check if child was already rendered.
        if (renderedNodes.contains(child)) return outputValuesMap.get(child);
        renderedNodes.add(child);

        // Process dependencies
        for (Connection c : network.getConnections()) {
            if (Thread.currentThread().isInterrupted()) throw new NodeRenderException(child, "Interrupted");
            if (c.getInputNode().equals(child.getName())) {
                Node outputNode = network.getChild(c.getOutputNode());
                renderChild(network, outputNode);
                Iterable<?> result = outputValuesMap.get(outputNode);
                // Check if the result is null. This can happen if there is a cycle in the network.
                if (result != null) {
                    inputValuesMap.put(NodePort.of(child, c.getInputPort()), result);
                }
            }
        }

        return renderNode(child);
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
    public Iterable<?> renderNode(Node node) throws NodeRenderException {
        checkNotNull(node);
        checkState(!outputValuesMap.containsKey(node), "Node %s already has a rendered value.", node);

        // If the node has children, forgo the operation of the current node and evaluate the child.
        if (node.hasRenderedChild()) {
            return renderChild(node, node.getRenderedChild());
        }

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
        Iterable<?> results;
        if (node.getListStrategy().equals(Node.AS_IS_STRATEGY)) {
            results = renderAsIsStrategy(node, function, inputValues);
        } else if (node.getListStrategy().equals(Node.WRAP_IN_LIST_STRATEGY)) {
            results = renderWrapInListStrategy(node, function, inputValues);
        } else if (node.getListStrategy().equals(Node.MAP_STRATEGY)) {
            results = renderMapStrategy(node, function, inputValues);
        } else if (node.getListStrategy().equals(Node.FLATTEN_STRATEGY)) {
            results = renderFlattenStrategy(node, function, inputValues);
        } else if (node.getListStrategy().equals(Node.FILTER_STRATEGY)) {
            results = renderFilterStrategy(node, function, inputValues);
        } else {
            throw new NodeRenderException(node, "Node " + node + " has an unknown list strategy " + node.getListStrategy());
        }

        outputValuesMap.put(node, results);
        return results;
    }

    /**
     * Render an as-is node.
     * <p/>
     * As-is nodes know that the inputs and outputs of a node are always lists,
     * and operate on this list. The function is called with each argument as a list.
     * <p/>
     * Examples of as-is nodes are "reverse", "sublist", "shuffle", ...
     *
     * @param node        The node to render.
     * @param function    The node's function implementation.
     * @param inputValues A list of all values for the input ports.
     * @return The list of results.
     */
    private Iterable<?> renderAsIsStrategy(Node node, Function function, List<ValueOrList> inputValues) {
        List<Object> arguments = inputValuesToArguments(inputValues);
        Object returnValue = invokeFunction(node, function, arguments);
        checkState(returnValue instanceof Iterable, "Return value of list-aware function needs to be a List.");
        return (Iterable<?>) returnValue;
    }

    /**
     * Render a node using the wrap-in-list strategy.
     * <p/>
     * Wrap-in-list nodes receive a list and return a single value.
     * The NodeContext wraps this value into a list with one element.
     * <p/>
     * Example of wrap-in-list nodes are "sum", "avg".
     *
     * @param node        The node to render.
     * @param function    The node's function implementation.
     * @param inputValues A list of all values for the input ports.
     * @return The list of results.
     */
    private Iterable<?> renderWrapInListStrategy(Node node, Function function, List<ValueOrList> inputValues) {
        List<Object> arguments = inputValuesToArguments(inputValues);
        Object returnValue = invokeFunction(node, function, arguments);
        return ImmutableList.of(returnValue);
    }

    /**
     * Render a node using the map strategy.
     * <p/>
     * Map nodes do not know anything about lists. The function processes the input values one by one,
     * each one returning a simple value. These values are combined into a list.
     * If the input lists are of different length, stop after the shortest list.
     *
     * @param node        The node to render.
     * @param function    The node's function implementation.
     * @param inputValues A list of all values for the input ports.
     * @return The list of return values.
     */
    private List<?> renderMapStrategy(final Node node, final Function function, List<ValueOrList> inputValues) {
        // If the node has no input ports, execute the node once for its side effects.
        if (node.getInputs().isEmpty()) {
            Object returnValue = invokeFunction(node, function, ImmutableList.of());
            return ImmutableList.of(returnValue);
        }

        return renderMapStrategyInternal(node, inputValues, new FunctionInvoker() {
            public void call(List<Object> arguments, List<Object> results) {
                results.add(invokeFunction(node, function, arguments));
            }
        });
    }

    /**
     * Render a node using the flatten strategy.
     * <p/>
     * Flatten nodes take in one value and return a list. All these lists are combined.
     * <p/>
     * Example of flatten nodes are "grid", "scatter".
     *
     * @param node        The node to render.
     * @param function    The node's function implementation.
     * @param inputValues A list of all values for the input ports.
     * @return The list of results.
     */
    private List<?> renderFlattenStrategy(Node node, Function function, List<ValueOrList> inputValues) {
        List<?> results = renderMapStrategy(node, function, inputValues);
        ImmutableList.Builder<Object> b = new ImmutableList.Builder<Object>();
        for (Object o : results) {
            checkState(o instanceof Iterable);
            b.addAll((Iterable<?>) o);
        }
        return b.build();
    }

    /**
     * Render a node using the filter strategy.
     * <p/>
     * Filter nodes do not know anything about lists. The function returns true / false, and the values for
     * which the function returns true are returned.
     * <p/>
     * Example of filter nodes are "even", "greater-than".
     *
     * @param node        The node to render.
     * @param function    The node's function implementation.
     * @param inputValues A list of all values for the input ports.
     * @return The list of results.
     */
    private List<?> renderFilterStrategy(final Node node, final Function function, List<ValueOrList> inputValues) {
        return renderMapStrategyInternal(node, inputValues, new FunctionInvoker() {
            public void call(List<Object> arguments, List<Object> results) {
                boolean pass = (Boolean) invokeFunction(node, function, arguments);
                if (pass)
                    results.add(arguments.get(0));

            }
        });
    }

    /**
     * Do the actual mapping function. This uses a higher-order function "FunctionInvoker" that is free to execute
     * something with the arguments it gets and add to the results.
     *
     * @param node        The node to render.
     * @param inputValues A list of all values for the input ports.
     * @param op          The higher-order function that receives arguments and can manipulate the results.
     * @return The list of results.
     */
    private List<?> renderMapStrategyInternal(Node node, List<ValueOrList> inputValues, FunctionInvoker op) {
        // If the node has no input ports, or if the minimum list size is zero, return an empty list.
        if (node.getInputs().isEmpty() || !hasElements(inputValues)) {
            return ImmutableList.of();
        }

        List<Object> results = new ArrayList<Object>();
        Map<ValueOrList, Iterator> iteratorMap = new HashMap<ValueOrList, Iterator>();
        boolean hasListArgument = false;
        while (true) {
            if (Thread.interrupted()) throw new NodeRenderException(node, "Interrupted.");
            // Collect arguments by going through the input values.
            List<Object> arguments = new ArrayList<Object>();
            for (ValueOrList v : inputValues) {
                if (v.isList()) {
                    // Store each iterator in the map.
                    if (!iteratorMap.containsKey(v)) {
                        iteratorMap.put(v, v.getList().iterator());
                    }
                    Iterator iterator = iteratorMap.get(v);
                    // End when the first list is exhausted.
                    if (!iterator.hasNext()) return results;
                    arguments.add(iterator.next());
                    hasListArgument = true;
                } else {
                    arguments.add(v.getValue());
                }
            }
            // Invoke the function.
            op.call(arguments, results);
            // If none of the arguments are lists, we're done.
            if (!hasListArgument) break;
        }
        return results;
    }

    private List<Object> inputValuesToArguments(List<ValueOrList> inputValues) {
        List<Object> arguments = new ArrayList<Object>(inputValues.size());
        for (ValueOrList v : inputValues) {
            arguments.add(v.value);
        }
        return arguments;
    }

    private Object invokeFunction(Node node, Function function, List<?> arguments) throws NodeRenderException {
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
     * Return true if each element has a next element.
     *
     * @param ll The list of lists or values.
     * @return true if each of the lists has values.
     */
    private static boolean hasElements(List<ValueOrList> ll) {
        checkNotNull(ll);
        if (ll.isEmpty()) return false;
        for (ValueOrList v : ll) {
            if (v.isList()) {
                if (!v.getList().iterator().hasNext()) return false;
            }
        }
        return true;
    }

    private static final class ValueOrList {
        private final boolean isList;
        private final Object value;

        private static ValueOrList ofValue(Object value) {
            return new ValueOrList(false, value);
        }

        private static ValueOrList ofList(Iterable list) {
            return new ValueOrList(true, list);
        }

        private ValueOrList(boolean isList, Object value) {
            checkArgument(!isList || value instanceof Iterable);
            this.isList = isList;
            this.value = value;
        }


        private Object getValue() {
            checkState(!isList);
            return value;
        }

        private Iterable getList() {
            checkState(isList);
            return (Iterable) value;
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

    /**
     * Higher-order function that receives a list of arguments to invoke a function with.
     * It can add something to the list of results, if it wants.
     */
    private interface FunctionInvoker {
        public void call(List<Object> arguments, List<Object> results);
    }

}
