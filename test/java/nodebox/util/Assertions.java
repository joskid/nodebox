package nodebox.util;

import com.google.common.collect.ImmutableList;

import java.util.List;

import static org.junit.Assert.assertEquals;

public final class Assertions {

    public static void assertResultsEqual(Iterable<Object> result, Object... args) {
        assertEquals(ImmutableList.copyOf(args), ImmutableList.copyOf(result));
    }

}
