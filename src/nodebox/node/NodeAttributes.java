package nodebox.node;

/**
 * The node attributes contain all the metadata about a node, such as its name and description.
 */
public final class NodeAttributes {

    public static final NodeAttributes DEFAULT = new NodeAttributes();

    public static final String IMAGE_GENERIC = "__generic";

    private final String description;
    private final String image;

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String description = "";
        private String image = IMAGE_GENERIC;

        public Builder() {
        }

        public Builder description(String v) {
            description = v;
            return this;
        }

        public Builder image(String v) {
            image = v;
            return this;
        }

        public NodeAttributes build() {
            return new NodeAttributes(this);
        }
    }

    public NodeAttributes() {
        description = "";
        image = IMAGE_GENERIC;
    }

    public NodeAttributes(Builder b) {
        description = b.description;
        image = b.image;
    }

    public String getDescription() {
        return description;
    }

    public String getImage() {
        return image;
    }

}
