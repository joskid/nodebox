package nodebox.client;

import nodebox.client.port.*;
import nodebox.node.Node;
import nodebox.node.Port;
import nodebox.ui.Pane;
import nodebox.ui.PaneView;
import nodebox.ui.Theme;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PortView extends JComponent implements PaneView, PortControl.OnValueChangeListener {

    private static Logger logger = Logger.getLogger("nodebox.client.PortView");

    private static final Map<Port.Widget, Class> CONTROL_MAP;

    // At this width, the label background lines out with the pane header divider.
    public static final int LABEL_WIDTH = 114;

    static {
        CONTROL_MAP = new HashMap<Port.Widget, Class>();
        CONTROL_MAP.put(Port.Widget.ANGLE, FloatControl.class);
        CONTROL_MAP.put(Port.Widget.COLOR, ColorControl.class);
        CONTROL_MAP.put(Port.Widget.FILE, FileControl.class);
        CONTROL_MAP.put(Port.Widget.FLOAT, FloatControl.class);
        CONTROL_MAP.put(Port.Widget.FONT, FontControl.class);
        CONTROL_MAP.put(Port.Widget.GRADIENT, null);
        CONTROL_MAP.put(Port.Widget.IMAGE, ImageControl.class);
        CONTROL_MAP.put(Port.Widget.INT, IntControl.class);
        CONTROL_MAP.put(Port.Widget.MENU, StringControl.class); // TODO MenuControl
        CONTROL_MAP.put(Port.Widget.SEED, IntControl.class);
        CONTROL_MAP.put(Port.Widget.STRING, StringControl.class);
        CONTROL_MAP.put(Port.Widget.TEXT, StringControl.class); // TODO TextControl
        CONTROL_MAP.put(Port.Widget.TOGGLE, ToggleControl.class);
        CONTROL_MAP.put(Port.Widget.POINT, PointControl.class);
    }

    private final NodeBoxDocument document;
    private final Pane pane;
    private Node activeNode;
    private JPanel controlPanel;
    private Map<Port, PortControl> controlMap = new HashMap<Port, PortControl>();

    public PortView(Pane pane, NodeBoxDocument document) {
        this.pane = pane;
        this.document = document;
        setLayout(new BorderLayout());
        controlPanel = new ControlPanel(new GridBagLayout());
        // controlPanel = new JPanel(new GridBagLayout());
        //controlPanel.setOpaque(false);
        //controlPanel.setBackground(Theme.getInstance().getParameterViewBackgroundColor());
        JScrollPane scrollPane = new JScrollPane(controlPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane, BorderLayout.CENTER);
    }

    public NodeBoxDocument getDocument() {
        return document;
    }

    public Node getActiveNode() {
        return activeNode;
    }

    public void setActiveNode(Node node) {
        this.activeNode = node;
        rebuildInterface();
        validate();
        repaint();
    }

    /**
     * Fully rebuild the port view.
     */
    public void updateAll() {
        rebuildInterface();
    }

    /**
     * The port was updated, either its metadata, expression or value.
     * <p/>
     * Rebuild the interface for this port.
     *
     * @param port The updated port.
     */
    public void updatePort(Port port) {
        // TODO More granular rebuild.
        rebuildInterface();
    }

    /**
     * The value for a port was changed.
     * <p/>
     * Display the new value in the port's control UI.
     *
     * @param port  The changed port.
     * @param value The new port value.
     */
    public void updatePortValue(Port port, Object value) {
        // Nodes that have expressions set don't display the actual value but the expression.
        // Since the expression doesn't change, we can return immediately.
        if (port.hasExpression()) return;
        PortControl control = getControlForPort(port);
        if (control != null && control.isVisible()) {
            control.setValueForControl(value);
        }
    }

    /**
     * Check the enabled state of all Parameters and sync the port rows accordingly.
     */
    public void updateEnabledState() {
        for (Component c : controlPanel.getComponents())
            if (c instanceof PortRow) {
                PortRow row = (PortRow) c;
                if (row.isEnabled() != row.getPort().isEnabled()) {
                    row.setEnabled(row.getPort().isEnabled());
                }
            }
    }

    private void rebuildInterface() {
        controlPanel.removeAll();
        controlMap.clear();
        if (activeNode == null) return;
        int rowIndex = 0;
        for (Port p : activeNode.getInputs()) {
            // Ports starting with underscores are hidden.
            if (p.getName().startsWith("_")) continue;
            // Ports of which the values aren't persisted are hidden as well.
            if (p.isCustomType()) continue;
            Class widgetClass = CONTROL_MAP.get(p.getWidget());
            JComponent control;
            if (widgetClass != null) {
                control = (JComponent) constructControl(widgetClass, p);
                ((PortControl) control).setValueChangeListener(this);
                controlMap.put(p, (PortControl) control);
            } else {
                control = new JLabel("  ");
            }

            GridBagConstraints rowConstraints = new GridBagConstraints();
            rowConstraints.gridx = 0;
            rowConstraints.gridy = rowIndex;
            rowConstraints.fill = GridBagConstraints.HORIZONTAL;
            rowConstraints.weightx = 1.0;
            PortRow portRow = new PortRow(getDocument(), p, control);
            portRow.setEnabled(p.isEnabled());
            controlPanel.add(portRow, rowConstraints);
            rowIndex++;
        }

        if (rowIndex == 0) {
            JLabel noPorts = new JLabel("No ports");
            noPorts.setFont(Theme.SMALL_BOLD_FONT);
            noPorts.setForeground(Theme.TEXT_NORMAL_COLOR);
            controlPanel.add(noPorts);
        }
        JLabel filler = new JLabel();
        GridBagConstraints fillerConstraints = new GridBagConstraints();
        fillerConstraints.gridx = 0;
        fillerConstraints.gridy = rowIndex;
        fillerConstraints.fill = GridBagConstraints.BOTH;
        fillerConstraints.weighty = 1.0;
        fillerConstraints.gridwidth = GridBagConstraints.REMAINDER;
        controlPanel.add(filler, fillerConstraints);
        revalidate();
    }

    @SuppressWarnings("unchecked")
    private PortControl constructControl(Class controlClass, Port p) {
        try {
            Constructor constructor = controlClass.getConstructor(Port.class);
            return (PortControl) constructor.newInstance(p);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Cannot construct control", e);
            throw new AssertionError("Cannot construct control:" + e);
        }
    }

    public PortControl getControlForPort(Port p) {
        return controlMap.get(p);
    }

    public void onValueChange(PortControl control, Object newValue) {
        document.setPortValue(control.getPort().getName(), newValue);
    }

    private class ControlPanel extends JPanel {
        private ControlPanel(LayoutManager layout) {
            super(layout);
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (activeNode == null) {
                Rectangle clip = g.getClipBounds();
                g.setColor(new Color(196, 196, 196));
                g.fillRect(clip.x, clip.y, clip.width, clip.height);
            } else {
                int height = getHeight();
                int width = getWidth();
                g.setColor(Theme.PARAMETER_LABEL_BACKGROUND);
                g.fillRect(0, 0, LABEL_WIDTH - 3, height);
                g.setColor(new Color(146, 146, 146));
                g.fillRect(LABEL_WIDTH - 3, 0, 1, height);
                g.setColor(new Color(133, 133, 133));
                g.fillRect(LABEL_WIDTH - 2, 0, 1, height);
                g.setColor(new Color(112, 112, 112));
                g.fillRect(LABEL_WIDTH - 1, 0, 1, height);
                g.setColor(Theme.PARAMETER_VALUE_BACKGROUND);
                g.fillRect(LABEL_WIDTH, 0, width - LABEL_WIDTH, height);
            }
        }
    }
}
