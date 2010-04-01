package nodebox.library.geometry;

import nodebox.graphics.Geometry;
import nodebox.graphics.Path;
import nodebox.node.CookContext;
import nodebox.node.Macro;
import nodebox.node.Port;

/**
 * the Ellipse node creates ellipses and circles.
 */
public class Ellipse extends ColoredGeometryNode {

    private final Port pX, pY, pWidth, pHeight;

    public Ellipse(Macro parent) {
        super(parent);
        pX = createPort("x", Float.class, Port.Direction.IN);
        pY = createPort("y", Float.class, Port.Direction.IN);
        pWidth = createPort("width", Float.class, Port.Direction.IN);
        pWidth.setValue(100f);
        pHeight = createPort("height", Float.class, Port.Direction.IN);
        pHeight.setValue(100f);
        addColorPorts();
    }

    @Override
    public Geometry cookGeometry(CookContext context) {
        Path p = new Path();
        p.ellipse(pX.asFloat(), pY.asFloat(), pWidth.asFloat(), pHeight.asFloat());
        color(p);
        return p.asGeometry();
    }

}
