package nodebox.node;

import nodebox.node.event.ChildAddedEvent;
import nodebox.node.event.ChildRemovedEvent;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * All tests that have to do with parent/child relationships between nodes.
 */
public class MacroTest extends NodeTestCase {

    /**
     * A node that has a constructor with an extra argument, which the macro cannot instantiate.
     */
    public static class BadConstructorNode extends Node {

        public BadConstructorNode(Macro parent, Object otherArgument) {
            super(parent);
        }

    }

    /**
     * Test creating child nodes.
     */
    public void testCreate() {
        Macro grandParent = (Macro) testLibrary.getRootMacro().createChild(Macro.class, "grandParent");
        Macro parent = (Macro) grandParent.createChild(Macro.class, "parent");
        Node child = parent.createChild(numberNode);
        assertTrue(grandParent.hasChild(parent));
        assertTrue(parent.hasChild(child));
        // Contains doesn't go into child networks.
        assertFalse(grandParent.hasChild(child));
        assertTrue(child.hasParent());
        assertTrue(grandParent.hasParent());
        assertTrue(child.hasParent());
        assertFalse(testLibrary.getRootMacro().hasParent());
        assertEquals(testLibrary.getRootMacro(), grandParent.getParent());
        assertEquals(grandParent, parent.getParent());
        assertEquals(parent, child.getParent());
    }

    /**
     * Test what happens if invoking createChild with a bad class.
     */
    public void testCreateError() {
        try {
            rootMacro.createChild(BadConstructorNode.class);
            fail("The child should not have been created. Bad constructor.");
        } catch (Exception ignored) {
        }
        // You can still construct the node using its constructor.
        Node n = new BadConstructorNode(rootMacro, null);
        n.setName("bad");
        assertTrue(rootMacro.hasChild("bad"));
    }

    /**
     * Test if renaming a child works.
     */
    public void testRenameChild() {
        Macro parent = (Macro) rootMacro.createChild(Macro.class);
        Node n1 = parent.createChild(Node.class, "n1");
        Node x = parent.createChild(Node.class, "x");
        assertTrue(parent.hasChild(n1));
        assertTrue(parent.hasChild(x));
        assertTrue(parent.hasChild("n1"));
        assertTrue(parent.hasChild("x"));
        assertFalse(parent.hasChild("n2"));
        n1.setName("n2");
        assertTrue(parent.hasChild(n1));
        assertTrue(parent.hasChild(x));
        assertFalse(parent.hasChild("n1"));
        assertTrue(parent.hasChild("n2"));
        assertTrue(parent.hasChild("x"));
    }

    /**
     * Test creating, retrieving and removing child nodes.
     */
    public void testChildren() {
        Macro parent = (Macro) rootMacro.createChild(Macro.class);
        // You can create a child using createChild...
        Node child1 = parent.createChild(Node.class, "child1");
        // ...or by using the constructor, which will also add it to the parent macro.
        Node child2 = new Node(parent);
        child2.setName("child2");
        assertTrue(parent.hasChild(child1));
        assertTrue(parent.hasChild(child2));
        assertFalse(parent.hasChild(rootMacro));
        assertTrue(parent.hasChild("child1"));
        assertTrue(parent.hasChild("child2"));
        assertFalse(parent.hasChild("xxx"));
        assertEquals(child1, parent.getChild("child1"));
        assertEquals(child2, parent.getChild("child2"));
        try {
            parent.getChild(null);
            fail("getChild should not accept null.");
        } catch (NullPointerException ignored) {
        }
        try {
            parent.getChild("xxx");
            fail("Child xxx should not be found.");
        } catch (ChildNotFoundException e) {
            assertEquals(parent, e.getParent());
            assertEquals("xxx", e.getName());
        }
        assertTrue(parent.getChildren().contains(child1));
        assertTrue(parent.getChildren().contains(child2));
    }

