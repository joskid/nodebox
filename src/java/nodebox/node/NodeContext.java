package nodebox.node;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import nodebox.function.Function;
import nodebox.function.FunctionRepository;

import java.util.*;

import static com.google.common.base.Preconditions.*;

public class NodeContext {

    /**
     * This is used as the key for the outputValuesMap.
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

    private final FunctionRepository repository;
    private final double frame;
    private final Map<NodePort, List<Object>> outputValuesMap = new HashMap<NodePort, List<Object>>();
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

    public Map<NodePort, List<Object>> getResultsMap() {
        return outputValuesMap;
    }

    public List<Object> getResults(Node node, String outputPort) {
        checkArgument(node.hasOutput(outputPort), "Node %s does not have an output port named %s.", node, outputPort);
        return outputValuesMap.get(NodePort.of(node, outputPort));
    }

    public List<Object> getResults(Node node, Port port) {
        return outputValuesMap.get(new NodePort(node, port));
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
                Port outputPort = outputNode.getOutput(c.getOutputPort());
                Port inputPort = child.getInput(c.getInputPort());
                List<Object> result = outputValuesMap.get(NodePort.of(outputNode, outputPort));
                // Check if the result is null. This can happen if there is a cycle in the network.
                if (result != null) {
                    inputValuesMap.put(NodePort.of(child, inputPort), result);
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
        ArrayList<List<Object>> inputValues = new ArrayList<List<Object>>();
        for (Port p : node.getInputs()) {
            if (inputValuesMap.containsKey(NodePort.of(node, p))) {
                inputValues.add(inputValuesMap.get(NodePort.of(node, p)));
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
    private void renderListAwareNode(Node node, Function function, List<List<Object>> inputValues) {
        Object returnValues = invokeFunction(node, function, inputValues);
        checkState(returnValues instanceof Iterable, "Return value of list-aware function needs to be a List.");

        List<Port> outputs = node.getOutputs();
        checkState(outputs.size() == 1, "Only one output port is supported.");
        Port thePort = node.getOutputs().get(0);

        NodePort np = NodePort.of(node, thePort);
        checkState(!outputValuesMap.containsKey(np), "This value should not have been set yet.");
        outputValuesMap.put(np, ImmutableList.copyOf((Iterable<? extends Object>) returnValues));
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
    private void renderListUnawareNode(Node node, Function function, List<List<Object>> inputValues) {
        checkState(node.getListPolicy() != ListPolicy.LIST_AWARE);

        List<Port> outputs = node.getOutputs();
        for (Port output : outputs) {
            checkState(!outputValuesMap.containsKey(NodePort.of(node, output)), "This value should not have been set yet.");
        }

        if (node.getInputs().isEmpty()) {
            // Execute the node once if there are no input ports.
            Map<Port, Object> returnValues = invokeListUnawareFunction(node, function, ImmutableList.of());
            for (Map.Entry<Port, Object> returnValue : returnValues.entrySet()) {
                outputValuesMap.put(NodePort.of(node, returnValue.getKey()), ImmutableList.of(returnValue.getValue()));
            }
        } else {
            // There are input ports. Use the list policy to determine what to do with them.
            final int minimumSize = minimumSize(inputValues);
            ListOfListsIterator inputsIterator;
            if (node.getListPolicy() == ListPolicy.SHORTEST_LIST) {
                inputsIterator = new ShortestListIterator(inputValues);
            } else if (node.getListPolicy() == ListPolicy.LONGEST_LIST) {
                inputsIterator = new LongestListIterator(inputValues);
            } else if (node.getListPolicy() == ListPolicy.CROSS_REFERENCE) {
                inputsIterator = new CrossReferenceListIterator(inputValues);
            } else {
                throw new AssertionError("Invalid list policy " + node.getListPolicy());
            }
            checkState(inputsIterator.size() >= 0);
            if (minimumSize == 0) {
                // If the minimum list size is zero a list of zero elements was passed in to one of the inputs.
                // This means the function doesn't get called, and returns an empty list.
                for (Port port : node.getOutputs()) {
                    outputValuesMap.put(NodePort.of(node, port), ImmutableList.of());
                }
            } else {
                Multimap<Port, Object> resultsMultimap = LinkedListMultimap.create();
                while (inputsIterator.hasNext()) {
                    List<? extends Object> arguments = inputsIterator.next();
                    Map<Port, Object> returnValues = invokeListUnawareFunction(node, function, arguments);
                    for (Map.Entry<Port, Object> returnValue : returnValues.entrySet()) {
                        resultsMultimap.put(returnValue.getKey(), returnValue.getValue());
                    }
                }
                for (Port port : resultsMultimap.keySet()) {
                    NodePort np = NodePort.of(node, port);
                    checkState(!outputValuesMap.containsKey(np), "This value should not have been set yet.");
                    outputValuesMap.put(NodePort.of(node, port), ImmutableList.copyOf(resultsMultimap.get(port)));
                }
            }
        }
    }

    /**
     * Get the value at the given index. If the index is larger than the list, wrap around.
     * The list cannot be null. If the list is empty, null is returned.
     *
     * @param values The input values.
     * @param index  The index number. Negative indices are not supported.
     * @return The value at the index.
     */
    public static Object getValueAtIndexWrapped(List<? extends Object> values, int index) {
        checkNotNull(values, "Values cannot be null.");
        checkArgument(index >= 0, "Index needs to be greater than or equal to zero.");
        if (values.isEmpty()) return null;
        return values.get(index % values.size());
    }

