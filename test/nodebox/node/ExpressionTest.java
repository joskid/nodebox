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

public class ExpressionTest extends NodeTestCase {

    public void testSimple() throws ExpressionError {
        Node n =  rootMacro.createChild(TestNodes.IntVariable.class);
        Port pValue = n.getPort("value");
        Expression e = new Expression(pValue, "1 + 2");
        assertEquals(3, e.asInt());
    }

    /**
     * Test port interaction between nodes.
     *
     * @throws ExpressionError if the expression causes an error. This indicates a regression.
     */
    public void testNodeLocal() throws ExpressionError {
//        Node net = testNetworkNode.newInstance(testLibrary, "net");
//        Node addDirect = net.create(addDirectNode);
//        Port p1 = addDirect.getPort("v1");
//        Port p2 = addDirect.getPort("v2");
//        p2.setValue(12);
//        assertExpressionEquals(12, p1, "v2");
    }

    public void testExpressionErrors() throws ExpressionError {
//        // Setting an expression immediately evaluates it, but does not throw an exception if the expression is invalid.
//        // Instead, you need to check hasPortExpression.
//        Node test = Node.ROOT_NODE.newInstance(testLibrary, "test");
//        Port pX = test.addPort("x", Port.Type.INT, 3);
//        assertInvalidExpression(pX, "y", "could not access: y");
//        Port pY = test.addPort("y", Port.Type.INT, 5);
//        assertExpressionEquals(5, pX, "y");
//        // Expression of port x is still set to "y"
//        assertEquals("y", pX.getExpression());
    }

    /**
     * Test if changing the expression removes the previous dependencies.
     *
     * @throws ExpressionError if the expression causes an error. This indicates a regression.
     */
    public void testDependencyRemoval() throws ExpressionError {
//        Node alpha = Node.ROOT_NODE.newInstance(testLibrary, "alpha");
//        Node beta = Node.ROOT_NODE.newInstance(testLibrary, "beta");
//        Node gamma = Node.ROOT_NODE.newInstance(testLibrary, "gamma");
//        Port aValue = alpha.addPort("value", Port.Type.INT, 42);
//        Port bValue = beta.addPort("value", Port.Type.INT, 33);
//        Port gValue = gamma.addPort("value", Port.Type.INT);
//        // gamma.value depends on alpha.value.
//        gamma.setExpression("value", "alpha.value");
//        assertTrue(gValue.dependsOn(aValue));
//        gamma.update();
//        assertEquals(42, gamma.getValue("value"));
//        // gamma.value depends on beta.value.
//        gamma.setExpression("value", "beta.value");
//        assertFalse(gValue.dependsOn(aValue));
//        assertTrue(gValue.dependsOn(bValue));
//        gamma.update();
//        assertEquals(33, gamma.getValue("value"));
//        // gamma.value no longer depends on alpha or beta.
//        gamma.setExpression("value", "10 + 1");
//        assertFalse(gValue.dependsOn(aValue));
//        assertFalse(gValue.dependsOn(bValue));
//        gamma.update();
//        assertEquals(11, gamma.getValue("value"));
    }

    /**
     * When an error is triggered while setting the expression, make sure that all dependencies already created
     * are removed.
     *
     * @throws ExpressionError if the expression causes an error. This indicates a regression.
     */
    public void testDependencyRemovalOnError() throws ExpressionError {
//        Node alpha = Node.ROOT_NODE.newInstance(testLibrary, "alpha");
//        Node beta = Node.ROOT_NODE.newInstance(testLibrary, "beta");
//        Port aValue = alpha.addPort("value", Port.Type.INT, 42);
//        Port bValue = beta.addPort("value", Port.Type.INT);
//        // Create a good expression, replace it with a bad expression.
//        beta.setExpression("value", "alpha.value");
//        assertTrue(bValue.dependsOn(aValue));
//        beta.update();
//        assertEquals(42, beta.getValue("value"));
//        // Replace the good expression with an invalid expression.
//        assertInvalidExpression(bValue, "****", "not a statement");
//        // Check that the dependency was removed.
//        assertFalse(bValue.dependsOn(aValue));
//        // Create a "semi-good" expression: one that can evaluate up to a certain point.
//        assertInvalidExpression(bValue, "alpha.value + xxx", "could not access: xxx");
//        assertFalse(bValue.dependsOn(aValue));
    }

