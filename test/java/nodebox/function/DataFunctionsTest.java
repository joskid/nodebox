package nodebox.function;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import java.awt.*;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.*;
import static nodebox.function.DataFunctions.*;
import static nodebox.util.Assertions.assertResultsEqual;

public class DataFunctionsTest {

    @Test
    public void testLookupInMap() {
        Map<String, Integer> greek = ImmutableMap.of("alpha", 1, "beta", 2, "gamma", 3);
        assertEquals(1, lookup(greek, "alpha"));
        assertEquals(2, lookup(greek, "beta"));
        assertNull(lookup(greek, "xxx"));
    }

    @Test
    public void testLookupInObject() {
        Point awtPoint = new Point(11, 22);
        assertEquals(11.0, lookup(awtPoint, "x"));
        assertEquals(22.0, lookup(awtPoint, "y"));
        assertNull(lookup(awtPoint, "xxx"));
    }

    @Test
    public void testMakeStrings() {
        assertResultsEqual(makeStrings("a;b", ";"), "a", "b");
        assertResultsEqual(makeStrings("a;b", ""), "a", ";", "b");
        assertResultsEqual(makeStrings("hello", ""), "h", "e", "l", "l", "o");
        assertResultsEqual(makeStrings("a b c", " "), "a", "b", "c");
        assertResultsEqual(makeStrings("a; b; c", ";"), "a", " b", " c");
        assertResultsEqual(makeStrings(null, ";"));
        assertResultsEqual(makeStrings(null, null));
    }


    @Test
    public void testImportCSV() {
        List<Map<String,String>> l = importCSV("test/files/colors.csv");
        assertEquals(5, l.size());
        Map<String, String> black = l.get(0);
        assertResultsEqual(black.keySet(), "Name", "Red", "Green", "Blue");
        assertEquals("Black", black.get("Name"));
        assertEquals("0", black.get("Red"));
    }
    
    @Test
    public void testImportEmptyCSV() {
        List l = importCSV(null);
        assertTrue(l.isEmpty());
    }

    @Test(expected = RuntimeException.class)
    public void testImportNonexistentCSV() {
        importCSV("blah/blah.csv");
    }

}
