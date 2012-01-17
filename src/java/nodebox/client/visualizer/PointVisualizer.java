package nodebox.client.visualizer;

import nodebox.graphics.Path;
import nodebox.graphics.Point;

import java.awt.*;
import java.awt.geom.Dimension2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

public class PointVisualizer implements Visualizer {

    public static final PointVisualizer INSTANCE = new PointVisualizer();
    public static final double POINT_SIZE = 4;

    private PointVisualizer() {
    }

    public boolean accepts(List objects, Class listClass) {
        return listClass == Point.class;
    }

    public Rectangle2D getBounds(List objects, Dimension2D viewerSize) {
        Rectangle2D.Double bounds = new Rectangle2D.Double();
        for (Object o : objects) {
            Point pt = (Point) o;
            bounds.add(pt.toPoint2D());
        }
        return bounds;
    }

    public Point2D getOffset(List objects, Dimension2D size) {
        return new Point2D.Double(size.getWidth() / 2, size.getHeight() / 2);
    }

    @SuppressWarnings("unchecked")
    public void draw(Graphics2D g, List objects) {
        drawPoints(g, objects);
    }

    public static void drawPoints(Graphics2D g, Iterable<Point> points) {
        Path onCurves = new Path();
        Path offCurves = new Path();
        onCurves.setFill(new nodebox.graphics.Color(0, 0, 1));
        offCurves.setFill(new nodebox.graphics.Color(1, 0, 0));
        for (Point point : points) {
            if (point.isOnCurve()) {
                onCurves.ellipse(point.x, point.y, POINT_SIZE, POINT_SIZE);
            } else {
                offCurves.ellipse(point.x, point.y, POINT_SIZE, POINT_SIZE);
            }
        }
        onCurves.draw(g);
        offCurves.draw(g);
    }

}
