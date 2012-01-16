package nodebox.client;

import nodebox.ui.PaneView;

import javax.swing.*;
import java.awt.*;

/**
 * The Data Sheet presents data in a spreadsheet view
 */
public class DataSheet extends JPanel implements PaneView {

    private final NodeBoxDocument document;

    public DataSheet(NodeBoxDocument document) {
        super(new BorderLayout());
        this.document = document;
        add(new JLabel("Data Sheet"), BorderLayout.CENTER);
    }

}
