package nodebox.node.event;

import nodebox.node.Node;

public class NodeDirtyEvent extends AbstractNodeEvent {

    public NodeDirtyEvent(Node source) {
        super(source);
    }

}
