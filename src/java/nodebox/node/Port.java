package nodebox.node;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import nodebox.graphics.Color;
import nodebox.graphics.Point;

import java.util.List;

import static com.google.common.base.Preconditions.*;

public final class Port {

    public static final String TYPE_INT = "int";
    public static final String TYPE_FLOAT = "float";
    public static final String TYPE_STRING = "string";
    public static final String TYPE_BOOLEAN = "boolean";
    public static final String TYPE_POINT = "point";
    public static final String TYPE_COLOR = "color";

    public enum Attribute {NAME, TYPE, VALUE, MINIMUM_VALUE, MAXIMUM_VALUE, MENU_ITEMS}

    /**
     * The UI control for this port. This defines how the port is represented in the user interface.
     */
    public enum Widget {
        ANGLE, COLOR, FILE, FLOAT, FONT, GRADIENT, IMAGE, INT, MENU, SEED, STRING, TEXT, TOGGLE, POINT
    }

    public enum Direction {
        INPUT, OUTPUT
    }

    public static final ImmutableMap<String, Object> DEFAULT_VALUES;
    public static final ImmutableSet<String> STANDARD_TYPES;

    static {
        ImmutableMap.Builder<String, Object> b = ImmutableMap.builder();
        b.put(TYPE_INT, 0L);
        b.put(TYPE_FLOAT, 0.0);
        b.put(TYPE_BOOLEAN, false);
        b.put(TYPE_STRING, "");
        b.put(TYPE_POINT, Point.ZERO);
        b.put(TYPE_COLOR, Color.BLACK);
        DEFAULT_VALUES = b.build();
        STANDARD_TYPES = ImmutableSet.of(TYPE_INT, TYPE_FLOAT, TYPE_BOOLEAN, TYPE_STRING, TYPE_POINT, TYPE_COLOR);
    }

    private final String name;
    private final String type;
    private final Object value;
    private final Double minimumValue;
    private final Double maximumValue;
    private final ImmutableList<MenuItem> menuItems;

    public static Port intPort(String name, long value) {
        return intPort(name, value, null, null);
    }

    public static Port intPort(String name, long value, Integer minimumValue, Integer maximumValue) {
        checkNotNull(value, "Value cannot be null.");
        return new Port(name, TYPE_INT, value, minimumValue != null ? minimumValue.doubleValue() : null, maximumValue != null ? maximumValue.doubleValue() : null);
    }

    public static Port floatPort(String name, double value) {
        return floatPort(name, value, null, null);
    }

    public static Port floatPort(String name, double value, Double minimumValue, Double maximumValue) {
        checkNotNull(value, "Value cannot be null.");
        return new Port(name, TYPE_FLOAT, value, minimumValue, maximumValue);
    }

    public static Port booleanPort(String name, boolean value) {
        checkNotNull(value, "Value cannot be null.");
        return new Port(name, TYPE_BOOLEAN, value);
    }

    public static Port stringPort(String name, String value) {
        checkNotNull(value, "Value cannot be null.");
        return new Port(name, TYPE_STRING, value);
    }

    public static Port stringPort(String name, String value, Iterable<MenuItem> menuItems) {
        checkNotNull(value, "Value cannot be null.");
        return new Port(name, TYPE_STRING, value, menuItems);
    }

    public static Port pointPort(String name, Point value) {
        checkNotNull(value, "Value cannot be null.");
        return new Port(name, TYPE_POINT, value);
    }

    public static Port colorPort(String name, Color value) {
        checkNotNull(value, "Value cannot be null.");
        return new Port(name, TYPE_COLOR, value);
    }

    public static Port customPort(String name, String type) {
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
        checkNotNull(type, "Type cannot be null.");
        // If the type is not found in the default values, get() returns null, which is what we need for custom types.
        return new Port(name, type, DEFAULT_VALUES.get(type), null, null, ImmutableList.<MenuItem>of());
    }

    /**
     * Create a new Port with the given value as a string parsed to the correct format.
     *
     * @param name        The port name.
     * @param type        The port type.
     * @param stringValue The port value as a string, e.g. "32.5".
     * @return A new Port.
     */

    public static Port parsedPort(String name, String type, String stringValue) {
        return parsedPort(name, type, stringValue, null, null, ImmutableList.<MenuItem>of());
    }

