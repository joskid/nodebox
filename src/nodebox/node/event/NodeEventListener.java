package nodebox.node.event;

/**
 * A NodeEventListener can receive events from the node library, such as when a node is added/removed, a value is changed, etc.
 * <p/>
 * NodeEventListeners receive all events and need to filter themselves by checking the class for the event.
 *
 * @see nodebox.node.NodeLibrary#addListener(NodeEventListener)
 * @see nodebox.node.NodeLibrary#removeListener(NodeEventListener)
 */
public interface NodeEventListener {

    public void receive(NodeEvent event);

}
