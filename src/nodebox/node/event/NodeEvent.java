package nodebox.node.event;

import nodebox.node.Node;

/**
 * A NodeEvent is the base interface for every event that can occur in the lifespan of a node library.
 */
public interface NodeEvent {

    public Node getSource();

}