    /**
     * Create a new Port with the given value as a string parsed to the correct format.
     *
     * @param name        The port name.
     * @param type        The port type.
     * @param valueString The port value as a string, e.g. "32.5".
     * @param minString   The minimum value as a string.
     * @param maxString   The maximum value as a string.
     * @param menuItems   The list of menu items.
     * @return A new Port.
     */
    public static Port parsedPort(String name, String type, String valueString, String minString, String maxString, ImmutableList<MenuItem> menuItems) {
        checkNotNull(name, "Name cannot be null.");
        checkNotNull(type, "Type cannot be null.");
        if (STANDARD_TYPES.contains(type)) {
            Object value;
            if (valueString == null) {
                value = DEFAULT_VALUES.get(type);
                checkNotNull(value);
            } else {
                if (type.equals("int")) {
                    value = Long.valueOf(valueString);
                } else if (type.equals("float")) {
                    value = Double.valueOf(valueString);
                } else if (type.equals("string")) {
                    value = valueString;
                } else if (type.equals("boolean")) {
                    value = Boolean.valueOf(valueString);
                } else if (type.equals("point")) {
                    value = Point.valueOf(valueString);
                } else if (type.equals("color")) {
                    value = Color.valueOf(valueString);
                } else {
                    throw new AssertionError("Unknown type " + type);
                }
            }
            Double minimumValue = null;
            Double maximumValue = null;
            if (minString != null)
                minimumValue = Double.valueOf(minString);
            if (maxString != null)
                maximumValue = Double.valueOf(maxString);
            return new Port(name, type, value, minimumValue, maximumValue, menuItems);
        } else {
            return Port.customPort(name, type);
        }
    }

    private Port(String name, String type, Object value) {
        this(name, type, value, null, null, ImmutableList.<MenuItem>of());
    }

    private Port(String name, String type, Object value, Double minimumValue, Double maximumValue) {
        this(name, type, value, minimumValue, maximumValue, ImmutableList.<MenuItem>of());
    }

    private Port(String name, String type, Object value, Iterable<MenuItem> menuItems) {
        this(name, type, value, null, null, menuItems);
    }

