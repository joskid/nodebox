package nodebox.node;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import nodebox.graphics.Point;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.*;

public final class Node {

    public static final Node ROOT = new Node();
    public static final String KEY_NAME = "name";
    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_FUNCTION = "function";
    public static final String KEY_OUTPUT_TYPE = "outputType";
    public static final String KEY_POSITION = "position";
    public static final String KEY_PORTS = "ports";
    public static final String KEY_CHILDREN = "children";
    public static final String KEY_RENDERED_CHILD_NAME = "renderedChildName";
    public static final String KEY_CONNECTIONS = "connections";

    private enum Attribute {PROTOTYPE, NAME, DESCRIPTION, IMAGE, FUNCTION, OUTPUT_TYPE, POSITION, PORTS, CHILDREN, RENDERED_CHILD_NAME, CONNECTIONS}

    private final Node prototype;
    private final PrototypeMap properties;

    //// Constructors ////

    /**
     * Constructor for the root node. This can only be called once.
     */
    private Node() {
        checkState(ROOT == null, "You cannot create more than one root node.");
        prototype = null;
        HashMap<String, Object> m = new HashMap<String, Object>();
        m.put(KEY_NAME, "root");
        m.put(KEY_DESCRIPTION, "");
        m.put(KEY_IMAGE, "");
        m.put(KEY_FUNCTION, "core/zero");
        m.put(KEY_OUTPUT_TYPE, Port.TYPE_INT);
        m.put(KEY_POSITION, Point.ZERO);
        m.put(KEY_PORTS, ImmutableList.<Port>of());
        m.put(KEY_CHILDREN, ImmutableMap.<String, Node>of());
        m.put(KEY_RENDERED_CHILD_NAME, "");
        m.put(KEY_CONNECTIONS, ImmutableList.<Connection>of());
        this.properties = new PrototypeMap(null, m);
    }

    /**
     * Constructor for a Node.
     * <p/>
     * This is private because it takes a map containing possibly mutable property values.
     * Use Node.ROOT.withXXX() to create a new Node.
     *
     * @param prototype  The new Node's prototype. This cannot be null.
     * @param properties The list of new properties.
     */
    private Node(Node prototype, ImmutableMap<String, Object> properties) {
        checkNotNull(prototype, "The prototype cannot be null. Use Node.ROOT.withXXX()");
        this.prototype = prototype;
        this.properties = new PrototypeMap(prototype.properties, properties);
    }

    //// Getters ////

    public Node getPrototype() {
        return prototype;
    }

    public String getName() {
        return (String) getProperty(KEY_NAME);
    }

    public String getDescription() {
        return (String) getProperty(KEY_DESCRIPTION);
    }

    public String getImage() {
        return (String) getProperty(KEY_IMAGE);
    }

    public String getFunction() {
        return (String) getProperty(KEY_FUNCTION);
    }

    public String getOutputType() {
        return (String) getProperty(KEY_OUTPUT_TYPE);
    }

    public Point getPosition() {
        return (Point) getProperty(KEY_POSITION);
    }

    public Collection<Node> getChildren() {
        return getChildMap().values();
    }

    public Node getChild(String name) {
        checkNotNull(name, "Name cannot be null.");
        return getChildMap().get(name);
    }

    public boolean hasChild(String name) {
        checkNotNull(name, "Name cannot be null.");
        return getChildMap().containsKey(name);
    }

    @SuppressWarnings("unchecked")
    private ImmutableMap<String, Node> getChildMap() {
        return (ImmutableMap<String, Node>) getProperty(KEY_CHILDREN);
    }

    @SuppressWarnings("unchecked")
    public List<Port> getPorts() {
        return (List<Port>) getProperty(KEY_PORTS);
    }

    public Port getPort(String name) {
        checkNotNull(name, "Port name cannot be null.");
        for (Port p : getPorts()) {
            if (p.getName().equals(name)) {
                return p;
            }
        }
        return null;
    }

