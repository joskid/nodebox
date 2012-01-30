package nodebox.node;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import nodebox.graphics.Point;

import java.util.Collection;
import java.util.List;

import static com.google.common.base.Preconditions.*;

public final class Node {

    public static final Node ROOT = new Node();

    public static String path(String parentPath, Node node) {
        checkNotNull(node);
        return path(parentPath, node.getName());
    }

    public static String path(String parentPath, String nodeName) {
        checkNotNull(parentPath);
        checkNotNull(nodeName);
        checkArgument(parentPath.startsWith("/"), "Only absolute paths are supported.");
        if (parentPath.equals("/")) {
            return "/" + nodeName;
        } else {
            return Joiner.on("/").join(parentPath, nodeName);
        }
    }

    public static String MAP_STRATEGY = "map";
    public static String AS_IS_STRATEGY = "as-is";
    public static String FLATTEN_STRATEGY = "flatten";

    public enum Attribute {PROTOTYPE, NAME, DESCRIPTION, IMAGE, FUNCTION, LIST_STRATEGY, POSITION, INPUTS, OUTPUTS, CHILDREN, RENDERED_CHILD_NAME, CONNECTIONS}

    private final Node prototype;
    private final String name;
    private final String description;
    private final String image;
    private final String function;
    private final String listStrategy;
    private final Point position;
    private final ImmutableList<Port> inputs;
    private final ImmutableList<Port> outputs;
    private final ImmutableList<Node> children;
    private final String renderedChildName;
    private final ImmutableList<Connection> connections;

    //// Constructors ////

    /**
     * Constructor for the root node. This can only be called once.
     */
    private Node() {
        checkState(ROOT == null, "You cannot create more than one root node.");
        prototype = null;
        name = "_root";
        description = "";
        image = "";
        function = "core/zero";
        listStrategy = "map";
        position = Point.ZERO;
        inputs = ImmutableList.of();
        outputs = ImmutableList.of();
        children = ImmutableList.of();
        renderedChildName = "";
        connections = ImmutableList.of();
    }

    private void checkAllNotNull(Object... args) {
        for (Object o : args) {
            checkNotNull(o);
        }
    }

    private Node(Node prototype, String name, String description, String image, String function, String listStrategy,
                 Point position, ImmutableList<Port> inputs, ImmutableList<Port> outputs, ImmutableList<Node> children,
                 String renderedChildName, ImmutableList<Connection> connections) {
        checkAllNotNull(prototype, name, description, image, function, listStrategy,
                position, inputs, outputs, children,
                renderedChildName, connections);
        checkArgument(!name.equals("_root"), "The name _root is a reserved internal name.");
        this.prototype = prototype;
        this.name = name;
        this.description = description;
        this.image = image;
        this.function = function;
        this.listStrategy = listStrategy;
        this.position = position;
        this.inputs = inputs;
        this.outputs = outputs;
        this.children = children;
        this.renderedChildName = renderedChildName;
        this.connections = connections;
    }

    //// Getters ////

