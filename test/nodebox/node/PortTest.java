package nodebox.node;

import nodebox.graphics.*;
import nodebox.node.event.ValueChangedEvent;

import java.util.ArrayList;
import java.util.List;

public class PortTest extends NodeTestCase {

    /**
     * Test the naming of ports, which follows the same naming rules as nodes.
     */
    public void testNaming() {
        Node n = rootMacro.createChild(Node.class);
        assertInvalidName(n, null, "names cannot be null.");
        assertInvalidName(n, "", "names cannot be empty.");
        assertInvalidName(n, "  ", "names cannot contain spaces.");
        assertInvalidName(n, "  x  ", "names cannot contain spaces.");
        assertInvalidName(n, "1234", "names cannot start with a digit.");
        assertInvalidName(n, "node", "names can not be one of the reserved words.");
        assertInvalidName(n, "context", "names can not be one of the reserved words.");
        assertValidName(n, "radius");
        assertInvalidName(n, "radius", "port  names must be unique for the node");
        n.createPort("myparam", Integer.class, Port.Direction.IN);
        assertInvalidName(n, "myparam", "port names must be unique across parameters and ports");
    }

    /**
     * Test the default values for port types.
     */
    public void testDefaultValues() {
        Node n = rootMacro.createChild(Node.class);
        Port pInteger = n.createPort("integer", Integer.class, Port.Direction.IN);
        Port pFloat = n.createPort("float", Float.class, Port.Direction.IN);
        Port pString = n.createPort("string", String.class, Port.Direction.IN);
        Port pColor = n.createPort("color", Color.class, Port.Direction.IN);
        Port pObject = n.createPort("object", Object.class, Port.Direction.IN);
        assertEquals(0, pInteger.getValue());
        assertEquals(0f, pFloat.getValue());
        assertEquals("", pString.getValue());
        assertEquals(new Color(), pColor.getValue());
        assertNull(pObject.getValue());
    }

    /**
     * Test if isAssignableFrom works when validating.
     */
    public void testCasting() {
        Node grobNode = nodeWithDataClass("grob", Grob.class);
        Node canvasNode = nodeWithDataClass("canvas", Canvas.class);
        Node imageNode = nodeWithDataClass("image", Image.class);
        Node pathNode = nodeWithDataClass("path", Path.class);
        Node textNode = nodeWithDataClass("text", Text.class);

        Canvas canvas = new Canvas();
        Image image = new Image();
        Path path = new Path();
        Text text = new Text("", 0, 0);

        assertValidValue(grobNode, canvas);
        assertValidValue(grobNode, image);
        assertValidValue(grobNode, path);
        assertValidValue(grobNode, text);

        assertValidValue(canvasNode, canvas);
        assertInvalidValue(canvasNode, image);
        assertInvalidValue(canvasNode, path);
        assertInvalidValue(canvasNode, text);

        assertInvalidValue(imageNode, canvas);
        assertValidValue(imageNode, image);
        assertInvalidValue(imageNode, path);
        assertInvalidValue(imageNode, text);

        assertInvalidValue(pathNode, canvas);
        assertInvalidValue(pathNode, image);
        assertValidValue(pathNode, path);
        assertInvalidValue(pathNode, text);

        assertInvalidValue(textNode, canvas);
        assertInvalidValue(textNode, image);
        assertInvalidValue(textNode, path);
        assertValidValue(textNode, text);

        Port grobIn = grobNode.getPort("in");
        Port grobOut = grobNode.getPort("out");
        Port canvasIn = canvasNode.getPort("in");
        Port canvasOut = canvasNode.getPort("out");
        Port imageOut = imageNode.getPort("out");
        Port pathOut = pathNode.getPort("out");
        Port textOut = textNode.getPort("out");

        assertTrue(grobIn.canConnectTo(canvasOut));
        assertTrue(grobIn.canConnectTo(imageOut));
        assertTrue(grobIn.canConnectTo(pathOut));
        assertTrue(grobIn.canConnectTo(textOut));
        assertTrue(canvasOut.canConnectTo(grobIn));
        assertFalse(grobIn.canConnectTo(grobOut)); // Cannot connect to port on the same node

        assertFalse(canvasIn.canConnectTo(canvasOut));
        assertFalse(canvasIn.canConnectTo(grobOut));
        assertFalse(canvasIn.canConnectTo(imageOut));
        assertFalse(grobOut.canConnectTo(canvasIn));

        assertFalse(grobIn.canConnectTo(grobIn));
        assertFalse(grobIn.canConnectTo(grobOut));
    }