    /**
     * Test if expression errors mark the port as dirty.
     */
    public void testDirtyOnError() throws ExpressionError {
//        Node alpha = Node.ROOT_NODE.newInstance(testLibrary, "alpha");
//        Port aValue = alpha.addPort("value", Port.Type.INT, 42);
//        assertTrue(alpha.isDirty());
//        alpha.update();
//        assertFalse(alpha.isDirty());
//        assertInvalidExpression(aValue, "****", "not a statement");
//        assertTrue(alpha.isDirty());
//        try {
//            alpha.update();
//        } catch (ExecuteException e) {
//            assertTrue(e.getMessage().toLowerCase().contains("cannot compile expression"));
//        }
//        assertEquals(42, aValue.getValue());
    }


    /**
     * Test what happens if your expression depends on a port that gets removed.
     *
     * @throws ExpressionError if the expression causes an error. This indicates a regression.
     */
    public void testDeadDependencies() throws ExpressionError {
//        Node test = Node.ROOT_NODE.newInstance(testLibrary, "test");
//        Port pX = test.addPort("x", Port.Type.INT, 3);
//        Port pY = test.addPort("y", Port.Type.INT, 5);
//        pX.setExpression("y");
//        assertTrue(pX.dependsOn(pY));
//        pX.update(new CookContext());
//        assertEquals(5, pX.getValue());
//        test.removePort("y");
//        // At this point, the port dependency should no longer exist.
//        assertFalse(pX.getDependencies().contains(pY));
//        try {
//            pX.update(new CookContext());
//            fail();
//        } catch (ExpressionError e) {
//            // update throws an error since the expression references a port that cannot be found.
//            //throw e;
//            assertTrue(e.getCause().getMessage().toLowerCase().contains("unable to resolve variable 'y'"));
//        }
//        // The value hasn't changed.
//        assertEquals(5, pX.getValue());
    }

    /**
     * Test the same as testDeadDependencies, but in the reverse. (What happens if a port that depends on this
     * port gets removed).
     * <p/>
     * This is less dramatic than the other case; we just need to make sure that we don't accidentally dereference
     * a dead Port.
     */
    public void testDeadDependents() {
//        Node net = Node.ROOT_NODE.newInstance(testLibrary, "net");
//        Node number1 = net.create(numberNode);
//        Node number2 = net.create(numberNode);
//        Port pValue1 = number1.getPort("value");
//        Port pValue2 = number2.getPort("value");
//        number1.setValue("value", 5);
//        number2.getPort("value").setExpression("number1.value");
//        number2.setRendered();
//        net.update();
//        assertFalse(net.isDirty());
//        assertEquals(5, net.getOutputValue());
//        number1.setValue("value", 13);
//        assertTrue(net.isDirty());
//        net.update();
//        assertEquals(13, net.getOutputValue());
//        assertTrue(pValue1.getDependents().contains(pValue2));
//        number2.removePort("value");
//        assertFalse(pValue1.getDependents().contains(pValue2));
    }

    /**
     * You can only reference other parameters and the built-in functions.
     * <p/>
     * Test what happens if you break this rule.
     */
    public void testOnlyReferencePorts() {
//        Node net = Node.ROOT_NODE.newInstance(testLibrary, "net");
//        Node number1 = net.create(numberNode);
//        Node number2 = net.create(numberNode);
//        Port pValue2 = number2.getPort("value");
//        // Setting the expression does not throw an error.
//        pValue2.setExpression("number1");
//        // Evaluating the node does.
//        assertProcessingError(number2, "cannot be converted to int");
    }

