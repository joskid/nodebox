package nodebox.library.geometry;

import nodebox.graphics.Color;
import nodebox.graphics.Geometry;
import nodebox.graphics.Path;
import nodebox.node.CookContext;
import nodebox.node.Macro;
import nodebox.node.Port;

/**
 * Rect generates rectangles and rounded rectangles.
 */
public class Rect extends ColoredGeometryNode {

    private final Port pX, pY, pWidth, pHeight, pRx, pRy;

    public Rect(Macro parent) {
        super(parent);
        pX = createPort("x", Float.class, Port.Direction.IN);
        pY = createPort("y", Float.class, Port.Direction.IN);
        pWidth = createPort("width", Float.class, Port.Direction.IN);
        pWidth.setValue(100f);
        pHeight = createPort("height", Float.class, Port.Direction.IN);
        pHeight.setValue(100f);
        pRx = createPort("rx", Float.class, Port.Direction.IN);
        pRy = createPort("ry", Float.class, Port.Direction.IN);
        addColorPorts();
    }

    @Override
    public Geometry cookGeometry(CookContext context) {
        Path p = new Path();
        // No rounded corners.
        if (pRx.asFloat() == 0f && pRy.asFloat() == 0) {
            p.rect(pX.asFloat(), pY.asFloat(), pWidth.asFloat(), pHeight.asFloat());
        } else {
            p.roundedRect(pX.asFloat(), pY.asFloat(), pWidth.asFloat(), pHeight.asFloat(), pRx.asFloat(), pRy.asFloat());
        }
        color(p);
        return p.asGeometry();
    }

}
