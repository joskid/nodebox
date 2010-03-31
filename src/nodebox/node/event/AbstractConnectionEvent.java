package nodebox.node.event;

import nodebox.node.Connection;
import nodebox.node.Node;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A subclass of AbstractConnectionEvent event is generated whenever a connection is made or destroyed.
 */
public class AbstractConnectionEvent extends AbstractNodeEvent {

    private final Connection connection;

    public AbstractConnectionEvent(Node source, Connection connection) {
        super(source);
        checkNotNull(connection);
        this.connection = connection;
    }

    public Connection getConnection() {
        return connection;
    }

    @Override
    public String toString() {
        return String.format("[%s source=%s connection=%s]", getClass().getSimpleName(), getSource(), getConnection());
    }

}
