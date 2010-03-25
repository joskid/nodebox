package nodebox.client.port;

import nodebox.node.NodeEventListener;
import nodebox.node.Port;

/**
 * Interface for controls. We also expect the control to extend JComponent or a subclass.
 * <p/>
 * Port controls are also NodeEventListeners, because they receive events from
 * their port. They do this by overriding addNotify() to register for the event, and removeNotify() to unregister.
 * <p/>
 * In the receive method they need to check if they are the source for the event.
 */
public interface PortControl extends NodeEventListener {

    public Port getPort();

    public void setValueForControl(Object v);

    public boolean isEnabled();

}
