package nodebox.node;

import nodebox.util.StringUtils;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The NodeInfo contains all the metadata about a node.
 * <p/>
 * This includes the human-readable name (the label), the category, the description and the image.
 */
public final class NodeInfo {

    public static Builder builder(String label) {
        return new Builder(label);
    }

    public static Builder builder(Node node) {
        return new Builder(StringUtils.humanizeName(node.getTypeName()));
    }

    public static final String IMAGE_GENERIC = "__generic";

    private final String label;
    private final String category;
    private final String description;
    private final String image;

    public static class Builder {

        private String label = "";
        private String category = "Custom";
        private String description = "";
        private String image = IMAGE_GENERIC;

        public Builder(String label) {
            checkNotNull(label);
            this.label = label;
        }

        public Builder category(String v) {
            checkNotNull(v);
            category = v;
            return this;
        }

        public Builder description(String v) {
            checkNotNull(v);
            description = v;
            return this;
        }

        public Builder image(String v) {
            checkNotNull(v);
            image = v;
            return this;
        }

        public NodeInfo build() {
            return new NodeInfo(this);
        }

    }

    private NodeInfo(Builder b) {
        this.label = b.label;
        this.category = b.category;
        this.description = b.description;
        this.image = b.image;
    }

    /**
     * Get the node label.
     *
     * @return the node label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Get the node category.
     *
     * @return the node category
     */
    public String getCategory() {
        return category;
    }

    /**
     * Get the node description.
     *
     * @return the node description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Get the node image, as a relative file name.
     *
     * @return the node image
     */
    public String getImage() {
        return image;
    }

}
