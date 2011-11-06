package nodebox.node;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import nodebox.graphics.Color;
import nodebox.graphics.Point;
import org.python.google.common.base.Objects;

import static com.google.common.base.Preconditions.*;

public final class Port {

    public static final String TYPE_INT = "int";
    public static final String TYPE_FLOAT = "float";
    public static final String TYPE_STRING = "string";
    public static final String TYPE_POINT = "point";
    public static final String TYPE_COLOR = "color";

    /**
     * The UI control for this port. This defines how the port is represented in the user interface.
     */
    public enum Widget {
        ANGLE, COLOR, FILE, FLOAT, FONT, GRADIENT, IMAGE, INT, MENU, SEED, STRING, TEXT, TOGGLE, POINT
    }

    public static final ImmutableMap<String, Object> DEFAULT_VALUES;
    public static final ImmutableSet<String> STANDARD_TYPES;

    static {
        DEFAULT_VALUES = ImmutableMap.of(
                TYPE_INT, 0L,
                TYPE_FLOAT, 0.0,
                TYPE_STRING, "",
                TYPE_POINT, Point.ZERO,
                TYPE_COLOR, Color.BLACK
        );
        STANDARD_TYPES = ImmutableSet.of(TYPE_INT, TYPE_FLOAT, TYPE_STRING, TYPE_POINT, TYPE_COLOR);
    }

    private final String name;
    private final String type;
    private final Object value;

    public static Port intPort(String name, long value) {
        checkNotNull(name, "Name cannot be null.");
        checkNotNull(value, "Value cannot be null.");
        return new Port(name, TYPE_INT, value);
    }

    public static Port floatPort(String name, double value) {
        checkNotNull(name, "Name cannot be null.");
        checkNotNull(value, "Value cannot be null.");
        return new Port(name, TYPE_FLOAT, value);
    }

    public static Port stringPort(String name, String value) {
        checkNotNull(name, "Name cannot be null.");
        checkNotNull(value, "Value cannot be null.");
        return new Port(name, TYPE_STRING, value);
    }

    public static Port pointPort(String name, Point value) {
        checkNotNull(name, "Name cannot be null.");
        checkNotNull(value, "Value cannot be null.");
        return new Port(name, TYPE_POINT, value);
    }

    public static Port colorPort(String name, Color value) {
        checkNotNull(name, "Name cannot be null.");
        checkNotNull(value, "Value cannot be null.");
        return new Port(name, TYPE_COLOR, value);
    }

    public static Port customPort(String name, String type) {
        checkNotNull(name, "Name cannot be null.");
        checkNotNull(type, "Type cannot be null.");
        return new Port(name, type, null);
    }

    /**
     * Parse the type and create the appropriate Port. Use the default value appropriate for the port type.
     *
     * @param name The port name.
     * @param type The port type.
     * @return A new Port.
     */
    public static Port portForType(String name, String type) {
        checkNotNull(name, "Name cannot be null.");
        checkNotNull(type, "Type cannot be null.");
        // If the type is not found in the default values, get() returns null, which is what we need for custom types.
        return new Port(name, type, DEFAULT_VALUES.get(type));
    }

    /**
     * Create a new Port with the given value as a string parsed to the correct format.
     *
     * @param name        The port name.
     * @param type        The port type.
     * @param stringValue The port value as a string, e.g. "32.5"
     * @return A new Port.
     */
    public static Port parsedPort(String name, String type, String stringValue) {
        checkNotNull(name, "Name cannot be null.");
        checkNotNull(type, "Type cannot be null.");
        checkNotNull(stringValue, "String value cannot be null.");
        checkArgument(STANDARD_TYPES.contains(type), "Given type %s is not a standard type.");
        Object value;
        if (type.equals("int")) {
            value = Long.valueOf(stringValue);
        } else if (type.equals("float")) {
            value = Double.valueOf(stringValue);
        } else if (type.equals("point")) {
            value = Point.valueOf(stringValue);
        } else if (type.equals("color")) {
            value = Color.valueOf(stringValue);
        } else {
            throw new AssertionError("Unknown type " + type);
        }
        return new Port(name, type, value);
    }

