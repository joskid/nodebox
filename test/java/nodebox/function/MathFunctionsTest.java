package nodebox.function;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import nodebox.node.Node;
import nodebox.node.NodeContext;
import nodebox.node.NodeRenderException;
import nodebox.node.Port;
import nodebox.util.Assertions;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static nodebox.function.MathFunctions.sample;
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
                .withInputAdded(Port.floatPort("value", 5));
        assertEquals(ImmutableList.of(-5.0), context.renderNode(invertNode));
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
                .withInputAdded(Port.floatPort("b", 3));
        assertEquals(ImmutableList.of(7.0), context.renderNode(subtract1));

        Node subtract2 = Node.ROOT
                .withFunction("math/subtract")
                .withInputAdded(Port.floatPort("b", 3))
                .withInputAdded(Port.floatPort("a", 10));
        assertEquals(ImmutableList.of(-7.0), context.renderNode(subtract2));
    }

    @Test
    public void testSum() {
        assertEquals(0.0, MathFunctions.sum(ImmutableList.<Double>of()), 0.001);
        assertEquals(6.0, MathFunctions.sum(ImmutableList.of(1.0, 2.0, 3.0)), 0.001);
        assertEquals(-6.0, MathFunctions.sum(ImmutableList.of(-1.0, -2.0, -3.0)), 0.001);
    }

    @Test
    public void testMax() {
        assertEquals(0.0, MathFunctions.max(ImmutableList.<Double>of()), 0.001);
        assertEquals(3.0, MathFunctions.max(ImmutableList.of(1.0, 2.0, 3.0)), 0.001);
        assertEquals(-1.0, MathFunctions.max(ImmutableList.of(-1.0, -2.0, -3.0)), 0.001);
    }

    @Test
    public void testMin() {
        assertEquals(0.0, MathFunctions.min(ImmutableList.<Double>of()), 0.001);
        assertEquals(1.0, MathFunctions.min(ImmutableList.of(1.0, 2.0, 3.0)), 0.001);
        assertEquals(-3.0, MathFunctions.min(ImmutableList.of(-1.0, -2.0, -3.0)), 0.001);
    }

    @Test
    public void testSample() {
        Assertions.assertResultsEqual(sample(0, 1, 2));
        Assertions.assertResultsEqual(sample(1, 100, 200), 150.0);
        Assertions.assertResultsEqual(sample(2, 100, 200), 100.0, 200.0);
        Assertions.assertResultsEqual(sample(3, 100, 200), 100.0, 150.0, 200.0);
        Assertions.assertResultsEqual(sample(4, 100, 250), 100.0, 150.0, 200.0, 250.0);
        Assertions.assertResultsEqual(sample(3, 200, 100), 100.0, 150.0, 200.0);
        Assertions.assertResultsEqual(sample(3, 1, 1), 1.0, 1.0, 1.0);
        List<Double> values = sample(1000, 0, 100);
        double lastValue = values.get(values.size()-1);
        assertEquals("The last value needs to be exactly 100.", 100.0, lastValue, 0.0);
        assertTrue("The last value needs to be exactly 100.", lastValue <= 100.0);
    }

}
