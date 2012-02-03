package nodebox.function;

import nodebox.graphics.Point;

/**
 * A function library containing special functions for testing.
 */
public class TestFunctions {

    public static final FunctionLibrary LIBRARY;

    static {
        LIBRARY = JavaLibrary.ofClass("test", TestFunctions.class, "allTypes", "makeNull");
    }

    public static String allTypes(int i, float f, String s, Point pt) {
        StringBuilder b = new StringBuilder()
                .append(i).append(", ")
                .append(f).append(", ")
                .append(s).append(", ")
                .append(pt);
        return b.toString();
    }

    /**
     * Whatever the input, returns null.
     * @param ignored The input, which is ignored
     * @return null.
     */
    public static Double makeNull(Double ignored) {
        return null;
    }

}
