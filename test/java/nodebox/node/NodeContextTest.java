package nodebox.node;

import nodebox.function.CoreVectorFunctions;
import nodebox.function.FunctionRepository;
import nodebox.graphics.Point;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

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

    public static final FunctionRepository functions = FunctionRepository.of(CoreVectorFunctions.LIBRARY);
    private NodeContext context;

    @Before
    public void setUp() throws Exception {
        context = new NodeContext(functions);
    }

    @Test
    public void testSingleOutput() {
        context.renderNode(valuesToPointNode);
        Map<Port, Object> results = context.getResults();
        assertEquals(1, results.size());
        assertTrue(results.containsKey(valuesToPointNode.getOutput("point")));
        Object firstOutputValue = results.values().iterator().next();
        assertEquals(Point.ZERO, firstOutputValue);
    }

    @Test
    public void testMultipleOutputs() {
        context.renderNode(pointToValuesNode);
        Map<Port, Object> results = context.getResults();
        assertEquals(2, results.size());
        assertEquals(11.0, context.getResult(pointToValuesNode, "x"));
        assertEquals(22.0, context.getResult(pointToValuesNode, "y"));
    }

}
