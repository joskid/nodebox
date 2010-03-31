package nodebox.node.event;

import nodebox.node.Node;
import nodebox.node.Port;

/**
 * A ValueChangedEvent is fired whenever the value of a port changes.
 */
public class ValueChangedEvent extends AbstractPortEvent {

    public ValueChangedEvent(Node source, Port port) {
        super(source, port);
    }

}
