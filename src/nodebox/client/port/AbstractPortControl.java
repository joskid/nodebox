package nodebox.client.port;

import nodebox.node.NodeEvent;
import nodebox.node.Port;
import nodebox.node.event.ValueChangedEvent;

import javax.swing.*;

public abstract class AbstractPortControl extends JComponent implements PortControl {

    protected final Port port;
    private boolean disabled;

    protected AbstractPortControl(Port port) {
        this.port = port;
    }

    public Port getPort() {
        return port;
    }

    @Override
    public void addNotify() {
        super.addNotify();
        port.getLibrary().addListener(this);
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        port.getLibrary().removeListener(this);
    }

    public void receive(NodeEvent event) {
        if (!(event instanceof ValueChangedEvent)) return;
        if (((ValueChangedEvent) event).getPort() != port) return;
        setValueForControl(port.getValue());
    }

    public void valueChanged(Port source) {
        if (port != source) return;
        setValueForControl(source.getValue());
    }

}
