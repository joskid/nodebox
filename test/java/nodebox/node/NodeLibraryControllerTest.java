package nodebox.node;

import org.junit.Before;
import org.junit.Test;

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

    public void testSetPortValue() {
        Node numberNode = Node.ROOT.withName("number").withPortAdded(Port.intPort("value", 10));
        controller.addNode("/", numberNode);
        assertEquals(10, controller.getNode("/number").getPort("value").intValue());
        controller.setPortValue("/number", "value", 42);
        assertEquals(42, controller.getNode("/number").getPort("value").intValue());
    }

}
