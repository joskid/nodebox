package nodebox.function;

import nodebox.node.Node;
import nodebox.node.NodeContext;
import nodebox.node.NodeRenderException;
import nodebox.node.Port;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MathFunctionsTest {

    private final FunctionLibrary mathLibrary = MathFunctions.LIBRARY;
    private final FunctionRepository functionRepository = FunctionRepository.of(mathLibrary);
    private final NodeContext context = new NodeContext(0);

    @Test
    public void testInvertExists() {
        assertTrue(functionRepository.hasFunction("math/invert"));
        assertTrue(mathLibrary.hasFunction("invert"));
        Function function = functionRepository.getFunction("math/invert");
        assertEquals("invert", function.getName());
    }

    @Test(expected = NodeRenderException.class)
    public void testCallInvertWithNoArguments() {
        Node invertNode = Node.ROOT.withFunction("math/invert");
        context.renderChildNode(functionRepository, invertNode);
    }

    @Test
    public void testCallInvert() {
        Node invertNode = Node.ROOT
                .withFunction("math/invert")
                .withInputAdded(Port.intPort("value", 5))
                .withOutputAdded(Port.intPort("output", 0));
        assertEquals(-5, context.firstOutputOfRender(functionRepository, invertNode));
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
                .withInputAdded(Port.intPort("a", 10))
                .withInputAdded(Port.intPort("b", 3))
                .withOutputAdded(Port.intPort("output", 0));
        assertEquals(7, context.firstOutputOfRender(functionRepository, subtract1));

        Node subtract2 = Node.ROOT
                .withFunction("math/subtract")
                .withInputAdded(Port.intPort("b", 3))
                .withInputAdded(Port.intPort("a", 10))
                .withOutputAdded(Port.intPort("output", 0));
        assertEquals(-7, context.firstOutputOfRender(functionRepository, subtract2));
    }

}
