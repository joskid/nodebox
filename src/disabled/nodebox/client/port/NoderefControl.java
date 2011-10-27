package nodebox.client.port;

import nodebox.client.PathDialog;
import nodebox.client.Theme;
import nodebox.node.Port;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class NoderefControl extends AbstractPortControl implements ActionListener {

    private JTextField pathField;
    private JButton chooseButton;

    public NoderefControl(Port port) {
        super(port);
        setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));
        pathField = new JTextField();
        pathField.putClientProperty("JComponent.sizeVariant", "small");
        pathField.setPreferredSize(new Dimension(150, 19));
        pathField.setEditable(false);
        pathField.setFont(Theme.SMALL_BOLD_FONT);
        chooseButton = new JButton("...");
        chooseButton.putClientProperty("JButton.buttonType", "gradient");
        chooseButton.setPreferredSize(new Dimension(30, 27));
        chooseButton.addActionListener(this);
        add(pathField);
        add(chooseButton);
        setValueForControl(port.getValue());
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        pathField.setEnabled(enabled);
        chooseButton.setEnabled(enabled);
    }

    public void setValueForControl(Object v) {
        pathField.setText(v.toString());
    }

    public void actionPerformed(ActionEvent e) {
        String newPath = PathDialog.choosePath(port.getNode().getRoot(), port.stringValue());
        setPortValue(newPath);
    }
}
