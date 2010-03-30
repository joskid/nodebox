/*
 * This file is part of NodeBox.
 *
 * Copyright (C) 2008 Frederik De Bleser (frederik@pandora.be)
 *
 * NodeBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NodeBox is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with NodeBox. If not, see <http://www.gnu.org/licenses/>.
 */
package nodebox.node;

import nodebox.node.event.NodeAttributesChangedEvent;
import nodebox.node.event.NodeDirtyEvent;
import nodebox.node.event.NodePositionChangedEvent;
import nodebox.node.event.NodeUpdatedEvent;

public class NodeTest extends NodeTestCase {

    private class TestAttributeListener implements NodeEventListener {

        public int counter;

        public void receive(NodeEvent event) {
            if (!(event instanceof NodeAttributesChangedEvent)) return;
            counter++;
        }

    }

    /**
     * A listener that counts dirty and updated events.
     */
    private class TestDirtyListener implements NodeEventListener {

        public Node source;
        public int dirtyCounter, updatedCounter;

        private TestDirtyListener(Node source) {
            this.source = source;
        }

        public void receive(NodeEvent event) {
            if (event.getSource() != source) return;
            if (event instanceof NodeDirtyEvent) {
                dirtyCounter++;
            } else if (event instanceof NodeUpdatedEvent) {
                updatedCounter++;
            }
        }

    }

    /**
     * A listener that stores the last received event.
     */
    private class MockNodeEventListener implements NodeEventListener {

        NodeEvent event;

        public void receive(NodeEvent event) {
            this.event = event;
        }

        public void reset() {
            this.event = null;
        }

    }

    /**
     * A node that dynamically creates a new port on cook.
     */
    public static class AddPortNode extends Node {

        public AddPortNode(Macro parent) {
            super(parent);
        }

        @Override
        public void cook(CookContext context) throws RuntimeException {
            createPort("test", Integer.class, Port.Direction.IN);
        }

    }

    /**
     * An empty node with a short class name.
     */
    public static class N extends Node {

        public N(Macro parent) {
            super(parent);
        }

    }

    /**
     * Test the attributes of the base Node class.
     */
    public void testBaseNode() {
        Node baseNode = rootMacro.createChild(Node.class);
        assertEquals("", baseNode.getDescription());
        assertEquals(0, baseNode.getPorts().size());
        assertEquals(Node.Mode.CONSUMER, baseNode.getMode());
        // Check if cooking throws an exception.
        baseNode.cook(new CookContext());
    }

    /**
     * Test the automatically created type name.
     */
    public void testTypeName() {
        Node n1 = rootMacro.createChild(Node.class);
        assertEquals("node", n1.getTypeName());
        Node n2 = rootMacro.createChild(AddPortNode.class);
        assertEquals("addPortNode", n2.getTypeName());
        Node n3 = rootMacro.createChild(N.class);
        assertEquals("n", n3.getTypeName());
    }

    /**
     * Test the node attributes.
     */
    public void testAttributes() {
        Node n = rootMacro.createChild(Node.class);
        assertEquals("", n.getDescription());
        assertEquals(NodeAttributes.IMAGE_GENERIC, n.getImage());
        MockNodeEventListener l = new MockNodeEventListener();
        testLibrary.addListener(l);
        n.setAttributes(NodeAttributes.builder().description("hello").image("test.png").build());
        assertEquals(NodeAttributesChangedEvent.class, l.event.getClass());
        assertEquals("hello", n.getDescription());
        assertEquals("test.png", n.getImage());
    }

    /**
     * Test the parent and root attribute.
     */
    public void testParent() {
        // Creating a node with the Node constructor does not add it to the children.
        Node n = new Node(rootMacro);
        assertTrue(n.hasParent());
        assertEquals(rootMacro, n.getParent());
        assertEquals(rootMacro, n.getRootMacro());
        assertTrue(rootMacro.hasChild(n));
        assertTrue(rootMacro.hasChild(n.getName()));
        assertFalse(rootMacro.hasChild("xxx"));

        Macro parent = (Macro) rootMacro.createChild(Macro.class, "parent");
        Node child = parent.createChild(Macro.class, "child");
        assertEquals(rootMacro, rootMacro.getRootMacro());
        assertEquals(rootMacro, parent.getRootMacro());
        assertEquals(rootMacro, child.getRootMacro());
        assertNull(rootMacro.getParent());
        assertEquals(rootMacro, parent.getParent());
        assertEquals(parent, child.getParent());
        assertTrue(rootMacro.hasChild(parent));
        assertTrue(rootMacro.hasChild("parent"));
    }

