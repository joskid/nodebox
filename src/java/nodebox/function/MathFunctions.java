package nodebox.function;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Basic math function library.
 */
public class MathFunctions {

    public static final FunctionLibrary LIBRARY;

    static {
        LIBRARY = JavaLibrary.ofClass("math", MathFunctions.class, "value", "range", "add", "subtract", "invert", "slowNumber", "toNumbers");
    }

    public static double value(double v) {
        return v;
    }

    public static double add(double a, double b) {
        return a + b;
    }

    public static double subtract(double a, double b) {
        return a - b;
    }

    public static double invert(double v) {
        return -v;
    }

    public static double slowNumber(double v) {
        try {
            Thread.sleep(500);
        } catch (InterruptedException ignored) {
            return -999;
        }
        return v;
    }

    public static Iterable<Double> toNumbers(Iterable<String> strings) {
        ArrayList<Double> numbers = new ArrayList<Double>();
        for (String s : strings) {
            if (!s.isEmpty()) {
                for (String part : s.split(" ")) {
                    numbers.add(Double.parseDouble(part));
                }
            }
        }
        return numbers;
    }

    public static Iterable<Double> range(Iterable<Double> start, Iterable<Double> end, Iterable<Double> step) {
        double startValue = start.iterator().next();
        double endValue = end.iterator().next();
        double stepValue = step.iterator().next();
        return range(startValue, endValue, stepValue);
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
            this.next = start;
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


}