    /**
     * Test where the value null can be used.
     */
    public void testNullValue() {
        Node n = rootMacro.createChild(Node.class);

        Port pInt = n.createPort("int", Integer.class, Port.Direction.IN);
        assertValidValue(pInt, 12);
        assertInvalidValue(pInt, null);

        Port pList = n.createPort("list", List.class, Port.Direction.IN);
        assertValidValue(pList, new ArrayList());
        assertInvalidValue(pList, null);


        Port pObject = n.createPort("object", Object.class, Port.Direction.IN);
        assertValidValue(pObject, 12);
        assertValidValue(pObject, null);
    }

    /**
     * Test if creating a port adds it to the node.
     */
    public void testAutoAdd() {
        try {
            new Port(null, "test", Integer.class, Port.Direction.IN);
            fail("Ports with empty nodes cannot be created.");
        } catch (Exception e) {
        }
        Node n = rootMacro.createChild(Node.class);
        assertFalse(n.hasPort("test"));
        Port p = new Port(n, "test", Integer.class, Port.Direction.IN);
        assertTrue(n.hasPort("test"));
        n.removePort(p);
        assertFalse(n.hasPort("test"));
        // The port still refers to the node, though.
        assertEquals(n, p.getNode());
    }

    /**
     * Test the absolute port path.
     */
    public void testAbsolutePath() {
        Port p1 = rootMacro.createPort("p1", Integer.class, Port.Direction.IN);
        assertEquals("/.p1", p1.getAbsolutePath());
        Macro parent = (Macro) rootMacro.createChild(Macro.class, "parent");
        Port p2 = parent.createPort("p2", Integer.class, Port.Direction.IN);
        assertEquals("/parent.p2", p2.getAbsolutePath());
        Node child = parent.createChild(Node.class, "child");
        Port p3 = child.createPort("p3", Integer.class, Port.Direction.IN);
        assertEquals("/parent/child.p3", p3.getAbsolutePath());
    }

    /**
     * Test the value shortcut methods.
     */
    public void testValueShortcuts() {
        Node n = rootMacro.createChild(Node.class);
        Port pInteger = n.createPort("integer", Integer.class, Port.Direction.IN);
        assertValues(pInteger, 0, 0, 0f, "0", new Color());
        pInteger.setValue(12);
        assertValues(pInteger, 12, 12, 12f, "12", new Color());

        Port pFloat = n.createPort("float", Float.class, Port.Direction.IN);
        assertValues(pFloat, 0f, 0, 0f, "0.0", new Color());
        pFloat.setValue(33);
        assertValues(pFloat, 33f, 33, 33f, "33.0", new Color(1, 1, 1));
        pFloat.setValue(0.6f);
        assertValues(pFloat, 0.6f, 1, 0.6f, "0.6", new Color(0.6f, 0.6f, 0.6f));

        Port pString = n.createPort("string", String.class, Port.Direction.IN);
        assertValues(pString, "", 0, 0f, "", new Color());
        pString.setValue("hello");
        assertValues(pString, "hello", 0, 0f, "hello", new Color());
        pString.setValue("42");
        assertValues(pString, "42", 0, 0f, "42", new Color());

        Port pColor = n.createPort("color", Color.class, Port.Direction.IN);
        assertValues(pColor, new Color(), 0, 0f, "#000000ff", new Color());
        Color red = new Color(0.9, 0.2, 0.1);
        pColor.setValue(red);
        assertValues(pColor, red, 0, 0f, "#e6331aff", red);

        Port pObject = n.createPort("object", Object.class, Port.Direction.IN);
        assertValues(pObject, null, 0, 0f, "", new Color());
        pObject.setValue("object");
        assertValues(pObject, "object", 0, 0f, "object", new Color());
    }