    /**
     * Test the absolute path of a node.
     */
    public void testAbsolutePath() {
        Macro root = testLibrary.getRootMacro();
        Macro parent = (Macro) root.createChild(Macro.class, "parent");
        Node child = parent.createChild(Macro.class, "child");
        assertEquals("/", root.getAbsolutePath());
        assertEquals("/parent", parent.getAbsolutePath());
        assertEquals("/parent/child", child.getAbsolutePath());
    }

    /**
     * Test the position attribute.
     */
    public void testPosition() {
        MockNodeEventListener l = new MockNodeEventListener();
        testLibrary.addListener(l);
        rootMacro.setPosition(50, 20);
        assertEquals(NodePositionChangedEvent.class, l.event.getClass());
        assertEquals(50.0, rootMacro.getX());
        assertEquals(20.0, rootMacro.getY());
    }

    /**
     * Test creating and removing ports.
     */
    public void testPorts() {
        Node n = rootMacro.createChild(Node.class);
        assertFalse(n.hasPort("p"));
        try {
            n.getPort(null);
            fail("Should have failed precondition check.");
        } catch (Exception e) {
        }
        try {
            assertNull(n.getPort("p"));
            fail("Should have thrown PortNotFoundException.");
        } catch (PortNotFoundException e) {
        }

        Port p = n.createPort("p", Integer.class, Port.Direction.IN);
        assertNotNull(p);
        assertTrue(n.hasPort("p"));
        assertEquals(p, n.getPort("p"));
        assertEquals(Integer.class, p.getDataClass());
        assertEquals(Port.Direction.IN, p.getDirection());

        boolean success = n.removePort(p);
        assertTrue(success);
        assertFalse(n.hasPort("p"));
    }

    /**
     * Test basic node execution.
     */
    public void testBasicUsage() {
        Node add = rootMacro.createChild(TestNodes.Add.class);

        // Check default values
        assertEquals(0, add.getValue("v1"));
        assertEquals(0, add.getValue("v2"));
        assertEquals(0, add.getValue("result"));

        // Execute
        add.execute(new CookContext());
        assertEquals(0, add.getValue("result"));

        // Change values and execute
        add.setValue("v1", 40);
        add.setValue("v2", 2);
        add.execute(new CookContext());
        assertEquals(42, add.getValue("result"));
    }

    public void testGetValue() {
//        // Inheritance: A <- B <- C
//        Node nodeA = Node.ROOT_NODE.newInstance(testLibrary, "A");
//        nodeA.addPort("a", Port.Type.FLOAT, 1F);
//        Node nodeB = nodeA.newInstance(testLibrary, "B");
//        nodeB.addPort("b", Port.Type.FLOAT, 2F);
//        Node nodeC = nodeB.newInstance(testLibrary, "C");
//        nodeC.addPort("c", Port.Type.FLOAT, 3F);
//        assertEquals(1F, nodeC.asFloat("a"));
//        assertEquals(2F, nodeC.asFloat("b"));
//        assertEquals(3F, nodeC.asFloat("c"));
    }

    public void testSetValue() {
//        // Inheritance: A <- B <- C
//        Node nodeA = Node.ROOT_NODE.newInstance(testLibrary, "A");
//        nodeA.addPort("a", Port.Type.FLOAT, 1F);
//        Node nodeB = nodeA.newInstance(testLibrary, "B");
//        nodeB.addPort("b", Port.Type.FLOAT, 2F);
//        Node nodeC = nodeB.newInstance(testLibrary, "C");
//        nodeC.addPort("c", Port.Type.FLOAT, 3F);
//        nodeC.setValue("a", 10F);
//        nodeC.setValue("b", 20F);
//        nodeC.setValue("c", 30F);
//        assertEquals(1F, nodeA.asFloat("a"));
//        assertEquals(2F, nodeB.asFloat("b"));
//        assertEquals(10F, nodeC.asFloat("a"));
//        assertEquals(20F, nodeC.asFloat("b"));
//        assertEquals(30F, nodeC.asFloat("c"));
    }

