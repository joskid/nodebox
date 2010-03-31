package nodebox.node.event;

import nodebox.node.Node;

/**
 * A NodeAttributesChangedEvent is fired whenever the attributes of a node, such as its description, change.
 *
 * @see Node#setAttributes(nodebox.node.NodeAttributes) 
 */
public class NodeAttributesChangedEvent extends AbstractNodeEvent {

    public NodeAttributesChangedEvent(Node source) {
        super(source);
    }

}
