package nodebox.library.geometry;

import nodebox.graphics.Geometry;
import nodebox.node.*;

/**
 * This node serves as the base template for writing your own geometry generators.
 * <p/>
 * Subclasses don't override the cook method, but the cookGeometry node, which returns Geometry.
 *
 * AbstractGeometryNode creates one output port, result, that will be automatically set to the return value
 * of cookGeometry.
 */
public abstract class AbstractGeometryNode extends Node {

    private final Port result;

    public AbstractGeometryNode(Macro parent) {
        super(parent);
        setAttributes(NodeAttributes.builder().description("Template for creating nodes that generate geometry.").build());
        result = createPort("result", Geometry.class, Port.Direction.OUT);
        result.setAttributes(PortAttributes.builder().label("Geometry").build());
    }

    @Override
    public void cook(CookContext context) throws RuntimeException {
        result.setValue(cookGeometry(context));
    }

    /**
     * This method does the actual cooking for geometry nodes.
     *
     * It is called by cook. The result value of this method is placed in the "result" port.
     *
     * @param context the cook context
     * @return the newly created Geometry object.
     */
    public abstract Geometry cookGeometry(CookContext context);

}
