package nodebox.client;

import nodebox.node.Port;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public abstract class AbstractPortEditor extends JFrame implements PortEditor {

    private Port port;

    public AbstractPortEditor(Port port) {
        this.port = port;
        setTitle(port.getName());

        JPanel content = new JPanel(new BorderLayout(5, 5));
        content.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel buttonRow = new JPanel();
        buttonRow.setLayout(new BoxLayout(buttonRow, BoxLayout.LINE_AXIS));
        JButton cancelButton = new JButton(new CancelAction());
        JButton applyButton = new JButton(new SaveAction());
        JButton saveButton = new JButton(new SaveAndCloseAction());
        buttonRow.add(cancelButton);
        buttonRow.add(Box.createHorizontalGlue());
        buttonRow.add(applyButton);
        buttonRow.add(saveButton);

        content.add(getContentArea(), BorderLayout.CENTER);
        content.add(buttonRow, BorderLayout.SOUTH);
        setSize(500, 300);
        setContentPane(content);
    }

    public abstract Component getContentArea();

    /**
     * Save the editor changes to the port.
     *
     * @return false if an error occurred.
     */
    public abstract boolean save();

    public abstract void valueChanged(Port source);

    public Port getPort() {
        return port;
    }

    private class CancelAction extends AbstractAction {
        private CancelAction() {
            super("Cancel");
        }

        public void actionPerformed(ActionEvent e) {
            AbstractPortEditor.this.dispose();
        }
    }

    private class SaveAction extends AbstractAction {
        private SaveAction() {
            super("Save");
        }

        public void actionPerformed(ActionEvent e) {
            save();
        }
    }

    private class SaveAndCloseAction extends AbstractAction {
        private SaveAndCloseAction() {
            super("Save & Close");
        }

        public void actionPerformed(ActionEvent e) {
            if (save()) {
                dispose();
            }
        }
    }

}
