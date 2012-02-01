package nodebox.function;

import nodebox.graphics.Color;

public final class ColorFunctions {

    public static final FunctionLibrary LIBRARY;

    static {
        LIBRARY = JavaLibrary.ofClass("color", ColorFunctions.class, "rgb");
    }

    public static Color rgb(double red, double green, double blue, double alpha) {
        return new Color(red, green, blue, alpha);
    }

}
