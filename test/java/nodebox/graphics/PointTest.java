package nodebox.graphics;

import org.junit.Test;

import java.util.Iterator;

import static junit.framework.Assert.*;

public class PointTest {

    @Test
    public void testIterator() {
        Point pt = new Point(22, 33);
        Iterator<Double> iterator = pt.iterator();
        assertEquals(22.0, iterator.next());
        assertEquals(33.0, iterator.next());
        assertFalse(iterator.hasNext());
        try {
            iterator.next();
            fail("Should have thrown an exception");
        } catch (Exception ignored) {
        }
    }

}
