package nodebox.function;

import nodebox.node.Node;
import nodebox.node.NodeContext;
import nodebox.node.Port;
import nodebox.util.LoadException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ClojureLibraryTest {

    private FunctionRepository functionRepository;

    @Before
    public void setUp() throws Exception {
        ClojureLibrary mathLibrary = ClojureLibrary.loadScript("test/clojure/math.clj");
        functionRepository = FunctionRepository.of(mathLibrary);
    }

    @Test
    public void testAdd() {
        NodeContext context = new NodeContext(functionRepository);
        Node addNode = Node.ROOT
                .withName("add")
                .withFunction("clojure-math/add");
        Object result = context.render(addNode);
        assertEquals(0L, result);
    }

    @Test
    public void testAddWithArguments() {
        NodeContext context = new NodeContext(functionRepository);
        Node addNode = Node.ROOT
                .withName("add")
                .withFunction("clojure-math/add")
                .withPortAdded(Port.intPort("v1", 1))
                .withPortAdded(Port.intPort("v2", 2))
                .withPortAdded(Port.intPort("v3", 3));
        Object result = context.render(addNode);
        assertEquals(6L, result);
    }

    @Test(expected = LoadException.class)
    public void testNoVarAtEnd() {
        ClojureLibrary.loadScript("test/clojure/no-var-at-end.clj");
    }
}
