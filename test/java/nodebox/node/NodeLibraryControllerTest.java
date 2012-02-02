package nodebox.node;

import org.junit.Before;
import org.junit.Test;
import static nodebox.util.Assertions.assertResultsEqual;

import static org.junit.Assert.*;

public class NodeLibraryControllerTest {

    private NodeLibraryController controller;

    @Before
    public void setUp() throws Exception {
        controller = NodeLibraryController.create();

    }

    @Test
    public void testChangeRenderedNode() throws Exception {
        Node alpha = Node.ROOT.withName("alpha");
        Node beta = Node.ROOT.withName("beta");
        controller.addNode("/", alpha);
        controller.addNode("/", beta);
        assertNull(controller.getNodeLibrary().getRoot().getRenderedChild());

        controller.setRenderedChild("/", "alpha");
        assertEquals(alpha, controller.getNodeLibrary().getRoot().getRenderedChild());

        controller.setRenderedChild("/", "beta");
        assertEquals(beta, controller.getNodeLibrary().getRoot().getRenderedChild());

        controller.setRenderedChild("/", "");
        assertNull(controller.getNodeLibrary().getRoot().getRenderedChild());
    }

    @Test
    public void testAddNode() {
        NodeLibrary library;

        Node parent = Node.ROOT.withName("parent");
        controller.addNode("/", parent);
        library = controller.getNodeLibrary();
        assertTrue(library.getRoot().hasChild("parent"));
        assertSame(parent, library.getRoot().getChild("parent"));
        assertSame(parent, library.getNodeForPath("/parent"));

        Node child = Node.ROOT.withName("child");
        controller.addNode("/parent", child);
        library = controller.getNodeLibrary();
        assertTrue(library.getRoot().getChild("parent").hasChild("child"));
        assertSame(child, library.getNodeForPath("/parent/child"));
        assertNotSame("No longer the same since the new parent has an extra child.", parent, library.getNodeForPath("/parent"));
    }
    
    @Test
    public void testCreateNode() {
        Node proto = Node.ROOT.withName("protoNode");
        controller.createNode("/", proto);
        assertTrue(controller.getNodeLibrary().getRoot().hasChild("protoNode1"));
        assertSame(proto, controller.getNodeLibrary().getNodeForPath("/protoNode1").getPrototype());
    }
    
    @Test
    public void testRemoveNode() {
        Node child = Node.ROOT.withName("child");
        controller.addNode("/", child);
        assertTrue(controller.getNodeLibrary().getRoot().hasChild("child"));
        controller.removeNode("/", "child");
        assertFalse(controller.getNodeLibrary().getRoot().hasChild("child"));
        assertNull(controller.getNodeLibrary().getNodeForPath("/child"));
    }

    public void testSetPortValue() {
        Node numberNode = Node.ROOT.withName("number").withInputAdded(Port.intPort("value", 10));
        controller.addNode("/", numberNode);
        assertEquals(10, controller.getNode("/number").getInput("value").intValue());
        controller.setPortValue("/number", "value", 42);
        assertEquals(42, controller.getNode("/number").getInput("value").intValue());
    }

    @Test
    public void testUniqueNodeName() {
        Node proto = Node.ROOT.withName("protoNode");
        controller.createNode("/", proto);
        controller.createNode("/", proto);
        controller.createNode("/", proto);
        Node rootNode = controller.getNodeLibrary().getRoot();
        assertFalse(rootNode.hasChild("protoNode"));
        assertTrue(rootNode.hasChild("protoNode1"));
        assertTrue(rootNode.hasChild("protoNode2"));
        assertTrue(rootNode.hasChild("protoNode3"));

        controller.removeNode("/","protoNode2");
        rootNode = controller.getNodeLibrary().getRoot();
        assertFalse(rootNode.hasChild("protoNode2"));

        controller.createNode("/", proto);
        rootNode = controller.getNodeLibrary().getRoot();
        assertTrue(rootNode.hasChild("protoNode2"));
        assertFalse(rootNode.hasChild("protoNode4"));
    }
    
    @Test
    public void testSimpleRename() {
        Node child = Node.ROOT.withName("child");
        controller.addNode("/", child);
        controller.renameNode("/", "/child", "n");
        assertFalse(controller.getNodeLibrary().getRoot().hasChild("child"));
        assertTrue(controller.getNodeLibrary().getRoot().hasChild("n"));
    }

    @Test
    public void testSimpleConnection() {
        assertEquals(0, controller.getNodeLibrary().getRoot().getConnections().size());
        createSimpleConnection();
        assertEquals(1, controller.getNodeLibrary().getRoot().getConnections().size());
        Connection c = controller.getNodeLibrary().getRoot().getConnections().get(0);
        assertEquals("invert", c.getInputNode());
        assertEquals("value", c.getInputPort());
        assertEquals("number", c.getOutputNode());
        assertResultsEqual(controller.getNodeLibrary().getRoot(), controller.getNode("/invert"), -20.0);
    }

    @Test
    public void testSimpleDisconnect() {
        createSimpleConnection();
        Connection c = controller.getNodeLibrary().getRoot().getConnections().get(0);
        controller.disconnect("/", c);
        assertEquals(0, controller.getNodeLibrary().getRoot().getConnections().size());
    }
    
    @Test
    public void testRemoveNodeWithConnections() {
        createSimpleConnection();
        Node invert2Node = Node.ROOT.withName("invert2").withFunction("math/invert").withInputAdded(Port.floatPort("value", 0));
        controller.addNode("/", invert2Node);
        controller.connect("/", controller.getNode("/number"), invert2Node, invert2Node.getInput("value"));
        assertEquals(2, controller.getNodeLibrary().getRoot().getConnections().size());
        controller.removeNode("/", "number");
        assertEquals(0, controller.getNodeLibrary().getRoot().getConnections().size());
    }

    private void createSimpleConnection() {
        Node numberNode = Node.ROOT.withName("number").withFunction("math/number").withInputAdded(Port.floatPort("value", 20));
        Node invertNode = Node.ROOT.withName("invert").withFunction("math/invert").withInputAdded(Port.floatPort("value", 0));
        controller.addNode("/", numberNode);
        controller.addNode("/", invertNode);
        controller.connect("/", numberNode, invertNode, invertNode.getInput("value"));
    }
}
