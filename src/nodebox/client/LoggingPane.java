package nodebox.client;

import nodebox.node.Macro;
import nodebox.node.event.NodeEventListener;
import nodebox.node.event.NodeEvent;
import nodebox.node.event.NodeUpdatedEvent;

import javax.swing.*;
import java.awt.*;

public class LoggingPane extends Pane implements NodeEventListener {

    private LoggingArea loggingArea;
    private Macro macro;

    public LoggingPane(NodeBoxDocument document) {
        super(document);
        document.getNodeLibrary().addListener(this);
        setLayout(new BorderLayout(0, 0));
        PaneHeader paneHeader = new PaneHeader(this);
        loggingArea = new LoggingArea(80, 30);
        loggingArea.setFont(Theme.INFO_FONT);
        loggingArea.setEditable(false);
        JScrollPane loggingScroll = new JScrollPane(loggingArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add(paneHeader, BorderLayout.NORTH);
        add(loggingScroll, BorderLayout.CENTER);
    }

    @Override
    public void currentMacroChanged(Macro activeNetwork) {
        this.macro = activeNetwork;
    }

    public void receive(NodeEvent event) {
        if (event.getSource() != this.macro) return;
        if (event instanceof NodeUpdatedEvent) {
            StringBuffer sb = new StringBuffer();
            if (macro.hasError()) {
                sb.append(macro.getError().toString());
            }
            loggingArea.setText(sb.toString());
        }
    }

    public Pane clone() {
        return new LoggingPane(getDocument());
    }

    public String getPaneName() {
        return "Log";
    }

    public PaneHeader getPaneHeader() {
        return null;
    }

    public PaneView getPaneView() {
        return loggingArea;
    }

    public static class LoggingArea extends JTextArea implements PaneView {

        public LoggingArea(int rows, int columns) {
            super(rows, columns);
        }
    }
}
