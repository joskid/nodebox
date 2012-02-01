package nodebox.function;

import nodebox.graphics.Point;
import nodebox.util.Geometry;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Basic math function library.
 */
public class MathFunctions {

    public static final FunctionLibrary LIBRARY;

    static {
        LIBRARY = JavaLibrary.ofClass("math", MathFunctions.class,
                "number",  "invert", "add", "subtract", "multiply", "divide", "sqrt",
                "makeNumbers",
                "range",
                "radians", "degrees", "angle", "distance", "coordinates", "reflect",
                "slowNumber");
    }

    public static double number(double n) {
        return n;
    }

    public static double add(double n1, double n2) {
        return n1 + n2;
    }

    public static double subtract(double n1, double n2) {
        return n1 - n2;
    }

    public static double multiply(double n1, double n2) {
        return n1 * n2;
    }

    public static double sqrt(double n) {
        return Math.sqrt(n);
    }

    public static double divide(double n1, double n2) {
        checkArgument(n2 != 0, "Divider cannot be zero.");
        return n1 / n2;
    }

    public static double invert(double n) {
        return -n;
    }

    public static Iterable<Double> makeNumbers(String s) {
        ArrayList<Double> numbers = new ArrayList<Double>();
        if (!s.isEmpty()) {
            for (String part : s.split(" ")) {
                numbers.add(Double.parseDouble(part));
            }
        }
        return numbers;
    }

    public static Iterable<Double> range(final double start, final double end, final double step) {
        return new Iterable<Double>() {
            public Iterator<Double> iterator() {
                return new RangeIterator(start, end, step);
            }
        };
    }

    private static final class RangeIterator implements Iterator<Double> {
        private final double start;
        private final double end;
        private final double step;
        private double next;

        private RangeIterator() {
            this(0, Double.POSITIVE_INFINITY, 1);
        }

        private RangeIterator(double end) {
            this(0, end, 1);
        }

        private RangeIterator(double start, double end) {
            this(start, end, 1);
        }

        private RangeIterator(double start, double end, double step) {
            this.start = start;
            this.end = end;
            this.step = step;
            this.next = this.start;
        }

        public boolean hasNext() {
            return next < end;
        }

        public Double next() {
            if (!hasNext())
                throw new NoSuchElementException();
            double result = next;
            next += step;
            return result;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    public static double radians(double degrees) {
        return Geometry.radians(degrees);
    }

    public static double degrees(double radians) {
        return Geometry.degrees(radians);
    }

    /**
     * Calculate the angle between two points.
     *
     * @param p1 The first point.
     * @param p2 The second point.
     * @return The angle in radians.
     */
    public static double angle(Point p1, Point p2) {
        return Geometry.angle(p1.x, p1.y, p2.x, p2.y);
    }

    /**
     * The distance between two points.
     */
    public static double distance(Point p1, Point p2) {
        return Geometry.distance(p1.x, p1.y, p2.x, p2.y);
    }

    /**
     * The location of a point based on angle and distance.
     */
    public static Point coordinates(Point p, double distance, double angle) {
        double x = p.x + Math.cos(radians(angle)) * distance;
        double y = p.y + Math.sin(radians(angle)) * distance;
        return new Point(x, y);
    }

    /**
     * The reflection of a point through an origin point.
     */
    public static Point reflect(Point p1, Point p2, double distance, double angle) {
        distance *= distance(p1, p2);
        angle += angle(p1, p2);
        return coordinates(p1, distance, angle);
    }

    public static double slowNumber(double n) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {
            return -999;
        }
        return n;
    }

}
