package nodebox.function;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class ListFunctions {

    public static final FunctionLibrary LIBRARY;

    static {
        LIBRARY = JavaLibrary.ofClass("list", ListFunctions.class, "take", "cycle", "reverse");
    }

    public static Iterable take(Iterable iterable, int limitSize) {
        return Iterables.limit(iterable, limitSize);
    }

    public static Iterable cycle(Iterable iterable) {
        return Iterables.cycle(iterable);
    }

    public static Iterable reverse(Iterable iterable) {
        return Lists.reverse(ImmutableList.copyOf(iterable));
    }

}
