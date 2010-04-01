package nodebox.library.geometry;

import nodebox.graphics.Color;
import nodebox.graphics.Geometry;
import nodebox.graphics.Path;
import nodebox.node.CookContext;
import nodebox.node.Node;
import nodebox.node.NodeTestCase;

import java.util.List;

/**
 * Test the rect node.
 */
public class RectTest extends NodeTestCase {

    public void testDefaults() {
        Node n = rootMacro.createChild(Rect.class);
        n.execute(new CookContext());
        Object result = n.getValue("result");
        assertTrue(result instanceof Geometry);
        Geometry geometry = (Geometry) result;
        assertEquals(nodebox.graphics.Rect.centeredRect(0, 0, 100, 100), geometry.getBounds());
        List<Path> paths = geometry.getPaths();
        assertEquals(1, paths.size());
        Path path = paths.get(0);
        assertEquals(new Color(), path.getFillColor());
        assertNull(path.getStrokeColor());
        assertEquals(1f, path.getStrokeWidth());
        assertEquals(4, path.getPointCount());
    }

    public void testRoundedRectangles() {
        Node n = rootMacro.createChild(Rect.class);
        n.setValue("rx", 10f);
        n.setValue("ry", 5f);
        n.execute(new CookContext());
        Geometry geometry = (Geometry) n.getValue("result");
        assertEquals(nodebox.graphics.Rect.centeredRect(0, 0, 100, 100), geometry.getBounds());
        assertEquals(17, geometry.getPointCount());
    }

    public void testStrokeColor() {
        Node n = rootMacro.createChild(Rect.class);
        n.setValue("strokeWidth", 5f);
        n.execute(new CookContext());
        Geometry geometry = (Geometry) n.getValue("result");
        List<Path> paths = geometry.getPaths();
        assertEquals(1, paths.size());
        Path path = paths.get(0);
        assertEquals(new Color(), path.getFillColor());
        assertEquals(new Color(), path.getStrokeColor());
        assertEquals(5f, path.getStrokeWidth());
    }

}
