package nodebox.function;

import nodebox.graphics.Color;

public final class ColorFunctions {

    public static final FunctionLibrary LIBRARY;

    static {
        LIBRARY = JavaLibrary.ofClass("color", ColorFunctions.class, "rgb");
    }

    public static Color rgb(double red, double green, double blue, double alpha, double range) {
        range = Math.max(range, 1);
        return new Color(red / range, green / range, blue / range, alpha / range);
    }

}