    public void testChildEvent() {
        MockNodeEventListener l = new MockNodeEventListener();
        testLibrary.addListener(l);

        // Try creating a child with a bad constructor.
        // This will fail, so the event should not fire.
        try {
            rootMacro.createChild(BadConstructorNode.class);
        } catch (Exception ignored) {
        }
        assertNull(l.event);

        Node n1 = rootMacro.createChild(Node.class);
        assertEquals(ChildAddedEvent.class, l.event.getClass());
        assertEquals(n1, ((ChildAddedEvent) l.event).getChild());

        l.reset();
        Node n2 = rootMacro.createChild(Node.class);
        assertEquals(ChildAddedEvent.class, l.event.getClass());
        assertEquals(n2, ((ChildAddedEvent) l.event).getChild());

        // Try removing a child that does not exist.
        // We'll use the rootMacro, which is not a child of itself.
        l.reset();
        try {
            rootMacro.removeChild(rootMacro);
            fail("Cannot be removed.");
        } catch (IllegalStateException e) {
            assertErrorMessage(e, "not a child of this macro");
        }
        assertNull(l.event);

        boolean success = rootMacro.removeChild(n1);
        assertTrue(success);
        assertEquals(ChildRemovedEvent.class, l.event.getClass());
        assertEquals(n1, ((ChildRemovedEvent) l.event).getChild());
    }

    public void testBasicProcessing() {
        Macro parent = (Macro) rootMacro.createChild(Macro.class, "parent");
        Node intVariable = parent.createChild(numberNode);
        intVariable.setValue("value", 42);
        Node negate1 = parent.createChild(negateNode);
        Node negate2 = parent.createChild(negateNode);
        // Negate2's mode is changed to check if it is executed.
        negate2.setMode(Node.Mode.PRODUCER);
        parent.connect(negate1.getPort("value"), intVariable.getPort("result"));
        parent.connect(negate2.getPort("value"), intVariable.getPort("result"));

        parent.execute(new CookContext());
        assertEquals(-42, negate1.getValue("result"));
        // Because negate2 is set to producer, it is not executed.
        assertEquals(0, negate2.getValue("result"));

        negate2.setMode(Node.Mode.CONSUMER);
        intVariable.setValue("value", 12);
        parent.execute(new CookContext());
        assertEquals(-12, negate1.getValue("result"));
        assertEquals(-12, negate2.getValue("result"));
    }

    public void testPersistence() {
//        Node polynet1 = manager.getNode("polygraph.network").newInstance(testLibrary, "polynet1");
//        //Node polynet1 = testLibrary.getRootMacro().create(manager.getMacro("polygraph.network"), "polynet1");
//        polynet1.setPosition(10, 10);
//        assertEquals("polynet1", polynet1.getName());
//        polynet1.setRendered();
//        Node polygon1 = polynet1.create(manager.getNode("polygraph.polygon"));
//        assertEquals("polygon1", polygon1.getName());
//        polygon1.setRendered();
//        polygon1.setPosition(100, 30);
//        Node translate1 = polynet1.create(manager.getNode("polygraph.translate"));
//        assertEquals("translate1", translate1.getName());
//        translate1.setPosition(40, 80);
//        translate1.setRendered();
//        translate1.getPort("polygon").connect(polygon1);
//        Node rect1 = polynet1.create(manager.getNode("polygraph.rect"));
//        assertEquals("rect1", rect1.getName());
//        rect1.setPosition(180, 30);
//        Node merge1 = polynet1.create(manager.getNode("polygraph.merge"));
//        assertEquals("merge1", merge1.getName());
//        merge1.getPort("polygons").connect(translate1);
//        merge1.getPort("polygons").connect(rect1);
//
//        NodeLibrary newLibrary = storeAndLoad(testLibrary);
//        Node newRoot = newLibrary.getRootMacro();
//
//        assertEquals("root", newRoot.getName());
//        assertTrue(newRoot.contains("polynet1"));
//        Node nPolynet1 = newRoot.getChild("polynet1");
//        assertTrue(nPolynet1.contains("polygon1"));
//        assertTrue(nPolynet1.contains("translate1"));
//        Node nPolygon1 = nPolynet1.getChild("polygon1");
//        Node nTranslate1 = nPolynet1.getChild("translate1");
//        Node nRect1 = nPolynet1.getChild("rect1");
//        Node nMerge1 = nPolynet1.getChild("merge1");
//        assertEquals(polygon1.getValue("x"), nPolygon1.getValue("x"));
//        assertEquals(polygon1.getValue("fill"), nPolygon1.getValue("fill"));
//        assertEquals(polygon1.getValue("stroke"), nPolygon1.getValue("stroke"));
//        assertTrue(nPolygon1.isConnected());
//        assertTrue(nTranslate1.isConnected());
//        assertTrue(nTranslate1.getPort("polygon").isConnectedTo(nPolygon1));
//        assertTrue(nMerge1.getPort("polygons").isConnectedTo(nRect1));
//        assertTrue(nMerge1.getPort("polygons").isConnectedTo(nTranslate1));
//        // Check if this is the same connection
//        Port nPolygons = nMerge1.getPort("polygons");
//        assertEquals(1, nTranslate1.getDownstreamConnections().size());
//        assertEquals(1, nRect1.getDownstreamConnections().size());
//        Connection c1 = nTranslate1.getDownstreamConnections().iterator().next();
//        Connection c2 = nRect1.getDownstreamConnections().iterator().next();
//        assertTrue(c1 == c2);
//        // This tests for a bug where the connection would be created twice.
//        nMerge1.getPort("polygons").disconnect();
//        assertFalse(nPolygons.isConnectedTo(nRect1));
//        assertFalse(nPolygons.isConnectedTo(nTranslate1));
    }

