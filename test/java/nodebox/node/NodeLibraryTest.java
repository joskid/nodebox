package nodebox.node;

import nodebox.function.FunctionRepository;
import nodebox.function.MathFunctions;
import nodebox.graphics.Color;
import nodebox.graphics.Point;
import org.junit.Test;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static junit.framework.Assert.*;

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
        NodeLibrary simple = NodeLibrary.create("test", Node.ROOT.extend(), FunctionRepository.of());
        assertReadWriteEquals(simple, NodeRepository.of());
    }

    @Test
    public void testNestedReadWrite() {
        assertReadWriteEquals(library, NodeRepository.of());
    }

    @Test
    public void testDoNotWriteRootPrototype() {
        Node myNode = Node.ROOT.withName("myNode");
        NodeLibrary library = libraryWithChildren("test", myNode);
        // Because myNode uses the _root prototype, it shouldn't write the prototype attribute.
        assertFalse(library.toXml().contains("prototype"));
    }

    @Test
    public void testRenderedNode() {
        Node child1 = Node.ROOT.withName("child1");
        Node originalRoot = Node.ROOT.withChildAdded(child1).withRenderedChild(child1);
        NodeLibrary originalLibrary = NodeLibrary.create("test", originalRoot, FunctionRepository.of());
        NodeLibrary library = NodeLibrary.load("test", originalLibrary.toXml(), NodeRepository.of());
        assertEquals("child1", library.getRoot().getRenderedChildName());
        assertNotNull(library.getRoot().getRenderedChild());
    }

    @Test
    public void testPortSerialization() {
        assertPortSerialization(Port.intPort("int", 42));
        assertPortSerialization(Port.floatPort("float", 33.3));
        assertPortSerialization(Port.stringPort("string", "hello"));
        assertPortSerialization(Port.colorPort("color", Color.BLACK));
        assertPortSerialization(Port.pointPort("point", new Point(11, 22)));
        assertPortSerialization(Port.customPort("geometry", "nodebox.graphics.Geometry"));
    }

    @Test
    public void testLink() {
        Node originalAdd = Node.ROOT
                .withName("add")
                .withFunction("math/add")
                .withInputAdded(Port.floatPort("v1", 11))
                .withInputAdded(Port.floatPort("v2", 22));
        NodeLibrary originalLibrary = NodeLibrary.create("test", originalAdd, FunctionRepository.of(MathFunctions.LIBRARY));
        assertSingleResult(33.0, originalAdd, originalLibrary.getFunctionRepository());
        NodeLibrary library = NodeLibrary.load("test", originalLibrary.toXml(), NodeRepository.of());
        assertTrue(library.getFunctionRepository().hasLibrary("math"));
        Node add = library.getRoot();
        assertEquals("add", add.getName());
        assertEquals("math/add", add.getFunction());
        assertSingleResult(33.0, add, library.getFunctionRepository());
    }

    private void assertSingleResult(Object expected, Node node, FunctionRepository functionRepository) {
        NodeContext context = new NodeContext(functionRepository);
        List<Object> values = context.renderNode(node);
        assertEquals(1, values.size());
        assertEquals(expected, values.get(0));
    }

    /**
     * Assert that the value that goes in to the port comes out correctly in XML.
     *
     * @param originalPort The port to serialize / deserialize
     */
    private void assertPortSerialization(Port originalPort) {
        Node originalNode;
        originalNode = Node.ROOT.withInputAdded(originalPort);
        NodeLibrary originalLibrary = libraryWithChildren("test", originalNode);

        NodeLibrary library = NodeLibrary.load("test", originalLibrary.toXml(), NodeRepository.of());
        Node node = library.getRoot().getChild("node");
        assertNotNull(node);
        Port port;
        port = node.getInput(originalPort.getName());
        assertEquals(originalPort.getName(), port.getName());
        assertEquals(originalPort.getType(), port.getType());
        assertEquals(originalPort.getValue(), port.getValue());
    }


    private NodeLibrary libraryWithChildren(String libraryName, Node... children) {
        Node root = Node.ROOT.withName("root");
        for (Node child : children) {
            root = root.withChildAdded(child);
        }
        return NodeLibrary.create(libraryName, root, FunctionRepository.of());
    }

    /**
     * Assert that a NodeLibrary equals itself after reading and writing.
     *
     * @param library        The NodeLibrary.
     * @param nodeRepository The repository of NodeLibraries.
     */
    private void assertReadWriteEquals(NodeLibrary library, NodeRepository nodeRepository) {
        String xml = library.toXml();
        assertEquals(library, NodeLibrary.load(library.getName(), xml, nodeRepository));
    }

}
