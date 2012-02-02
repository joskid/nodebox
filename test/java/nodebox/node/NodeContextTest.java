package nodebox.node;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import nodebox.function.CoreVectorFunctions;
import nodebox.function.FunctionRepository;
import nodebox.function.ListFunctions;
import nodebox.function.MathFunctions;
import nodebox.graphics.Point;
import nodebox.util.SideEffects;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
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

    public static final Node makeNumbersNode = Node.ROOT
            .withName("makeNumbers")
            .withFunction("math/makeNumbers")
            .withListStrategy(Node.FLATTEN_STRATEGY)
            .withInputAdded(Port.stringPort("string", ""));

    public static final Node threeNumbers = makeNumbersNode
            .extend()
            .withName("threeNumbers")
            .withInputValue("string", "1 2 3");

    public static final Node fiveNumbers = makeNumbersNode
            .extend()
            .withName("fiveNumbers")
            .withInputValue("string", "100 200 300 400 500");

    public static final Node cycle = Node.ROOT
            .withName("cycle")
            .withFunction("list/cycle")
            .withListStrategy(Node.AS_IS_STRATEGY)
            .withInputAdded(Port.customPort("list", "seq"));

    public static final FunctionRepository functions = FunctionRepository.of(CoreVectorFunctions.LIBRARY, MathFunctions.LIBRARY, ListFunctions.LIBRARY, SideEffects.LIBRARY);
    private NodeContext context;

    @Before
    public void setUp() throws Exception {
        context = new NodeContext(functions);
        SideEffects.reset();
    }

    @Test
    public void testSingleOutput() {
        context.renderNode(valuesToPointNode);
        Map<Node, Iterable<?>> resultsMap = context.getResultsMap();
        assertEquals(1, resultsMap.size());
        Iterable<?> results = context.getResults(valuesToPointNode);
        List resultsList = ImmutableList.copyOf(results);
        assertEquals(1, resultsList.size());
        assertResultsEqual(resultsList, Point.ZERO);
    }

    @Test
    public void testSameOutputPort() {
        Node invert1 = invertNode.extend().withName("invert1").withInputValue("value", 1.0);
        Node invert2 = invertNode.extend().withName("invert2").withInputValue("value", 10.0);
        assertResultsEqual(context.renderNode(invert1), -1.0);
        assertResultsEqual(context.renderNode(invert2), -10.0);
    }

    @Test
    public void testListAwareProcessing() {
        Node makeNumbers1 = makeNumbersNode.extend().withInputValue("string", "1 2 3 4");
        assertResultsEqual(context.renderNode(makeNumbers1), 1.0, 2.0, 3.0, 4.0);
    }

    @Test
    public void testListUnawareProcessing() {
        Node invert1 = invertNode.extend().withName("invert1").withInputValue("value", 42.0);
        assertResultsEqual(context.renderNode(invert1), -42.0);
    }

    @Test
    public void testConnectedListProcessing() {
        Node makeNumbers1 = makeNumbersNode.extend().withName("makeNumbers1").withInputValue("string", "1 2 3 4");
        Node invert1 = invertNode.extend().withName("invert1");
        Node net = Node.ROOT
                .withChildAdded(makeNumbers1)
                .withChildAdded(invert1)
                .connect("makeNumbers1", "invert1", "value")
                .withRenderedChildName("invert1");
        assertResultsEqual(context.renderChild(net, invert1), -1.0, -2.0, -3.0, -4.0);
    }

    @Test
    public void testEmptyListProcessing() {
        Node noNumbers = makeNumbersNode.extend().withName("noNumbers").withInputValue("string", "");
        Node add1 = addNode.extend().withName("add1");
        Node net = Node.ROOT
                .withChildAdded(noNumbers)
                .withChildAdded(add1)
                .connect("noNumbers", "add1", "v1");
        context.renderChild(net, add1);
        assertResultsEqual(context.renderChild(net, add1));
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
        assertResultsEqual(context.renderNode(getNumberNode), 42L);
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
        assertResultsEqual(context.renderChild(net, invert2Node), 42.0);
    }

    /**
     * Test that the node function is executed the exact amount we expect.
     */
    @Test
    public void testExecuteAmount() {
        Node makeNumbers1 = makeNumbersNode.withName("makeNumbers1").withInputValue("string", "1 2 3");
        Node incNode = Node.ROOT
                .withName("inc")
                .withFunction("side-effects/increaseAndCount")
                .withInputAdded(Port.floatPort("number", 0));
        Node net = Node.ROOT
                .withChildAdded(makeNumbers1)
                .withChildAdded(incNode)
                .connect("makeNumbers1", "inc", "number");
        context.renderChild(net, incNode);
        assertEquals(3, SideEffects.theCounter);
        Iterable<?> results = context.getResults(incNode);
        assertResultsEqual(results, 2.0, 3.0, 4.0);
    }

    /**
     * Test the combination of a list input and port value.
     */
    @Test
    public void testListWithValue() {
        Node makeNumbers1 = makeNumbersNode.withName("makeNumbers1").withInputValue("string", "1 2 3");
        Node add1 = addNode.extend().withName("add1").withInputValue("v2", 100.0);
        Node net = Node.ROOT
                .withChildAdded(makeNumbers1)
                .withChildAdded(add1)
                .connect("makeNumbers1", "add1", "v1");
        assertResultsEqual(context.renderChild(net, add1), 101.0, 102.0, 103.0);
    }

    @Test
    public void testShortestList() {
        Node net = Node.ROOT
                .withChildAdded(threeNumbers)
                .withChildAdded(fiveNumbers)
                .withChildAdded(addNode)
                .connect("threeNumbers", addNode.getName(), "v1")
                .connect("fiveNumbers", addNode.getName(), "v2");
        assertResultsEqual(context.renderChild(net, addNode), 101.0, 202.0, 303.0);
    }

    @Test
    public void testWrapInListStrategy() {
        Node sum = Node.ROOT
                .withName("sum")
                .withFunction("math/sum")
                .withListStrategy("wrap-in-list")
                .withInputAdded(Port.floatPort("numbers", 0));
        Node net = Node.ROOT
                .withChildAdded(sum)
                .withChildAdded(threeNumbers)
                .connect("threeNumbers", sum.getName(), "numbers");
        assertResultsEqual(context.renderChild(net, sum), 6.0);
    }

    @Test
    public void testInfiniteLists() {
        Node net = Node.ROOT
                .withChildAdded(threeNumbers)
                .withChildAdded(cycle)
                .connect("threeNumbers", "cycle", "list");
        Iterable<?> results = context.renderChild(net, cycle);
        // This list is infinite! Ask only the first ten numbers.
        List<Object> resultsList = ImmutableList.copyOf(Iterables.limit(results, 10));
        assertResultsEqual(resultsList, 1.0, 2.0, 3.0, 1.0, 2.0, 3.0, 1.0, 2.0, 3.0, 1.0);
    }

    // TODO Check list-aware node with no inputs.
    // TODO Check list-aware node with no outputs.
    // TODO Check list-aware node with single output.
    // TODO Check list-aware node with multiple outputs.

    // TODO Check list-unaware node with single output.
    // TODO Check list-unaware node with multiple outputs.
    // TODO Check list-unaware node with multiple inputs, single output.

}
