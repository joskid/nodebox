package nodebox.client;

import nodebox.client.port.*;
import nodebox.node.*;
import nodebox.node.event.NodeAttributesChangedEvent;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PortView extends JComponent implements PaneView, NodeEventListener {

    private static Logger logger = Logger.getLogger("nodebox.client.PortView");

    private static final Map<PortAttributes.Widget, Class> CONTROL_MAP;

    // At this width, the label background lines out with the pane header divider.
    public static final int LABEL_WIDTH = 114;

    static {
        CONTROL_MAP = new HashMap<PortAttributes.Widget, Class>();
        CONTROL_MAP.put(PortAttributes.Widget.ANGLE, FloatControl.class);
        CONTROL_MAP.put(PortAttributes.Widget.COLOR, ColorControl.class);
        CONTROL_MAP.put(PortAttributes.Widget.FILE, FileControl.class);
        CONTROL_MAP.put(PortAttributes.Widget.FLOAT, FloatControl.class);
        CONTROL_MAP.put(PortAttributes.Widget.FONT, FontControl.class);
        CONTROL_MAP.put(PortAttributes.Widget.GRADIENT, null);
        CONTROL_MAP.put(PortAttributes.Widget.IMAGE, ImageControl.class);
        CONTROL_MAP.put(PortAttributes.Widget.INT, IntControl.class);
        CONTROL_MAP.put(PortAttributes.Widget.MENU, MenuControl.class);
        CONTROL_MAP.put(PortAttributes.Widget.SEED, IntControl.class);
        CONTROL_MAP.put(PortAttributes.Widget.STRING, StringControl.class);
        CONTROL_MAP.put(PortAttributes.Widget.TEXT, TextControl.class);
        CONTROL_MAP.put(PortAttributes.Widget.TOGGLE, ToggleControl.class);
        CONTROL_MAP.put(PortAttributes.Widget.STAMP_EXPRESSION, StampExpressionControl.class);
    }

    private Node node;

    private JPanel controlPanel;

    public PortView() {
        setLayout(new BorderLayout());
        controlPanel = new ControlPanel(new GridBagLayout());
        // controlPanel = new JPanel(new GridBagLayout());
        //controlPanel.setOpaque(false);
        //controlPanel.setBackground(Theme.getInstance().getParameterViewBackgroundColor());
        JScrollPane scrollPane = new JScrollPane(controlPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane, BorderLayout.CENTER);
    }

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        // If this is the first time the node is set, register to the library.
        // This only needs to be done once, and this view is never re-used in another library.
        if (this.node == null) {
            node.getLibrary().addListener(this);
        }
        this.node = node;
        rebuildInterface();
        validate();
        repaint();
    }

    private void rebuildInterface() {
        controlPanel.removeAll();
        if (node == null) return;
        int rowindex = 0;


        for (Port p : node.getPorts()) {
            // Parameters starting with underscores are hidden.
            if (p.getName().startsWith("_")) continue;
            Class widgetClass = CONTROL_MAP.get(p.getWidget());
            JComponent control;
            if (widgetClass != null) {
                control = (JComponent) constructControl(widgetClass, p);
            } else {
                control = new JLabel("  ");
            }
            PortRow portRow = new PortRow(p, control);
            // portRow.setEnabled(p.isEnabled());
            GridBagConstraints rowConstraints = new GridBagConstraints();
            rowConstraints.gridx = 0;
            rowConstraints.gridy = rowindex;
            rowConstraints.fill = GridBagConstraints.HORIZONTAL;
            rowConstraints.weightx = 1.0;
            controlPanel.add(portRow, rowConstraints);
            rowindex++;
        }

        if (rowindex == 0) {
            JLabel noParameters = new JLabel("No parameters");
            noParameters.setFont(Theme.SMALL_BOLD_FONT);
            noParameters.setForeground(Theme.TEXT_NORMAL_COLOR);
            controlPanel.add(noParameters);
        }
        JLabel filler = new JLabel();
        GridBagConstraints fillerConstraints = new GridBagConstraints();
        fillerConstraints.gridx = 0;
        fillerConstraints.gridy = rowindex;
        fillerConstraints.fill = GridBagConstraints.BOTH;
        fillerConstraints.weighty = 1.0;
        fillerConstraints.gridwidth = GridBagConstraints.REMAINDER;
        controlPanel.add(filler, fillerConstraints);
        revalidate();
    }

    private PortControl constructControl(Class controlClass, Port p) {
        try {
            Constructor constructor = controlClass.getConstructor(Port.class);
            return (PortControl) constructor.newInstance(p);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Cannot construct control", e);
            throw new AssertionError("Cannot construct control:" + e);
        }
    }

    public void receive(NodeEvent event) {
        if (event.getSource() != node) return;
        if (!(event instanceof NodeAttributesChangedEvent)) return;
        rebuildInterface();
    }

    private class ControlPanel extends JPanel {
        private ControlPanel(LayoutManager layout) {
            super(layout);
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (node == null) {
                Rectangle clip = g.getClipBounds();
                g.setColor(new Color(196, 196, 196));
                g.fillRect(clip.x, clip.y, clip.width, clip.height);
            } else {
                int height = getHeight();
                int width = getWidth();
                g.setColor(Theme.PORT_LABEL_BACKGROUND);
                g.fillRect(0, 0, LABEL_WIDTH - 3, height);
                g.setColor(new Color(146, 146, 146));
                g.fillRect(LABEL_WIDTH - 3, 0, 1, height);
                g.setColor(new Color(133, 133, 133));
                g.fillRect(LABEL_WIDTH - 2, 0, 1, height);
                g.setColor(new Color(112, 112, 112));
                g.fillRect(LABEL_WIDTH - 1, 0, 1, height);
                g.setColor(Theme.PORT_VALUE_BACKGROUND);
                g.fillRect(LABEL_WIDTH, 0, width - LABEL_WIDTH, height);
            }
        }
    }

    public static void main(String[] args) {
//        JFrame frame = new JFrame();
//        NodeLibraryManager manager = new NodeLibraryManager();
//        NodeLibrary testLibrary = new NodeLibrary("test");
//        Node n = manager.getMacro("corevector.rect").newInstance(testLibrary, "myrect");
//        PortView p = new PortView();
//        p.setMacro(n);
//        frame.setContentPane(p);
//        frame.setSize(500, 500);
//        frame.setVisible(true);
    }

}
