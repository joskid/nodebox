package nodebox.function;

import com.google.common.collect.ImmutableList;
import nodebox.graphics.Color;
import nodebox.graphics.Geometry;
import nodebox.graphics.Path;
import nodebox.graphics.Point;

import java.util.List;

/**
 * Core vector function library.
 */
public class CoreVectorFunctions {

    public static final FunctionLibrary LIBRARY;


    static {
        LIBRARY = JavaLibrary.ofClass("corevector", CoreVectorFunctions.class,
                "rect", "color", "pointToValues", "valuesToPoint");
    }

    public static Geometry rect(Point position, double width, double height) {
        Path p = new Path();
        p.rect(position.getX(), position.getY(), width, height);
        return p.asGeometry();
    }

    public static Geometry color(Geometry geometry, Color fill, Color stroke, double strokeWidth) {
        Geometry copy = geometry.clone();
        for (Path path:copy.getPaths()) {
            path.setFill(fill);
            path.setStroke(stroke);
            path.setStrokeWidth(strokeWidth);
        }
        return copy;
    }

    public static List<Double> pointToValues(Point point) {
        return ImmutableList.of(point.getX(), point.getY());
    }

    public static Point valuesToPoint(double x, double y) {
        return new Point(x, y);
    }

}
