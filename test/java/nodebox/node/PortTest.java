package nodebox.node;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class PortTest {

    @Test
    public void testParsedPort() {
        assertEquals(42, Port.parsedPort("myInt", "int", "42").intValue());
        assertEquals(33.3, Port.parsedPort("myInt", "float", "33.3").floatValue());
        assertEquals("hello", Port.parsedPort("myInt", "string", "hello").stringValue());
    }

    /**
     * If the value is null, the default value is used.
     */
    @Test
    public void testParsedPortNullValue() {
        assertEquals(0, Port.parsedPort("myInt", "int", null).intValue());
        assertEquals(0.0, Port.parsedPort("myInt", "float", null).floatValue());
        assertEquals("", Port.parsedPort("myInt", "string", null).stringValue());
    }

}
