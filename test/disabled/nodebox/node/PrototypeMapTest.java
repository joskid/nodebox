package nodebox.node;

import org.junit.Test;

import static org.junit.Assert.*;

public class PrototypeMapTest {

    @Test
    public void testRoot() {
        PrototypeMap root = PrototypeMap.ROOT;
        assertEquals(0, root.size());
        assertEquals(null, root.getPrototype());
    }

    @Test
    public void testWithProperty() {
        PrototypeMap m = PrototypeMap.ROOT.withProperty("foo", "bar");
        assertEquals(null, m.getPrototype());
        assertTrue(m.hasProperty("foo"));
    }

    @Test
    public void testReplaceProperty() {
        PrototypeMap m1 = PrototypeMap.ROOT.withProperty("foo", "bar");
        PrototypeMap m2 = m1.withProperty("foo", "baz");
        assertEquals("bar", m1.getProperty("foo"));
        assertEquals("baz", m2.getProperty("foo"));
    }

    @Test
    public void testHasProperty() {
        PrototypeMap parent = PrototypeMap.ROOT.withProperty("alpha", 1);
        PrototypeMap child = parent.extend().withProperty("beta", 2);
        assertTrue(child.hasProperty("alpha"));
        assertTrue(child.hasProperty("beta"));
    }

    @Test
    public void testHasOwnProperty() {
        PrototypeMap parent = PrototypeMap.ROOT.withProperty("alpha", 1);
        PrototypeMap child = parent.extend().withProperty("beta", 2);
        assertFalse(child.hasOwnProperty("alpha"));
        assertTrue(child.hasOwnProperty("beta"));
    }

    @Test
    public void testChaining() {
        PrototypeMap parent = PrototypeMap.ROOT.withProperty("alpha", 1);
        // withProperty doesn't change the prototype but re-uses it.
        PrototypeMap child = parent.withProperty("beta", 2);
        assertFalse(child.getPrototype() == parent);
        assertTrue(child.getPrototype() == parent.getPrototype());

        // Use extend if you want the correct parent.
        PrototypeMap correctChild = parent.extend().withProperty("beta", 2);
        assertTrue(correctChild.getPrototype() == parent);
    }

}
