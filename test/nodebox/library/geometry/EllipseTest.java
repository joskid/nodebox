package nodebox.library.geometry;

import nodebox.graphics.Color;
import nodebox.graphics.Geometry;
import nodebox.graphics.Path;
import nodebox.node.CookContext;
import nodebox.node.Node;
import nodebox.node.NodeTestCase;

import java.util.List;

/**
 * Test the ellipse node.
 */
public class EllipseTest extends NodeTestCase {

    public void testDefaults() {
        Node n = rootMacro.createChild(Ellipse.class);
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
        assertEquals(13, path.getPointCount());
    }

}
