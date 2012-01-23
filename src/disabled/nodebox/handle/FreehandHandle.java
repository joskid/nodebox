package nodebox.handle;

import nodebox.graphics.GraphicsContext;
import nodebox.graphics.Point;
import nodebox.node.Node;
import nodebox.node.Port;

import java.util.Locale;

/**
 * Handle for the freehand node.
 */
public class FreehandHandle extends AbstractHandle {

    private final String pathPortName;
    private final Port port;
    private boolean newPath = true;
    private Point currentPoint;

    public FreehandHandle(Node node, String pathPortName) {
        super(node);
        this.pathPortName = pathPortName;
        this.port = node.getInput(pathPortName);
    }

    public void draw(GraphicsContext ctx) {
        if (currentPoint == null) return;
        ctx.nofill();
        ctx.stroke(0.5f);
        ctx.ellipse(currentPoint.x - 5, currentPoint.y - 5, 10, 10);
    }

    @Override
    public boolean mousePressed(Point pt) {
        newPath = true;
        return true;
    }

    @Override
    public boolean mouseMoved(Point pt) {
        currentPoint = pt;
        updateHandle();
        return true;
    }

    @Override
    public boolean mouseDragged(Point pt) {
        // Note that the freehand handle is not concerned with parsing the actual path.
        // All it does is append new points to the string.
        // The actual path parsing is done by the freehand node code.
        currentPoint = pt;
        String pathString = port.stringValue();
        if (newPath) {
            if (pathString.isEmpty()) {
                pathString = " ";
            }
            pathString += "M ";
            newPath = false;
        }
        // Use US locale, otherwise the code might generate a "," instead of a "." as the floating point.
        pathString += String.format(Locale.US, "%.2f %.2f ", pt.getX(), pt.getY());
        silentSet(pathPortName, pathString);
        updateHandle();
        return true;
    }

    @Override
    public boolean mouseReleased(Point pt) {
        stopCombiningEdits();
        return true;
    }
}