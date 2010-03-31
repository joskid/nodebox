package nodebox.node.event;

import nodebox.node.Node;
import nodebox.node.Port;

/**
 * A PortAddedEvent is fired whenever a port is added.
 *
 * @see Node#createPort(String, Class, nodebox.node.Port.Direction)
 */
public class PortAddedEvent extends AbstractPortEvent {

    public PortAddedEvent(Node source, Port port) {
        super(source, port);
    }

}
