package nodebox.client;

import java.awt.*;

public class PortPane extends Pane {

    private final PaneHeader paneHeader;
    private final PortView portView;
    private final NodeBoxDocument document;

    public PortPane(NodeBoxDocument document) {
        this.document = document;
        setLayout(new BorderLayout());
        paneHeader = new PaneHeader(this);
        NButton metadataButton = new NButton("Metadata", "res/port-metadata.png");
        metadataButton.setActionMethod(this, "editMetadata");
        paneHeader.add(metadataButton);
        portView = new PortView(this);
        add(paneHeader, BorderLayout.NORTH);
        add(portView, BorderLayout.CENTER);
    }

    public PortView getPortView() {
        return portView;
    }

    public Pane duplicate() {
        return new PortPane(document);
    }

    public String getPaneName() {
        return "Parameters";
    }

    public PaneHeader getPaneHeader() {
        return paneHeader;
    }

    public PaneView getPaneView() {
        return portView;
    }

    public void editMetadata() {
        document.editMetadata();
    }

}
