package nodebox.client;

import nodebox.ui.NButton;
import nodebox.ui.Pane;
import nodebox.ui.PaneHeader;
import nodebox.ui.PaneView;

import javax.swing.*;
import java.awt.*;

public class ViewerPane extends Pane {

    private final NodeBoxDocument document;
    private final PaneHeader paneHeader;
    private final JTabbedPane tabbedPane;
    private final Viewer viewer;
    private final DataSheet dataSheet;
    private final NButton handlesCheck, pointsCheck, pointNumbersCheck, originCheck;

    public ViewerPane(final NodeBoxDocument document) {
        this.document = document;
        setLayout(new BorderLayout(0, 0));
        paneHeader = new PaneHeader(this);
        handlesCheck = new NButton(NButton.Mode.CHECK, "Handles");
        handlesCheck.setChecked(true);
        handlesCheck.setActionMethod(this, "toggleHandles");
        pointsCheck = new NButton(NButton.Mode.CHECK, "Points");
        pointsCheck.setActionMethod(this, "togglePoints");
        pointNumbersCheck = new NButton(NButton.Mode.CHECK, "Point Numbers");
        pointNumbersCheck.setActionMethod(this, "togglePointNumbers");
        originCheck = new NButton(NButton.Mode.CHECK, "Origin");
        originCheck.setActionMethod(this, "toggleOrigin");
        paneHeader.add(handlesCheck);
        paneHeader.add(pointsCheck);
        paneHeader.add(pointNumbersCheck);
        paneHeader.add(originCheck);
        add(paneHeader, BorderLayout.NORTH);

        viewer = new Viewer(document);
        dataSheet = new DataSheet(document);
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Viewer", null, viewer);
        tabbedPane.addTab("Data", null, dataSheet);
        add(tabbedPane, BorderLayout.CENTER);
    }

    public Viewer getViewer() {
        return viewer;
    }

    public void toggleHandles() {
        viewer.setShowHandle(handlesCheck.isChecked());
    }

    public void togglePoints() {
        viewer.setShowPoints(pointsCheck.isChecked());
    }

    public void togglePointNumbers() {
        viewer.setShowPointNumbers(pointNumbersCheck.isChecked());
    }

    public void toggleOrigin() {
        viewer.setShowOrigin(originCheck.isChecked());
    }

    public Pane duplicate() {
        return new ViewerPane(document);
    }

    public String getPaneName() {
        return "Viewer";
    }

    public PaneHeader getPaneHeader() {
        return paneHeader;
    }

    public PaneView getPaneView() {
        return viewer;
    }

    public DataSheet getDataSheet() {
        return dataSheet;
    }
}
