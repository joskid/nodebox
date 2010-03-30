package nodebox.node;

import junit.framework.TestCase;

public class NodeTestCase extends TestCase {

    protected NodeLibraryManager manager;
    protected NodeLibrary testNodes, polygraphLibrary, testLibrary;
    protected Class numberNode, negateNode, addNode, addDirectNode, addConstantNode, multiplyNode, multiAddNode,
            floatNegateNode, convertToUppercaseNode, crashNode, testNetworkNode;
    protected Macro rootMacro;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        manager = new NodeLibraryManager();
        testNodes = new TestNodes();
        testLibrary = new NodeLibrary("test");
        rootMacro = testLibrary.getRootMacro();
        manager.add(testNodes);
        numberNode = TestNodes.IntVariable.class;
        negateNode = TestNodes.Negate.class;
        addNode = TestNodes.Add.class;
        multiplyNode = TestNodes.Multiply.class;
        multiAddNode = TestNodes.MultiAdd.class;
        floatNegateNode = TestNodes.FloatNegate.class;
        convertToUppercaseNode = TestNodes.ConvertToUppercase.class;
        crashNode = TestNodes.Crash.class;
        testNetworkNode = TestNodes.TestNetwork.class;
    }

    public void testDummy() {
        // This needs to be here, otherwise jUnit complains that there are no tests in this class.
    }

    //// Custom assertions ////

    public void assertExecuteException(Node node, Class expectedErrorClass) {
        try {
            node.execute(new CookContext());
            fail("The node " + node + " should have failed processing.");
        } catch (ExecuteException e) {
            // ProcessingErrors are not wrapped, so check if the expected error is a ExecuteException.
            if (expectedErrorClass == ExecuteException.class) return;
            assertEquals(expectedErrorClass, e.getCause().getClass());
        }
    }

    public void assertExecuteException(Node node, String expectedErrorMessage) {
        try {
            node.execute(new CookContext());
            fail("The node " + node + " should have failed processing.");
        } catch (ExecuteException e) {
            assertTrue("Was expecting error " + expectedErrorMessage + ", got " + e.toString(),
                    e.toString().toLowerCase().contains(expectedErrorMessage.toLowerCase()));
        }
    }

}
