/*
 * This file is part of NodeBox.
 *
 * Copyright (C) 2008 Frederik De Bleser (frederik@pandora.be)
 *
 * NodeBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NodeBox is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with NodeBox. If not, see <http://www.gnu.org/licenses/>.
 */
package nodebox.node;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMap;
import nodebox.graphics.Color;
import nodebox.graphics.Point;
import nodebox.handle.Handle;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * A Node is a building block in a network and encapsulates specific functionality.
 * <p/>
 * The operation of the Node is specified through its parameters. The data that flows
 * through the node passes through ports.
 * <p/>
 * Nodes can be nested using parent/child relationships. Then, you can connect them together.
 * This allows for many processing possibilities, where you can connect several nodes together forming
 * very complicated networks. Networks, in turn, can be rigged up to form sort of black-boxes, with some
 * input parameters and an output port, so they form a Node themselves, that can be used to form
 * even more complicated networks, etc.
 * <p/>
 * Central in this concept is the directed acyclic graph, or DAG. This is a graph where all the edges
 * are directed, and no cycles can be formed, so you do not run into recursive loops. The vertexes of
 * the graph are the nodes, and the edges are the connections between them.
 * <p/>
 * One of the vertexes in the graph is set as the rendered node, and from there on, the processing starts,
 * working its way upwards in the network, processing other nodes (and their inputs) as they come along.
 */
public class Node {

