package nodebox.client.visualizer;

import nodebox.graphics.Grob;

import java.awt.*;
import java.awt.geom.Dimension2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

/**
 * Visualizes NodeBox graphics objects.
 */
public final class GrobVisualizer implements Visualizer {

    public static final GrobVisualizer INSTANCE = new GrobVisualizer();

    private GrobVisualizer() {
    }

    public boolean accepts(List objects, Class listClass) {
        return Grob.class.isAssignableFrom(listClass);
    }

    public Rectangle2D getBounds(List objects, Dimension2D size) {
        Rectangle2D.Double bounds = new Rectangle2D.Double();
        for (Object o : objects) {
            Grob grob = (Grob) o;
            bounds.add(grob.getBounds().getRectangle2D());
        }
        return bounds;
    }

    public Point2D getOffset(List objects, Dimension2D size) {
        return new Point2D.Double(size.getWidth() / 2, size.getHeight() / 2);
    }

    @SuppressWarnings("unchecked")
    public void draw(Graphics2D g, List objects) {
        drawGrobs(g, objects);
    }
    
    public static void drawGrobs(Graphics2D g, Iterable<Grob> objects) {
        for (Grob grob : objects) {
            Shape oldClip = g.getClip();
            grob.draw(g);
            g.setClip(oldClip);
        }
    }

}
