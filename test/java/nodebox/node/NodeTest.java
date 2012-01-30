package nodebox.node;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static junit.framework.Assert.*;

public class NodeTest {

    static final private String DIRECTION_IN = "in";
    static final private String DIRECTION_OUT = "out";

    @Test
    public void testPath() {
        assertEquals("/child", Node.path("/", Node.ROOT.withName("child")));
        assertEquals("/parent/child", Node.path("/parent", Node.ROOT.withName("child")));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRelativePath() {
        Node.path("", Node.ROOT.withName("child"));
    }

    @Test
    public void testRootName() {
        assertEquals("_root", Node.ROOT.getName());
        // The moment we extend from root, the name changes.
        assertEquals("node", Node.ROOT.withFunction("test").getName());
        // Trying to change the name back to _root fails.
        assertEquals("node", Node.ROOT.withName("_root").getName());
    }

    @Test
    public void testChangeFunction() {
        Node test = Node.ROOT.extend().withFunction("test/test");
        assertEquals("test/test", test.getFunction());
    }

    @Test
    public void testPrototype() {
        Node alpha = Node.ROOT.withName("alpha");
        assertSame("Using withXXX on the root sets the root automatically on the prototype.",
                alpha.getPrototype(), Node.ROOT);
        Node beta = alpha.withName("beta");
        assertSame("Using withXXX methods doesn't automatically change the prototype.",
                beta.getPrototype(), Node.ROOT);
        Node gamma = alpha.extend().withName("gamma");
        assertSame("Use extend() to change the prototype.",
                gamma.getPrototype(), alpha);
    }

    @Test
    public void testPortOrder() {
        Port pAlpha = Port.intPort("alpha", 1);
        Port pBeta = Port.intPort("beta", 2);
        Node original = Node.ROOT.withInputAdded(pAlpha).withInputAdded(pBeta);
        ImmutableList<String> orderedPortNames = ImmutableList.of("alpha", "beta");
        assertEquals(orderedPortNames, portNames(original));

        Node alphaChanged = original.withInputValue("alpha", 11L);
        assertEquals(orderedPortNames, portNames(alphaChanged));
    }

    @Test
    public void testPorts() {
        testPorts(DIRECTION_IN);
        testPorts(DIRECTION_OUT);
    }

    public void testPorts(String direction) {
        Port pX = Port.floatPort("x", 0);
        Port pY = Port.floatPort("y", 0);
        Node rectNode1 = newNodeWithPortAdded(Node.ROOT.withName("rect1"), pX, direction);
        assertNull(getNodePort(Node.ROOT, "x", direction));
        assertSame(pX, getNodePort(rectNode1, "x", direction));
        Node rectNode2 = newNodeWithPortAdded(rectNode1.withName("rect2"), pY, direction);
        assertSame(pX, getNodePort(rectNode2, "x", direction));
        assertSame(pY, getNodePort(rectNode2, "y", direction));
        assertNull(getNodePort(rectNode1, "y", direction));
        assertNodePortsSizeEquals(0, Node.ROOT, direction);
        assertNodePortsSizeEquals(1, rectNode1, direction);
        assertNodePortsSizeEquals(2, rectNode2, direction);
        Node rectNode3 = newNodeWithPortRemoved(rectNode2.withName("rect3"), "x", direction);
        assertNodePortsSizeEquals(2, rectNode2, direction);
        assertNodePortsSizeEquals(1, rectNode3, direction);
        assertNull(getNodePort(rectNode3, "x", direction));
        assertSame(pY, getNodePort(rectNode3, "y", direction));
    }

    private Node newNodeWithPortAdded(Node node, Port port, String direction) {
        if (direction.equals(DIRECTION_IN))
            return node.withInputAdded(port);
        else if (direction.equals(DIRECTION_OUT))
            return node.withOutputAdded(port);
        else
            throw new IllegalArgumentException("Port direction should be either '" + DIRECTION_IN + "' or '" + DIRECTION_OUT + "', not " + direction);
    }

    private Node newNodeWithPortRemoved(Node node, String portName, String direction) {
        if (direction.equals(DIRECTION_IN))
            return node.withInputRemoved(portName);
        else if (direction.equals(DIRECTION_OUT))
            return node.withOutputRemoved(portName);
        else
            throw new IllegalArgumentException("Port direction should be either '" + DIRECTION_IN + "' or '" + DIRECTION_OUT + "', not " + direction);
    }

    private Port getNodePort(Node node, String portName, String direction) {
        if (direction.equals(DIRECTION_IN))
            return node.getInput(portName);
        else if (direction.equals(DIRECTION_OUT))
            return node.getOutput(portName);
        else
            throw new IllegalArgumentException("Port direction should be either '" + DIRECTION_IN + "' or '" + DIRECTION_OUT + "', not " + direction);
    }

    private void assertNodePortsSizeEquals(int expected, Node node, String direction) {
        if (direction.equals(DIRECTION_IN))
            assertEquals(expected, node.getInputs().size());
        else if (direction.equals(DIRECTION_OUT))
            assertEquals(expected, node.getOutputs().size());
        else
            throw new IllegalArgumentException("Port direction should be either '" + DIRECTION_IN + "' or '" + DIRECTION_OUT + "', not " + direction);
    }

    public List<String> portNames(Node n) {
        List<String> portNames = new LinkedList<String>();
        for (Port p : n.getInputs()) {
            portNames.add(p.getName());
        }
        return portNames;
    }

}
