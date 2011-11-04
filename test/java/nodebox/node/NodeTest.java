package nodebox.node;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

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

    @Test
    public void testPortOrder() {
        Port pAlpha = Port.intPort("alpha", 1);
        Port pBeta = Port.intPort("beta", 2);
        Node original = Node.ROOT.withInputAdded(pAlpha).withInputAdded(pBeta);
        ImmutableList<String> orderedPortNames = ImmutableList.of("alpha", "beta");
        assertEquals(orderedPortNames, portNames(original));

        Node alphaChanged = original.withInputValue("alpha", 11);
        assertEquals(orderedPortNames, portNames(alphaChanged));
    }

    public List<String> portNames(Node n) {
        List<String> portNames = new LinkedList<String>();
        for (Port p : n.getInputs()) {
            portNames.add(p.getName());
        }
        return portNames;
    }

}
