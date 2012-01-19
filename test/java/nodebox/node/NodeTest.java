package nodebox.node;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static junit.framework.Assert.*;

public class NodeTest {

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

    @Test(expected = IllegalArgumentException.class)
    public void testOnlyConnectSameType() {
        Node floatNode = Node.ROOT.withName("float1").withOutputAdded(Port.floatPort("float", 0));
        Node intNode = Node.ROOT.withName("int1").withInputAdded(Port.intPort("int", 0));
        Node.ROOT
                .withChildAdded(floatNode)
                .withChildAdded(intNode)
                .connect("float1", "float", "int1", "int");
    }

    @Test
    public void testInputs() {
        Port pX = Port.floatPort("x", 0);
        Port pY = Port.floatPort("y", 0);
        Node rectNode1 = Node.ROOT
                .withName("rect1")
                .withInputAdded(pX);
        assertNull(Node.ROOT.getInput("x"));
        assertSame(pX, rectNode1.getInput("x"));
        Node rectNode2 = rectNode1
                .withName("rect2")
                .withInputAdded(pY);
        assertSame(pX, rectNode2.getInput("x"));
        assertSame(pY, rectNode2.getInput("y"));
        assertNull(rectNode1.getInput("y"));
        assertNodeInputsSizeEquals(0, Node.ROOT);
        assertNodeInputsSizeEquals(1, rectNode1);
        assertNodeInputsSizeEquals(2, rectNode2);
        Node rectNode3 = rectNode2
                .withName("rect3")
                .withInputRemoved("x");
        assertNodeInputsSizeEquals(2, rectNode2);
        assertNodeInputsSizeEquals(1, rectNode3);
        assertNull(rectNode3.getInput("x"));
        assertSame(pY, rectNode3.getInput("y"));
    }

    @Test
    public void testOutputs() {
        Port pX = Port.floatPort("x", 0);
        Port pY = Port.floatPort("y", 0);
        Node rectNode1 = Node.ROOT
                .withName("rect1")
                .withOutputAdded(pX);
        assertNull(Node.ROOT.getOutput("x"));
        assertSame(pX, rectNode1.getOutput("x"));
        Node rectNode2 = rectNode1
                .withName("rect2")
                .withOutputAdded(pY);
        assertSame(pX, rectNode2.getOutput("x"));
        assertSame(pY, rectNode2.getOutput("y"));
        assertNull(rectNode1.getOutput("y"));
        assertNodeOutputsSizeEquals(0, Node.ROOT);
        assertNodeOutputsSizeEquals(1, rectNode1);
        assertNodeOutputsSizeEquals(2, rectNode2);
        Node rectNode3 = rectNode2
                .withName("rect3")
                .withOutputRemoved("x");
        assertNodeOutputsSizeEquals(2, rectNode2);
        assertNodeOutputsSizeEquals(1, rectNode3);
        assertNull(rectNode3.getOutput("x"));
        assertSame(pY, rectNode3.getOutput("y"));
    }

    private void assertNodeInputsSizeEquals(int expected, Node node) {
        assertEquals(expected, node.getInputs().size());
    }

    private void assertNodeOutputsSizeEquals(int expected, Node node) {
        assertEquals(expected, node.getOutputs().size());
    }

    public List<String> portNames(Node n) {
        List<String> portNames = new LinkedList<String>();
        for (Port p : n.getInputs()) {
            portNames.add(p.getName());
        }
        return portNames;
    }

}
