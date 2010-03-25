package nodebox.node.event;

import nodebox.node.Node;
import nodebox.node.NodeEvent;
import nodebox.node.Port;

public class PortAttributesChangedEvent extends NodeEvent {

    private final Port port;

    public PortAttributesChangedEvent(Node source, Port port) {
        super(source);
        this.port = port;
    }

    public Port getPort() {
        return port;
    }
}
