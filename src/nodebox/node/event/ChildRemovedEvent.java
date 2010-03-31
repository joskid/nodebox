package nodebox.node.event;

import nodebox.node.Macro;
import nodebox.node.Node;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A ChildRemovedEvent is fired whenever a child node is removed.
 *
 * @see Macro#removeChild(nodebox.node.Node)
 */
public class ChildRemovedEvent extends AbstractNodeEvent {

    private Node child;

    public ChildRemovedEvent(Macro source, Node child) {
        super(source);
        checkNotNull(child);
        this.child = child;
    }

    public Node getChild() {
        return child;
    }

    @Override
    public String toString() {
        return String.format("[%s source=%s child=%s]", getClass().getSimpleName(), getSource(), getChild());
    }

}
