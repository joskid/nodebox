package nodebox.node;

import com.google.common.collect.ImmutableList;
import nodebox.client.PythonUtils;
import nodebox.function.FunctionRepository;
import nodebox.function.ListFunctions;
import nodebox.function.MathFunctions;
import nodebox.graphics.Color;
import nodebox.graphics.Point;
import org.junit.Test;

import java.io.File;
import java.util.List;
import java.util.Locale;

import static junit.framework.Assert.*;
import static junit.framework.Assert.assertEquals;
import static nodebox.util.Assertions.assertResultsEqual;

public class NodeLibraryTest {

    private final NodeLibrary library;
    private final Node child1;
    private final Node child2;
    private final Node parent;
    private final Node root;
    private final FunctionRepository functions;

    public NodeLibraryTest() {
        PythonUtils.initializePython();
        child1 = Node.ROOT.withName("child1");
        child2 = Node.ROOT.withName("child2");
        parent = Node.ROOT.withName("parent")
                .withChildAdded(child1)
                .withChildAdded(child2);
        root = Node.ROOT.withChildAdded(parent);
        library = NodeLibrary.create("test", root, FunctionRepository.of());
        functions = FunctionRepository.of(MathFunctions.LIBRARY, ListFunctions.LIBRARY);
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
    public void testPrototypeInSameLibrary() {
        // You can refer to a prototype in the same library as the current node.
        Node invert = Node.ROOT
                .withName("invert")
                .withFunction("math/invert")
                .withInputAdded(Port.floatPort("number", 0));
        Node invert1 = invert.extend().withName("invert1").withInputValue("number", 42.0);
        Node root = Node.ROOT
                .withName("root")
                .withChildAdded(invert)
                .withChildAdded(invert1)
                .withRenderedChild(invert1);
        NodeLibrary originalLibrary = NodeLibrary.create("test", root);
        // Assert the original library returns the correct result.
        NodeContext context = new NodeContext(FunctionRepository.of(MathFunctions.LIBRARY));
        assertResultsEqual(context.renderNode(root), -42.0);

        // Persist / load the library and assert it still returns the correct result.
        NodeLibrary restoredLibrary = NodeLibrary.load("test", originalLibrary.toXml(), NodeRepository.of());
        assertResultsEqual(context.renderNode(restoredLibrary.getRoot()), -42.0);
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


    /**
     * Test if the NodeLibrary stores / loads the list strategy correctly.
     */
    @Test
    public void testListStrategyPersistence() {
        // Default check.
        Node makeNumbers = Node.ROOT
                .withName("makeNumbers")
                .withListStrategy(Node.FLATTEN_STRATEGY)
                .withFunction("math/makeNumbers")
                .withInputAdded(Port.stringPort("s", "1 2 3 4 5"));
        Node reverse = Node.ROOT
                .withName("reverse")
                .withListStrategy(Node.AS_IS_STRATEGY)
                .withFunction("list/reverse")
                .withInputAdded(Port.customPort("list", "list"));
        Node net = Node.ROOT
                .withChildAdded(makeNumbers)
                .withChildAdded(reverse)
                .withRenderedChild(reverse)
                .connect("makeNumbers", "reverse", "list");
        NodeLibrary originalLibrary = NodeLibrary.create("test", net, functions);
        assertResultsEqual(originalLibrary.getRoot(), 5.0, 4.0, 3.0, 2.0, 1.0);
        // Now save / load the library and check the output.
        NodeLibrary library = NodeLibrary.load("test", originalLibrary.toXml(), NodeRepository.of());
        assertResultsEqual(library.getRoot(), 5.0, 4.0, 3.0, 2.0, 1.0);
    }

    @Test
    public void testPrototypeOverridePersistence() {
        NodeLibrary mathLibrary = NodeLibrary.load(new File("libraries/math/math.ndbx"), NodeRepository.of());
        Node rangePrototype = mathLibrary.getRoot().getChild("range");
        Node range1 = rangePrototype.extend().withName("range1").withInputValue("end", 5.0);
        assertResultsEqual(range1, 0.0, 1.0, 2.0, 3.0, 4.0);
        NodeLibrary originalLibrary = NodeLibrary.create("test", range1, functions);
        // Now save / load the library and check the output.
        NodeLibrary library = NodeLibrary.load("test", originalLibrary.toXml(), NodeRepository.of(mathLibrary));
        assertResultsEqual(library.getRoot(), 0.0, 1.0, 2.0, 3.0, 4.0);
    }

    /**
     * Test if ports can persist their min / max values.
     */
    @Test
    public void testMinMaxPersistence() {
        Node originalRoot = Node.ROOT.withName("root").withInputAdded(Port.floatPort("v", 5.0, 0.0, 10.0));
        NodeLibrary originalLibrary = NodeLibrary.create("test", originalRoot);
        NodeLibrary library = NodeLibrary.load("test", originalLibrary.toXml(), NodeRepository.of());
        Port v = library.getRoot().getInput("v");
        assertEquals(0.0, v.getMinimumValue());
        assertEquals(10.0, v.getMaximumValue());
    }

    /**
     * Test if file writing is independent from the locale.
     * <p/>
     * We use String.format in writing points, which is locale-dependent.
     * Having the "wrong" locale would mean that the returned point was invalid.
     */
    @Test
    public void testLocale() {
        Locale savedLocale = Locale.getDefault();
        // The german locale uses a comma to separate the decimals, which could make points fail.
        Locale.setDefault(Locale.GERMAN);
        try {
            // We use points for the position and for the input port.
            Node originalRoot = Node.ROOT
                    .withName("root")
                    .withPosition(new Point(12, 34))
                    .withInputAdded(Port.pointPort("point", new Point(12, 34)));
            NodeLibrary originalLibrary = NodeLibrary.create("test", originalRoot);
            NodeLibrary library = NodeLibrary.load("test", originalLibrary.toXml(), NodeRepository.of());
            Node root = library.getRoot();
            assertPointEquals(root.getPosition(), 12.0, 34.0);
            assertPointEquals(root.getInput("point").pointValue(), 12.0, 34.0);
        } finally {
            Locale.setDefault(savedLocale);
        }
    }
    
    @Test
    public void testReadMenus() {
        NodeLibrary menuLibrary = NodeLibrary.load(new File("test/files/menus.ndbx"),NodeRepository.DEFAULT);
        Port thePort = menuLibrary.getRoot().getInput("thePort");
        assertTrue(thePort.hasMenu());
        assertEquals(2, thePort.getMenuItems().size());
        assertEquals(new MenuItem("a", "Alpha"), thePort.getMenuItems().get(0));
        assertEquals(new MenuItem("b", "Beta"), thePort.getMenuItems().get(1));
    }
    
    @Test
    public void testMenuSerialization() {
        Node originalRoot = makeLetterMenuNode();
        NodeLibrary originalLibrary = NodeLibrary.create("test", originalRoot);
        NodeLibrary library = NodeLibrary.load("test", originalLibrary.toXml(), NodeRepository.of());
        Port letterPort = library.getRoot().getInput("letter");
        assertTrue(letterPort.hasMenu());
        assertEquals(2, letterPort.getMenuItems().size());
        assertEquals(new MenuItem("a", "Alpha"), letterPort.getMenuItems().get(0));
        assertEquals(new MenuItem("b", "Beta"), letterPort.getMenuItems().get(1));
    }

    /**
     * Test if a port using a menu prototype is correctly serialized.
     */
    @Test
    public void testMenuPrototypeSerialization() {
        Node letterPrototype = makeLetterMenuNode();
        Node letterNode = letterPrototype.extend().withName("my_letter").withInputValue("letter", "b");
        Node originalRoot = Node.ROOT
                .withChildAdded(letterPrototype)
                .withChildAdded(letterNode);
        NodeLibrary originalLibrary = NodeLibrary.create("test", originalRoot);
        NodeLibrary library = NodeLibrary.load("test", originalLibrary.toXml(), NodeRepository.of());
        Port letterPort = library.getRoot().getChild("my_letter").getInput("letter");
        assertTrue(letterPort.hasMenu());
        assertEquals(2, letterPort.getMenuItems().size());
        assertEquals("b", letterPort.getValue());
    }
    
    public Node makeLetterMenuNode() {
        MenuItem alpha = new MenuItem("a", "Alpha");
        MenuItem beta = new MenuItem("b", "Beta");
        return Node.ROOT.withName("letter")
                .withInputAdded(Port.stringPort("letter", "a", ImmutableList.of(alpha, beta)));
    }

    private void assertPointEquals(Point point, double x, double y) {
        assertEquals(x, point.getX());
        assertEquals(y, point.getY());
    }

    private void assertSingleResult(Double expected, Node node, FunctionRepository functionRepository) {
        NodeContext context = new NodeContext(functionRepository);
        List<Object> values = ImmutableList.copyOf(context.renderNode(node));
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
