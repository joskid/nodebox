package nodebox.client;

import nodebox.node.*;

import javax.swing.*;
import java.awt.*;

public class TextWindow extends AbstractPortEditor {

    private JTextArea textArea;

    public TextWindow(Port port) {
        super(parameter);
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
            JOptionPane.showMessageDialog(this, "Error while saving port: " + ee.getMessage(), "Parameter error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public void valueChanged(Parameter source) {
        // Don't change the expression area if the expression is the same.
        // This would cause an infinite loop of setExpression/valueChanged calls.
        if (textArea.getText().equals(getPort().asString())) return;
        textArea.setText(getPort().asString());
    }

    public static void main(String[] args) {
        NodeLibrary testLibrary = new NodeLibrary("test");
        Node node = Node.ROOT_NODE.newInstance(testLibrary, "test");
        Parameter pText = node.addParameter("text", Parameter.Type.STRING);
        AbstractPortEditor win = new TextWindow(pText);
        win.setVisible(true);
    }

}
