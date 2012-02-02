package nodebox.client;

import nodebox.node.Node;
import nodebox.node.NodeRenderException;
import nodebox.ui.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class NetworkPane extends Pane {

    private final NodeBoxDocument document;
    private final PaneHeader paneHeader;
    private final JLabel errorLabel;
    private final NetworkView networkView;

    public NetworkPane(NodeBoxDocument document) {
        this.document = document;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        paneHeader = new PaneHeader(this);
        paneHeader.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        NButton newNodeButton = new NButton("New Node", "res/network-new-node.png");
        newNodeButton.setToolTipText("New Node (TAB)");
        newNodeButton.setActionMethod(this, "showNodeSelectionDialog");
        paneHeader.add(newNodeButton);
        add(paneHeader);

        errorLabel = new JLabel("Error");
        errorLabel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        errorLabel.setBorder(null);
        errorLabel.setOpaque(true);
        errorLabel.setFont(Theme.SMALL_FONT);
        errorLabel.setPreferredSize(new Dimension(9999, 25));
        errorLabel.setMinimumSize(new Dimension(100, 25));
        errorLabel.setMaximumSize(new Dimension(9999, 25));
        errorLabel.setBackground(new Color(146, 22, 22));
        errorLabel.setForeground(Color.WHITE);
        errorLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                System.out.println("click");
            }
        });
        errorLabel.setVisible(false);
        add(errorLabel);

        networkView = new NetworkView(document);
        networkView.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        add(networkView);
    }

    public NetworkView getNetworkView() {
        return networkView;
    }

    public Pane duplicate() {
        return new NetworkPane(document);
    }

    public PaneHeader getPaneHeader() {
        return paneHeader;
    }

    public String getPaneName() {
        return "Network";
    }

    public PaneView getPaneView() {
        return networkView;
    }

    public void showNodeSelectionDialog() {
        document.showNodeSelectionDialog();
    }

    public void setError(Throwable e) {
        StringBuilder sb = new StringBuilder("<html>&nbsp;&nbsp;&nbsp;");
        if (e instanceof NodeRenderException) {
            Node node = ((NodeRenderException)e).getNode();
            sb.append("<b>");
            sb.append(node.getName());
            sb.append(":</b> ");
        }
        Throwable cause = getRootCause(e);
        sb.append(cause.getMessage());
        sb.append("</html>");
        errorLabel.setText(sb.toString());
        errorLabel.setVisible(true);
    }

    public Throwable getRootCause(Throwable e) {
        if (e.getCause() == null) return e;
        if (e.getCause() == e) return e;
        return getRootCause(e.getCause());
    }

    public void clearError() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
    }

//    public void propertyChange(PropertyChangeEvent evt) {
//        if (!evt.getPropertyName().equals(NetworkView.SELECT_PROPERTY)) return;
//        Set<NodeView> selection = (Set<NodeView>) evt.getNewValue();
//        // If there is no selection, set the active node to null.
//        if (selection == null || selection.isEmpty()) {
//            getDocument().setActiveNode((Node) null);
//        } else {
//            // If the active node is in the new selection leave the active node as is.
//            NodeView nv = networkView.getNodeView(getDocument().getActiveNode());
//            if (selection.contains(nv)) return;
//            // If there are multiple elements selected, the first one will be the active node.
//            NodeView firstElement = selection.iterator().next();
//            getDocument().setActiveNode(firstElement.getNode());
//        }
//    }

}