    public void testCycles() throws ExpressionError {
//        Node net = testNetworkNode.newInstance(testLibrary, "net");
//        Node number1 = net.create(numberNode);
//        Node addDirect1 = net.create(addDirectNode);
//        Port pValue = number1.getPort("value");
//        Port pV1 = addDirect1.getPort("v1");
//        Port pV2 = addDirect1.getPort("v2");
//        // Create a direct cycle.
//        assertInvalidExpression(pValue, "value", "refers to itself");
//        // This should not have created any connections
//        assertTrue(pValue.getDependencies().isEmpty());
//        // Set a direct value to number1.value. Clear the expression first.
//        pValue.clearExpression();
//        pValue.set(42);
//        assertExpressionEquals(42, pV1, "number1.value");
//        // Create a 2-node cycle with expressions
//        assertInvalidExpression(pValue, "addDirect1.v1", "cyclic dependency");
//        // Now create a 2-port cycle within the same node.
//        pV1.setExpression("v2");
//        addDirect1.update();
//        assertInvalidExpression(pV2, "v1", "cyclic dependency");
    }

    /**
     * This test checks if parameters that refer to other parameters in the same node have the most recent data.
     * Specifically, it tests if the order of processing doesn't affect the data flow.
     */
    public void testStaleData() {
//        Node net = testNetworkNode.newInstance(testLibrary, "net");
//        Node number1 = net.create(numberNode);
//        Node add1 = net.create(addDirectNode);
//        Port v1 = add1.getPort("v1");
//        Port v2 = add1.getPort("v2");
//        // Basic setup: v2 -> v1 -> number1.value
//        // For this to work, v1 needs to update number1 first before v2 gets the data.
//        // If the value is not updated, v2 will get the value from v1, which hasn't updated yet,
//        // and which will thus return 0.
//        number1.setValue("value", 42);
//        v1.setExpression("number1.value");
//        v2.setExpression("v1");
//        add1.update();
//        assertEquals(42 + 42, add1.getOutputValue());
//        // Because we cannot determine the exact order of processing, we need to run this test twice.
//        // So this is the setup in the other direction: v1 -> v2 -> number1.value
//        // This time, v2 needs to update number1 first, then v1.
//        number1.setValue("value", 33);
//        // Setting v1 to the expression v2 would cause a cycle, since v2 is already linked to v1.
//        // Clear v2's expression first.
//        v2.clearExpression();
//        v1.setExpression("v2");
//        v2.setExpression("number1.value");
//        add1.update();
//        assertEquals(33 + 33, add1.getOutputValue());
    }

    public void testNetworkLocal() throws ExpressionError {
//        Node net = testNetworkNode.newInstance(testLibrary, "net");
//        net.addPort("pn", Port.Type.INT, 33);
//        Node number1 = net.create(numberNode);
//        Port pValue1 = number1.getPort("value");
//        pValue1.set(84);
//        assertEquals("number1", number1.getName());
//        //Port p1 = test1.addPort("p1", Port.Type.INT);
//        Node number2 = net.create(numberNode);
//        assertEquals("number2", number2.getName());
//        //Port p2 = number2.addPort("p2", Port.Type.INT);
//        Port pValue2 = number2.getPort("value");
//        pValue2.set(12);
//        // Trying to get the value of number2 by just using the expression "value" is impossible,
//        // since it will retrieve the value port of number1, which will cause a cycle.
//        assertInvalidExpression(pValue1, "value", "refers to itself");
//        // Access p2 through the node name.
//        assertExpressionEquals(12, pValue1, "number2.value");
//        // Access p2 through the network.
//        assertExpressionEquals(12, pValue1, "parent.number2.value");
//        // Access the pn Port on the network.
//        assertExpressionEquals(33, pValue1, "parent.pn");
    }

