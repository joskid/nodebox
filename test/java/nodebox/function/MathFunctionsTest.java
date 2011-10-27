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
    private final NodeContext context = new NodeContext(functionRepository);

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
        context.render(invertNode);
    }

    @Test
    public void testCallInvert() {
        Node invertNode = Node.ROOT
                .withFunction("math/invert")
                .withPortAdded(Port.intPort("value", 5));
        Object result = context.render(invertNode);
        assertEquals(-5, result);
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
                .withPortAdded(Port.intPort("a", 10))
                .withPortAdded(Port.intPort("b", 3));
        assertEquals(7, context.render(subtract1));

        Node subtract2 = Node.ROOT
                .withFunction("math/subtract")
                .withPortAdded(Port.intPort("b", 3))
                .withPortAdded(Port.intPort("a", 10));
        assertEquals(-7, context.render(subtract2));
    }

}