    /**
     * Test if code can be persisted correctly.
     */
    public void testCodeLoading() {
//        Node hello = Node.ROOT_NODE.newInstance(testLibrary, "hello");
//        String code = "def cook(self):\n  return 'hello'";
//        hello.setValue("_code", new PythonCode(code));
//        hello.update();
//        assertEquals("hello", hello.getOutputValue());
//        // Store/load library
//        NodeLibrary newLibrary = storeAndLoad(testLibrary);
//        Node newHello = newLibrary.getRootMacro().getChild("hello");
//        newHello.update();
//        assertEquals("hello", newHello.getOutputValue());
    }

    /**
     * Test if all types load correctly.
     */
    public void testTypeLoading() {
//        Node allTypes = Node.ROOT_NODE.newInstance(testLibrary, "allTypes");
//        allTypes.addParameter("i", Parameter.Type.INT, 42);
//        allTypes.addParameter("f", Parameter.Type.FLOAT, 42F);
//        allTypes.addParameter("s", Parameter.Type.STRING, "42");
//        allTypes.addParameter("c", Parameter.Type.COLOR, new Color(0.4, 0.2, 0.1, 0.9));
//        NodeLibrary newLibrary = storeAndLoad(testLibrary);
//        Node newAllTypes = newLibrary.getRootMacro().getChild("allTypes");
//        Parameter pI = newAllTypes.getParameter("i");
//        Parameter pF = newAllTypes.getParameter("f");
//        Parameter pS = newAllTypes.getParameter("s");
//        Parameter pC = newAllTypes.getParameter("c");
//        assertEquals(Parameter.Type.INT, pI.getType());
//        assertEquals(Parameter.Type.FLOAT, pF.getType());
//        assertEquals(Parameter.Type.STRING, pS.getType());
//        assertEquals(Parameter.Type.COLOR, pC.getType());
//        assertEquals("i", pI.getName());
//        assertEquals(42, pI.getValue());
//        assertEquals(42F, pF.getValue());
//        assertEquals("42", pS.getValue());
//        assertEquals(new Color(0.4, 0.2, 0.1, 0.9), pC.getValue());
    }

    /**
     * Tests whether the network does copy the output of the rendered node.
     * <p/>
     * Output values are not copied, since we have no reliable way to clone them.
     */
    public void testCopy() {
//        Node net1 = Node.ROOT_NODE.newInstance(testLibrary, "net");
//        Node rect1 = net1.create(rectNode);
//        rect1.setRendered();
//        net1.update();
//        assertTrue(net1.getOutputValue() == rect1.getOutputValue());
    }

