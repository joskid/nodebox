package nodebox.node;

/**
 * Test the NodeInfo class.
 */
public class NodeInfoTest extends NodeTestCase {

    public static class MyNode extends Node {

        public MyNode(Macro parent) {
            super(parent);
            NodeInfo info = NodeInfo.builder(this)
                    .category("myCategory")
                    .description("myDescription")
                    .image("myImage")
                    .build();
            setInfo(info);
        }

    }

    /**
     * Test initializing NodeInfo with a builder.
     */
    public void testBuilder() {
        NodeInfo info = NodeInfo.builder("myLabel")
                .category("myCategory")
                .description("myDescription")
                .image("myImage")
                .build();
        assertEquals("myLabel", info.getLabel());
        assertEquals("myCategory", info.getCategory());
        assertEquals("myDescription", info.getDescription());
        assertEquals("myImage", info.getImage());
    }

    /**
     * Test the Node defaults.
     */
    public void testDefaults() {
        Node n = rootMacro.createChild(Node.class, "myName");
        NodeInfo info = n.getInfo();
        assertEquals("Node", info.getLabel());
        assertEquals("Custom", info.getCategory());
        assertEquals("", info.getDescription());
        assertEquals(NodeInfo.IMAGE_GENERIC, info.getImage());
    }

    /**
     * Test the info of a custom node class.
     */
    public void testCustomInfo() {
        Node n = rootMacro.createChild(MyNode.class, "customName");
        NodeInfo info = n.getInfo();
        assertEquals("MyNode", info.getLabel());
        assertEquals("myCategory", info.getCategory());
        assertEquals("myDescription", info.getDescription());
        assertEquals("myImage", info.getImage());
    }

}
