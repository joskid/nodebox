package nodebox.node;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import nodebox.graphics.Color;

import javax.annotation.Nullable;

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

    private static final ImmutableMap<Class, Object> defaultValues = ImmutableMap.<Class, Object>of(
            Integer.class, 0,
            Float.class, 0f,
            String.class, "",
            Color.class, new Color());

    private final Node node;
    private final String name;
    private final Class dataClass;
    private final Direction direction;
    private PortAttributes attributes = PortAttributes.DEFAULT;
    private Object value;

    Port(Node node, String name, Class dataClass, Direction direction) {
        checkNotNull(node);
        checkNotNull(name);
        checkNotNull(direction);
        this.node = node;
        // validateName accesses the node. Do this after this.node is initialized.
        validateName(name);
        this.name = name;
        this.dataClass = dataClass;
        this.value = defaultValues.get(dataClass);
        this.direction = direction;
        this.node.addPort(this);
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
        if (name == null)
            throw new InvalidNameException(this, name, "Name cannot be null.");
        if (name.trim().length() == 0)
            throw new InvalidNameException(this, name, "Name cannot empty.");
        if (node.hasPort(name))
            throw new InvalidNameException(this, name, "There is already a port named " + name + ".");
        // Use the same validation as for nodes.
        Node.validateName(name);
    }

    public Direction getDirection() {
        return direction;
    }

    public void validate(@Nullable Object value) throws IllegalArgumentException {
        // Opaque objects are never checked.
        if (dataClass == Object.class) return;
        if (value == null) throw new IllegalArgumentException("Value cannot be null.");
        // As a special exception, a float port can accept integer values.
        if (value.getClass() == Integer.class && dataClass == Float.class) return;
        if (!dataClass.isAssignableFrom(value.getClass()))
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

    public Object defaultValue() {
        return defaultValues.get(dataClass);
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
    public void setValue(@Nullable Object value) throws IllegalArgumentException {
        if (this.value == value) return;
        validate(value);
        // As a special exception, a float port can accept integer values.
        if (value != null && value.getClass() == Integer.class && dataClass == Float.class) {
            this.value = (float) (Integer) value;
        } else {
            this.value = value;
        }
        getLibrary().fireValueChanged(node, this);
    }

    /**
     * Parse the string and return a value appropriate for this port.
     * Null is never accepted. If the string is empty, return the default value.
     * <p/>
     * The value is not set on the port.
     *
     * @param s the value to parse
     * @return a value appropriate for this port
     * @throws IllegalArgumentException if parsing fails.
     */
    public Object parseValue(String s) throws IllegalArgumentException {
        checkNotNull(s);
        if (s.isEmpty()) return defaultValue();
        if (dataClass == Integer.class) {
            return Integer.parseInt(s);
        } else if (dataClass == Float.class) {
            return Float.parseFloat(s);
        } else if (dataClass == String.class) {
            return s;
        } else if (dataClass == Color.class) {
            return Color.parseColor(s);
        } else {
            throw new IllegalArgumentException("Cannot parse values of class " + dataClass);
        }
    }

    /**
     * Get the value as an integer.
     * <p/>
     * If the value is a floating-point value, it is rounded to an integer. If the value is not a number, returns 0.
     *
     * @return the value as an integer.
     */
    public int asInt() {
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof Float) {
            return Math.round((Float) value);
        } else {
            return 0;
        }
    }

    /**
     * Get the value as a floating-point value.
     * <p/>
     * If the value is an integer, it will be converted to a float. If the value is not a number, returns 0f.
     *
     * @return the value as a float.
     */
    public float asFloat() {
        if (value instanceof Float) {
            return (Float) value;
        } else if (value instanceof Integer) {
            return ((Integer) value).floatValue();
        } else {
            return 0f;
        }
    }

    /**
     * Get the value as a string.
     * <p/>
     * If the value is null, return an empty string. Otherwise use toString.
     *
     * @return the value as a string.
     */
    public String asString() {
        return value == null ? "" : value.toString();
    }

    /**
     * Get the value as a color.
     * <p/>
     * If the value is a floating-point value, the value is used for the r/g/b components of the color. Otherwise,
     * returns a new black color object.
     *
     * @return the value as a color.
     */
    public Color asColor() {
        if (value instanceof Color) {
            return (Color) value;
        } else if (value instanceof Float) {
            float v = (Float) value;
            return new Color(v, v, v);
        } else {
            return new Color();
        }
    }

    /**
     * Reset the value of the port. This method is called when the port is disconnected.
     */
    public void revertToDefault() {
        setValue(defaultValue());
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
     * The default operation checks if the directions match up, if the ports are on different nodes
     * and if the data classes are equal or is assignable from the input to the output.
     *
     * @param p the port to check
     * @return true if they can connect
     */
    public boolean canConnectTo(Port p) {
        if (this.direction == p.direction) return false;
        if (this.node == p.node) return false;
        if (this.direction == Direction.IN) {
            return getDataClass().isAssignableFrom(p.getDataClass());
        } else {
            return p.getDataClass().isAssignableFrom(getDataClass());
        }
    }

    //// Copying ////

    /**
     * Copy this port onto another node.
     * <p/>
     * The value of this port is not cloned, since values cannot be cloned.
     *
     * @param node the node to copy the port onto
     * @return a new Port object
     */
    public Port copyOnto(Node node) {
        Port p = new Port(node, name, dataClass, direction);
        p.value = value;
        return p;
    }

    @Override
    public String toString() {
        return String.format("[Port %s on Node %s]", name, node.getName());
    }
}
