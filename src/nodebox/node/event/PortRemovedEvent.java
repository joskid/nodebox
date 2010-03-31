package nodebox.node.event;

import nodebox.node.Node;
import nodebox.node.Port;

/**
 * A PortRemovedEvent is fired whenever a port is removed.
 *
 * @see Node#removePort(nodebox.node.Port)
 */
public class PortRemovedEvent extends AbstractPortEvent {

    public PortRemovedEvent(Node source, Port port) {
        super(source, port);
    }

}
