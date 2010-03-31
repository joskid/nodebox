package nodebox.node.event;

import nodebox.node.Connection;
import nodebox.node.Node;

/**
 * A ConnectionRemovedEvent is fired whenever a connection is made.
 *
 * Note that when removing a child node all connections are broken as well,
 * which can result in multiple ConnectionRemovedEvents
 *
 * @see nodebox.node.Macro#disconnect(nodebox.node.Node)
 * @see nodebox.node.Macro#disconnect(nodebox.node.Port)
 * @see nodebox.node.Macro#removeChild(nodebox.node.Node)
 */
public class ConnectionRemovedEvent extends AbstractConnectionEvent {

    public ConnectionRemovedEvent(Node source, Connection connection) {
        super(source, connection);
    }

}
