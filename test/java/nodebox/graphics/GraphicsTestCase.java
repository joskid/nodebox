package nodebox.graphics;

import junit.framework.TestCase;

public abstract class GraphicsTestCase extends TestCase {

    protected final double SIDE = 50;

    protected void assertPointEquals(double x, double y, Point actual) {
        assertEquals(x, actual.x, 0.001);
        assertEquals(y, actual.y, 0.001);
    }

    protected void addRect(IGeometry g, double x, double y, double width, double height) {
        g.addPoint(x, y);
        g.addPoint(x + width, y);
        g.addPoint(x + width, y + height);
        g.addPoint(x, y + height);
    }

}
