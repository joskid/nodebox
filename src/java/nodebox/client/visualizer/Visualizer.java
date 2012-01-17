package nodebox.client.visualizer;

import java.awt.*;
import java.awt.geom.Dimension2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

public interface Visualizer {

    public boolean accepts(List objects, Class listClass);

    public Rectangle2D getBounds(List objects, Dimension2D viewerSize);

    public Point2D getOffset(List objects, Dimension2D viewerSize);

    public void draw(Graphics2D g, List objects);

}
