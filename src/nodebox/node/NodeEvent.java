package nodebox.node;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class NodeEvent {

    private final Node source;

    protected NodeEvent(Node source) {
        checkNotNull(source);
        this.source = source;
    }

    public Node getSource() {
        return source;
    }

    @Override
    public String toString() {
        return String.format("[%s source=%s]", getClass().getSimpleName(), source);
    }
}
