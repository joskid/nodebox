package nodebox.node;

import nodebox.function.FunctionRepository;
import nodebox.function.MathFunctions;
import org.junit.Before;
import org.junit.Test;

import static nodebox.util.Assertions.assertResultsEqual;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ConnectionTest {

    public static final Node value42Node = Node.ROOT
            .withName("value42")
            .withFunction("math/value")
            .withInputAdded(Port.floatPort("number", 42));

    public static final Node value5Node = Node.ROOT
            .withName("value5")
            .withFunction("math/value")
            .withInputAdded(Port.floatPort("number", 5));

    public static final Node addNode = Node.ROOT
            .withName("add")
            .withFunction("math/add")
            .withInputAdded(Port.floatPort("v1", 0))
            .withInputAdded(Port.floatPort("v2", 0));

    public static final Node net = Node.ROOT
            .withChildAdded(value42Node)
            .withChildAdded(value5Node)
            .withChildAdded(addNode)
            .withRenderedChildName("add");

    private FunctionRepository functions = FunctionRepository.of(MathFunctions.LIBRARY);
    private NodeContext context;

    @Before
    public void setUp() throws Exception {
        context = new NodeContext(functions);
    }

    @Test
    public void testBasicConnection() {
        Node n = net;
        assertFalse(n.isConnected("value42"));
        assertFalse(n.isConnected("add"));
        n = n.connect("value42", "add", "v1");
        assertTrue(n.isConnected("value42"));
        assertTrue(n.isConnected("add"));
        n = n.connect("value5", "add", "v2");
        assertTrue(n.isConnected("value5"));
    }

    @Test
    public void testReplaceConnection() {
        Node n = net;
        n = n.connect("value42", "add", "v1");
        assertTrue(n.isConnected("value42"));
        n = n.connect("value5", "add", "v1");
        assertFalse(n.isConnected("value42"));
    }

    @Test
    public void testExecute() {
        Node n = net
                .connect("value42", "add", "v1")
                .connect("value5", "add", "v2");
        context.renderNetwork(n);
        assertResultsEqual(context.getResults(addNode), 47.0);
    }

    @Test
    public void testCycles() {
        // Create an infinite loop.
        Node n = net
                .connect("value42", "add", "v1")
                .connect("add", "value42", "number");
        // Infinite loops are allowed: each node is only executed once.
        context.renderNetwork(n);
        assertResultsEqual(context.getResults(addNode), 42.0);
    }

}