    /**
     * Test propagation behaviour for parameters.
     */
    public void testPortPropagation() {
//        // Inheritance: A <- B
//        Node nodeA = Node.ROOT_NODE.newInstance(testLibrary, "A");
//        nodeA.addPort("f", Port.Type.FLOAT, 1F);
//        Node nodeB = nodeA.newInstance(testLibrary, "B");
//        // The parameters of A and B are not the same.
//        assertNotSame(nodeA.getPort("f"), nodeB.getPort("f"));
//
//        nodeA.setValue("f", 10F);
//        // The value for the B port doesn't automatically change when A was changed.
//        assertEquals(10F, nodeA.asFloat("f"));
//        assertEquals(1F, nodeB.asFloat("f"));
//        // Setting the value of B does not affect the value of A.
//        nodeB.getPort("f").setValue(55F);
//        assertEquals(10F, nodeA.asFloat("f"));
//        assertEquals(55F, nodeB.asFloat("f"));
//        // Reverting to the default value will force B to load the new port value
//        // from the prototype.
//        nodeB.getPort("f").revertToDefault();
//        assertEquals(10F, nodeB.asFloat("f"));
    }

    /**
     * Test if parameters with expressions are inherited correctly.
     */
    public void testExpressionPropagation() {
//        // Inheritance: A <- B
//        Node nodeA = Node.ROOT_NODE.newInstance(testLibrary, "A");
//        Port pF = nodeA.addPort("f", Port.Type.INT, 0);
//        String expr1 = "12 + 5";
//        pF.setExpression(expr1);
//        Node nodeB = nodeA.newInstance(testLibrary, "B");
//        assertEquals(expr1, nodeB.getPort("f").getExpression());
//        // Changing the expression of A does not automatically change that of B.
//        String expr2 = "4 * 2";
//        pF.setExpression(expr2);
//        assertEquals(expr1, nodeB.getPort("f").getExpression());
//        // Reverting to default does.
//        nodeB.getPort("f").revertToDefault();
//        assertEquals(expr2, nodeB.getPort("f").getExpression());
    }

    /**
     * Test if the attributes on ports are set correctly.
     */
    public void testPortAttributes() {
//        Node nodeA = Node.ROOT_NODE.newInstance(testLibrary, "A", String.class);
//        assertEquals(String.class, nodeA.getDataClass());
//        Port outputPort = nodeA.getOutputPort();
//        assertEquals("output", outputPort.getName());
//        assertEquals(Port.Direction.OUT, outputPort.getDirection());
//        assertEquals(null, outputPort.getValue());
//        Port stringPort = nodeA.addPort("stringPort");
//        assertEquals("stringPort", stringPort.getName());
//        assertEquals(Port.Direction.IN, stringPort.getDirection());
//        assertEquals(null, stringPort.getValue());
    }

    /**
     * Test if ports are copied from the prototype to the new instance.
     */
    public void testPortPropagation2() {
//        Node nodeA = Node.ROOT_NODE.newInstance(testLibrary, "A", Polygon.class);
//        nodeA.addPort("polygon");
//        Node nodeB = nodeA.newInstance(testLibrary, "B");
//        assertTrue(nodeB.hasPort("polygon"));
//        assertEquals(Polygon.class, nodeB.getDataClass());
    }

    /**
     * Test parent/child relationships
     */
    public void testChildNodes() {
//        Node net = Node.ROOT_NODE.newInstance(testLibrary, "net");
//        Node rect = Node.ROOT_NODE.newInstance(testLibrary, "rect");
//        rect.setParent(net);
//        assertTrue(net.contains("rect"));
    }

    public void testNodeNaming() {
        Node n = rootMacro.createChild(Node.class);
        assertEquals("node1", n.getName());

        assertInvalidName(n, null, "names cannot be null.");
        assertInvalidName(n, "", "names cannot be empty.");
        assertInvalidName(n, "  ", "names cannot contain spaces.");
        assertInvalidName(n, "  x  ", "names cannot contain spaces.");


        assertInvalidName(n, "1234", "names cannot start with a digit.");

        assertInvalidName(n, "root", "names can not be one of the reserved words.");
        assertInvalidName(n, "network", "names can not be one of the reserved words.");
        assertInvalidName(n, "context", "names can not be one of the reserved words.");

        assertInvalidName(n, "__reserved", "names cannot start with double underscores");
        assertInvalidName(n, "what!", "Only lowercase, numbers and underscore are allowed");
        assertInvalidName(n, "$-#34", "Only lowercase, numbers and underscore are allowed");
        assertInvalidName(n, "", "names cannot be empty");
        assertInvalidName(n, "very_very_very_very_very_very_long_name", "names cannot be longer than 30 characters");

        assertValidName(n, "radius");
        assertValidName(n, "_test");
        assertValidName(n, "_");
        assertValidName(n, "_1234");
        assertValidName(n, "a1234");
        assertValidName(n, "node1");
        assertValidName(n, "UPPERCASE");
        assertValidName(n, "uPpercase");
    }

