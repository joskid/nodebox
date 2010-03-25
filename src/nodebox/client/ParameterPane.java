package nodebox.client;

import nodebox.node.Node;

import javax.swing.*;
import java.awt.*;

public class ParameterPane extends Pane {

    private PaneHeader paneHeader;
    private PortView portView;
    private Node node;

    public ParameterPane(NodeBoxDocument document) {
        super(document);
        setLayout(new BorderLayout());
        paneHeader = new PaneHeader(this);
        portView = new PortView();
        add(paneHeader, BorderLayout.NORTH);
        add(portView, BorderLayout.CENTER);
        setNode(document.getActiveNode());
    }

    public Pane clone() {
        return new ParameterPane(getDocument());
    }

    public String getPaneName() {
        return "Parameters";
    }

    public PaneHeader getPaneHeader() {
        return paneHeader;
    }

    public PaneView getPaneView() {
        return portView;
    }

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        if (this.node == node) return;
        this.node = node;
        portView.setNode(node);
    }

    @Override
    public void focusedNodeChanged(Node activeNode) {
        setNode(activeNode);
    }

}