    /**
     * Test parsing of values as strings.
     */
    public void testParseValue() {
        Node n = rootMacro.createChild(Node.class);
        Port pInteger = n.createPort("integer", Integer.class, Port.Direction.IN);
        Port pFloat = n.createPort("float", Float.class, Port.Direction.IN);
        Port pString = n.createPort("string", String.class, Port.Direction.IN);
        Port pColor = n.createPort("color", Color.class, Port.Direction.IN);
        Port pObject = n.createPort("object", Object.class, Port.Direction.IN);

        // Null is never accepted
        assertParseValueFails(pInteger, null);

        // An empty string returns the default value for the type.
        assertEquals(0, pInteger.parseValue(""));
        assertEquals(33, pInteger.parseValue("33"));
        assertParseValueFails(pInteger, "22.8");
        assertEquals(3, pInteger.parseValue("03")); // Octal
        assertParseValueFails(pInteger, "0xaa"); // Hexadecimal
        assertParseValueFails(pInteger, "999999999999999999999999999999999999999999999999999999");
        assertParseValueFails(pInteger, "xxx");

        assertEquals(0f, pFloat.parseValue(""));
        assertEquals(29.3f, pFloat.parseValue("29.3"));
        assertEquals(3f, pFloat.parseValue("03")); // Octal
        assertParseValueFails(pFloat, "0xaa"); // Hexadecimal
        assertEquals(Float.POSITIVE_INFINITY, pFloat.parseValue("999999999999999999999999999999999999999999999999999999999"));
        assertParseValueFails(pFloat, "xxx");

        assertEquals("", pString.parseValue(""));
        assertEquals("12", pString.parseValue("12"));
        assertEquals("hello", pString.parseValue("hello"));

        assertEquals(new Color(), pColor.parseValue(""));
        assertEquals(new Color(1, 0, 0), pColor.parseValue("#ff0000ff"));
        assertParseValueFails(pColor, "0.3");
        assertParseValueFails(pColor, "xxx");

        // Other data types always throw an exception, except for the empty string.
        assertNull(pObject.parseValue(""));
        assertParseValueFails(pObject, "xxx");
    }

    /**
     * Test reverting to the default value.
     */
    public void testRevertToDefault() {
        Node n = rootMacro.createChild(Node.class);
        Port pInteger = n.createPort("integer", Integer.class, Port.Direction.IN);
        Port pFloat = n.createPort("float", Float.class, Port.Direction.IN);
        Port pString = n.createPort("string", String.class, Port.Direction.IN);
        Port pColor = n.createPort("color", Color.class, Port.Direction.IN);
        Port pObject = n.createPort("object", Object.class, Port.Direction.IN);

        pInteger.setValue(12);
        pFloat.setValue(33f);
        pString.setValue("");
        pColor.setValue(new Color(0.1, 0.2, 0.3));
        pObject.setValue("object");

        pInteger.revertToDefault();
        pFloat.revertToDefault();
        pString.revertToDefault();
        pColor.revertToDefault();
        pObject.revertToDefault();

        assertEquals(pInteger.defaultValue(), pInteger.getValue());
        assertEquals(pFloat.defaultValue(), pFloat.getValue());
        assertEquals(pString.defaultValue(), pString.getValue());
        assertEquals(pColor.defaultValue(), pColor.getValue());
        assertEquals(pObject.defaultValue(), pObject.getValue());
    }

    /**
     * Test creating connections.
     */
    public void testConnecting() {
        Node int1 = rootMacro.createChild(TestNodes.IntVariable.class, "int1");
        Node negate1 = rootMacro.createChild(TestNodes.Negate.class, "negate1");
        assertFalse(rootMacro.isConnected(int1));
        assertFalse(rootMacro.isConnected(negate1));
        rootMacro.connect(negate1.getPort("value"), int1.getPort("result"));
        Connection c = rootMacro.getConnections().iterator().next();
        assertNotNull(c);
        assertEquals(int1, c.getOutputNode());
        assertEquals(negate1, c.getInputNode());
    }

