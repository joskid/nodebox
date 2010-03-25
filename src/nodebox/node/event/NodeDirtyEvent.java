package nodebox.node.event;

import nodebox.node.Node;
import nodebox.node.NodeEvent;

public class NodeDirtyEvent extends NodeEvent {

    public NodeDirtyEvent(Node source) {
        super(source);
    }

}
