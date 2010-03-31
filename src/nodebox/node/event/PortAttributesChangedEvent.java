package nodebox.node.event;

import nodebox.node.Node;
import nodebox.node.Port;

/**
 * A PortAttributesChangedEvent is fired whenever attributes on the port are modified.
 *
 * @see Port#setAttributes(nodebox.node.PortAttributes)
 */
public class PortAttributesChangedEvent extends AbstractPortEvent {

    public PortAttributesChangedEvent(Node source, Port port) {
        super(source, port);
    }

}