    public void testUniqueName() {
        Macro macro = (Macro) testLibrary.getRootMacro().createChild(Macro.class);
        macro.createChild(Node.class, "mynode");
        Node node1 = macro.createChild(Node.class);
        assertEquals("node1", node1.getName());
        assertEquals("node2", macro.uniqueName("node"));
        assertEquals("node2", macro.uniqueName("node1"));
        assertEquals("node33", macro.uniqueName("node33"));
        macro.createChild(Node.class, "node99");
        assertEquals("node2", macro.uniqueName("node"));
        assertEquals("node100", macro.uniqueName("node99"));
        assertEquals("node12a1", macro.uniqueName("node12a"));
    }

    public void testError() {
        Node bad = rootMacro.createChild(crashNode, "crash");
        TestDirtyListener listener = new TestDirtyListener(bad);
        testLibrary.addListener(listener);
        assertExecuteException(bad, "/ by zero");
    }

    /**
     * Test if errors with expressions also set the error flag on the node.
     */
    public void testExpressionError() {
//        Node n = numberNode.newInstance(testLibrary, "number1");
//        n.setExpression("value", "***");
//        try {
//            n.update();
//            fail("Should have caused an exception.");
//        } catch (ExecuteException e) {
//            assertTrue(e.getCause().toString().toLowerCase().contains("cannot compile expression"));
//            assertTrue(n.hasError());
//            // As stated in Node#update(CookContext), even if an error occurred the node is still marked as clean
//            // and events are fired. It is important to mark the node as clean so that subsequent changes to the node
//            // mark it as dirty, triggering an event. This allows you to fix the cause of the error in the node.
//            assertFalse(n.isDirty());
//            assertNull(n.getOutputValue());
//        }
//        n.setExpression("value", "10 + 1");
//        assertTrue(n.isDirty());
//        n.update();
//        assertFalse(n.hasError());
//        assertFalse(n.isDirty());
//        assertEquals(11, n.getOutputValue());
    }

    /**
     * Test if errors with dependencies fail fast, and have the correct error behaviour.
     */
    public void testDependencyError() {
        Macro root = testLibrary.getRootMacro();
        Node negate1 = root.createChild(negateNode);
        Node crash1 = root.createChild(crashNode);
        root.connect(negate1.getPort("value"), crash1.getPort("result"));
        try {
            root.execute(new CookContext());
            fail("Should have raised an ExecuteException.");
        } catch (ExecuteException e) {
            // The error flag is limited to the dependency that caused the error.
            // The crash node caused the error, so it has the error flag,
            // but the dependent node, negate1, doesn't get the error flag.
            assertTrue(crash1.hasError());
            assertFalse(negate1.hasError());
        }
    }

    /**
     * This is the same test as PortTest#testExpressionDependencies, but at the Node level.
     */
    public void testRemoveExpressionDependency() {
//        Node net = testNetworkNode.newInstance(testLibrary, "net");
//        Node number1 = net.create(numberNode);
//        number1.addPort("bob", Port.Type.INT, 10);
//        number1.setExpression("value", "bob");
//        number1.setRendered();
//        net.update();
//        assertEquals(10, net.getOutputValue());
//        number1.removePort("bob");
//        try {
//            net.update();
//            fail();
//        } catch (ExecuteException e) {
//            assertTrue(e.getCause().getMessage().toLowerCase().contains("cannot evaluate expression"));
//        }
//        assertTrue(net.hasError());
//        assertTrue(number1.hasError());
//        assertNull(net.getOutputValue());
    }

    /**
     * Test if a node can dynamically create ports.
     */
    public void testDynamicPortCreation() {
        Node n = testLibrary.getRootMacro().createChild(AddPortNode.class);
        n.execute(new CookContext());
        assertTrue(n.hasPort("test"));
        assertExecuteException(n, "already a port named test");
    }