    public void testDependencies() {
//        Node polynet = Node.ROOT_NODE.newInstance(testLibrary, "polynet");
//        Node rect1 = polynet.create(rectNode);
//        Node translate1 = polynet.create(manager.getMacro("polygraph.translate"));
//        assertEquals("translate1", translate1.getName());
//        rect1.getPort("y").setExpression("x");
//        Set<Port> dependencies = rect1.getPort("y").getDependencies();
//        assertEquals(1, dependencies.size());
//        dependencies.contains(rect1.getPort("x"));
//        rect1.getPort("y").setExpression("translate1.ty + x");
//        dependencies = rect1.getPort("y").getDependencies();
//        assertEquals(2, dependencies.size());
//        assertTrue(dependencies.contains(translate1.getPort("ty")));
//        assertTrue(dependencies.contains(rect1.getPort("x")));
    }

    public void testStamp() {
//        Polygon p;
//
//        Node rect1 = rectNode.newInstance(testLibrary, "rect1");
//        // Sets the width to a stamp expression. If this node gets executed, it retrieves
//        // "mywidth" from the context and uses that. If mywidth could not be found, it uses
//        // the default value of 20 for this port.
//        rect1.getPort("width").setExpression("stamp(\"mywidth\", 20)");
//        // Update the node to see if it works.
//        rect1.update();
//        p = (Polygon) rect1.getOutputValue();
//        assertEquals(new Rectangle(0, 0, 20, 100), p.getBounds());
//
//        // The stamper is a node that relies on copy stamping to replace
//        // one of the parameters of the connected node. The connected node (rect1)
//        // still needs to use the "stamp" expression.
//        Node stamper = translateNode.newInstance(testLibrary, "stamper");
//        // Nodes are automatically evaluated once, even though we do not use the output.
//        // TODO: Set a flag on the node that allows control over cooking.
//        String code = "def cook(self):\n" +
//                "  context.put(self.key, self.value)\n" +
//                "  self.node.stampDirty()\n" +
//                "  self.node.updateDependencies(context)\n" +
//                "  return self.polygon\n";
//        stamper.setValue("_code", new PythonCode(code));
//        stamper.addPort("key", Port.Type.STRING);
//        stamper.addPort("value", Port.Type.FLOAT);
//        stamper.setValue("key", "mywidth");
//        stamper.setValue("value", 50);
//        stamper.getPort("polygon").connect(rect1);
//        stamper.update();
//        p = (Polygon) stamper.getOutputValue();
//        assertEquals(new Rectangle(0, 0, 50, 100), p.getBounds());
    }

    /**
     * Test the equals method on Expression.
     * Two expressions are equal if their expression strings are equal.
     */
    public void testEquals() {
//        Node a = Node.ROOT_NODE.newInstance(testLibrary, "a");
//        Node b = Node.ROOT_NODE.newInstance(testLibrary, "b");
//        Port pA = a.addPort("a", Port.Type.INT);
//        Port pB = b.addPort("b", Port.Type.INT);
//        String expr1 = "random(1, 2, 3)";
//        Expression eA = new Expression(pA, expr1);
//        Expression eB = new Expression(pB, expr1);
//        assertEquals(eA, eB);
    }

