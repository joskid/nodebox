package nodebox.node;

import nodebox.function.FunctionRepository;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NodeContextTest {

    private FunctionRepository functionRepository;

    @Before
    public void setUp() throws Exception {
        functionRepository = FunctionRepository.of();
    }

    @Test
    public void testRenderRoot() {
        NodeContext context = new NodeContext(functionRepository);
        Object result = context.renderNetwork(Node.ROOT);
        assertEquals(0, result);
    }

}
