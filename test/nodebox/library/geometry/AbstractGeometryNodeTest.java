package nodebox.library.geometry;

import nodebox.graphics.Geometry;
import nodebox.graphics.Path;
import nodebox.node.*;

/**
 * Test the abstract base class for geometry nodes.
 */
public class AbstractGeometryNodeTest extends NodeTestCase {

    public static class MyGeometryNode extends AbstractGeometryNode {

        public MyGeometryNode(Macro parent) {
            super(parent);
        }

        @Override
        public Geometry cookGeometry(CookContext context) {
            Path p = new Path();
            p.rect(10, 20, 30, 40);
            return p.asGeometry();
        }

    }

    public void testPorts() {
        Node n = rootMacro.createChild(MyGeometryNode.class);
        assertTrue(n.hasPort("result"));
        Port pResult = n.getPort("result");
        assertEquals("Geometry", pResult.getLabel());
        assertEquals(Geometry.class, pResult.getDataClass());
    }

    public void testCooking() {
        Node n = rootMacro.createChild(MyGeometryNode.class);
        n.execute(new CookContext());
        Object result = n.getValue("result");
        assertTrue(result instanceof Geometry);
        Geometry geometry = (Geometry) result;
        assertEquals(nodebox.graphics.Rect.centeredRect(10, 20, 30, 40), geometry.getBounds());
    }

}
