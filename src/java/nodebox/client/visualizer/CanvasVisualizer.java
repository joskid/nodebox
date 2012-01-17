package nodebox.client.visualizer;

import nodebox.graphics.Canvas;

import java.awt.*;
import java.awt.geom.Dimension2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

public class CanvasVisualizer implements Visualizer {

    public static final CanvasVisualizer INSTANCE = new CanvasVisualizer();

    private CanvasVisualizer() {
    }

    public boolean accepts(List objects, Class listClass) {
        return nodebox.graphics.Canvas.class.isAssignableFrom(listClass);
    }

    public Rectangle2D getBounds(List objects, Dimension2D viewerSize) {
        // HACK only gets the bounds of the first canvas.
        checkArgument(objects.size() > 0);
        Canvas firstCanvas = (Canvas) objects.get(0);
        return firstCanvas.getBounds().getRectangle2D();
    }

    public Point2D getOffset(List objects, Dimension2D size) {
        return new Point2D.Double(size.getWidth() / 2, size.getHeight() / 2);
    }

    @SuppressWarnings("unchecked")
    public void draw(Graphics2D g, List objects) {
        checkArgument(objects.size() > 0);
        Canvas firstCanvas = (Canvas) objects.get(0);
        drawCanvasBounds(g, firstCanvas);
        GrobVisualizer.drawGrobs(g, objects);
    }

    private void drawCanvasBounds(Graphics2D g, Canvas canvas) {
        Rectangle2D canvasBounds = canvas.getBounds().getRectangle2D();
        g.setColor(Color.DARK_GRAY);
        g.setStroke(new BasicStroke(1f));
        g.draw(canvasBounds);
    }

}
