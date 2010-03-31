package nodebox.node.event;

import nodebox.node.Node;

public class RenderedChildChangedEvent extends AbstractNodeEvent {

    public Node renderedChild;

    public RenderedChildChangedEvent(Node source, Node renderedChild) {
        super(source);
        this.renderedChild = renderedChild;
    }

    public Node getRenderedChild() {
        return renderedChild;
    }

    @Override
    public String toString() {
        return "RenderedChildChangedEvent{" +
                "source=" + getSource() +
                "renderedChild=" + renderedChild +
                '}';
    }
}
