package nodebox.node.event;

import nodebox.node.Node;
import nodebox.node.NodeEvent;

public class NodeAttributesChangedEvent extends NodeEvent {

    public NodeAttributesChangedEvent(Node source) {
        super(source);
    }
}
