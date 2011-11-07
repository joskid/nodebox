package nodebox.function;

import nodebox.node.Node;
import nodebox.node.NodeContext;
import nodebox.node.Port;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PythonLibraryTest {

    private final FunctionLibrary pyLibrary = PythonLibrary.loadScript("py-functions", "test/python/functions.py");
    private final FunctionRepository functions = FunctionRepository.of(pyLibrary);
    private final NodeContext context = new NodeContext(functions);

    @Test
    public void testAdd() {
        Node addNode = Node.ROOT
                .withName("add")
                .withFunction("py-functions/add")
                .withOutputAdded(Port.intPort("output", 0));
        Object result = context.renderPort(addNode, "output");
        assertEquals(0L, result);
    }

    @Test
    public void testAddWithArguments() {
        Node addNode = Node.ROOT
                .withName("add")
                .withFunction("py-functions/add")
                .withInputAdded(Port.intPort("v1", 1))
                .withInputAdded(Port.intPort("v2", 2))
                .withInputAdded(Port.intPort("v3", 3))
                .withOutputAdded(Port.intPort("output", 0));
        Object result = context.renderPort(addNode, "output");
        assertEquals(6L, result);
    }

    @Test
    public void testMultiplyFloat() {
        Node multiplyNode = Node.ROOT
                .withName("multiply")
                .withFunction("py-functions/multiply")
                .withInputAdded(Port.floatPort("v1", 10))
                .withInputAdded(Port.floatPort("v2", 2))
                .withOutputAdded(Port.floatPort("output", 0));
        Object result = context.renderPort(multiplyNode, "output");
        assertEquals(20.0, result);
    }

    @Test
    public void testMultiplyString() {
        Node multiplyNode = Node.ROOT
                .withName("multiply")
                .withFunction("py-functions/multiply")
                .withInputAdded(Port.stringPort("v1", "spam"))
                .withInputAdded(Port.intPort("v2", 3))
                .withOutputAdded(Port.intPort("output", 0));
        Object result = context.renderPort(multiplyNode, "output");
        assertEquals("spamspamspam", result);
    }
}
