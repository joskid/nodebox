package nodebox.function;

import nodebox.node.Node;
import nodebox.node.NodeContext;
import nodebox.node.Port;
import nodebox.util.LoadException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ClojureLibraryTest {

    private final FunctionLibrary mathLibrary = ClojureLibrary.loadScript("test/clojure/math.clj");
    private final FunctionRepository functionRepository = FunctionRepository.of(mathLibrary);
    private final NodeContext context = new NodeContext(0);

    @Test
    public void testAdd() {
        Node addNode = Node.ROOT
                .withName("add")
                .withFunction("clojure-math/add");
        Object result = context.renderChildNode(functionRepository, addNode);
        assertEquals(0L, result);
    }

    @Test
    public void testAddWithArguments() {
        Node addNode = Node.ROOT
                .withName("add")
                .withFunction("clojure-math/add")
                .withPortAdded(Port.intPort("v1", 1))
                .withPortAdded(Port.intPort("v2", 2))
                .withPortAdded(Port.intPort("v3", 3));
        Object result = context.renderChildNode(functionRepository, addNode);
        assertEquals(6L, result);
    }

    @Test(expected = LoadException.class)
    public void testNoVarAtEnd() {
        ClojureLibrary.loadScript("test/clojure/no-var-at-end.clj");
    }
}
