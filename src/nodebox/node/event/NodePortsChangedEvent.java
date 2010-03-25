package nodebox.node.event;

import nodebox.node.Node;
import nodebox.node.NodeEvent;

public class NodePortsChangedEvent extends NodeEvent {

    public NodePortsChangedEvent(Node source) {
        super(source);
    }

}
