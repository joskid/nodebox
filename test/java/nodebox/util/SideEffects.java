package nodebox.util;

import nodebox.function.FunctionLibrary;
import nodebox.function.JavaLibrary;

/**
 * Function library that is used for testing and produces side effects.
 * <p/>
 * Side effects are things that happen outside of the function.
 * Functions that do not produce side effects are called pure functions and are preferred in NodeBox.
 */
public class SideEffects {

    public static final FunctionLibrary LIBRARY;

    static {
        LIBRARY = JavaLibrary.ofClass("side-effects", SideEffects.class, "getNumber", "setNumber");
    }

    public static long theInput = 0;
    public static long theOutput = 0;

    /**
     * Reset the side effects.
     */
    public static void reset() {
        theInput = 0;
        theOutput = 0;
    }

    /**
     * Get a number from theInput and return it.
     *
     * @return the current value of theInput.
     */
    public static long getNumber() {
        return theInput;
    }

    /**
     * Set the given number to theOutput.
     *
     * @param n The number to output.
     */
    public static void setNumber(long n) {
        theOutput = n;
    }

}
