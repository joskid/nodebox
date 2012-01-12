package nodebox.function;

import java.util.ArrayList;

/**
 * Basic math function library.
 */
public class MathFunctions {

    public static final FunctionLibrary LIBRARY;

    static {
        LIBRARY = JavaLibrary.ofClass("math", MathFunctions.class, "value", "add", "subtract", "invert", "slowNumber", "toNumbers");
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

}
