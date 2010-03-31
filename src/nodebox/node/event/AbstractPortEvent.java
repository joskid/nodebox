package nodebox.node.event;

import nodebox.node.Node;
import nodebox.node.Port;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A subclass of AbstractPortEvent event is generated whenever a port is added, removed, or its attributes have changed.
 */
public abstract class AbstractPortEvent extends AbstractNodeEvent {

    private final Port port;

    protected AbstractPortEvent(Node source, Port port) {
        super(source);
        checkNotNull(port);
        this.port = port;
    }

    public Port getPort() {
        return port;
    }

    @Override
    public String toString() {
        return String.format("[%s source=%s port=%s]", getClass().getSimpleName(), getSource(), getPort());
    }

}
