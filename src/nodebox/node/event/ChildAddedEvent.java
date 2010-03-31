package nodebox.node.event;

import nodebox.node.Macro;
import nodebox.node.Node;

/**
 * A ChildAddedEvent is fired whenever a child node is added.
 *
 * @see Macro#createChild(Class)
 * @see Macro#createChild(Class, String)
 */
public class ChildAddedEvent extends AbstractNodeEvent {

    private Node child;

    public ChildAddedEvent(Macro source, Node child) {
        super(source);
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