    /**
     * Test all possible connection errors.
     */
    public void testConnectionErrors() {
        Macro parent = (Macro) rootMacro.createChild(Macro.class);
        Node n1 = parent.createChild(numberNode);
        Node n2 = parent.createChild(numberNode);
        Node stringNode = parent.createChild(convertToUppercaseNode);
        Node outsider = rootMacro.createChild(numberNode);

        // Swap inputs and outputs
        assertInvalidConnect(n1.getPort("result"), n2.getPort("value"));
        // Connect to self
        assertInvalidConnect(n1.getPort("value"), n1.getPort("result"));
        // Connect to wrong type
        assertInvalidConnect(n1.getPort("value"), stringNode.getPort("result"));
        assertInvalidConnect(stringNode.getPort("value"), n1.getPort("result"));
        // Connect to outsider
        assertInvalidConnect(n1.getPort("value"), outsider.getPort("result"));
        assertInvalidConnect(outsider.getPort("value"), n1.getPort("result"));
        // Connect to parent
        parent.createPort("value", Integer.class, Port.Direction.IN);
        parent.createPort("result", Integer.class, Port.Direction.OUT);
        assertInvalidConnect(n1.getPort("value"), parent.getPort("result"));
        assertInvalidConnect(parent.getPort("value"), n1.getPort("result"));

        // Finally a valid connection
        assertValidConnect(n2.getPort("value"), n1.getPort("result"));

        // Creating the exact same connection again is legal: it replaces the old connection.
        assertValidConnect(n2.getPort("value"), n1.getPort("result"));
        assertEquals(1, parent.getConnections().size());
    }

    public void testBasicConnecting() {
        Macro parent = (Macro) rootMacro.createChild(Macro.class);
        Node number1 = parent.createChild(numberNode);
        Port number1Output = number1.getPort("result");
        Node number2 = parent.createChild(numberNode);
        Port number2Output = number2.getPort("result");
        Node negate1 = parent.createChild(negateNode);
        Port negate1Input = negate1.getPort("value");
        Node negate2 = parent.createChild(negateNode);
        Port negate2Input = negate2.getPort("value");

        parent.connect(negate1Input, number1Output);
        parent.connect(negate2Input, number1Output);

        assertTrue(parent.isConnected(number1));
        assertTrue(parent.isConnected(negate1));
        assertTrue(parent.isConnected(negate2));
        assertFalse(parent.isConnected(number2));
        assertTrue(parent.isConnectedTo(negate1Input, number1Output));
        assertTrue(parent.isConnectedTo(negate2Input, number1Output));
        assertFalse(parent.isConnectedTo(negate1Input, number2Output));
        assertFalse(parent.isConnectedTo(negate1Input, number2Output));

        // Replace negate1's input with number 2.
        parent.connect(negate1Input, number2Output);
        assertTrue(parent.isConnected(number2));
        assertTrue(parent.isConnected(number1)); // Still connected to negate2
        assertTrue(parent.isConnected(negate1));
        assertTrue(parent.isConnectedTo(negate1Input, number2Output));
        assertFalse(parent.isConnectedTo(negate1Input, number1Output));

        // Macro.disconnect(Port) only works on an input port.
        try {
            parent.disconnect(number1Output);
            fail("Should have thrown an error.");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("not an input port"));
        }

