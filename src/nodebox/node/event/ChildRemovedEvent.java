package nodebox.node.event;

import nodebox.node.Macro;
import nodebox.node.Node;
import nodebox.node.NodeEvent;

import static com.google.common.base.Preconditions.checkNotNull;

public class ChildRemovedEvent extends NodeEvent {

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
        return "ChildRemovedEvent{" +
                "source=" + getSource() +
                "child=" + child +
                '}';
    }
}