    public void testCopyWithUpstream() {
//        // We create a simple network where
//        // alpha1 <- beta1 <- gamma1
//        // beta1 will be the node to copy. This checks if upstreams/downstreams are handled correctly.
//        Node net1 = testNetworkNode.newInstance(testLibrary, "net1");
//        Node net2 = testNetworkNode.newInstance(testLibrary, "net2");
//        Node alpha1 = net1.create(Node.ROOT_NODE, "alpha1", Integer.class);
//        Node beta1 = net1.create(Node.ROOT_NODE, "beta1", Integer.class);
//        String originalDescription = "Beta description";
//        beta1.setDescription(originalDescription);
//        beta1.setValue("_code", new PythonCode("def cook(self): return self.value"));
//        Node gamma1 = net1.create(Node.ROOT_NODE, "gamma1", Integer.class);
//        int originalValue = 5;
//        beta1.addPort("value", Port.Type.INT, originalValue);
//        Port betaPort1 = beta1.addPort("betaPort1");
//        Port gammaPort1 = gamma1.addPort("gammaPort1");
//        betaPort1.connect(alpha1);
//        gammaPort1.connect(beta1);
//
//        // Update and clean the network.
//        gamma1.update();
//        assertFalse(beta1.isDirty());
//        assertEquals(originalValue, beta1.getOutputValue());
//
//        // Copying under the same parent will give the node a unique name.
//        Node beta2 = net1.copyChild(beta1, net1);
//        assertEquals("beta2", beta2.getName());
//
//        // Copying under a different parent keep the original name.
//        Node beta3 = net1.copyChild(beta1, net2);
//        assertEquals("beta1", beta3.getName());
//
//        // The node inherits from the same prototype as the original.
//        assertSame(beta1.getPrototype(), beta2.getPrototype());
//
//        // It also retains all the same changes as the original.
//        assertSame(beta1.getDataClass(), beta2.getDataClass());
//        assertTrue(beta2.hasPort("value"));
//        assertEquals(originalValue, beta2.asInt("value"));
//        assertTrue(beta2.hasPort("betaPort1"));
//
//        // Some other properties.
//        assertEquals(20.0, beta2.getX());
//        assertEquals(80.0, beta2.getY());
//        assertEquals(originalDescription, beta2.getDescription());
//
//        // The new node will be dirty and won't have any output data.
//        assertTrue(beta2.isDirty());
//        assertNull(beta2.getOutputValue());
//
//        // It also retains connections to the upstream nodes,
//        // although the connection objects differ.
//        // It does not retain connections to the downstream nodes since
//        // that would replace existing connections.
//        assertTrue(beta2.isConnectedTo(alpha1));
//        Connection newConn = beta2.getPort("betaPort1").getConnection();
//        assertNotSame(betaPort1.getConnection(), newConn);
//        assertFalse(beta2.isConnectedTo(gamma1));
//
//        // If the new node is under a different parent connections cannot be retained.
//        assertFalse(beta3.isConnected());
//
//        // Try updating the node to see if the results are still correct.
//        beta2.update();
//        assertEquals(originalValue, beta2.getOutputValue());
//
//        // Changes to the copy should not affect the original and vice versa.
//        int newValueForOriginal = 11;
//        int newValueForCopy = 33;
//        beta1.setValue("value", newValueForOriginal);
//        assertEquals(originalValue, beta2.asInt("value"));
//        beta2.setValue("value", newValueForCopy);
//        assertEquals(newValueForOriginal, beta1.asInt("value"));
    }

    public void testCopyChild() {
//        Node net1 = testNetworkNode.newInstance(testLibrary, "net1");
//        Node net2 = testNetworkNode.newInstance(testLibrary, "net2");
//        Node number1 = net1.create(numberNode);
//        Node newNumber1 = net1.copyChild(number1, net2);
//        assertEquals(net2, newNumber1.getParent());
    }

