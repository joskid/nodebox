package nodebox.node.event;

import nodebox.node.Node;
import nodebox.node.NodeEvent;
import nodebox.node.Port;

import static com.google.common.base.Preconditions.checkNotNull;

public class ValueChangedEvent extends NodeEvent {

    private final Port port;

    public ValueChangedEvent(Node source, Port port) {
        super(source);
        checkNotNull(port);
        this.port = port;
    }

    public Port getPort() {
        return port;
    }

    @Override
    public String toString() {
        return String.format("[%s source=%s port=%s]", getClass().getSimpleName(), getSource(), port);
    }

}
