package nodebox.function;

import nodebox.node.Node;
import nodebox.node.NodeContext;
import nodebox.node.Port;
import nodebox.util.LoadException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ClojureLibraryTest {

    private final FunctionLibrary mathLibrary = ClojureLibrary.loadScript("test/clojure/math.clj");
    private final FunctionRepository functions = FunctionRepository.of(mathLibrary);
    private final NodeContext context = new NodeContext(functions);

    @Test
    public void testAdd() {
        Node addNode = Node.ROOT
                .withName("add")
                .withFunction("clojure-math/add")
                .withOutputAdded(Port.intPort("output", 0));
        Object result = context.renderPort(addNode, "output");
        assertEquals(0L, result);
    }

    @Test
    public void testAddWithArguments() {
        Node addNode = Node.ROOT
                .withName("add")
                .withFunction("clojure-math/add")
                .withInputAdded(Port.intPort("v1", 1))
                .withInputAdded(Port.intPort("v2", 2))
                .withInputAdded(Port.intPort("v3", 3))
                .withOutputAdded(Port.intPort("output", 0));
        Object result = context.renderPort(addNode, "output");
        assertEquals(6L, result);
    }

    @Test(expected = LoadException.class)
    public void testNoVarAtEnd() {
        ClojureLibrary.loadScript("test/clojure/no-var-at-end.clj");
    }
}