    private Port(String name, String type, Object value, Double minimumValue, Double maximumValue, Iterable<MenuItem> menuItems) {
        checkNotNull(name, "Name cannot be null.");
        checkNotNull(type, "Type cannot be null.");
        checkNotNull(menuItems, "Menu items cannot be null.");
        this.name = name;
        this.type = type;
        this.minimumValue = minimumValue;
        this.maximumValue = maximumValue;
        this.value = clampValue(value);
        this.menuItems = ImmutableList.copyOf(menuItems);
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

    public Double getMinimumValue() {
        return minimumValue;
    }

    public Double getMaximumValue() {
        return maximumValue;
    }

    public boolean hasMenu() {
        return !menuItems.isEmpty();
    }

    public List<MenuItem> getMenuItems() {
        return menuItems;
    }

    /**
     * Check if the Port type is a standard type, meaning it can be persisted, and its value can be accessed.
     *
     * @return true if this is a standard type.
     */
    public boolean isStandardType() {
        return STANDARD_TYPES.contains(type);
    }

    /**
     * Check if the Port type is a custom type.
     *
     * @return true if this is a custom type.
     */
    public boolean isCustomType() {
        return !isStandardType();
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
     * Return the value stored in the port as a boolean.
     * <p/>
     * If the port has a different type, false is returned.
     *
     * @return The value as a Float or 0f if the value cannot be converted.
     */
    public boolean booleanValue() {
        checkValueType();
        if (type.equals(TYPE_BOOLEAN)) {
            return (Boolean) value;
        } else {
            return false;
        }
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
            if (hasMenu()) {
                return Widget.MENU;
            } else {
                return Widget.STRING;
            }
        } else if (getType().equals(TYPE_BOOLEAN)) {
            return Widget.TOGGLE;
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
        return new Port(getName(), getType(), clampValue(convertValue(getType(), value)), minimumValue, maximumValue, menuItems);
    }

    /**
     * Convert integers to longs and floats to doubles. All other values are passed through as-is.
     *
     * @param type  The expected type.
     * @param value The original value.
     * @return The converted value.
     */
    private Object convertValue(String type, Object value) {
        if (value instanceof Integer) {
            checkArgument(type.equals(TYPE_INT));
            return (long) ((Integer) value);
        } else if (value instanceof Float) {
            checkArgument(type.equals(TYPE_FLOAT));
            return (double) ((Float) value);
        } else {
            return value;
        }
    }

    /**
     * Convert integers to longs and floats to doubles. All other values are passed through as-is.
     *
     * @param value The original value.
     * @return The converted value.
     */
    private Object clampValue(Object value) {
        if (getType().equals(TYPE_FLOAT)) {
            return clamp((Double) value);
        } else if (getType().equals(TYPE_INT)) {
            return (long) clamp(((Long) value).doubleValue());
        } else {
            return value;
        }
    }

    private double clamp(double v) {
        if (minimumValue != null && v < minimumValue) {
            return minimumValue;
        } else if (maximumValue != null && v > maximumValue) {
            return maximumValue;
        } else {
            return v;
        }
    }

    private void checkValueType() {
        checkState(correctValueForType(this.value), "The internal value %s is not a %s.", value, type);
    }

    private boolean correctValueForType(Object value) {
        if (type.equals(TYPE_INT)) {
            return value instanceof Long || value instanceof Integer;
        } else if (type.equals(TYPE_FLOAT)) {
            return value instanceof Double || value instanceof Float;
        } else if (type.equals(TYPE_STRING)) {
            return value instanceof String;
        } else if (type.equals(TYPE_BOOLEAN)) {
            return value instanceof Boolean;
        } else if (type.equals(TYPE_POINT)) {
            return value instanceof Point;
        } else if (type.equals(TYPE_COLOR)) {
            return value instanceof Color;
        } else {
            // The value of a custom type should always be null.
            return value == null;
        }
    }

    public Object getAttributeValue(Attribute attribute) {
        if (attribute == Attribute.NAME) {
            return getName();
        } else if (attribute == Attribute.TYPE) {
            return getType();
        } else if (attribute == Attribute.MINIMUM_VALUE) {
            return getMinimumValue();
        } else if (attribute == Attribute.MAXIMUM_VALUE) {
            return getMaximumValue();
        } else if (attribute == Attribute.MENU_ITEMS) {
            return getMenuItems();
        } else {
            throw new AssertionError("Unknown port attribute " + attribute);
        }
    }

    private static Object parseValue(String type, String valueString) {
        if (type.equals("int")) {
            return Long.valueOf(valueString);
        } else if (type.equals("float")) {
            return Double.valueOf(valueString);
        } else if (type.equals("string")) {
            return valueString;
        } else if (type.equals("boolean")) {
            return Boolean.valueOf(valueString);
        } else if (type.equals("point")) {
            return Point.valueOf(valueString);
        } else if (type.equals("color")) {
            return Color.valueOf(valueString);
        } else {
            throw new AssertionError("Unknown type " + type);
        }
    }

    public Port withMenuItems(Iterable<MenuItem> items) {
        checkNotNull(items);
        checkArgument(type.equals(Port.TYPE_STRING), "You can only use menu items on string ports, not %s", this);
        return new Port(name, type, value, minimumValue, maximumValue, items);
    }

    public Port withParsedAttribute(Attribute attribute, String valueString) {
        checkNotNull(valueString);

        String name = this.name;
        String type = this.type;
        Object value = this.value;
        Double minimumValue = this.minimumValue;
        Double maximumValue = this.maximumValue;

        switch (attribute) {
            case VALUE:
                checkArgument(STANDARD_TYPES.contains(type), "Port %s: you can only set the value for one of the standard types, not %s (value=%s)", name, type, valueString);
                value = parseValue(type, valueString);
                break;
            case MINIMUM_VALUE:
                minimumValue = Double.valueOf(valueString);
                break;
            case MAXIMUM_VALUE:
                maximumValue = Double.valueOf(valueString);
                break;
            default:
                throw new AssertionError("You cannot use withParsedAttribute with attribute " + attribute);
        }
        return new Port(name, type, value, minimumValue, maximumValue, menuItems);
    }

    //// Object overrides ////

    @Override
    public int hashCode() {
        return Objects.hashCode(name, type, value);
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
    public String toString() {
        return String.format("<Port %s (%s): %s>", name, type, value);
    }

}
