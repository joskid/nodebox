package nodebox.util;

import com.google.common.collect.ImmutableList;
import junit.framework.Assert;
import nodebox.function.FunctionRepository;
import nodebox.function.ListFunctions;
import nodebox.function.MathFunctions;
import nodebox.node.Node;
import nodebox.node.NodeContext;

import java.util.List;

import static org.junit.Assert.assertEquals;

public final class Assertions {
    
    static private final FunctionRepository functionRepository = FunctionRepository.of(MathFunctions.LIBRARY, ListFunctions.LIBRARY);

    public static void assertResultsEqual(Iterable<Object> result, Object... args) {
        assertEquals(ImmutableList.copyOf(args), ImmutableList.copyOf(result));
    }
    
    public static void assertResultsEqual(Node network, Node child, Object... args) {
        NodeContext context = new NodeContext(functionRepository);
        Iterable<Object> values = context.renderChild(network, child);
        assertResultsEqual(values, args);
    }

    public static void assertResultsEqual(Node node, Object... args) {
        NodeContext context = new NodeContext(functionRepository);
        Iterable<Object> values = context.renderNode(node);
        assertResultsEqual(values, args);
    }
}
