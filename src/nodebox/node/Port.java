package nodebox.node;

import com.google.common.collect.ImmutableList;
import nodebox.graphics.Color;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A connectable object on a node. Ports provide input and output capabilities between nodes.
 * <p/>
 * Ports have a certain data class. Only ports with the same class of data can be connected together.
 */
public final class Port {

    public enum Direction {
        IN, OUT
    }

    private final Node node;
    private final String name;
    private final Class dataClass;
    private final Direction direction;
    private PortAttributes attributes;
    private Object value;

    protected Port(Node node, String name, Class dataClass, Direction direction) {
        checkNotNull(node);
        checkNotNull(name);
        checkNotNull(direction);
        this.node = node;
        // validateName accesses the node. Do this after this.node is initialized.
        validateName(name);
        this.name = name;
        this.dataClass = dataClass;
        this.direction = direction;
    }

    public String getAbsolutePath() {
        return node.getAbsolutePath() + "." + getName();
    }

    public NodeLibrary getLibrary() {
        return node.getLibrary();
    }

    public Node getNode() {
        return node;
    }

    public String getName() {
        return name;
    }

    public void validateName(String name) {
        if (name == null || name.trim().length() == 0)
            throw new InvalidNameException(this, name, "Name cannot be null or empty.");
        if (node.hasPort(name))
            throw new InvalidNameException(this, name, "There is already a port named " + name + ".");
        // Use the same validation as for nodes.
        Node.validateName(name);
    }

    public Direction getDirection() {
        return direction;
    }

    public void validate(Object value) throws IllegalArgumentException {
        if (value == null) throw new IllegalArgumentException("Value cannot be null.");
        if (!value.getClass().isAssignableFrom(dataClass))
            throw new IllegalArgumentException("Value is not a " + dataClass);
    }

    public String getLabel() {
        return attributes.getLabel();
    }

    public String getHelpText() {
        return attributes.getHelpText();
    }

    public PortAttributes.Widget getWidget() {
        return attributes.getWidget();
    }

    public boolean isVisible() {
        return attributes.isVisible();
    }

    public PortAttributes.BoundingMethod getBoundingMethod() {
        return attributes.getBoundingMethod();
    }

    public Float getMinimumValue() {
        return attributes.getMinimumValue();
    }

    public Float getMaximumValue() {
        return attributes.getMaximumValue();
    }

    public ImmutableList<MenuItem> getMenuItems() {
        return attributes.getMenuItems();
    }

    public PortAttributes getAttributes() {
        return attributes;
    }

    public void setAttributes(PortAttributes attributes) {
        this.attributes = attributes;
        getLibrary().firePortAttributesChangedEvent(node, this);
    }

    /**
     * Gets the value of this port.
     * <p/>
     * This value will be null if the port is disconnected
     * or an error occurred during processing.
     *
     * @return the value for this port.
     */
    public Object getValue() {
        return value;
    }

    /**
     * Set the value for this port.
     * <p/>
     * Setting this value will not trigger any notifications or dirty flags.
     *
     * @param value the value for this port.
     * @throws IllegalArgumentException if the value is not of the required data class.
     */
    public void setValue(Object value) throws IllegalArgumentException {
        validate(value);
        this.value = value;
    }

    public Object parseValue(String s) throws IllegalArgumentException {
        // TODO Implement parseValue
        return null;
    }

    public int asInt() {
        return 0;
    }

    public float asFloat() {
        return 0f;
    }

    public String asString() {
        return null;
    }

    public Color asColor() {
        return null;
    }

    /**
     * Reset the value of the port. This method is called when the port is disconnected.
     */
    public void revertToDefault() {
        value = null;
    }

    /**
     * Return the type of data that is stored in this port.
     *
     * @return the data type of this port
     */
    public Class getDataClass() {
        return dataClass;
    }

    //// Expressions ////

    public void setExpression(String s) {
        throw new UnsupportedOperationException("Expressions are not supported yet.");
    }

    public String getExpression() {
        throw new UnsupportedOperationException("Expressions are not supported yet.");
    }

    public boolean hasExpression() {
        throw new UnsupportedOperationException("Expressions are not supported yet.");
    }

    public void clearExpression() {
        throw new UnsupportedOperationException("Expressions are not supported yet.");
    }

    public boolean hasExpressionError() {
        throw new UnsupportedOperationException("Expressions are not supported yet.");
    }

    public Throwable getExpressionError() {
        throw new UnsupportedOperationException("Expressions are not supported yet.");
    }

    //// Connections ////

    public boolean isInputPort() {
        return direction == Direction.IN;
    }

    public boolean isOutputPort() {
        return direction == Direction.OUT;
    }

    /**
     * Check if this port can connect to the given port.
     * <p/>
     * The default operation checks if the directions match up and if the data classes are equal.
     *
     * @param p the port to check
     * @return true if they can connect
     */
    public boolean canConnectTo(Port p) {
        if (this.direction == p.direction) return false;
        if (getDataClass() != p.getDataClass()) return false;
        return true;
    }

    //// Cloning ////

    /**
     * Create a clone of this port.
     * This new port is not added to any node, and the node attribute will probably be need to be changed.
     * <p/>
     * The value of this port is not cloned, since values cannot be cloned.
     *
     * @return a new Port object
     */
    @Override
    public Port clone() {
        return new Port(node, name, dataClass, direction);
    }

    @Override
    public String toString() {
        return String.format("[Port %s on Node %s]", name, node.getName());
    }
}
