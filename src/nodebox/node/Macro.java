package nodebox.node;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
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

    private static final Pattern NUMBER_AT_THE_END = Pattern.compile("^(.*?)(\\d*)$");

    private ImmutableMap<String, Node> children = ImmutableMap.of();
    private ImmutableSet<Connection> connections = ImmutableSet.of();

    public Macro(NodeLibrary library) {
        super(library);
    }

    /**
     * Create a child node from the given node class.
     * <p/>
     * The node name will be automatically generated and is guaranteed to be unique.
     *
     * @param childClass the node class for this child
     * @return the new child node
     */
    public Node createChild(Class childClass) {
        return createChild(childClass, uniqueName(childClass.getSimpleName()));
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
        checkNotNull(name);
        try {
            Constructor c = childClass.getConstructor(NodeLibrary.class);
            Node child = (Node) c.newInstance(getLibrary());
            child.setName(name);
            addChild(child);
            return child;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void addChild(Node child) {
        checkNotNull(child);
        if (child.getParent() == this) return;
        if (child.getParent() != null)
            child.getParent().removeChild(child);
        children = ImmutableMap.<String, Node>builder()
                .putAll(children)
                .put(child.getName(), child)
                .build();
        child.setParent(this);
        getLibrary().fireChildAdded(this, child);
//        // This method is called indirectly by newInstance.
//        // newInstance has set the parent, but has not added it to
//        // the library yet. Therefore, we cannot do this.parent == parent,
//        // but need to check parent.contains()
//        if (parent != null && parent.contains(this)) return;
//        if (parent != null && parent.hasChild(name))
//            throw new InvalidNameException(this, name, "There is already a node named \"" + name + "\" in " + parent);
//        // Since this node will reside under a different parent, it can no longer maintain connections within
//        // the previous parent. Break all connections. We need to do this before the parent changes.
//        disconnect();
//        if (this.parent != null)
//            this.parent.remove(this);
//        this.parent = parent;
//        if (parent != null) {
//            parent.children.put(name, this);
//            for (Port p : ports.values()) {
//                if (parent.childGraph == null)
//                    parent.childGraph = new DependencyGraph<Port, Connection>();
//                parent.childGraph.addDependency(p, outputPort);
//            }
//            // We're on the child node, so we need to fire the child added event
//            // on the parent with this child as the argument.
//            getLibrary().fireChildAdded(parent, this);
//        }
    }

    public boolean removeChild(Node child) {
        return false;
//        assert (node != null);
//        if (!contains(node))
//            return false;
//        node.markDirty();
//        node.disconnect();
//        node.parent = null;
//        children.remove(node.getName());
//        if (node == renderedChild) {
//            setRenderedChild(null);
//        }
//        getLibrary().fireChildRemoved(this, node);
//        return true;

    }

    public boolean isLeaf() {
        return isEmpty();
    }

    public boolean isEmpty() {
        return children.isEmpty();
    }

    public void renameChild(String oldName, String newName) {

    }

    public boolean hasChild(String nodeName) {
        return children.containsKey(nodeName);
    }

    public boolean hasChild(Node node) {
        return children.containsValue(node);
    }

    public Node getChild(String nodeName) {
        return children.get(nodeName);
    }

    public Node getExportedChild(String nodeName) {
        Node child = getChild(nodeName);
        if (child == null) return null;
        if (child.isExported()) {
            return child;
        } else {
            return null;
        }
    }

    public Node getChildAt(int index) {
        Collection c = children.values();
        if (index >= c.size()) return null;
        return (Node) c.toArray()[index];
    }

    public int getChildCount() {
        return children.size();
    }

    public boolean hasChildren() {
        return !children.isEmpty();
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
        connections = ImmutableSet.<Connection>builder().addAll(connections).add(c).build();
        getLibrary().fireNodePortsChangedEvent(this);
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
        boolean removedSomething = false;
        ImmutableSet.Builder<Connection> builder = ImmutableSet.builder();
        for (Connection c : connections) {
            if (c.getInputNode() == child || c.getOutputNode() == child) {
                removedSomething = true;
            } else {
                builder.add(c);
            }
        }
        connections = builder.build();
        return removedSomething;
    }

    /**
     * Removes all connections on the given (input or output) child port.
     *
     * @param port the (input or output) port on this node.
     * @return true if a connection was removed.
     */
    public boolean disconnect(Port port) {
        checkNotNull(port);
        Node node = port.getNode();
        checkState(hasChild(node), "The given node is not a child of this macro.");
        boolean removedSomething = false;
        ImmutableSet.Builder<Connection> builder = ImmutableSet.builder();
        for (Connection c : connections) {
            if (c.getInput() == port || c.getOutput() == port) {
                removedSomething = true;
                if (port.isInputPort())
                    port.revertToDefault();
            } else {
                builder.add(c);
            }
        }
        connections = builder.build();
        return removedSomething;
    }

    /**
     * Remove the given connection.
     * @param conn the connection to remove
     * @return true if the connection was removed.
     */
    public boolean disconnect(Connection conn) {
        checkNotNull(conn);
        boolean removedSomething = false;
        ImmutableSet.Builder<Connection> builder = ImmutableSet.builder();
        for (Connection c : connections) {
            if (c == conn) {
                removedSomething = true;
            } else {
                builder.add(c);
            }
        }
        connections = builder.build();
        return removedSomething;
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
     * @param input the input port of a child of this node.
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

}
