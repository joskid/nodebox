package nodebox.node;

import com.google.common.collect.ImmutableList;

public final class PortAttributes {

    /**
     * The UI control for this port. This defines how the port is represented in the user interface.
     */
    public enum Widget {
        ANGLE, COLOR, FILE, FLOAT, FONT, GRADIENT, IMAGE, INT, MENU, SEED, STRING, TEXT, TOGGLE, STAMP_EXPRESSION, CODE
    }

    /**
     * The way in which values will be bound to a minimum and maximum value. Only hard bounding enforces the
     * minimum and maximum value.
     */
    public enum BoundingMethod {
        NONE, SOFT, HARD
    }

    private final String label;
    private final String helpText;
    private final Widget widget;
    private final boolean visible;
    private final BoundingMethod boundingMethod;
    private final Float minimumValue;
    private final Float maximumValue;
    private final ImmutableList<MenuItem> menuItems;

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String label = "";
        private String helpText;
        private Widget widget;
        private boolean visible;
        private BoundingMethod boundingMethod;
        private Float minimumValue;
        private Float maximumValue;
        private ImmutableList<MenuItem> menuItems;

        public Builder() {
        }

        public Builder label(String v) {
            label = v;
            return this;
        }

        public Builder helpText(String v) {
            helpText = v;
            return this;
        }

        public Builder widget(Widget v) {
            widget = v;
            return this;
        }

        public Builder visible(boolean v) {
            visible = v;
            return this;
        }

        public Builder boundingMethod(BoundingMethod v) {
            boundingMethod = v;
            return this;
        }

        public Builder minimumValue(Float v) {
            minimumValue = v;
            return this;
        }

        public Builder maximumValue(Float v) {
            maximumValue = v;
            return this;
        }

        public Builder menuItems(Iterable<MenuItem> v) {
            menuItems = ImmutableList.copyOf(v);
            return this;
        }

        public PortAttributes build() {
            return new PortAttributes(this);
        }
    }

    public PortAttributes(Builder b) {
        this.label = b.label;
        this.helpText = b.helpText;
        this.widget = b.widget;
        this.visible = b.visible;
        this.boundingMethod = b.boundingMethod;
        this.minimumValue = b.minimumValue;
        this.maximumValue = b.maximumValue;
        this.menuItems = b.menuItems;

    }

    public String getLabel() {
        return label;
    }

    public String getHelpText() {
        return helpText;
    }

    public Widget getWidget() {
        return widget;
    }

    public boolean isVisible() {
        return visible;
    }

    public BoundingMethod getBoundingMethod() {
        return boundingMethod;
    }

    public Float getMinimumValue() {
        return minimumValue;
    }

    public Float getMaximumValue() {
        return maximumValue;
    }

    public ImmutableList<MenuItem> getMenuItems() {
        return menuItems;
    }
}
