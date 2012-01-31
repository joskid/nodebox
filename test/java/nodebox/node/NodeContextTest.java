package nodebox.node;

import com.google.common.collect.ImmutableList;
import nodebox.function.CoreVectorFunctions;
import nodebox.function.FunctionRepository;
import nodebox.function.MathFunctions;
import nodebox.graphics.Point;
import nodebox.util.SideEffects;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static nodebox.util.Assertions.assertResultsEqual;

public class NodeContextTest {

    public static final Node valuesToPointNode = Node.ROOT
            .withName("values_to_point")
            .withFunction("corevector/valuesToPoint")
            .withOutputType("point")
            .withInputAdded(Port.floatPort("x", 0))
            .withInputAdded(Port.floatPort("y", 0));

    public static final Node addNode = Node.ROOT
            .withName("add")
            .withFunction("math/add")
            .withInputAdded(Port.floatPort("v1", 0.0))
            .withInputAdded(Port.floatPort("v2", 0.0));

    public static final Node invertNode = Node.ROOT
            .withName("invert")
            .withFunction("math/invert")
            .withInputAdded(Port.floatPort("value", 0.0));

    public static final Node toNumbersNode = Node.ROOT
            .withName("toNumbers")
            .withFunction("math/toNumbers")
            .withListStrategy(Node.AS_IS_STRATEGY)
            .withInputAdded(Port.stringPort("string", ""));

    public static final Node threeNumbers = toNumbersNode
            .extend()
            .withName("threeNumbers")
            .withInputValue("string", "1 2 3");

    public static final Node fiveNumbers = toNumbersNode
            .extend()
            .withName("fiveNumbers")
            .withInputValue("string", "100 200 300 400 500");

    public static final FunctionRepository functions = FunctionRepository.of(CoreVectorFunctions.LIBRARY, MathFunctions.LIBRARY, SideEffects.LIBRARY);
    private NodeContext context;

    @Before
    public void setUp() throws Exception {
        context = new NodeContext(functions);
        SideEffects.reset();
    }

    @Test
    public void testSingleOutput() {
        context.renderNode(valuesToPointNode);
        Map<Node, List<Object>> resultsMap = context.getResultsMap();
        assertEquals(1, resultsMap.size());
        List<Object> results = context.getResults(valuesToPointNode);
        assertEquals(1, results.size());
        assertResultsEqual(results, Point.ZERO);
    }

    @Test
    public void testSameOutputPort() {
        Node invert1 = invertNode.extend().withName("invert1").withInputValue("value", 1.0);
        Node invert2 = invertNode.extend().withName("invert2").withInputValue("value", 10.0);
        context = new NodeContext(functions);
        context.renderNode(invert1);
        context.renderNode(invert2);
        assertResultsEqual(context.getResults(invert1), -1.0);
        assertResultsEqual(context.getResults(invert2), -10.0);
    }

    @Test
    public void testListAwareProcessing() {
        Node toNumbers1 = toNumbersNode.extend().withInputValue("string", "1 2 3 4");
        context = new NodeContext(functions);
        context.renderNode(toNumbers1);
        List<Object> results = context.getResults(toNumbers1);
        assertResultsEqual(results, 1.0, 2.0, 3.0, 4.0);
    }

    @Test
    public void testListUnawareProcessing() {
        Node invert1 = invertNode.extend().withName("invert1").withInputValue("value", 42.0);
        context.renderNode(invert1);
        List<Object> results = context.getResults(invert1);
        assertResultsEqual(results, -42.0);
    }

    @Test
    public void testConnectedListProcessing() {
        Node toNumbers1 = toNumbersNode.extend().withName("toNumbers1").withInputValue("string", "1 2 3 4");
        Node invert1 = invertNode.extend().withName("invert1");
        Node net = Node.ROOT
                .withChildAdded(toNumbers1)
                .withChildAdded(invert1)
                .connect("toNumbers1", "invert1", "value")
                .withRenderedChildName("invert1");
        context.renderChild(net, invert1);
        List<Object> results = context.getResults(invert1);
        assertResultsEqual(results, -1.0, -2.0, -3.0, -4.0);
    }