    /**
     * The return value of expressions is accepted with a great margin.
     * If values can be converted in one form or another, they will.
     *
     * @throws ExpressionError if tests are correct, never.
     */
    public void testLenientTypes() throws ExpressionError {
//        Node alpha = Node.ROOT_NODE.newInstance(testLibrary, "alpha");
//        // Integer
//        Port pInt = alpha.addPort("int", Port.Type.INT);
//        assertExpressionEquals(5, pInt, "5");
//        // Note that values are not rounded. The floating-point part is just cut off.
//        assertExpressionEquals(12, pInt, "12.9");
//        assertUpdateError(pInt, "\"hello\"", "cannot be converted to int");
//        assertUpdateError(pInt, "color(0.5)", "cannot be converted to int");
//        // Float
//        Port pFloat = alpha.addPort("float", Port.Type.FLOAT);
//        assertExpressionEquals(1.234f, pFloat, "1.234");
//        assertExpressionEquals(100f, pFloat, "100");
//        assertUpdateError(pFloat, "\"hello\"", "cannot be converted to float");
//        assertUpdateError(pFloat, "color(0.1, 0.2, 0.3)", "cannot be converted to float");
//        // String
//        Port pString = alpha.addPort("string", Port.Type.STRING);
//        // Any value is converted to a string.
//        assertExpressionEquals("15", pString, "10 + 5");
//        // Color
//        Port pColor = alpha.addPort("color", Port.Type.COLOR);
//        assertExpressionEquals(new Color(0.1, 0.2, 0.3, 0.4), pColor, "color(0.1, 0.2, 0.3, 0.4)");
//        // Integers are converted to the 0-255 range
//        assertExpressionEquals(new Color(0.5, 0.5, 0.5), pColor, "128");
//        // Floats are converted to the 0-1 range
//        assertExpressionEquals(new Color(0.7, 0.7, 0.7), pColor, "0.7");
    }

    /**
     * Test geometric methods on expression.
     */
    public void testGeometry() {
//        Node alpha = Node.ROOT_NODE.newInstance(testLibrary, "alpha");
//        Port pAlpha = alpha.addPort("v", Port.Type.FLOAT);
    }

    /**
     * Test extended mathematical methods.
     *
     * @throws ExpressionError
     */
    public void testMath() throws ExpressionError {
//        Node alpha = Node.ROOT_NODE.newInstance(testLibrary, "alpha");
//        Port pFloat = alpha.addPort("float", Port.Type.FLOAT);
//        assertExpressionEquals((float) Math.PI, pFloat, "math.PI");
//        assertExpressionEquals((float) Math.sin(12), pFloat, "math.sin(12)");
    }

    /**
     * Test to see if the current frame is accessible from the expression.
     *
     * @throws ExpressionError
     */
    public void testFrame() throws ExpressionError {
//        Node alpha = Node.ROOT_NODE.newInstance(testLibrary, "alpha");
//        Port pInt = alpha.addPort("int", Port.Type.INT);
//        assertExpressionEquals(1, pInt, "FRAME");
    }

    /**
     * A bit silly test to see if the random range is correct.
     */
    public void testRandint() {
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        // Since these values are essentially random, we just have to generate enough
        // to get a good distribution that reaches the edges.
        for (int i = 0; i < 50; i++) {
            int v = ExpressionHelper.randint(i, 20, 30);
            min = v < min ? v : min;
            max = v > max ? v : max;
        }
        assertEquals(20, min);
        assertEquals(30, max);
    }

    public void assertExpressionEquals(Object expected, Port p, String expression) throws ExpressionError {
//        // We don't catch the ExpressionError but let it bubble up.
//        p.setExpression(expression);
//        p.update(new CookContext());
//        assertEquals(expected, p.getValue());
    }

    private void assertInvalidExpression(Port p, String expression, String expectedMessage) throws ExpressionError {
//        p.setExpression(expression);
//        if (!p.hasExpressionError()) {
//            fail("Expression should have failed with \"" + expectedMessage + "\"");
//        } else {
//            Throwable t = p.getExpressionError();
//            assertTrue("Expected message \"" + expectedMessage + "\", got \"" + t.getMessage() + "\"",
//                    t.getMessage().toLowerCase().contains(expectedMessage.toLowerCase()));
//        }
    }

    private void assertUpdateError(Port p, String expression, String expectedMessage) throws ExpressionError {
//        p.setExpression(expression);
//        try {
//            p.update(new CookContext());
//        } catch (IllegalArgumentException e) {
//            assertTrue("Expected message \"" + expectedMessage + "\", got \"" + e.getMessage() + "\"",
//                    e.getMessage().toLowerCase().contains(expectedMessage.toLowerCase()));
//        }
    }


}