    private Port(String name, String type, Object value) {
        this.name = name;
        this.type = type;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getLabel() {
        return name;
    }

    public String getType() {
        return type;
    }

    /**
     * Check if the Port type is a standard type, meaning it can be persisted, and its value can be accessed.
     *
     * @return true if this is a standard type.
     */
    private boolean isStandardType() {
        return STANDARD_TYPES.contains(type);
    }

    /**
     * Return the value stored in the port as a long.
     * <ul>
     * <li>Integers are returned as-is.</li>
     * <li>Floats are rounded using Math.round().</li>
     * <li>Other types return 0.</li>
     * </ul>
     *
     * @return The value as a long or 0 if the value cannot be converted.
     */
    public long intValue() {
        checkValueType();
        if (type.equals(TYPE_INT)) {
            return (Long) value;
        } else if (type.equals(TYPE_FLOAT)) {
            return Math.round((Double) value);
        } else {
            return 0L;
        }
    }

    /**
     * Return the value stored in the port as a Float.
     * <ul>
     * <li>Integers are converted to Floats.</li>
     * <li>Floats are returned as-is.</li>
     * <li>Other types return 0f.</li>
     * </ul>
     *
     * @return The value as a Float or 0f if the value cannot be converted.
     */
    public double floatValue() {
        checkValueType();
        if (type.equals(TYPE_INT)) {
            return ((Long) value).doubleValue();
        } else if (type.equals(TYPE_FLOAT)) {
            return (Double) value;
        } else {
            return 0.0;
        }
    }

    /**
     * Return the value stored in the port as a String.
     * <p/>
     * This conversion simply uses String.valueOf(), which does the right thing.
     *
     * @return The value as a String or "null" if the value is null. (for custom types)
     * @see String#valueOf(Object)
     */
    public String stringValue() {
        checkValueType();
        return String.valueOf(value);
    }

    /**
     * Return the value stored in the port as a Port.
     * <p/>
     * If the port has a different type, Point.ZERO is returned.
     *
     * @return The value as a Point or Point.ZERO if the value is of an incorrect type.
     */
    public Point pointValue() {
        checkValueType();
        if (type.equals(TYPE_POINT)) {
            return (Point) value;
        } else {
            return Point.ZERO;
        }
    }

    public Color colorValue() {
        checkValueType();
        if (type.equals(TYPE_COLOR)) {
            return (Color) value;
        } else {
            return Color.BLACK;
        }
    }

    /**
     * Return the value stored in the port as an Object.
     * <p/>
     * If this is a port with a custom type, this method returns null.
     *
     * @return The value as an Object or null.
     */
    public Object getValue() {
        checkValueType();
        return value;
    }

    //// Shim implementations of methods ////

    public boolean hasExpression() {
        return false;
    }

    public String getExpression() {
        return "";
    }

    public boolean isEnabled() {
        return true;
    }

    public Widget getWidget() {
        if (getType().equals(TYPE_INT)) {
            return Widget.INT;
        } else if (getType().equals(TYPE_FLOAT)) {
            return Widget.FLOAT;
        } else if (getType().equals(TYPE_STRING)) {
            return Widget.STRING;
        } else if (getType().equals(TYPE_POINT)) {
            return Widget.POINT;
        } else if (getType().equals(TYPE_COLOR)) {
            return Widget.COLOR;
        } else {
            throw new UnsupportedOperationException("No widget available for port type " + getType());
        }
    }

    //// Mutation methods ////

    /**
     * Return a new Port with the value set to the given value.
     *
     * @param value The new value. This must be of the correct type.
     * @return The new Port.
     * @throws IllegalStateException If you're trying to change the value of a standard type, or you give the wrong value.
     */
    public Port withValue(Object value) {
        checkState(isStandardType(), "You can only change the value of a standard type.");
        checkArgument(correctValueForType(value), "Value '%s' is not correct for %s port.", value, getType());
        return new Port(getName(), getType(), value);
    }

    private void checkValueType() {
        checkState(correctValueForType(this.value), "The internal value %s is not a %s.", value, type);
    }

    private boolean correctValueForType(Object value) {
        if (type.equals(TYPE_INT)) {
            return value instanceof Long;
        } else if (type.equals(TYPE_FLOAT)) {
            return value instanceof Double;
        } else if (type.equals(TYPE_STRING)) {
            return value instanceof String;
        } else if (type.equals(TYPE_POINT)) {
            return value instanceof Point;
        } else if (type.equals(TYPE_COLOR)) {
            return value instanceof Color;
        } else {
            // The value of a custom type should always be null.
            return value == null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Port)) return false;
        final Port other = (Port) o;
        return Objects.equal(name, other.name)
                && Objects.equal(type, other.type)
                && Objects.equal(value, other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name, type, value);
    }
}