    private static final Pattern NODE_NAME_PATTERN = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]{0,29}$");
    private static final Pattern DOUBLE_UNDERSCORE_PATTERN = Pattern.compile("^__.*$");
    private static final Pattern RESERVED_WORD_PATTERN = Pattern.compile("^(node|network|root|context)$");

    public enum Mode {
        PRODUCER, FILTER, CONSUMER
    }

    private final NodeLibrary library;
    private final Macro parent;
    private Mode mode = Mode.CONSUMER;
    private String name;
    private double x, y;
    private boolean exported = false;
    private NodeAttributes attributes = NodeAttributes.DEFAULT;
    private ImmutableMap<String, Port> ports = ImmutableMap.of();
    private Throwable error;

    //// Constructors ////

    protected Node(NodeLibrary library) {
        checkNotNull(library);
        checkState(library.getRootMacro() == null, "This method can only be used to create the root macro.");
        this.library = library;
        this.parent = null;
        this.name = "root";
    }

    public Node(Macro parent) {
        checkNotNull(parent);
        checkNotNull(parent.getLibrary());
        this.parent = parent;
        this.library = parent.getLibrary();
        this.name = this.parent.uniqueName(getTypeName());
        this.parent.addChild(this);
    }

    //// Library ////

    public NodeLibrary getLibrary() {
        return library;
    }

    //// Mode ////

    /**
     * Get the node's operational mode.
     * <p/>
     * The mode defines how a node is processed: within a macro, all consumer nodes are rendered in order.
     *
     * @return the operational mode.
     */
    public Mode getMode() {
        return mode;
    }

    /**
     * Set the operation mode.
     *
     * @param mode the operational mode.
     */
    public void setMode(Mode mode) {
        this.mode = mode;
    }

    //// Naming /////

    /**
     * Return the node's type name. This name can be used as the prefix for auto-numbering nodes.
     * It is based on the node's class name: if a node class is "ImportSVG", the type name is "importSVG".
     *
     * @return the node's type name
     */
    public String getTypeName() {
        String className = getClass().getSimpleName();
        return className.substring(0, 1).toLowerCase() + className.substring(1);
    }

    /**
     * Get the name of the node.
     *
     * @return the node name
     */
    public String getName() {
        return name;
    }

    /**
     * Set the node name.
     * <p/>
     * The name has some limitations: it cannot start with a number or a double underscore, or be a reserved word
     * (node, network, root, context).
     * <p/>
     * The name needs to be unique within its parent macro.
     *
     * @param name the new name
     * @throws InvalidNameException if the name is of the wrong format or not unique
     */
    public void setName(String name) throws InvalidNameException {
        if (name == null) throw new InvalidNameException(this, "", "Name cannot be null.");
        checkNotNull(this.name);
        if (this.name.equals(name)) return;
        validateName(name);
        this.name = name;
        if (parent != null)
            parent._renameChild(this, name);
        getLibrary().fireNodeAttributesChanged(this);
    }

    //// Attributes ////

    /**
     * Get the node's description.
     *
     * @return the node description
     */
    public String getDescription() {
        return attributes.getDescription();
    }

    /**
     * Get the image used to represent the node.
     *
     * @return the node image
     */
    public String getImage() {
        return attributes.getImage();
    }

    /**
     * Get the node attributes. This contains all the metadata about the node, such as its image and description.
     *
     * @return the node attributes
     */
    public NodeAttributes getAttributes() {
        return attributes;
    }

    /**
     * Set the node attributes (metadata).
     *
     * @param attributes the node attributes
     */
    public void setAttributes(NodeAttributes attributes) {
        this.attributes = attributes;
        getLibrary().fireNodeAttributesChanged(this);
    }

    /**
     * Checks if the given name would be valid for this node.
     *
     * @param name the name to check.
     * @throws InvalidNameException if the name was invalid.
     */
    public static void validateName(String name) throws InvalidNameException {
        if (name == null) throw new InvalidNameException(null, "", "Name cannot be null.");
        Matcher m1 = NODE_NAME_PATTERN.matcher(name);
        Matcher m2 = DOUBLE_UNDERSCORE_PATTERN.matcher(name);
        Matcher m3 = RESERVED_WORD_PATTERN.matcher(name);
        if (!m1.matches()) {
            throw new InvalidNameException(null, name, "Names can only contain lowercase letters, numbers, and the underscore. Names cannot be longer than 29 characters.");
        }
        if (m2.matches()) {
            throw new InvalidNameException(null, name, "Names starting with double underscore are reserved for internal use.");
        }
        if (m3.matches()) {
            throw new InvalidNameException(null, name, "Names cannot be a reserved word (network, node, root).");
        }
    }

    //// Parent/child relationship ////

    /**
     * Get the node's parent macro.
     *
     * @return the parent macro or null if this node has no parent
     */
    public Macro getParent() {
        return parent;
    }

    /**
     * Get the node's root macro.
     *
     * @return the root macro or null if this node has no parent
     */
    public Macro getRootMacro() {
        Macro root = getLibrary().getRootMacro();
        checkState(parent != null || this == root, "Parent is null and this node is not root.");
        return root;
    }

    /**
     * Check if the node has a parent macro.
     *
     * @return true if the node has a parent
     */
    public boolean hasParent() {
        return parent != null;
    }

    //// Path ////

    /**
     * Get an absolute path to this node starting from the root.
     * Example: "/grandParent1/parent1/child"
     *
     * @return the absolute path to this node
     */
    public String getAbsolutePath() {
        StringBuffer name = new StringBuffer("/");
        Macro parent = getParent();
        Macro root = getRootMacro();
        while (parent != null && parent != root) {
            name.insert(1, parent.getName() + "/");
            parent = parent.getParent();
        }
        if (this != root) {
            name.append(getName());
        }
        return name.toString();
    }

    //// Position ////

    public double getX() {
        return x;
    }

    public void setX(double x) {
        setPosition(x, this.y);
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        setPosition(this.x, y);
    }

    public Point getPosition() {
        return new Point((float) x, (float) y);
    }

    public void setPosition(Point p) {
        setPosition(p.getX(), p.getY());
    }

    public void setPosition(double x, double y) {
        if (this.x == x && this.y == y) return;
        this.x = x;
        this.y = y;
        getLibrary().fireNodePositionChanged(this);
    }

    //// Export flag ////

    public boolean isExported() {
        return exported;
    }

    public void setExported(boolean exported) {
        this.exported = exported;
    }

    //// Ports ////

    public Port createPort(String name, Class dataClass, Port.Direction direction) {
        return new Port(this, name, dataClass, direction);
    }

    protected void addPort(Port p) {
        ImmutableMap.Builder<String, Port> builder = ImmutableMap.builder();
        builder.putAll(ports);
        builder.put(p.getName(), p);
        ports = builder.build();
        getLibrary().fireNodePortsChangedEvent(this);
    }

    public boolean removePort(Port port) {
        checkState(ports.containsValue(port));
        // TODO Remove connections to port
        ImmutableMap.Builder<String, Port> builder = ImmutableMap.builder();
        for (Port p : ports.values()) {
            if (p != port) {
                builder.put(p.getName(), p);
            }
        }
        ports = builder.build();
        getLibrary().fireNodePortsChangedEvent(this);
        return true;
    }

    public Port getPort(String name) throws PortNotFoundException {
        checkNotNull(name);
        Port p = ports.get(name);
        if (p == null) {
            throw new PortNotFoundException(this, name);
        } else {
            return p;
        }
    }

    public boolean hasPort(String portName) {
        return ports.containsKey(portName);
    }

    public ImmutableCollection<Port> getPorts() {
        return ports.values();
    }

    //// Port values ////

    public Object getValue(String portName) throws PortNotFoundException {
        Port p = getPort(portName);
        checkNotNull(p);
        return p.getValue();
    }

    public int asInt(String portName) throws PortNotFoundException {
        return getPort(portName).asInt();
    }

    public float asFloat(String portName) throws PortNotFoundException {
        return getPort(portName).asFloat();
    }

    public String asString(String portName) throws PortNotFoundException {
        return getPort(portName).asString();
    }

    public Color asColor(String portName) throws PortNotFoundException {
        return getPort(portName).asColor();
    }

    public void setValue(String portName, Object value) throws PortNotFoundException {
        checkNotNull(portName);
        checkNotNull(value);
        Port p = getPort(portName);
        p.setValue(value);
    }

    /**
     * Set the given port to the given value.
     * <p/>
     * This method fails silently if the port does not exist or the value is not accepted.
     *
     * @param portName the name of the port
     * @param value    the new value
     */
    public void silentSet(String portName, Object value) {
        try {
            setValue(portName, value);
        } catch (Exception ignored) {
        }
    }

    //// Expression shortcuts ////

    public boolean setExpression(String portName, String expression) {
//        Port p = getPort(portName);
//        if (p == null)
//            throw new IllegalArgumentException("Parameter " + portName + " does not exist.");
//        return p.setExpression(expression);
        return true;
    }

    public void clearExpression(String portName) {
//        Port p = getPort(portName);
//        if (p == null)
//            throw new IllegalArgumentException("Parameter " + portName + " does not exist.");
//        p.clearExpression();
    }

    //// Processing ////

    /**
     * Execute the node code.
     * <p/>
     * This method calls the cook method and wraps any exceptions in an ExecuteException.
     * <p/>
     * This is the preferred method to call when executing the node.
     *
     * @param context the processing context
     * @throws ExecuteException if an error occurs during cooking
     */
    public void execute(CookContext context) throws ExecuteException {
        try {
            cook(context);
        } catch (ExecuteException e) {
            error = e;
            throw e;
        } catch (Exception e) {
            error = e;
            throw new ExecuteException(this, e);
        }
    }

    /**
     * This is the default cook implementation of the node.
     * <p/>
     * If this node has children, it will look up the rendered child and update it. The return value will be the
     * return value of the rendered child.
     * <p/>
     * If the node doesn't have children, this method returns null.
     * <p/>
     * Although you can call this method directly, you'll probably want to call execute, which wraps any
     * exceptions in an ExecuteException.
     *
     * @param context the processing context
     * @throws RuntimeException if an error occurred during processing.
     * @see #execute(CookContext)
     */
    public void cook(CookContext context) throws RuntimeException {
    }

    /**
     * Checks if an error occurred during the last update of this node.
     *
     * @return true if this node is in an error state.
     */
    public boolean hasError() {
        return error != null;
    }

    /**
     * Get the error that occurred during the last update.
     *
     * @return the error or null if no error occurred.
     */
    public Throwable getError() {
        return error;
    }

    //// Handle support ////

    /**
     * Creates and returns a Handle object that can be used for direct manipulation of the parameters of this node.
     * The handle is bound to this node.
     * <p/>
     * This method may return null to indicate that no handle is available.
     * <p/>
     * You should not override this method, but rather the createHandle method on the NodeType.
     *
     * @return a handle instance bound to this node, or null.
     */
    public Handle createHandle() {
        return null;
    }

    /**
     * Return a brief description of a node.
     *
     * @return
     */
    @Override
    public String toString() {
        return String.format("[Node %s]", getName());
    }
}
