package nodebox.client;

import nodebox.node.Node;
import nodebox.node.NodeLibrary;
import nodebox.node.Port;

import javax.swing.*;
import java.awt.*;

public class ExpressionWindow extends AbstractPortEditor {

    private JTextArea expressionArea;
    private JTextArea errorArea;

    public ExpressionWindow(Port port) {
        super(port);
    }

    public Component getContentArea() {
        expressionArea = new JTextArea(getPort().getExpression());
        expressionArea.setFont(Theme.EDITOR_FONT);
        expressionArea.setBorder(null);
        JScrollPane expressionScroll = new JScrollPane(expressionArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        expressionScroll.setBorder(BorderFactory.createEtchedBorder());

        errorArea = new JTextArea();
        errorArea.setFont(Theme.EDITOR_FONT);
        errorArea.setBorder(null);
        errorArea.setBackground(Theme.EXPRESSION_ERROR_BACKGROUND_COLOR);
        errorArea.setEditable(false);
        errorArea.setForeground(Theme.EXPRESSION_ERROR_FOREGROUND_COLOR);
        JScrollPane errorScroll = new JScrollPane(errorArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        errorScroll.setBorder(BorderFactory.createEtchedBorder());

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, expressionScroll, errorScroll);
        split.setBorder(null);
        split.setDividerLocation(150);
        split.setDividerSize(2);
        return split;
    }

    public void valueChanged(Port source) {
        // Don't change the expression area if the expression is the same.
        // This would cause an infinite loop of setExpression/valueChanged calls.
        if (expressionArea.getText().equals(getPort().getExpression())) return;
        expressionArea.setText(getPort().getExpression());
    }

    public boolean save() {
        expressionArea.requestFocus();
        getPort().setExpression(expressionArea.getText());
        if (getPort().hasExpressionError()) {
            errorArea.setText(getPort().getExpressionError().toString());
            return false;
        } else {
            errorArea.setText("");
            return true;
        }
    }

    public static void main(String[] args) {
//        NodeLibrary testLibrary = new NodeLibrary("test");
//        Node node = Node.ROOT_NODE.newInstance(testLibrary, "test");
//        Parameter pX = node.addParameter("x", Parameter.Type.FLOAT);
//        AbstractPortEditor win = new ExpressionWindow(pX);
//        win.setVisible(true);
    }

}
