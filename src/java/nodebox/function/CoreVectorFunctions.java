package nodebox.function;

import nodebox.graphics.Geometry;
import nodebox.graphics.Path;
import nodebox.graphics.Point;
import org.python.google.common.collect.ImmutableList;

import java.util.List;

/**
 * Core vector function library.
 */
public class CoreVectorFunctions {

    public static final FunctionLibrary LIBRARY;


    static {
        LIBRARY = JavaLibrary.ofClass("corevector", CoreVectorFunctions.class,
                "rect", "pointToValues", "valuesToPoint");
    }

    public static Geometry rect(Point position, double width, double height) {
        Path p = new Path();
        p.rect(position.getX(), position.getY(), width, height);
        return p.asGeometry();
    }

    public static List<Double> pointToValues(Point point) {
        return ImmutableList.of(point.getX(), point.getY());
    }

    public static Point valuesToPoint(double x, double y) {
        return new Point(x, y);
    }

}
