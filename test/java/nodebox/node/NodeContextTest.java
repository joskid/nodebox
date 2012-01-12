package nodebox.node;

import nodebox.function.CoreVectorFunctions;
import nodebox.function.FunctionRepository;
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

    public static final Node pointToValuesNode = Node.ROOT
            .withName("point_to_values")
            .withFunction("corevector/pointToValues")
            .withInputAdded(Port.pointPort("point", new Point(11, 22)))
            .withOutputAdded(Port.floatPort("x", 0))
            .withOutputAdded(Port.floatPort("y", 0));

    public static final Node valuesToPointNode = Node.ROOT
            .withName("values_to_point")
            .withFunction("corevector/valuesToPoint")
            .withInputAdded(Port.floatPort("x", 0))
            .withInputAdded(Port.floatPort("y", 0))
            .withOutputAdded(Port.pointPort("point", Point.ZERO));

    public static final Node invertNode = Node.ROOT
            .withName("invert")
            .withFunction("math/invert")
            .withInputAdded(Port.floatPort("value", 0.0))
            .withOutputAdded(Port.floatPort("output", 0.0));

    public static final Node toNumbersNode = Node.ROOT
            .withName("toNumbers")
            .withFunction("math/toNumbers")
            .withListAwareness(true)
            .withInputAdded(Port.stringPort("string", ""))
            .withOutputAdded(Port.floatPort("numbers", 0.0));

    public static final FunctionRepository functions = FunctionRepository.of(CoreVectorFunctions.LIBRARY, MathFunctions.LIBRARY, SideEffects.LIBRARY);
    private NodeContext context;

    @Before
    public void setUp() throws Exception {
        context = new NodeContext(functions);
    }

    @Test
    public void testSingleOutput() {
        context.renderNode(valuesToPointNode);
        Map<NodeContext.NodePort, List<Object>> resultsMap = context.getResultsMap();
        assertEquals(1, resultsMap.size());
        List<Object> results = context.getResults(valuesToPointNode, "point");
        assertEquals(1, results.size());
        assertResultsEqual(results, Point.ZERO);
    }

    @Test
    public void testMultipleOutputs() {
        context.renderNode(pointToValuesNode);
        Map<NodeContext.NodePort, List<Object>> resultsMap = context.getResultsMap();
        assertEquals(2, resultsMap.size());
        assertResultsEqual(context.getResults(pointToValuesNode, "x"), 11.0);
        assertResultsEqual(context.getResults(pointToValuesNode, "y"), 22.0);
    }

    @Test
    public void testMultipleInputsMultipleOutputs() {
        Node xNumbers = toNumbersNode.extend().withName("xNumbers").withInputValue("string", "1 2 3");
        Node yNumbers = toNumbersNode.extend().withName("yNumbers").withInputValue("string", "11 22 33");
        Node net = Node.ROOT
                .withChildAdded(xNumbers)
                .withChildAdded(yNumbers)
                .withChildAdded(valuesToPointNode)
                .connect("xNumbers", "numbers", "values_to_point", "x")
                .connect("yNumbers", "numbers", "values_to_point", "y");
        context.renderChild(net, valuesToPointNode);
        List<Object> results = context.getResults(valuesToPointNode, "point");
        assertResultsEqual(results, new Point(1, 11), new Point(2, 22), new Point(3, 33));
    }

    @Test
    public void testSameOutputPort() {
        Node invert1 = invertNode.extend().withName("invert1").withInputValue("value", 1.0);
        Node invert2 = invertNode.extend().withName("invert2").withInputValue("value", 10.0);
        context = new NodeContext(functions);
        context.renderNode(invert1);
        context.renderNode(invert2);
        assertResultsEqual(context.getResults(invert1, "output"), -1.0);
        assertResultsEqual(context.getResults(invert2, "output"), -10.0);
    }

    @Test
    public void testListAwareProcessing() {
        Node toNumbers1 = toNumbersNode.extend().withInputValue("string", "1 2 3 4");
        context = new NodeContext(functions);
        context.renderNode(toNumbers1);
        List<Object> results = context.getResults(toNumbers1, "numbers");
        assertResultsEqual(results, 1.0, 2.0, 3.0, 4.0);
    }

    @Test
    public void testListUnawareProcessing() {
        Node invert1 = invertNode.extend().withName("invert1").withInputValue("value", 42.0);
        context.renderNode(invert1);
        List<Object> results = context.getResults(invert1, "output");
        assertResultsEqual(results, -42.0);
    }

    @Test
    public void testConnectedListProcessing() {
        Node toNumbers1 = toNumbersNode.extend().withName("toNumbers1").withInputValue("string", "1 2 3 4");
        Node invert1 = invertNode.extend().withName("invert1");
        Node net = Node.ROOT
                .withChildAdded(toNumbers1)
                .withChildAdded(invert1)
                .connect("toNumbers1", "numbers", "invert1", "value")
                .withRenderedChildName("invert1");
        context.renderChild(net, invert1);
        List<Object> results = context.getResults(invert1, "output");
        assertResultsEqual(results, -1.0, -2.0, -3.0, -4.0);
    }

    /**
     * Some nodes are not "pure" but produce side-effects, for example by fetching from an input device
     * or writing to an output device. Those nodes typically do not have inputs or outputs.
     */
    @Test
    public void testInputSideEffect() {
        Node getNumberNode = Node.ROOT
                .withFunction("side-effects/getNumber")
                .withOutputAdded(Port.intPort("number", 0));
        SideEffects.reset();
        SideEffects.theInput = 42;
        context.renderNode(getNumberNode);
        List<Object> results = context.getResults(getNumberNode, "number");
        assertResultsEqual(results, 42L);
    }

    @Test
    public void testOutputSideEffect() {
        Node setNumberNode = Node.ROOT
                .withFunction("side-effects/setNumber")
                .withInputAdded(Port.intPort("number", 42));
        SideEffects.reset();
        context.renderNode(setNumberNode);
        assertEquals(SideEffects.theOutput, 42L);
    }
    
    @Test
    public void testNodeTwice() {
        Node invert1Node = invertNode.withName("invert1").withInputValue("value", 42.0);
        Node invert2Node = invertNode.withName("invert2");
        Node net = Node.ROOT
                .withChildAdded(invert1Node)
                .withChildAdded(invert2Node)
                .connect("invert1", "output", "invert2", "value");
        context.renderChild(net, invert2Node);
        List<Object> results = context.getResults(invert2Node, "output");
        assertResultsEqual(results, 42.0);
     }

    // TODO Check that the node function is executed the exact amount we expect.

    // TODO Check list-aware node with no inputs.
    // TODO Check list-aware node with no outputs.
    // TODO Check list-aware node with single output.
    // TODO Check list-aware node with multiple outputs.

    // TODO Check list-unaware node with single output.
    // TODO Check list-unaware node with multiple outputs.
    // TODO Check list-unaware node with multiple inputs, single output.

}
