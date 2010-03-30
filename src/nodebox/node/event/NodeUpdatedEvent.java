package nodebox.node.event;

import nodebox.node.CookContext;
import nodebox.node.Node;
import nodebox.node.NodeEvent;

public class NodeUpdatedEvent extends NodeEvent {

    private CookContext context;


    public NodeUpdatedEvent(Node source, CookContext context) {
        super(source);
        this.context = context;
    }

    public CookContext getContext() {
        return context;
    }

    @Override
    public String toString() {
        return "NodeUpdatedEvent{" +
                "source=" + getSource() +
                '}';
    }

}