    @Test
    public void testEmptyListProcessing() {
        Node noNumbers = toNumbersNode.extend().withName("noNumbers").withInputValue("string", "");
        Node add1 = addNode.extend().withName("add1");
        Node net = Node.ROOT
                .withChildAdded(noNumbers)
                .withChildAdded(add1)
                .connect("noNumbers", "add1", "v1");
        context.renderChild(net, add1);
        List<Object> results = context.getResults(add1);
        assertTrue(results.isEmpty());
    }

    /**
     * Some nodes are not "pure" but produce side-effects, for example by fetching from an input device
     * or writing to an output device. Those nodes typically do not have inputs or outputs.
     */
    @Test
    public void testInputSideEffect() {
        Node getNumberNode = Node.ROOT
                .withFunction("side-effects/getNumber");
        SideEffects.theInput = 42;
        context.renderNode(getNumberNode);
        List<Object> results = context.getResults(getNumberNode);
        assertResultsEqual(results, 42L);
    }

    @Test
    public void testOutputSideEffect() {
        Node setNumberNode = Node.ROOT
                .withFunction("side-effects/setNumber")
                .withInputAdded(Port.intPort("number", 42));
        context.renderNode(setNumberNode);
        assertEquals(SideEffects.theOutput, 42L);
    }

    @Test
    public void testSamePrototypeTwice() {
        Node invert1Node = invertNode.withName("invert1").withInputValue("value", 42.0);
        Node invert2Node = invertNode.withName("invert2");
        Node net = Node.ROOT
                .withChildAdded(invert1Node)
                .withChildAdded(invert2Node)
                .connect("invert1", "invert2", "value");
        context.renderChild(net, invert2Node);
        List<Object> results = context.getResults(invert2Node);
        assertResultsEqual(results, 42.0);
    }

    /**
     * Test that the node function is executed the exact amount we expect.
     */
    @Test
    public void testExecuteAmount() {
        Node toNumbers1Node = toNumbersNode.withName("toNumbers1").withInputValue("string", "1 2 3");
        Node incNode = Node.ROOT
                .withName("inc")
                .withFunction("side-effects/increaseAndCount")
                .withInputAdded(Port.floatPort("number", 0));
        Node net = Node.ROOT
                .withChildAdded(toNumbers1Node)
                .withChildAdded(incNode)
                .connect("toNumbers1", "inc", "number");
        context.renderChild(net, incNode);
        assertEquals(3, SideEffects.theCounter);
        List<Object> results = context.getResults(incNode);
        assertResultsEqual(results, 2.0, 3.0, 4.0);
    }

    /**
     * Test the combination of a list input and port value.
     */
    @Test
    public void testListWithValue() {
        Node toNumbers1Node = toNumbersNode.withName("toNumbers1").withInputValue("string", "1 2 3");
        Node add1 = addNode.extend().withName("add1").withInputValue("v2", 100.0);
        Node net = Node.ROOT
                .withChildAdded(toNumbers1Node)
                .withChildAdded(add1)
                .connect("toNumbers1", "add1", "v1");
        context.renderChild(net, add1);
        List<Object> results = context.getResults(add1);
        assertResultsEqual(results, 101.0, 102.0, 103.0);
    }

    @Test
    public void testListPolicies() {
        Node net = Node.ROOT
                .withChildAdded(threeNumbers)
                .withChildAdded(fiveNumbers)
                .withChildAdded(addNode)
                .connect("threeNumbers", addNode.getName(), "v1")
                .connect("fiveNumbers", addNode.getName(), "v2");
        context.renderChild(net, addNode);
        assertResultsEqual(context.getResults(addNode), 101.0, 202.0,303.0);
    }

    // TODO Check list-aware node with no inputs.
    // TODO Check list-aware node with no outputs.
    // TODO Check list-aware node with single output.
    // TODO Check list-aware node with multiple outputs.

    // TODO Check list-unaware node with single output.
    // TODO Check list-unaware node with multiple outputs.
    // TODO Check list-unaware node with multiple inputs, single output.

}
