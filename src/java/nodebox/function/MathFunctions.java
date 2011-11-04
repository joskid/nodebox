package nodebox.function;

/**
 * Basic math function library.
 */
public class MathFunctions {

    public static final FunctionLibrary LIBRARY;

    static {
        LIBRARY = JavaLibrary.ofClass("math", MathFunctions.class, "add", "subtract", "invert", "slowNumber");
    }

    public static int add(int a, int b) {
        return a + b;
    }

    public static int subtract(int a, int b) {
        return a - b;
    }

    public static int invert(int v) {
        return -v;
    }

    public static int slowNumber(int v) {
        try {
            Thread.sleep(500);
        } catch (InterruptedException ignored) {
            return -999;
        }
        return v;
    }

}
