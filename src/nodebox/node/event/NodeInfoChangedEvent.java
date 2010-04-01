package nodebox.node.event;

import nodebox.node.Node;

/**
 * A NodeInfoChangedEvent is fired whenever the metadata of a node, such as its description, changes.
 *
 * @see Node#setInfo(nodebox.node.NodeInfo)
 */
public class NodeInfoChangedEvent extends AbstractNodeEvent {

    public NodeInfoChangedEvent(Node source) {
        super(source);
    }

}
