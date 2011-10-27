package nodebox.node;

import nodebox.function.FunctionRepository;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

public class NodeLibraryControllerTest {

    private NodeLibraryController controller;

    @Before
    public void setUp() throws Exception {
        controller = NodeLibraryController.create("test", FunctionRepository.of());
    }

    @Test
    public void testCreateNode() {
        NodeLibrary start = controller.getNodeLibrary();
        assertEquals(0, start.getRoot().getChildren().size());

        controller.addNode("/", Node.ROOT.withName("parent"));
        controller.addNode("/parent", Node.ROOT.withName("child"));

        // Check if all the nodes are there.
        NodeLibrary end = controller.getNodeLibrary();
        assertEquals(1, end.getRoot().getChildren().size());
        assertTrue(end.getRoot().hasChild("parent"));
        Node parent = end.getRoot().getChild("parent");
        assertEquals(1, parent.getChildren().size());
        assertTrue(parent.hasChild("child"));

        // Start library has not changed.
        assertEquals(0, start.getRoot().getChildren().size());
    }

}
