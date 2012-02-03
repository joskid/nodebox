package nodebox.util;

import com.google.common.collect.ImmutableList;
import nodebox.function.FunctionRepository;
import nodebox.function.ListFunctions;
import nodebox.function.MathFunctions;
import nodebox.function.TestFunctions;
import nodebox.node.Node;
import nodebox.node.NodeContext;

import static org.junit.Assert.assertEquals;

public final class Assertions {

    static private final FunctionRepository functionRepository =
            FunctionRepository.of(TestFunctions.LIBRARY, MathFunctions.LIBRARY, ListFunctions.LIBRARY);

    public static void assertResultsEqual(Iterable<?> result, Object... args) {
        assertEquals(ImmutableList.copyOf(args), ImmutableList.copyOf(result));
    }

    public static void assertResultsEqual(Node network, Node child, Object... args) {
        NodeContext context = new NodeContext(functionRepository);
        Iterable<?> values = context.renderChild(network, child);
        assertResultsEqual(values, args);
    }

    public static void assertResultsEqual(Node node, Object... args) {
        NodeContext context = new NodeContext(functionRepository);
        Iterable<?> values = context.renderNode(node);
        assertResultsEqual(values, args);
    }
}
