package nodebox.function;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;

/**
 * Library with functions for String manipulation.
 */
public class StringFunctions {

    public static final FunctionLibrary LIBRARY;

    static {
        LIBRARY = JavaLibrary.ofClass("string", StringFunctions.class, "length", "wordCount");
    }

    public static int length(String s) {
        if (s == null) return 0;
        return s.length();
    }

    public static int wordCount(String s) {
        if (s == null) return 0;
        Iterable<String> split = Splitter.onPattern("\\w+").split(s);
        return Iterables.size(split) - 1;
    }

}
