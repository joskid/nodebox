package nodebox.function;

import nodebox.node.Node;
import nodebox.node.NodeContext;
import nodebox.node.NodeRenderException;
import nodebox.node.Port;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MathFunctionsTest {

    private final FunctionLibrary mathLibrary = MathFunctions.LIBRARY;
    private final FunctionRepository functions = FunctionRepository.of(mathLibrary);
    private NodeContext context;

    @Before
    public void setUp() {
        context = new NodeContext(functions);
    }

    @Test
    public void testInvertExists() {
        assertTrue(functions.hasFunction("math/invert"));
        assertTrue(mathLibrary.hasFunction("invert"));
        Function function = functions.getFunction("math/invert");
        assertEquals("invert", function.getName());
    }

    @Test(expected = NodeRenderException.class)
    public void testCallInvertWithNoArguments() {
        Node invertNode = Node.ROOT.withFunction("math/invert");
        context.renderNode(invertNode);
    }

    @Test
    public void testCallInvert() {
        Node invertNode = Node.ROOT
                .withFunction("math/invert")
                .withInputAdded(Port.floatPort("value", 5))
                .withOutputAdded(Port.floatPort("output", 0));
        assertEquals(-5.0, context.renderPort(invertNode, "output"));
    }

    /**
     * Test if the insertion order of the ports is respected.
     * <p/>
     * This method tests a non-commutative operation in two directions to see if both work.
     */
    @Test
    public void testPortOrder() {
        Node subtract1 = Node.ROOT
                .withFunction("math/subtract")
                .withInputAdded(Port.floatPort("a", 10))
                .withInputAdded(Port.floatPort("b", 3))
                .withOutputAdded(Port.floatPort("output", 0));
        assertEquals(7.0, context.renderPort(subtract1, "output"));

        Node subtract2 = Node.ROOT
                .withFunction("math/subtract")
                .withInputAdded(Port.floatPort("b", 3))
                .withInputAdded(Port.floatPort("a", 10))
                .withOutputAdded(Port.floatPort("output", 0));
        assertEquals(-7.0, context.renderPort(subtract2, "output"));
    }

}
