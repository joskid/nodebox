package nodebox.node.event;

import nodebox.node.Node;
import nodebox.node.NodeEvent;
import nodebox.node.Port;

public class PortAttributesEvent extends NodeEvent {

    private final Port port;

    public PortAttributesEvent(Node source, Port port) {
        super(source);
        this.port = port;
    }

    
}