    public Node getPrototype() {
        return prototype;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getImage() {
        return image;
    }

    public String getFunction() {
        return function;
    }

    public String getListStrategy() {
        return listStrategy;
    }

    public boolean isListAware() {
        // HACK this is a quick way to port old code into the list strategy pattern.
        // The "as-is" strategy, where lists are passed as-is, could be called "list-aware", so
        // we check for that.
        return listStrategy.equals(AS_IS_STRATEGY);
    }

    public Point getPosition() {
        return position;
    }

    public Collection<Node> getChildren() {
        return children;
    }

    public Node getChild(String name) {
        checkNotNull(name, "Name cannot be null.");
        for (Node child : getChildren()) {
            if (child.getName().equals(name)) {
                return child;
            }
        }
        return null;
    }

    public boolean hasChild(String name) {
        checkNotNull(name, "Name cannot be null.");
        return getChild(name) != null;
    }

    public boolean hasChild(Node node) {
        checkNotNull(node, "Node cannot be null.");
        return children.contains(node);
    }

    public boolean isEmpty() {
        return children.isEmpty();
    }

    public List<Port> getInputs() {
        return inputs;
    }

    public Port getInput(String name) {
        checkNotNull(name, "Port name cannot be null.");
        for (Port p : getInputs()) {
            if (p.getName().equals(name)) {
                return p;
            }
        }
        return null;
    }

    public ImmutableList<Port> getInputsOfType(String type) {
        ImmutableList.Builder<Port> b = ImmutableList.builder();
        for (Port p : getInputs()) {
            if (p.getType().equals(type)) {
                b.add(p);
            }
        }
        return b.build();
    }

    public boolean hasInput(String name) {
        return getInput(name) != null;
    }

    public List<Port> getOutputs() {
        return outputs;
    }

    public Port getOutput(String name) {
        checkNotNull(name, "Port name cannot be null.");
        for (Port p : getOutputs()) {
            if (p.getName().equals(name)) {
                return p;
            }
        }
        return null;
    }

    public ImmutableList<Port> getOutputsOfType(String type) {
        ImmutableList.Builder<Port> b = ImmutableList.builder();
        for (Port p : getOutputs()) {
            if (p.getType().equals(type)) {
                b.add(p);
            }
        }
        return b.build();
    }

    public boolean hasOutput(String name) {
        return getOutput(name) != null;
    }

    /**
     * Get the name of the rendered child. This node is guaranteed to exist as a child on the network.
     * The rendered child name can be null, indicating no child node will be rendered.
     *
     * @return the name of the rendered child or null.
     */
    public String getRenderedChildName() {
        return renderedChildName;
    }

    /**
     * Get the rendered child Node.
     *
     * @return The rendered child node or null if none is set.
     */
    public Node getRenderedChild() {
        if (getRenderedChildName().isEmpty()) return null;
        Node renderedChild = getChild(getRenderedChildName());
        checkNotNull(renderedChild, "The child with name %s cannot be found. This is a bug in NodeBox.", getRenderedChildName());
        return renderedChild;
    }

    public List<Connection> getConnections() {
        return connections;
    }

    public Object getAttributeValue(Attribute attribute) {
        if (attribute == Attribute.PROTOTYPE) {
            return getPrototype();
        } else if (attribute == Attribute.NAME) {
            return getName();
        } else if (attribute == Attribute.DESCRIPTION) {
            return getDescription();
        } else if (attribute == Attribute.IMAGE) {
            return getImage();
        } else if (attribute == Attribute.FUNCTION) {
            return getFunction();
        } else if (attribute == Attribute.LIST_STRATEGY) {
            return getListStrategy();
        } else if (attribute == Attribute.POSITION) {
            return getPosition();
        } else if (attribute == Attribute.INPUTS) {
            return getInputs();
        } else if (attribute == Attribute.OUTPUTS) {
            return getOutputs();
        } else if (attribute == Attribute.CHILDREN) {
            return getChildren();
        } else if (attribute == Attribute.RENDERED_CHILD_NAME) {
            return getRenderedChildName();
        } else if (attribute == Attribute.CONNECTIONS) {
            return getConnections();
        } else {
            throw new AssertionError("Unknown node attribute " + attribute);
        }
    }

    //// Mutation functions ////

    /**
     * Create a new node with this node as the prototype.
     *
     * @return The new node.
     */
    public Node extend() {
        return newNodeWithAttribute(Attribute.PROTOTYPE, this);
    }

    /**
     * Create a new node with the given name.
     * <p/>
     * If you call this on ROOT, extend() is called implicitly.
     *
     * @param name The new node name.
     * @return A new Node.
     */
    public Node withName(String name) {
        return newNodeWithAttribute(Attribute.NAME, name);
    }

    /**
     * Create a new node with the given description.
     * <p/>
     * If you call this on ROOT, extend() is called implicitly.
     *
     * @param description new node description.
     * @return A new Node.
     */
    public Node withDescription(String description) {
        return newNodeWithAttribute(Attribute.DESCRIPTION, description);
    }

    /**
     * Create a new node with the given image.
     * <p/>
     * If you call this on ROOT, extend() is called implicitly.
     *
     * @param image new node image.
     * @return A new Node.
     */
    public Node withImage(String image) {
        return newNodeWithAttribute(Attribute.DESCRIPTION, image);
    }

    /**
     * Create a new node with the given function identifier.
     * <p/>
     * If you call this on ROOT, extend() is called implicitly.
     *
     * @param function The new function identifier.
     * @return A new Node.
     */
    public Node withFunction(String function) {
        return newNodeWithAttribute(Attribute.FUNCTION, function);
    }

    /**
     * Create a new node with the given list strategy.
     * <p/>
     * The list strategy is a higher-oder mapping function that defines how sequences are passed in the node function.
     * Possible values are:
     * <ul>
     * <li><strong>none</strong>: pass the sequence as-is.</li>
     * <li><strong>map</strong>: apply the function over each of the elements until any of the input sequences are
     * exhausted and return a new sequence.</li>
     * <li><strong>filter</strong>: apply the function as a predicate and return a new sequence with the elements that
     * pass the predicate.</li>
     * </ul>
     * <p/>
     * If you call this on ROOT, extend() is called implicitly.
     *
     * @param listStrategy The higher-order mapping function.
     * @return A new Node.
     */
    public Node withListStrategy(String listStrategy) {
        return newNodeWithAttribute(Attribute.LIST_STRATEGY, listStrategy);
    }

    /**
     * Create a new node with the given position.
     * <p/>
     * If you call this on ROOT, extend() is called implicitly.
     *
     * @param position The new position.
     * @return A new Node.
     */
    public Node withPosition(Point position) {
        return newNodeWithAttribute(Attribute.POSITION, position);
    }

    /**
     * Create a new node with the given input port added.
     * <p/>
     * If you call this on ROOT, extend() is called implicitly.
     *
     * @param port The port to add.
     * @return A new Node.
     */
    public Node withInputAdded(Port port) {
        checkNotNull(port, "Port cannot be null.");
        checkArgument(!hasInput(port.getName()), "An input port named %s already exists on node %s.", port.getName(), this);
        ImmutableList.Builder<Port> b = ImmutableList.builder();
        b.addAll(getInputs());
        b.add(port);
        return newNodeWithAttribute(Attribute.INPUTS, b.build());
    }

    /**
     * Create a new node with the given input port removed.
     * <p/>
     * If you call this on ROOT, extend() is called implicitly.
     *
     * @param portName The name of the port to remove.
     * @return A new Node.
     */
    public Node withInputRemoved(String portName) {
        Port portToRemove = getInput(portName);
        checkArgument(portToRemove != null, "Input port %s does not exist on node %s.", portName, this);

        ImmutableList.Builder<Port> b = ImmutableList.builder();
        for (Port port : getInputs()) {
            if (portToRemove != port)
                b.add(port);
        }
        return newNodeWithAttribute(Attribute.INPUTS, b.build());
    }

    /**
     * Create a new node with the given input port replaced.
     * <p/>
     * If you call this on ROOT, extend() is called implicitly.
     *
     * @param portName The name of the port to replace.
     * @param newPort  The new Port instance.
     * @return A new Node.
     */
    public Node withInputChanged(String portName, Port newPort) {
        Port oldPort = getInput(portName);
        checkNotNull(oldPort, "Input port %s does not exist on node %s.", portName, this);
        ImmutableList.Builder<Port> b = ImmutableList.builder();
        // Add all ports back in the correct order.
        for (Port port : getInputs()) {
            if (port == oldPort) {
                b.add(newPort);
            } else {
                b.add(port);
            }
        }
        return newNodeWithAttribute(Attribute.INPUTS, b.build());
    }

    /**
     * Create a new node with the given input port set to a new value.
     * Only standard port types (int, float, string, point) can have their value set.
     * <p/>
     * If you call this on ROOT, extend() is called implicitly.
     *
     * @param portName The name of the port to set.
     * @param value    The new Port value.
     * @return A new Node.
     */
    public Node withInputValue(String portName, Object value) {
        Port p = getInput(portName);
        checkNotNull(p, "Input port %s does not exist on node %s.", portName, this);
        p = p.withValue(value);
        return withInputChanged(portName, p);
    }

    /**
     * Create a new node with the given output port added.
     * <p/>
     * If you call this on ROOT, extend() is called implicitly.
     *
     * @param port The port to add.
     * @return A new Node.
     */
    public Node withOutputAdded(Port port) {
        checkNotNull(port, "Port cannot be null.");
        checkArgument(!hasOutput(port.getName()), "An output port named %s already exists on node %s.", port.getName(), this);
        ImmutableList.Builder<Port> b = ImmutableList.builder();
        b.addAll(getOutputs());
        b.add(port);
        return newNodeWithAttribute(Attribute.OUTPUTS, b.build());
    }

    /**
     * Create a new node with the given output port removed.
     * <p/>
     * If you call this on ROOT, extend() is called implicitly.
     *
     * @param portName The name of the port to remove.
     * @return A new Node.
     */
    public Node withOutputRemoved(String portName) {
        Port portToRemove = getOutput(portName);
        checkArgument(portToRemove != null, "Output port %s does not exist on node %s.", portName, this);

        ImmutableList.Builder<Port> b = ImmutableList.builder();
        for (Port port : getOutputs()) {
            if (portToRemove != port)
                b.add(port);
        }
        return newNodeWithAttribute(Attribute.OUTPUTS, b.build());
    }

    /**
     * Create a new node with the given output port replaced.
     * <p/>
     * If you call this on ROOT, extend() is called implicitly.
     *
     * @param portName The name of the port to replace.
     * @param newPort  The new Port instance.
     * @return A new Node.
     */
    public Node withOutputChanged(String portName, Port newPort) {
        Port oldPort = getOutput(portName);
        checkNotNull(oldPort, "Output port %s does not exist on node %s.", portName, this);
        ImmutableList.Builder<Port> b = ImmutableList.builder();
        // Add all ports back in the correct order.
        for (Port port : getOutputs()) {
            if (port == oldPort) {
                b.add(newPort);
            } else {
                b.add(port);
            }
        }
        return newNodeWithAttribute(Attribute.OUTPUTS, b.build());
    }

    /**
     * Create a new node with the given child added.
     * <p/>
     * If you call this on ROOT, extend() is called implicitly.
     *
     * @param node The child node to add.
     * @return A new Node.
     */
    public Node withChildAdded(Node node) {
        checkNotNull(node, "Node cannot be null.");
        checkArgument(!hasChild(node.getName()), "A node named %s is already a child of node %s.", node.getName(), this);
        ImmutableList.Builder<Node> b = ImmutableList.builder();
        b.addAll(getChildren());
        b.add(node);
        return newNodeWithAttribute(Attribute.CHILDREN, b.build());
    }

    /**
     * Create a new node with the given child removed.
     * <p/>
     * If you call this on ROOT, extend() is called implicitly.
     *
     * @param childName The name of the child node to remove.
     * @return A new Node.
     */
    public Node withChildRemoved(String childName) {
        Node childToRemove = getChild(childName);
        checkArgument(childToRemove != null, "Node %s is not a child of node %s.", childName, this);
        ImmutableList.Builder<Node> b = ImmutableList.builder();
        for (Node child : getChildren()) {
            if (child != childToRemove)
                b.add(child);
        }
        return newNodeWithAttribute(Attribute.CHILDREN, b.build());
    }

    /**
     * Create a new node with the child replaced by the given node.
     * <p/>
     * If you call this on ROOT, extend() is called implicitly.
     *
     * @param childName The name of the child node to replace.
     * @param newChild  The new child node.
     * @return A new Node.
     */
    public Node withChildReplaced(String childName, Node newChild) {
        Node childToReplace = getChild(childName);
        checkNotNull(newChild);
        checkArgument(newChild.getName().equals(childName), "New child %s does not have the same name as old child %s.", newChild, childName);
        checkArgument(childToReplace != null, "Node %s is not a child of node %s.", childName, this);
        ImmutableList.Builder<Node> b = ImmutableList.builder();
        for (Node child : getChildren()) {
            if (child != childToReplace) {
                b.add(child);
            } else {
                b.add(newChild);
            }
        }
        return newNodeWithAttribute(Attribute.CHILDREN, b.build());
    }

    /**
     * Create a new node with the given child set as rendered.
     * <p/>
     * The rendered node should exist as a child on this node.
     * If you don't want a child node to be rendered, set it to an empty string ("").
     * <p/>
     * If you call this on ROOT, extend() is called implicitly.
     *
     * @param name The new rendered child.
     * @return A new Node.
     */
    public Node withRenderedChildName(String name) {
        checkNotNull(name, "Rendered child name cannot be null.");
        checkArgument(name.isEmpty() || hasChild(name), "Node does not have a child named %s.", name);
        return newNodeWithAttribute(Attribute.RENDERED_CHILD_NAME, name);
    }

    /**
     * Create a new node with the given child set as rendered.
     * <p/>
     * The rendered node should exist as a child on this node.
     * If you don't want a child node to be rendered, set it to null.
     * <p/>
     * If you call this on ROOT, extend() is called implicitly.
     *
     * @param renderedChild The new rendered child or null if you don't want anything rendered.
     * @return A new Node.
     */
    public Node withRenderedChild(Node renderedChild) {
        return withRenderedChildName(renderedChild == null ? "" : renderedChild.getName());
    }

    /**
     * Create a new node that connects the given child nodes.
     *
     * @param outputNode The name of the output (upstream) Node.
     * @param outputPort The name of the output (upstream) Node.
     * @param inputNode  The name of the input (downstream) Node.
     * @param inputPort  The name of the input (downstream) Port.
     * @return A new Node.
     */
    public Node connect(String outputNode, String outputPort, String inputNode, String inputPort) {
        checkArgument(hasChild(outputNode), "Node %s does not have a child named %s.", this, outputNode);
        checkArgument(hasChild(inputNode), "Node %s does not have a child named %s.", this, inputNode);
        Node output = getChild(outputNode);
        Node input = getChild(inputNode);
        checkArgument(output.hasOutput(outputPort), "Node %s does not have an output port %s.", outputNode, outputPort);
        checkArgument(input.hasInput(inputPort), "Node %s does not have an input port %s.", inputNode, inputPort);
        Connection newConnection = new Connection(outputNode, outputPort, inputNode, inputPort);
        ImmutableList.Builder<Connection> b = ImmutableList.builder();
        for (Connection c : getConnections()) {
            if (c.getInputNode().equals(inputNode) && c.getInputPort().equals(inputPort)) {
                // There was already a connection, on this input port.
                // We "disconnect" it by not including it in the new list.
            } else {
                b.add(c);
            }
        }
        b.add(newConnection);
        return newNodeWithAttribute(Attribute.CONNECTIONS, b.build());
    }

    public Node withConnectionAdded(Connection connection) {
        return connect(connection.getOutputNode(), connection.getOutputPort(), connection.getInputNode(), connection.getInputPort());
    }

    public boolean isConnected(String node) {
        for (Connection c : getConnections()) {
            if (c.getInputNode().equals(node) || c.getOutputNode().equals(node))
                return true;
        }
        return false;
    }

    /**
     * Change an attribute on the node and return a new copy.
     * The prototype remains the same.
     * <p/>
     * We use this more complex function instead of having every withXXX method call the constructor, because
     * it allows us a to be more flexible when changing Node attributes.
     *
     * @param attribute The Node's attribute.
     * @param value     The value for the attribute. The type needs to match the internal type.
     * @return A copy of this node with the attribute changed.
     */
    @SuppressWarnings("unchecked")
    private Node newNodeWithAttribute(Attribute attribute, Object value) {
        Node prototype = this.prototype;
        String name = this.name;
        String description = this.description;
        String image = this.image;
        String function = this.function;
        String listStrategy = this.listStrategy;
        Point position = this.position;
        ImmutableList<Port> inputs = this.inputs;
        ImmutableList<Port> outputs = this.outputs;
        ImmutableList<Node> children = this.children;
        String renderedChildName = this.renderedChildName;
        ImmutableList<Connection> connections = this.connections;

        switch (attribute) {
            case PROTOTYPE:
                prototype = (Node) value;
                break;
            case NAME:
                name = (String) value;
                break;
            case DESCRIPTION:
                description = (String) value;
                break;
            case IMAGE:
                image = (String) value;
                break;
            case FUNCTION:
                function = (String) value;
                break;
            case LIST_STRATEGY:
                listStrategy = (String) value;
                break;
            case POSITION:
                position = (Point) value;
                break;
            case INPUTS:
                inputs = (ImmutableList<Port>) value;
                break;
            case OUTPUTS:
                outputs = (ImmutableList<Port>) value;
                break;
            case CHILDREN:
                children = (ImmutableList<Node>) value;
                break;
            case RENDERED_CHILD_NAME:
                renderedChildName = (String) value;
                break;
            case CONNECTIONS:
                connections = (ImmutableList<Connection>) value;
                break;
            default:
                throw new AssertionError("Unknown attribute " + attribute);
        }
        // If we're "changing" an attribute on ROOT, make the ROOT the prototype.
        if (prototype == null) {
            prototype = ROOT;

        }

        // The name of a node can never be "_root".
        if (name.equals("_root")) {
            name = "node";
        }

        return new Node(prototype, name, description, image, function, listStrategy, position,
                inputs, outputs, children, renderedChildName, connections);
    }

    //// Object overrides ////

    @Override
    public int hashCode() {
        return Objects.hashCode(prototype, name, description, image, function, listStrategy, position,
                inputs, outputs, children, renderedChildName, connections);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Node)) return false;
        final Node other = (Node) o;
        return Objects.equal(prototype, other.prototype)
                && Objects.equal(name, other.name)
                && Objects.equal(description, other.description)
                && Objects.equal(image, other.image)
                && Objects.equal(function, other.function)
                && Objects.equal(listStrategy, other.listStrategy)
                && Objects.equal(position, other.position)
                && Objects.equal(inputs, other.inputs)
                && Objects.equal(outputs, other.outputs)
                && Objects.equal(children, other.children)
                && Objects.equal(renderedChildName, other.renderedChildName)
                && Objects.equal(connections, other.connections);
    }

    @Override
    public String toString() {
        return String.format("<Node %s:%s>", getName(), getFunction());
    }

}
