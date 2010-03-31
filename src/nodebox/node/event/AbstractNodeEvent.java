package nodebox.node.event;

import nodebox.node.Node;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A subclass of AbstractNodeEvent is fired whenever a change is made to a node in the current library.
 */
public abstract class AbstractNodeEvent implements NodeEvent {

    private final Node source;

    protected AbstractNodeEvent(Node source) {
        checkNotNull(source);
        this.source = source;
    }

    public Node getSource() {
        return source;
    }

    @Override
    public String toString() {
        return String.format("[%s source=%s]", getClass().getSimpleName(), getSource());
    }

}
