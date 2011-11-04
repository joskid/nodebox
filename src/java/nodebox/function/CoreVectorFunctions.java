package nodebox.function;

import nodebox.graphics.Geometry;
import nodebox.graphics.Path;
import nodebox.graphics.Point;

/**
 * Core vector function library.
 */
public class CoreVectorFunctions {

    public static final FunctionLibrary LIBRARY;


    static {
        LIBRARY = JavaLibrary.ofClass("corevector", CoreVectorFunctions.class,
                "rect");
    }

    public static Geometry rect(Point position, double width, double height) {
        Path p = new Path();
        p.rect(position.getX(), position.getY(), width, height);
        return p.asGeometry();
    }


}