    public ImmutableList<Port> getPortsOfType(String type) {
        ImmutableList.Builder<Port> b = ImmutableList.builder();
        for (Port p : getPorts()) {
            if (p.getType().equals(type)) {
                b.add(p);
            }
        }
        return b.build();
    }

    public boolean hasPort(String name) {
        return getPort(name) != null;
    }

    /**
     * Get the name of the rendered child. This node is guaranteed to exist as a child on the network.
     * The rendered child name can be null, indicating no child node will be rendered.
     *
     * @return the name of the rendered child or null.
     */
    public String getRenderedChildName() {
        return (String) getProperty(KEY_RENDERED_CHILD_NAME);
    }

    /**
     * Get the rendered child Node.
     *
     * @return The rendered child node or null if none is set.
     */
    public Node getRenderedChild() {
        if (getRenderedChildName().isEmpty()) return null;
        Node renderedChild = getChildMap().get(getRenderedChildName());
        checkNotNull(renderedChild, "The child with name %s cannot be found. This is a bug in NodeBox.");
        return renderedChild;
    }

    @SuppressWarnings("unchecked")
    public List<Connection> getConnections() {
        return (List<Connection>) getProperty(KEY_CONNECTIONS);
    }

    private Object getProperty(String key) {
        return properties.getProperty(key);
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
     * Create a new node with the given output type.
     * <p/>
     * If you call this on ROOT, extend() is called implicitly.
     *
     * @param outputType The new output type.
     * @return A new Node.
     */
    public Node withOutputType(String outputType) {
        return newNodeWithAttribute(Attribute.OUTPUT_TYPE, outputType);
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
     * Create a new node with the given port added.
     * <p/>
     * If you call this on ROOT, extend() is called implicitly.
     *
     * @param port The port to add.
     * @return A new Node.
     */
    public Node withPortAdded(Port port) {
        checkNotNull(port, "Port cannot be null.");
        checkArgument(!hasPort(port.getName()), "A port named %s already exists on node %s.", port.getName(), this);
        ImmutableList.Builder<Port> b = ImmutableList.builder();
        b.addAll(getPorts());
        b.add(port);
        return newNodeWithAttribute(Attribute.PORTS, b.build());
    }

    /**
     * Create a new node with the given port removed.
     * <p/>
     * If you call this on ROOT, extend() is called implicitly.
     *
     * @param portName The name of the port to remove.
     * @return A new Node.
     */
    public Node withPortRemoved(String portName) {
        Port portToRemove = getPort(portName);
        checkArgument(portToRemove != null, "Port %s does not exist on node %s.", portName, this);

        ImmutableList.Builder<Port> b = ImmutableList.builder();
        for (Port port : getPorts()) {
            if (portToRemove != port)
                b.add(port);
        }
        return newNodeWithAttribute(Attribute.PORTS, b.build());
    }

    /**
     * Create a new node with the named port replaced.
     * <p/>
     * If you call this on ROOT, extend() is called implicitly.
     *
     * @param portName The name of the port to replace.
     * @param newPort  The new Port instance.
     * @return A new Node.
     */
    public Node withPortChanged(String portName, Port newPort) {
        Port oldPort = getPort(portName);
        checkNotNull(oldPort, "Port %s does not exist on node %s.", portName, this);
        ImmutableList.Builder<Port> b = ImmutableList.builder();
        // Skip over the old Port.
        for (Port port : getPorts()) {
            if (port != oldPort) {
                b.add(port);
            }
        }
        b.add(newPort);
        return newNodeWithAttribute(Attribute.PORTS, b.build());
    }

    /**
     * Create a new node with the named port set to a new value.
     * Only standard port types (int, float, string, point) can have their value set.
     * <p/>
     * If you call this on ROOT, extend() is called implicitly.
     *
     * @param portName The name of the port to set.
     * @param value    The new Port value.
     * @return A new Node.
     */
    public Node withPortValue(String portName, Object value) {
        Port p = getPort(portName);
        checkArgument(p != null, "Port %s does not exist on node %s.", portName, this);
        p = p.withValue(value);
        return withPortChanged(portName, p);
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
        ImmutableMap.Builder<String, Node> b = ImmutableMap.builder();
        b.putAll(getChildMap());
        b.put(node.getName(), node);
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
        ImmutableMap.Builder<String, Node> b = ImmutableMap.builder();
        for (Node child : getChildMap().values()) {
            if (child != childToRemove)
                b.put(child.getName(), child);
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
     * Change an attribute on the node and return a new copy.
     * The prototype remains the same.
     *
     * @param attribute The Node's attribute.
     * @param value     The value for the attribute. The type needs to match the internal type.
     * @return A copy of this node with the attribute changed.
     */
    private Node newNodeWithAttribute(Attribute attribute, Object value) {
        switch (attribute) {
            case PROTOTYPE:
                checkArgument(value instanceof Node, "Setting the prototype requires a Node, not %s.", value);
                return new Node((Node) value, properties.getProperties());
            case NAME:
                checkArgument(value instanceof String, "Changing the name requires a String, not %s.", value);
                return newNode(KEY_NAME, value);
            case DESCRIPTION:
                checkArgument(value instanceof String, "Changing the description requires a String, not %s.", value);
                return newNode(KEY_DESCRIPTION, value);
            case IMAGE:
                checkArgument(value instanceof String, "Changing the image requires a String, not %s.", value);
                return newNode(KEY_IMAGE, value);
            case FUNCTION:
                checkArgument(value instanceof String, "Changing the function name requires a String, not %s.", value);
                return newNode(KEY_FUNCTION, value);
            case OUTPUT_TYPE:
                checkArgument(value instanceof String, "Changing the outputType requires a String, not %s.", value);
                return newNode(KEY_OUTPUT_TYPE, value);
            case POSITION:
                checkArgument(value instanceof Point, "Changing the position requires a Point, not %s.", value);
                return newNode(KEY_POSITION, value);
            case PORTS:
                checkArgument(value instanceof ImmutableList, "Changing the ports requires an ImmutableList, not %s.", value);
                return newNode(KEY_PORTS, value);
            case CHILDREN:
                checkArgument(value instanceof ImmutableMap, "Changing the children requires an ImmutableMap, not %s.", value);
                return newNode(KEY_CHILDREN, value);
            case RENDERED_CHILD_NAME:
                checkArgument(value instanceof String, "Changing the rendered child name requires a String, not %s.", value);
                return newNode(KEY_RENDERED_CHILD_NAME, value);
            default:
                throw new AssertionError("Unknown attribute " + attribute);
        }
    }

    /**
     * Create a new node with the given key and value.
     * <p/>
     * This method doesn't change the prototype! If you want to extend from another node, use extend().
     * The only exception is if the prototype is null, then it is set to ROOT.
     *
     * @param key   The key to set.
     * @param value The value to set.
     * @return A new Node.
     */
    private Node newNode(String key, Object value) {
        // There can only be one ROOT. If creating a new node from the ROOT, the prototype automatically becomes ROOT.
        // Otherwise, the prototype is kept unchanged.
        Node prototype = getPrototype() != null ? getPrototype() : ROOT;
        // This a verbose way of "adding" a property to a PrototypeMap, then constructing a new Node.
        PrototypeMap m = properties.withProperty(key, value);
        return new Node(prototype, m.getProperties());
    }

    /**
     * Build a map out of a sequence of nodes keyed by their names.
     * <p/>
     * The function checks if all node names in the sequence are unique.
     *
     * @param nodes An Iterable of nodes.
     * @return a map of nodes. Each name is guaranteed to be unique.
     */
    private static ImmutableMap<String, Node> buildNodeMap(Iterable<Node> nodes) {
        checkNotNull(nodes, "Given nodes cannot be null.");
        Map<String, Node> transientNodeMap = new HashMap<String, Node>();
        for (Node node : nodes) {
            checkState(!transientNodeMap.containsKey(node.getName()),
                    "In the given list of nodes, %s is not unique.", node.getName());
            transientNodeMap.put(node.getName(), node);
        }
        return ImmutableMap.copyOf(transientNodeMap);
    }

}