    private Map<Port, Object> invokeListUnawareFunction(Node node, Function function, List<? extends Object> arguments) throws NodeRenderException {
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
        return outputValuesMap.get(NodePort.of(node, outputPort));
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
    private static int minimumSize(List<List<Object>> ll) {
        if (ll.size() == 0) return 0;
        int minSpan = Integer.MAX_VALUE;
        for (List values : ll) {
            minSpan = Math.min(minSpan, values.size());
        }
        checkState(minSpan < Integer.MAX_VALUE);
        return minSpan;
    }

    /**
     * Get the maximum amount of elements in the list of lists.
     *
     * @param ll The list of lists.
     * @return The maximum amount of elements.
     */
    private static int maximumSize(List<List<Object>> ll) {
        if (ll.size() == 0) return 0;
        int maxSpan = 0;
        for (List values : ll) {
            maxSpan = Math.max(maxSpan, values.size());
        }
        return maxSpan;
    }

    /**
     * Get the cross product of the amount of elements in the list of lists.
     *
     * @param ll The list of lists.
     * @return The cross product amount of elements.
     */
    private static int crossReferencedSize(List<List<Object>> ll) {
        if (ll.size() == 0) return 0;
        int amount = 1;
        // TODO Check for overflow?
        for (List values : ll) {
            amount *= values.size();
        }
        return amount;
    }


    public static abstract class ListOfListsIterator implements Iterator<List<? extends Object>> {

        protected final List<List<Object>> listOfLists;
        protected final int size;
        protected int index = 0;

        public ListOfListsIterator(List<List<Object>> listOfLists, int size) {
            this.listOfLists = listOfLists;
            this.size = size;
        }

        public boolean hasNext() {
            return index < size;
        }

        public int size() {
            return size;
        }

        public void remove() {
        }

    }

    public static final class ShortestListIterator extends ListOfListsIterator {

        public ShortestListIterator(List<List<Object>> listOfLists) {
            super(listOfLists, minimumSize(listOfLists));
        }

        public List<Object> next() {
            ImmutableList.Builder<Object> builder = ImmutableList.builder();
            for (List<Object> list : listOfLists) {
                builder.add(list.get(index));
            }
            index++;
            return builder.build();
        }
    }

    public static final class LongestListIterator extends ListOfListsIterator {

        public LongestListIterator(List<List<Object>> listOfLists) {
            super(listOfLists, maximumSize(listOfLists));
        }

        public List<Object> next() {
            ImmutableList.Builder<Object> builder = ImmutableList.builder();
            for (List<Object> list : listOfLists) {
                builder.add(getValueAtIndexWrapped(list, index));
            }
            index++;
            return builder.build();
        }
    }

    public static final class CrossReferenceListIterator extends ListOfListsIterator {

        private int[] indices;
        private int[] sizes;

        public CrossReferenceListIterator(List<List<Object>> listOfLists) {
            super(listOfLists, crossReferencedSize(listOfLists));
            indices = new int[listOfLists.size()];
            sizes = new int[listOfLists.size()];
            for (int i = 0; i < listOfLists.size(); i++) {
                sizes[i] = listOfLists.get(i).size();
            }
        }

        public List<Object> next() {
            ImmutableList.Builder<Object> builder = ImmutableList.builder();
            for (int i = 0; i < indices.length; i++) {
                List<Object> list = listOfLists.get(i);
                builder.add(list.get(indices[i]));
            }
            for (int i = indices.length - 1; i >= 0; i--) {
                if (indices[i] < sizes[i] - 1) {
                    indices[i] += 1;
                    break;
                } else {
                    indices[i] = 0;
                }
            }
            index++;
            return builder.build();
        }
    }


}
