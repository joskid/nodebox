package nodebox.client.visualizer;

import nodebox.graphics.Color;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

/**
 * Visualizes Color objects.
 */
public final class ColorVisualizer implements Visualizer {

    public static final ColorVisualizer INSTANCE = new ColorVisualizer();
    private static final int COLOR_SIZE = 30;
    public static final int COLOR_MARGIN = 10;

    private ColorVisualizer() {
    }

    public boolean accepts(List objects, Class listClass) {
        return Color.class.isAssignableFrom(listClass);
    }


    public Rectangle2D getBounds(List objects, Dimension2D size) {
        return new Rectangle2D.Double(0, 0, objects.size() * 12, 10);
    }

    public Point2D getOffset(List objects, Dimension2D size) {
        return new Point2D.Double(10, 10);
    }

    @SuppressWarnings("unchecked")
    public void draw(Graphics2D g, List objects) {
        AffineTransform t = g.getTransform();
        int x = 0;
        int y = 0;

        for (Object o : objects) {
            Color c = (Color) o;
            drawColor(g, c, x, y);
            x += COLOR_SIZE + COLOR_MARGIN;
            if (x > 500) {
                x = 0;
                y += COLOR_SIZE + COLOR_MARGIN;
            }
        }
    }

    private void drawColor(Graphics2D g, Color c, int x, int y) {
        g.setColor(java.awt.Color.WHITE);
        g.fillRoundRect(x, y, COLOR_SIZE + 6, COLOR_SIZE + 6, 3, 3);
        g.setColor(java.awt.Color.LIGHT_GRAY);
        g.drawRoundRect(x, y, COLOR_SIZE + 6, COLOR_SIZE + 6, 3, 3);
        g.setColor(c.getAwtColor());
        g.fillRect(x + 3, y + 3, COLOR_SIZE, COLOR_SIZE);
    }

}
