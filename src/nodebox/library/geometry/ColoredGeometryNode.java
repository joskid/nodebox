package nodebox.library.geometry;

import nodebox.graphics.Color;
import nodebox.graphics.Path;
import nodebox.node.Macro;
import nodebox.node.Port;

import static com.google.common.base.Preconditions.checkState;

/**
 * Abstract class that provides color ports for geometry generators.
 */
public abstract class ColoredGeometryNode extends AbstractGeometryNode {

    private Port pFill, pStroke, pStrokeWidth;

    public ColoredGeometryNode(Macro parent) {
        super(parent);
    }

    protected void addColorPorts() {
        checkState(!hasPort("fill"), "addColorPorts can only be called once.");
        pFill = createPort("fill", Color.class, Port.Direction.IN);
        pStroke = createPort("stroke", Color.class, Port.Direction.IN);
        pStrokeWidth = createPort("strokeWidth", Float.class, Port.Direction.IN);
    }

    /**
     * Set the color of the given path to the value of the color/stroke ports.
     * <p/>
     * If the strokeWidth is zero, no stroke color is set.
     *
     * @param p the path to color.
     */
    protected void color(Path p) {
        checkState(hasPort("fill"), "No color ports found. Did you forget to call addColorPorts in your constructor?");
        p.setFillColor(pFill.asColor());
        if (pStrokeWidth.asFloat() > 0) {
            p.setStrokeColor(pStroke.asColor());
            p.setStrokeWidth(pStrokeWidth.asFloat());
        }
    }


}
