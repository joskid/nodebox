package nodebox.node;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class NodeTest {

    @Test
    public void testPath() {
        assertEquals("/child", Node.path("/", Node.ROOT.withName("child")));
        assertEquals("/parent/child", Node.path("/parent", Node.ROOT.withName("child")));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRelativePath() {
        Node.path("", Node.ROOT.withName("child"));
    }

}
