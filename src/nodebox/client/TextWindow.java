package nodebox.client;

import nodebox.node.*;

import javax.swing.*;
import java.awt.*;

public class TextWindow extends AbstractPortEditor {

    private JTextArea textArea;

    public TextWindow(Port port) {
        super(port);
    }

    public Component getContentArea() {
        textArea = new JTextArea(getPort().asString());
        textArea.setFont(Theme.EDITOR_FONT);
        textArea.setBorder(null);
        JScrollPane textScroll = new JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        textScroll.setBorder(BorderFactory.createEtchedBorder());
        return textScroll;
    }

    public boolean save() {
        textArea.requestFocus();
        try {
            getPort().setValue(textArea.getText());
            return true;
        } catch (IllegalArgumentException ee) {
            JOptionPane.showMessageDialog(this, "Error while saving port: " + ee.getMessage(), "Port error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public void valueChanged(Port source) {
        // Don't change the expression area if the expression is the same.
        // This would cause an infinite loop of setExpression/valueChanged calls.
        if (textArea.getText().equals(getPort().asString())) return;
        textArea.setText(getPort().asString());
    }

    public static void main(String[] args) {
        NodeLibrary testLibrary = new NodeLibrary("test");
        Node node = new Node(testLibrary);
        Port pText = node.addPort("text", String.class, Port.Direction.IN);
        PortAttributes attributes = PortAttributes.builder().widget(PortAttributes.Widget.TEXT).build();
        pText.setAttributes(attributes);
        node.addPort(pText);
        AbstractPortEditor win = new TextWindow(pText);
        win.setVisible(true);
    }

}
