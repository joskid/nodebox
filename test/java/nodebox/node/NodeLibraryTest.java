package nodebox.node;

import nodebox.function.FunctionRepository;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;

public class NodeLibraryTest {

    private final NodeLibrary library;
    private final Node child1;
    private final Node child2;
    private final Node parent;
    private final Node root;

    public NodeLibraryTest() {
        child1 = Node.ROOT.withName("child1");
        child2 = Node.ROOT.withName("child2");
        parent = Node.ROOT.withName("parent")
                .withChildAdded(child1)
                .withChildAdded(child2);
        root = Node.ROOT.withChildAdded(parent);
        library = NodeLibrary.create("test", root, FunctionRepository.of());
    }

    @Test
    public void testNodeForPath() {
        assertEquals(root, library.getNodeForPath("/"));
        assertEquals(parent, library.getNodeForPath("/parent"));
        assertEquals(child1, library.getNodeForPath("/parent/child1"));
        assertEquals(child2, library.getNodeForPath("/parent/child2"));

        assertNull("Invalid names return null.", library.getNodeForPath("/foo"));
        assertNull("Invalid nested names return null.", library.getNodeForPath("/parent/foo"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRelativePath() {
        library.getNodeForPath("parent");
    }

    @Test
    public void testSimpleReadWrite() {
        NodeLibrary simple = NodeLibrary.create("test", Node.ROOT, FunctionRepository.of());
        assertReadWriteEquals(simple, NodeRepository.of());
    }

    @Test
    public void testNestedReadWrite() {
        assertReadWriteEquals(library, NodeRepository.of());
    }

    /**
     * Assert that a NodeLibrary equals itself after reading and writing.
     * @param library The NodeLibrary.
     * @param nodeRepository The repository of NodeLibraries.
     */
    private void assertReadWriteEquals(NodeLibrary library, NodeRepository nodeRepository) {
        String xml  = library.toXml();
        System.out.println("xml = " + xml);
        assertEquals(library, NodeLibrary.load(library.getName(), xml, nodeRepository));
    }

}
