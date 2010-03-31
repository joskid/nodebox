package nodebox.node.event;

import nodebox.node.Node;

/**
 * A NodePositionChangedEvent is fired whenever the position of a node changes.
 *
 * @see Node#setPosition(nodebox.graphics.Point)
 * @see Node#setPosition(double, double)  
 */
public class NodePositionChangedEvent extends AbstractNodeEvent {

    public NodePositionChangedEvent(Node source) {
        super(source);
    }

}