        // Remove connection number1 <- negate2
        parent.disconnect(negate2Input);
        assertFalse(parent.isConnected(negate2));
        assertFalse(parent.isConnected(number1));
        assertFalse(parent.isConnectedTo(negate2Input, number1Output));
        assertTrue(parent.isConnected(negate1));
    }

    /**
     * Test disconnecting a node.
     */
    public void testDisconnectNode() {
        Macro parent = (Macro) rootMacro.createChild(Macro.class);
        Node number1 = parent.createChild(numberNode);
        Node negate1 = parent.createChild(negateNode);
        Node number2 = parent.createChild(numberNode);
        Node add1 = parent.createChild(addNode);
        parent.connect(negate1.getPort("value"), number1.getPort("result"));
        parent.connect(add1.getPort("v1"), negate1.getPort("result"));
        parent.connect(add1.getPort("v2"), number2.getPort("result"));
        assertEquals(3, parent.getConnections().size());

        parent.disconnect(negate1);
        assertFalse(parent.isConnected(number1));
        assertFalse(parent.isConnected(negate1));
        assertFalse(parent.isConnected(add1.getPort("v1")));
        assertTrue(parent.isConnected(add1));
        assertTrue(parent.isConnected(add1.getPort("v2")));
        assertEquals(1, parent.getConnections().size());
    }

    public void testCycles() {
        Macro parent = (Macro) rootMacro.createChild(Macro.class);
        Node n1 = parent.createChild(numberNode);
        Node n2 = parent.createChild(numberNode);
        Node n3 = parent.createChild(numberNode);
        Port n1Input = n1.getPort("value");
        Port n1Output = n1.getPort("result");
        Port n2Input = n2.getPort("value");
        Port n2Output = n2.getPort("result");
        Port n3Input = n3.getPort("value");
        Port n3Output = n3.getPort("result");
        assertFalse(parent.isConnected(n2));
        assertValidConnect(n2Input, n1Output);
        assertTrue(parent.isConnected(n2));
        assertTrue(parent.isConnectedTo(n2Input, n1Output));
        assertTrue(parent.isConnected(n1Output));
        assertValidConnect(n3Input, n2Output);
        assertTrue(parent.isConnected(n3));
        assertTrue(parent.isConnectedTo(n3Input, n2Output));
        assertTrue(parent.isConnected(n2Output));
        // Try creating a single-node cycle.
        assertInvalidConnect(n1Input, n1Output);
        // Try creating a 2-node cycle.
        assertInvalidConnect(n1Input, n2Output);
        // The connection didn't go through, so n1's input is not connected to n2.
        assertFalse(parent.isConnectedTo(n1Input, n2Output));
        // However the input of n2 is still connected to n1's output.
        assertTrue(parent.isConnectedTo(n2Input, n1Output));
        assertTrue(parent.isConnected(n1));
        assertTrue(parent.isConnected(n2));
        // Try creating a 3-node cycle.
        assertInvalidConnect(n1Input, n3Output);
    }

    /**
     * Test if you can set a value on a connected port.
     */
    public void testSetValueOnConnectedPort() {
        Macro parent = (Macro) rootMacro.createChild(Macro.class);
        Node number1 = parent.createChild(numberNode);
        Node add1 = parent.createChild(addNode);
        parent.connect(add1.getPort("v1"), number1.getPort("result"));

    }

    /**
     * Test if errors occur in the right level, for the parent or children.
     */
    public void testErrorPropagation() {
        Macro parent = (Macro) rootMacro.createChild(Macro.class);
        Node crash = parent.createChild(crashNode);
        crash.setMode(Node.Mode.PRODUCER); // So crash does not execute by itself.
        Node negate = parent.createChild(negateNode);
        parent.connect(negate.getPort("value"), crash.getPort("result"));
        try {
            parent.execute(new CookContext());
            fail("Should have thrown an execute exception.");
        } catch (ExecuteException e) {
            assertEquals(parent, e.getNode());
            assertErrorMessage(e, "error while updating child node crash1");
            assertEquals(ExecuteException.class, e.getCause().getClass());
            ExecuteException innerException = (ExecuteException) e.getCause();
            assertErrorMessage(innerException, "/ by zero");
            assertEquals(ArithmeticException.class, innerException.getCause().getClass());
            assertEquals(crash, innerException.getNode());
            Throwable t = parent.getError();
            assertEquals(ExecuteException.class, t.getClass());
            Throwable crashError = crash.getError();
            assertNotNull(crashError);
            assertEquals(ArithmeticException.class, crashError.getClass());
        }

        // Remove the connection, so crash no longer executes.
        parent.disconnect(negate);
        negate.setValue("value", 42);
        parent.execute(new CookContext());
        assertEquals(-42, negate.getValue("result"));
    }

    /**
     * Store the library in XML, then load it under the name "newLibrary".
     *
     * @param lib the library to store.
     * @return the new library object.
     */
    private NodeLibrary storeAndLoad(NodeLibrary lib) {
        String xml = testLibrary.toXml();
        return manager.load("newLibrary", xml);
    }

    private void assertValidConnect(Port input, Port output) {
        checkNotNull(input);
        checkNotNull(output);
        Macro macro = input.getNode().getParent();
        try {
            macro.connect(input, output);
        } catch (IllegalArgumentException e) {
            fail("Should not have thrown IllegalArgumentException: " + e);
        }
    }

    private void assertInvalidConnect(Port input, Port output) {
        checkNotNull(input);
        checkNotNull(output);
        Macro macro = input.getNode().getParent();
        try {
            macro.connect(input, output);
            fail("Should have thrown IllegalArgumentException.");
        } catch (IllegalStateException ignored) {
        }
    }

}