    /**
     * Test copying a port onto another node.
     */
    public void testCopyOnto() {
        Node n1 = rootMacro.createChild(Node.class);
        Port p1 = n1.createPort("p1", Integer.class, Port.Direction.IN);
        p1.setValue(42);

        Node n2 = rootMacro.createChild(Node.class);
        p1.copyOnto(n2);
        assertTrue(n2.hasPort("p1"));
        assertEquals(42, n2.getValue("p1"));

        // Copying again on the same node will cause a name clash.
        try {
            p1.copyOnto(n2);
            fail("Should have failed with name exception.");
        } catch (Exception e) {
        }
    }

    /**
     * Test for the value changed event.
     */
    public void testValueChangedEvent() {
        Node n = rootMacro.createChild(Node.class);
        Port pInt = n.createPort("port", Integer.class, Port.Direction.IN);
        MockNodeEventListener l = new MockNodeEventListener();
        testLibrary.addListener(l);
        // Set the value to the one it already has.
        pInt.setValue(0);
        assertNull(l.event);
        // Set the value to a new value.
        pInt.setValue(50);
        assertEquals(ValueChangedEvent.class, l.event.getClass());
    }

    //// Custom assertions ////

    private Node nodeWithDataClass(String name, Class dataClass) {
        Node n = rootMacro.createChild(Node.class, name);
        n.createPort("in", dataClass, Port.Direction.IN);
        n.createPort("out", dataClass, Port.Direction.OUT);
        return n;
    }

    private void assertValues(Port p, Object oValue, int iValue, float fValue, String sValue, Color cValue) {
        Node n = p.getNode();
        String portName = p.getName();
        assertEquals(oValue, p.getValue());
        assertEquals(oValue, n.getValue(portName));
        assertEquals(iValue, p.asInt());
        assertEquals(iValue, n.asInt(portName));
        assertEquals(fValue, p.asFloat());
        assertEquals(fValue, n.asFloat(portName));
        assertEquals(sValue, p.asString());
        assertEquals(sValue, n.asString(portName));
        assertEquals(cValue, p.asColor());
        assertEquals(cValue, n.asColor(portName));
    }

    private void assertInvalidName(Node n, String newName, String reason) {
        Port p = n.createPort("temporaryPort", Integer.class, Port.Direction.IN);
        try {
            p.validateName(newName);
            fail("the following condition was not met: " + reason);
        } catch (InvalidNameException ignored) {
        } finally {
            n.removePort(p);
        }
        try {
            n.createPort(newName, Integer.class, Port.Direction.IN);
            fail("the following condition was not met: " + reason);
        } catch (InvalidNameException ignored) {
        }
        catch (NullPointerException ignored) {
        }
    }

    private void assertValidName(Node n, String newName) {
        Port p = n.createPort("temporaryPort", Integer.class, Port.Direction.IN);
        try {
            p.validateName(newName);
        } catch (InvalidNameException e) {
            fail("The name \"" + newName + "\" should have been accepted.");
        } finally {
            n.removePort(p);
        }
        try {
            n.createPort(newName, Integer.class, Port.Direction.IN);
        } catch (InvalidNameException e) {
            fail("The name \"" + newName + "\" should have been accepted.");
        }
    }

    private void assertValidValue(Node n, Object value) {
        assertValidValue(n.getPort("in"), value);
    }

    private void assertValidValue(Port p, Object value) {
        try {
            p.validate(value);
        } catch (IllegalArgumentException e) {
            fail("The value '" + value + "' should have been accepted: " + e);
        }
        try {
            p.setValue(value);
        } catch (IllegalArgumentException e) {
            fail("The value '" + value + "' should have been accepted: " + e);
        }
    }

    private void assertInvalidValue(Node n, Object value) {
        assertInvalidValue(n.getPort("in"), value);
    }

    private void assertInvalidValue(Port p, Object value) {
        try {
            p.validate(value);
            fail("The value '" + value + "' should not have been accepted.");
        } catch (IllegalArgumentException ignored) {
        }
        try {
            p.setValue(value);
            fail("The value '" + value + "' should not have been accepted.");
        } catch (IllegalArgumentException ignored) {
        }
    }

    private void assertParseValueFails(Port p, String value) {
        try {
            p.parseValue(value);
            fail("The value " + value + " should not have been accepted.");
        } catch (Exception ignored) {
        }
    }

}