    public void testCopyComplex() {
//        // number1-> negate1 -> addConstant1 -> multiAdd1
//        // We'll copy negate1 and addConstant1.
//        Node net1 = testNetworkNode.newInstance(testLibrary, "net1");
//        Node number1 = net1.create(numberNode);
//        Node negate1 = net1.create(negateNode);
//        Node addConstant1 = net1.create(addConstantNode);
//        Node multiAdd1 = net1.create(multiAddNode);
//        // Wire up the network.
//        multiAdd1.getPort("values").connect(addConstant1);
//        addConstant1.getPort("value").connect(negate1);
//        negate1.getPort("value").connect(number1);
//        // Set some values.
//        number1.setValue("value", 42);
//        addConstant1.setValue("constant", 2);
//        multiAdd1.setRendered();
//        // Check the output.
//        net1.update();
//        assertEquals(-40, net1.getOutputValue());
//        // Copy negate1 and addConstant1.
//        ArrayList<Node> children = new ArrayList<Node>();
//        children.add(negate1);
//        children.add(addConstant1);
//        Collection<Node> newChildren = net1.copyChildren(children, net1);
//        assertEquals(2, newChildren.size());
//        Node negate2 = net1.getChild("negate2");
//        Node addConstant2 = net1.getChild("addConstant2");
//        assertNotNull(negate2);
//        assertNotNull(addConstant2);
//        assertTrue(negate2.isConnectedTo(number1));
//        assertTrue(addConstant2.isConnectedTo(negate2));
//        assertFalse(addConstant2.isConnectedTo(negate1));
//        assertFalse(multiAdd1.isConnectedTo(addConstant2));
//        // Connect the copies to multiAdd1 and update.
//        multiAdd1.getPort("values").connect(addConstant2);
//        net1.update();
//        assertEquals(-80, net1.getOutputValue());
//        // Copy negate1 and addConstant1 into a different network.
//        Node net2 = testNetworkNode.newInstance(testLibrary, "net2");
//        Collection<Node> net2Children = net1.copyChildren(children, net2);
//        assertEquals(2, net2Children.size());
//        Node net2Negate1 = net2.getChild("negate1");
//        Node net2AddConstant1 = net2.getChild("addConstant1");
//        assertNotNull(net2Negate1);
//        assertNotNull(net2AddConstant1);
//        assertFalse(net2Negate1.isConnectedTo(number1));
//        assertTrue(net2AddConstant1.isConnectedTo(net2Negate1));
//        assertFalse(multiAdd1.isConnectedTo(net2AddConstant1));
    }

    public void testCopyChildren() {
//        Node root = testLibrary.getRootMacro();
//        Node net1 = testNetworkNode.newInstance(testLibrary, "net1");
//        Node number1 = net1.create(numberNode);
//        Node negate1 = net1.create(negateNode);
//        Node subnet1 = net1.create(testNetworkNode, "subnet1");
//        Node subNumber1 = subnet1.create(numberNode);
//        negate1.getPort("value").connect(number1);
//        negate1.setRendered();
//        number1.setValue("value", 42);
//        subNumber1.setValue("value", 33);
//        net1.update();
//        assertEquals(-42, net1.getOutputValue());
//        try {
//            root.copyChild(negate1, root);
//            fail("Should have thrown error.");
//        } catch (IllegalArgumentException e) {
//            assertTrue(e.getMessage().contains("not a child of this parent"));
//        }
//        Node net2 = root.copyChild(net1, root);
//        assertEquals("net2", net2.getName());
//        Node net2number1 = net2.getChild("number1");
//        Node net2negate1 = net2.getChild("negate1");
//        assertEquals("negate1", net2negate1.getName());
//        assertTrue(net2negate1.getPort("value").isConnectedTo(net2number1));
//        assertEquals(33, net2.getChild("subnet1").getChild("number1").getValue("value"));
//        // Not updated yet.
//        assertNull(net2.getOutputValue());
//        net2.update();
//        assertEquals(-42, net1.getOutputValue());
    }

    public void testNewInstanceChildren() {
//        Node root = testLibrary.getRootMacro();
//        // Test if children of the prototype are copied as well.
//        Node protoNet = root.create(testNetworkNode, "protoNet");
//        Node number1 = protoNet.create(numberNode);
//        Node negate1 = protoNet.create(negateNode);
//        number1.setExpression("value", "40+2");
//        negate1.getPort("value").connect(number1);
//        negate1.setRendered();
//        // Create new node based on prototype.
//        Node protoNet1 = root.create(protoNet);
//        assertEquals("protoNet1", protoNet1.getName());
//        assertTrue(protoNet1.contains("number1"));
//        assertTrue(protoNet1.contains("negate1"));
//        assertTrue(protoNet1.getChild("negate1").isConnectedTo(protoNet1.getChild("number1")));
//        assertEquals(0, protoNet1.getChild("number1").getValue("value"));
//        assertEquals("40+2", protoNet1.getChild("number1").getPort("value").getExpression());
//        protoNet1.update();
//        assertEquals(42, protoNet1.getChild("number1").getValue("value"));
//        assertEquals(-42, protoNet1.getOutputValue());
    }

    public void testNewInstanceExpression() {
//        Node protoNumber = numberNode.newInstance(testLibrary, "protoNumber");
//        protoNumber.setExpression("value", "40+2");
//        Node proto1 = protoNumber.newInstance(testLibrary, "proto1");
//        assertEquals(0, proto1.getValue("value"));
//        assertEquals("40+2", proto1.getPort("value").getExpression());
//        proto1.update();
//        assertEquals(42, proto1.getValue("value"));
//        assertEquals(42, proto1.getOutputValue());
    }

