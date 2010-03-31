package nodebox.node.event;

import nodebox.node.Connection;
import nodebox.node.Node;

/**
 * A ConnectionAddedEvent is fired whenever a connection is made.
 *
 * @see nodebox.node.Macro#connect(nodebox.node.Port, nodebox.node.Port)
 */
public class ConnectionAddedEvent extends AbstractConnectionEvent {

    public ConnectionAddedEvent(Node source, Connection connection) {
        super(source, connection);
    }

}
