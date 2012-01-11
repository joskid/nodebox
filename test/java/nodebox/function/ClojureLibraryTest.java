package nodebox.function;

import com.google.common.collect.ImmutableList;
import nodebox.node.Node;
import nodebox.node.NodeContext;
import nodebox.node.Port;
import nodebox.util.LoadException;
import org.junit.Test;

import java.util.List;

import static nodebox.util.Assertions.assertResultsEqual;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
        List<Object> results = context.renderPort(addNode, "output");
        assertResultsEqual(results, 0L);
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
        List<Object> results = context.renderPort(addNode, "output");
        assertResultsEqual(results, 6L);
    }

    @Test(expected = LoadException.class)
    public void testNoVarAtEnd() {
        ClojureLibrary.loadScript("test/clojure/no-var-at-end.clj");
    }
}
