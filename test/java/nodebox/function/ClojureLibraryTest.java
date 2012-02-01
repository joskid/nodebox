package nodebox.function;

import nodebox.node.Node;
import nodebox.node.NodeContext;
import nodebox.node.Port;
import nodebox.util.LoadException;
import org.junit.Test;

import static nodebox.util.Assertions.assertResultsEqual;

public class ClojureLibraryTest {

    private final FunctionLibrary mathLibrary = ClojureLibrary.loadScript("test/clojure/math.clj");
    private final FunctionRepository functions = FunctionRepository.of(mathLibrary);
    private final NodeContext context = new NodeContext(functions);

    @Test
    public void testAdd() {
        Node addNode = Node.ROOT
                .withName("add")
                .withOutputType("int")
                .withFunction("clojure-math/add");
        Iterable<Object> results = context.renderNode(addNode);
        assertResultsEqual(results, 0L);
    }

    @Test
    public void testAddWithArguments() {
        Node addNode = Node.ROOT
                .withName("add")
                .withFunction("clojure-math/add")
                .withOutputType("int")
                .withInputAdded(Port.intPort("v1", 1))
                .withInputAdded(Port.intPort("v2", 2))
                .withInputAdded(Port.intPort("v3", 3));
        Iterable<Object> results = context.renderNode(addNode);
        assertResultsEqual(results, 6L);
    }

    @Test(expected = LoadException.class)
    public void testNoVarAtEnd() {
        ClojureLibrary.loadScript("test/clojure/no-var-at-end.clj");
    }
}
