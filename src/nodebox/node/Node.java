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

import java.util.Collection;
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

    public enum Attribute {
        LIBRARY, NAME, POSITION, EXPORT, DESCRIPTION, IMAGE, PARAMETER, PORT
    }

    private NodeLibrary library;
    private Macro parent;
    private Mode mode = Mode.PRODUCER;
    private NodeAttributes attributes;
    private String name;
    private double x, y;
    private boolean exported;
    private ImmutableMap<String, Port> ports = ImmutableMap.of();
    private Throwable error;

    //// Constructors ////

    public Node(NodeLibrary library) {
        checkNotNull(library);
        this.library = library;
    }

    //// Library ////

    public NodeLibrary getLibrary() {
        return library;
    }

    //// Mode ////

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    //// Naming /////

    public String getName() {
        return name;
    }

    public void setName(String name) throws InvalidNameException {
        if (this.name.equals(name)) return;
        validateName(name);
        parent.renameChild(this.name, name);
        this.name = name;
        getLibrary().fireNodeAttributesChanged(this);
    }

    //// Attributes ////

    public String getDescription() {
        return attributes.getDescription();
    }

    public String getImage() {
        return attributes.getImage();
    }

    public NodeAttributes getAttributes() {
        return attributes;
    }

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

    public Macro getParent() {
        return parent;
    }

    public Macro getRoot() {
        if (parent == null) return (Macro) this;
        Macro m = getParent();
        while (m.getParent() != null) {
            m = m.getParent();
        }
        return m;
    }

    /**
     * Reparent the node.
     * <p/>
     * This breaks all connections.
     *
     * @param parent the new parent
     */
    public void setParent(Macro parent) {
        checkNotNull(parent);
        checkState(parent.getLibrary() == library, "parent macro is not in the same library.");
        this.parent.removeChild(this);
        this.parent = parent;
        this.parent.addChild(this);
    }

    public boolean hasParent() {
        return parent != null;
    }

    public boolean isLeaf() {
        return true;
    }

    public boolean isEmpty() {
        return true;
    }

    public int size() {
        return 0;
    }

    //// Path ////

    public String getAbsolutePath() {
        StringBuffer name = new StringBuffer("/");
        Macro parent = getParent();
        while (parent != null) {
            name.insert(1, parent.getName() + "/");
            parent = parent.getParent();
        }
        name.append(getName());
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

    public Port addPort(Port p) {
        ImmutableMap.Builder<String, Port> builder = ImmutableMap.builder();
        builder.putAll(ports);
        builder.put(p.getName(), p);
        ports = builder.build();
        getLibrary().fireNodePortsChangedEvent(this);
        return p;
    }

    public Port addPort(String name, Class dataClass, Port.Direction direction) {
        Port p = new Port(this, name, dataClass, direction);
        addPort(p);
        return p;
    }

    public boolean removePort(Port port) {
        if (ports.containsValue(port)) return false;
        // TODO Remove connections to port
        ImmutableMap.Builder<String, Port> builder = ImmutableMap.builder();
        for (Port p : ports.values()) {
            if (p != port) {
                builder.put(port.getName(), p);
            }
        }
        ports = builder.build();
        getLibrary().fireNodePortsChangedEvent(this);
        return true;
    }

    public Port getPort(String name) {
        // TODO Test for null
        return ports.get(name);
    }

    public boolean hasPort(String portName) {
        return ports.containsKey(portName);
    }

    public ImmutableCollection<Port> getPorts() {
        return ports.values();
    }

    //// Port values ////

    public Object getValue(String portName) {
        Port p = getPort(portName);
        if (p == null) return null;
        return p.getValue();
    }

    private Object getValueAs(String portName, Class c) throws PortNotFoundException {
        checkNotNull(portName);
        checkNotNull(c);
        Port p = getPort(portName);
        if (p == null) throw new PortNotFoundException(this, portName);
        if (p.getDataClass() != c.getClass()) {
            throw new RuntimeException("Port " + portName + " is not a " + c.getSimpleName());
        }
        return p.getValue();
    }

    public int asInt(String portName) {
        return (Integer) getValueAs(portName, Integer.class);
    }

    public float asFloat(String portName) {
        return (Float) getValueAs(portName, Float.class);
    }

    public String asString(String portName) {
        return (String) getValueAs(portName, String.class);
    }

    public Color asColor(String portName) {
        return (Color) getValueAs(portName, Color.class);
    }

    public void setValue(String portName, Object value) throws PortNotFoundException {
        checkNotNull(portName);
        checkNotNull(value);
        Port p = getPort(portName);
        if (p == null)
            throw new IllegalArgumentException("Port " + portName + " does not exist.");
        p.setValue(value);
    }

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
     * This is the default cook implementation of the node.
     * <p/>
     * If this node has children, it will look up the rendered child and update it. The return value will be the
     * return value of the rendered child.
     * <p/>
     * If the node doesn't have children, this method returns null.
     *
     * @param context the processing context
     * @throws RuntimeException if an error occurred during processing.
     */
    public void cook(ProcessingContext context) throws RuntimeException {
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

    @Override
    public Node clone() {
        try {
            Node result = (Node) super.clone();
            // TODO Clone all attributes
            //result.parent = parent;
            //result.mode = mode;
            //result.attributes = attributes;
            //result.ports = ports.clone();
            return result;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
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