    public void testDisconnect() {
//        Node net1 = testNetworkNode.newInstance(testLibrary, "net1");
//        Node number1 = net1.create(numberNode);
//        Node number2 = net1.create(numberNode);
//        Node multiAdd1 = net1.create(multiAddNode);
//        number1.setValue("value", 5);
//        number2.setValue("value", 8);
//        multiAdd1.getPort("values").connect(number1);
//        multiAdd1.getPort("values").connect(number2);
//        multiAdd1.update();
//        assertFalse(multiAdd1.isDirty());
//        assertEquals(2, multiAdd1.getPort("values").getValues().size());
//        assertEquals(13, multiAdd1.getOutputValue());
//        multiAdd1.disconnect();
//        assertTrue(multiAdd1.isDirty());
//        assertFalse(multiAdd1.isConnected());
//        assertFalse(number1.isConnected());
//        assertFalse(number2.isConnected());
//        multiAdd1.update();
//        assertEquals(0, multiAdd1.getPort("values").getValues().size());
//        assertEquals(0, multiAdd1.getOutputValue());
    }

    public void testNodeAttributeEvent() {
//        TestAttributeListener l = new TestAttributeListener();
//        Node test = Node.ROOT_NODE.newInstance(testLibrary, "test");
//        testLibrary.addListener(l);
//        // Setting the name to itself does not trigger an event.
//        test.setName("test");
//        assertEquals(0, l.nameCounter);
//        test.setName("newname");
//        assertEquals(1, l.nameCounter);
//        Port p1 = test.addPort("p1", Port.Type.FLOAT);
//        assertEquals(1, l.parameterCounter);
//        p1.setName("parameter1");
//        assertEquals(2, l.parameterCounter);
//        // TODO: These trigger PortAttributeChanged
//        //p1.setBoundingMethod(Port.BoundingMethod.HARD);
//        //assertEquals(3, l.parameterCounter);
//        //p1.setMinimumValue(0F);
//        //assertEquals(4, l.parameterCounter);
//        // Changing the value does not trigger the event.
//        // The event only happens for metadata, not data.
//        // If you want to catch that, listen for NodeDirtyEvents.
//        p1.setValue(20F);
//        assertEquals(2, l.parameterCounter);
//        test.removePort("parameter1");
//        assertEquals(3, l.parameterCounter);
    }

    /**
     * Test if print messages get output.
     */
    public void testOutput() {
//        CookContext ctx;
//        PythonCode helloCode = new PythonCode("def cook(self): print 'hello'");
//        Node test = Node.ROOT_NODE.newInstance(testLibrary, "test");
//        test.setValue("_code", helloCode);
//        ctx = new CookContext();
//        test.update(ctx);
//        assertEquals("hello\n", ctx.getOutput());
//
//        // Try this in a network. All the output of the nodes should be merged.
//        Node parent = Node.ROOT_NODE.newInstance(testLibrary, "parent");
//        Node child = parent.create(Node.ROOT_NODE, "child");
//        child.setValue("_code", helloCode);
//        child.setRendered();
//        ctx = new CookContext();
//        parent.update(ctx);
//        assertEquals("hello\n", ctx.getOutput());
    }

    /**
     * Test the hasStampExpression on the node.
     * <p/>
     * This method is used to determine if parameters/nodes should be marked as dirty when re-evaluating upstream,
     * which is what happens in the copy node.
     */
    public void testHasStampExpression() {
//        Node n = Node.ROOT_NODE.newInstance(testLibrary, "test");
//        Port pAlpha = n.addPort("alpha", Port.Type.FLOAT);
//        Port pBeta = n.addPort("beta", Port.Type.FLOAT);
//        assertFalse(n.hasStampExpression());
//        // Set the parameters to expressions that do not use the stamp function.
//        pAlpha.setExpression(" 12 + 5");
//        pBeta.setExpression("random(1, 5, 10)");
//        assertFalse(n.hasStampExpression());
//        // Set one of the parameters to the stamp function.
//        pBeta.setExpression("stamp(\"mybeta\", 42)");
//        assertTrue(n.hasStampExpression());
//        // Set the other port expression to a stamp function as well.
//        pAlpha.setExpression("stamp(\"myalpha\", 0) * 5");
//        assertTrue(n.hasStampExpression());
//        // Clear out the expressions one by one.
//        pAlpha.clearExpression();
//        assertTrue(n.hasStampExpression());
//        // Change the beta port to some other expression.
//        pBeta.setExpression("85 - 6");
//        assertFalse(n.hasStampExpression());
    }

