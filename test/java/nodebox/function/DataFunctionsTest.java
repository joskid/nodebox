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
    public void testLookupNull() {
        assertNull(lookup(null, "xxx"));
        assertNull(lookup(new Point(11, 22), null));
    }

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
        List<Map<String, Object>> l = importCSV("test/files/colors.csv");
        assertEquals(5, l.size());
        Map<String, Object> black = l.get(0);
        assertResultsEqual(black.keySet(), "Name", "Red", "Green", "Blue");
        assertEquals("Black", black.get("Name"));
        // Numerical data is automatically converted to doubles.
        assertEquals(0.0, black.get("Red"));
    }

    @Test
    public void testImportCSVUnicode() {
        List<Map<String, Object>> l = importCSV("test/files/unicode.csv");
        assertEquals(2, l.size());
        Map<String, Object> frederik = l.get(0);
        assertResultsEqual(frederik.keySet(), "Name", "Age");
        assertEquals("Fr\u00e9d\u00ebr\u00eck", frederik.get("Name"));
        Map<String, Object> bob = l.get(1);
        assertEquals("B\u00f8b", bob.get("Name"));
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

    @Test
    public void testImportCSVWithWhitespace() {
        List<Map<String, Object>> l = importCSV("test/files/whitespace.csv");
        assertEquals(2, l.size());
        Map<String, Object> alice = l.get(0);
        assertResultsEqual(alice.keySet(), "Name", "Age");
        assertEquals("Alice", alice.get("Name"));
        // Numerical data is automatically converted to doubles.
        assertEquals(41.0, alice.get("Age"));
    }

}
