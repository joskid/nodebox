package nodebox.node;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * A Macro is a node that can contain child nodes.
 * <p/>
 * On execution, a macro renders its children.
 */
public class Macro extends Node {

    public static Macro createRootMacro(NodeLibrary library) {
        return new Macro(library);
    }

    private static final Pattern NUMBER_AT_THE_END = Pattern.compile("^(.*?)(\\d*)$");

    private ImmutableMap<String, Node> children = ImmutableMap.of();
    private ImmutableSet<Connection> connections = ImmutableSet.of();

    private Macro(NodeLibrary library) {
        super(library);
    }

    public Macro(Macro parent) {
        super(parent);
    }

    /**
     * Create a child node from the given node class.
     * <p/>
     * The node name will be automatically generated and is guaranteed to be unique.
     *
     * @param childClass the node class for this child
     * @return the new child node
     */
    public Node createChild(Class<? extends Node> childClass) {
        return createChild(childClass, null);
    }

    /**
     * Create a child node under this node from the given node class.
     *
     * @param childClass the node class for this child.
     * @param name       the name of the new node.
     * @return the new child node
     */
    public Node createChild(Class childClass, String name) {
        checkNotNull(childClass);

        // Create the child
        Node child;
        try {
            Constructor c = childClass.getConstructor(Macro.class);
            child = (Node) c.newInstance(this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Set the child name
        if (name != null) {
            child.setName(name);
        }

        return child;
    }

    /**
     * Add the child to the collection.
     * <p/>
     * This method is called from the node constructor, and cannot be used to move a child to a different parent.
     *
     * @param child the child node
     */
    protected void addChild(Node child) {
        checkState(child.getParent() == this, "The given child should have its parent already set to this macro.");
        children = ImmutableMap.<String, Node>builder()
                .putAll(children)
                .put(child.getName(), child)
                .build();
        getLibrary().fireChildAdded(this, child);
    }

    /**
     * Remove a child from the collection.
     * <p/>
     * All connections to and from the child are removed.
     *
     * @param child the child to remove
     * @return true if the child was removed.
     */
    public boolean removeChild(Node child) {
        checkNotNull(child);
        checkState(children.containsValue(child), "The given node is not a child of this macro.");

        disconnect(child);

        ImmutableMap.Builder<String, Node> b = ImmutableMap.builder();
        for (Node c : children.values()) {
            if (c != child) {
                b.put(c.getName(), c);
            }
        }
        children = b.build();

        getLibrary().fireChildRemoved(this, child);
        return true;
    }

    void _renameChild(Node child, String newName) {
        checkNotNull(child);
        checkState(child.getParent() == this);
        checkNotNull(newName);
        if (hasChild(newName)) {
            throw new InvalidNameException(child, newName, "This macro already contains a node called '" + newName + "'");
        }
        String oldName = child.getName();
        ImmutableMap.Builder<String, Node> b = ImmutableMap.builder();
        for (Node c : children.values()) {
            if (c.getName().equals(oldName)) {
                b.put(newName, c);
            } else {
                b.put(c.getName(), c);
            }
        }
        children = b.build();
    }

    public boolean hasChild(String nodeName) {
        return children.containsKey(nodeName);
    }

    public boolean hasChild(Node node) {
        return children.containsValue(node);
    }

    public Node getChild(String nodeName) throws ChildNotFoundException {
        checkNotNull(nodeName);
        Node n = children.get(nodeName);
        if (n == null) {
            throw new ChildNotFoundException(this, nodeName);
        } else {
            return n;
        }
    }

    public Collection<Node> getChildren() {
        return children.values();
    }

    public String uniqueName(String prefix) {
        Matcher m = NUMBER_AT_THE_END.matcher(prefix);
        m.find();
        String namePrefix = m.group(1);
        String number = m.group(2);
        int counter;
        if (number.length() > 0) {
            counter = Integer.parseInt(number);
        } else {
            counter = 1;
        }
        while (true) {
            String suggestedName = namePrefix + counter;
            if (!hasChild(suggestedName)) {
                // We don't use rename here, since it assumes the node will be in
                // this network.
                return suggestedName;
            }
            ++counter;
        }
    }

    //// Connections ////

    /**
     * Connect the downstream input port to the upstream output port.
     * <p/>
     * Both the output and input ports need to be on child nodes of this node.
     * <p/>
     * If the input port was already connected, and its cardinality is single, the connection is broken.
     *
     * @param input  the downstream port
     * @param output the upstream port
     * @return the connection object.
     */
    public Connection connect(Port input, Port output) {
        checkNotNull(input);
        checkNotNull(output);
        Node inputNode = input.getNode();
        Node outputNode = output.getNode();
        checkState(hasChild(inputNode), "The input node is not a child of this macro.");
        checkState(hasChild(outputNode), "The output node is not a child of this macro.");
        checkState(inputNode != outputNode, "The output and input nodes are the same.");
        checkState(input.isInputPort(), "The first argument is not an input port.");
        checkState(output.isOutputPort(), "The second argument is not an output port.");
        checkState(input.canConnectTo(output), "The input cannot be connected to the output: the data types are incompatible.");
        disconnect(input);
        Connection c = new Connection(input, output);
        ImmutableSet<Connection> newConnections = ImmutableSet.<Connection>builder().addAll(connections).add(c).build();
        CycleDetector detector = new CycleDetector(newConnections);
        checkState(!detector.hasCycles(), "Creating this connection would cause a cyclic dependency.");
        connections = newConnections;
        getLibrary().fireConnectionAdded(this, c);
        return c;
    }

    /**
     * Remove all connections to and from the given child node.
     *
     * @param child the child node to disconnect
     * @return true if connections were removed.
     */
    public boolean disconnect(Node child) {
        checkNotNull(child);
        checkState(hasChild(child), "The given node is not a child of this macro.");
        LinkedList<Connection> removedConnections = new LinkedList<Connection>();
        ImmutableSet.Builder<Connection> builder = ImmutableSet.builder();
        for (Connection c : connections) {
            if (c.getInputNode() == child || c.getOutputNode() == child) {
                removedConnections.add(c);
            } else {
                builder.add(c);
            }
        }
        connections = builder.build();
        for (Connection c : removedConnections) {
            getLibrary().fireConnectionRemoved(this, c);
        }
        return !removedConnections.isEmpty();
    }

    /**
     * Removes all connections on the given input child port.
     *
     * @param input the child input port on a child node of this macro.
     * @return true if a connection was removed.
     */
    public boolean disconnect(Port input) {
        checkNotNull(input);
        checkState(input.isInputPort(), "The given port is not an input port.");
        Node node = input.getNode();
        checkState(hasChild(node), "The given node is not a child of this macro.");
        Connection removed = null;
        ImmutableSet.Builder<Connection> builder = ImmutableSet.builder();
        for (Connection c : connections) {
            if (c.getInput() == input || c.getOutput() == input) {
                removed = c;
                if (input.isInputPort())
                    input.revertToDefault();
            } else {
                builder.add(c);
            }
        }
        connections = builder.build();
        if (removed != null) {
            getLibrary().fireConnectionRemoved(this, removed);
            return true;
        }
        return false;
    }

    /**
     * Get a set of all connection objects.
     *
     * @return a set of Connections objects. This list can safely be modified.
     */
    public Set<Connection> getConnections() {
        return connections;
    }

    /**
     * Get a list of all connections where the child node is the input.
     *
     * @param child the child node
     * @return an iterable of connections
     */
    public Iterable<Connection> getInputConnections(final Node child) {
        return Iterables.filter(connections, new Predicate<Connection>() {
            public boolean apply(@Nullable Connection connection) {
                return connection != null && connection.getInputNode() == child;
            }
        });
    }

    /**
     * Checks if the child node is connected.
     * <p/>
     * This method checks both input and output connections.
     *
     * @param child the child node to check.
     * @return true if this node is connected.
     */
    public boolean isConnected(Node child) {
        checkNotNull(child);
        checkState(hasChild(child), "The given node is not a child of this macro.");
        for (Connection c : connections) {
            if (c.getInputNode() == child || c.getOutputNode() == child) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if the given child port is connected.
     *
     * @param port a port on a child of this node.
     * @return true if this port is connected.
     */
    public boolean isConnected(Port port) {
        checkNotNull(port);
        Node node = port.getNode();
        checkState(hasChild(node), "The given node is not a child of this macro.");
        for (Connection c : connections) {
            if (c.getInput() == port || c.getOutput() == port) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if the given input is connected to the given output.
     *
     * @param input  the input port of a child of this node.
     * @param output the output port of a child of this node.
     * @return true if the input is connected to the output.
     */
    public boolean isConnectedTo(Port input, Port output) {
        checkNotNull(input);
        checkNotNull(output);
        checkState(input.getNode().getParent() == this, "The input node is not a child of this macro.");
        checkState(output.getNode().getParent() == this, "The output node is not a child of this macro.");
        checkState(input.isInputPort(), "First argument is not an input port.");
        checkState(output.isOutputPort(), "Second argument is not an output port.");
        for (Connection c : connections) {
            if (c.getInput() == input && c.getOutput() == output) {
                return true;
            }
        }
        return false;
    }

    //// Cooking ////


    /**
     * Perform the actual function of the node.
     * <p/>
     * For a macro node, this means rendering all consumers, making sure their dependencies are executed first.
     * Every node in the macro is only executed once. The processing context keeps a record of which nodes
     * have executed.
     * <p/>
     * If an error occurs during execution, this method will throw an ExecuteException with the node set to itself,
     * and the cause of the child exception.
     *
     * @param context the processing context
     * @throws RuntimeException whenever an error occurs during executing.
     */
    @Override
    public void cook(CookContext context) throws RuntimeException {
        try {
            for (Node child : getChildren()) {
                if (child.getMode() == Mode.CONSUMER) {
                    updateChildDependencies(child, context);
                    CookContext childContext = new CookContext(context);
                    child.execute(childContext);
                }
            }
        } catch (ExecuteException e) {
            throw new ExecuteException(this, "Error while updating child node " + e.getNode().getName(), e);
        }
    }

    /**
     * Update the dependencies for a child, recursively.
     *
     * @param child   the child node to update
     * @param context the processing context
     * @throws ExecuteException if executing a dependency failed
     */
    private void updateChildDependencies(Node child, CookContext context) throws ExecuteException {
        for (Connection c : getInputConnections(child)) {
            Node n = c.getOutputNode();
            if (!context.hasExecuted(n)) {
                context.addToExecutedNodes(n);
                updateChildDependencies(n, context);
                // The next line can throw an execute exception.
                n.execute(context);
            }
            c.getInput().setValue(c.getOutput().getValue());
        }
    }

    /**
     * Colors are used for the cycle detector
     */
    private enum Color {
        WHITE, GRAY, BLACK
    }

    /**
     * Check for cyclic connections.
     */
    private class CycleDetector {

        private ImmutableSet<Connection> connections;

        /**
         * Mark which nodes we have encountered.
         */
        private Map<Node, Color> marks;

        private CycleDetector(ImmutableSet<Connection> connections) {
            this.connections = connections;
        }

        private boolean hasCycles() {
            marks = new HashMap<Node, Color>(connections.size());
            for (Connection c : connections) {
                marks.put(c.getOutputNode(), Color.WHITE);
            }
            for (Connection c : connections) {
                if (marks.get(c.getOutputNode()) == Color.WHITE) {
                    if (visit(c.getOutputNode())) {
                        return true;
                    }
                }
            }

            return false;
        }

        private boolean visit(Node node) {
            marks.put(node, Color.GRAY);
            for (Connection c : connections) {
                Node outputNode = c.getOutputNode();
                // Only use the ones I'm the input for (downstream connections for this node).
                if (c.getInputNode() != node) continue;
                if (!marks.containsKey(outputNode)) continue;
                if (marks.get(outputNode) == Color.GRAY) {
                    return true;
                } else if (marks.get(outputNode) == Color.WHITE) {
                    if (visit(outputNode)) {
                        return true;
                    }
                } else {
                    // Visiting black vertices is okay.
                }
            }
            marks.put(node, Color.BLACK);
            return false;
        }

    }

}