    /**
     * Test if setting a stamp expressions marks the correct nodes as dirty.
     */
    public void testStampExpression() {
//        Node number1 = numberNode.newInstance(testLibrary, "number1");
//        Node stamp1 = Node.ROOT_NODE.newInstance(testLibrary, "stamp1", Integer.class);
//        stamp1.addPort("value");
//        stamp1.getPort("value").connect(number1);
//        // The code prepares upstream dependencies for stamping, processes them and negates the output.
//        String stampCode = "def cook(self):\n" +
//                "  context.put('my_a', 99)\n" +
//                "  self.node.stampDirty()\n" +
//                "  self.node.updateDependencies(context)\n" +
//                "  return -self.value # Negate the output";
//        stamp1.setValue("_code", new PythonCode(stampCode));
//        Port pValue = number1.getPort("value");
//        // Set number1 to a regular value. This should not influence the stamp operation.
//        pValue.set(12);
//        stamp1.update();
//        assertEquals(-12, stamp1.getOutputValue());
//        // Set number1 to an expression. Since we're not using stamp, nothing strange should happen to the output.
//        pValue.setExpression("2 + 1");
//        stamp1.update();
//        assertEquals(-3, stamp1.getOutputValue());
//        // Set number1 to an unknown stamp expression. The default value will be picked.
//        pValue.setExpression("stamp(\"xxx\", 19)");
//        stamp1.update();
//        assertEquals(-19, stamp1.getOutputValue());
//        // Set number1 to the my_a stamp expression. The expression will be picked up.
//        pValue.setExpression("stamp(\"my_a\", 33)");
//        stamp1.update();
//        assertEquals(-99, stamp1.getOutputValue());
    }

    /**
     * Test the behaviour of {@link Node#stampDirty()}.
     *
     * @throws ExpressionError if the expression causes an error. This indicates a regression.
     */
    public void testMarkStampedDirty() throws ExpressionError {
//        // Setup a graph where a <- b <- c.
//        Node a = Node.ROOT_NODE.newInstance(testLibrary, "a", Integer.class);
//        Node b = Node.ROOT_NODE.newInstance(testLibrary, "b", Integer.class);
//        Node c = Node.ROOT_NODE.newInstance(testLibrary, "c", Integer.class);
//        a.addPort("a", Port.Type.INT);
//        b.addPort("b", Port.Type.INT);
//        Port bIn = b.addPort("in");
//        Port cIn = c.addPort("in");
//        bIn.connect(a);
//        cIn.connect(b);
//        // Update the graph. This will make a, b and c clean.
//        c.update();
//        assertFalse(a.isDirty());
//        assertFalse(b.isDirty());
//        assertFalse(c.isDirty());
//        // Set b to a stamped expression. This will make node b, and all of its dependencies, dirty.
//        b.setExpression("b", "stamp(\"my_b\", 55)");
//        assertTrue(b.hasStampExpression());
//        assertFalse(a.isDirty());
//        assertTrue(b.isDirty());
//        assertTrue(c.isDirty());
//        // Update the graph, cleaning all of the nodes.
//        c.update();
//        assertFalse(a.isDirty());
//        assertFalse(b.isDirty());
//        assertFalse(c.isDirty());
//        // Mark only stamped upstream nodes as dirty. This will make b dirty, and all of its dependencies.
//        c.stampDirty();
//        assertFalse(a.isDirty());
//        assertTrue(b.isDirty());
//        assertTrue(c.isDirty());
//        // Remove the expression and update. This will make all nodes clean again.
//        b.clearExpression("b");
//        c.update();
//        // Node b will not be dirty, since everything was updated.
//        assertFalse(b.isDirty());
//        // Since there are no nodes with stamp expressions, marking the stamped upstream nodes will have no effect.
//        c.stampDirty();
//        assertFalse(a.isDirty());
//        assertFalse(b.isDirty());
//        assertFalse(c.isDirty());
    }

    //// Helper functions ////

    private void assertInvalidName(Node n, String newName, String reason) {
        try {
            n.setName(newName);
            fail("the following condition was not met: " + reason);
        } catch (Exception ignored) {
        }
    }

    private void assertValidName(Node n, String newName) {
        try {
            n.setName(newName);
        } catch (InvalidNameException e) {
            fail("The name \"" + newName + "\" should have been accepted.");
        }
    }

}
