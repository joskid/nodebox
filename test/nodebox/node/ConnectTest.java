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

import nodebox.node.event.NodeEvent;
import nodebox.node.event.ConnectionAddedEvent;
import nodebox.node.event.ConnectionRemovedEvent;
import nodebox.node.event.NodeEventListener;

public class ConnectTest extends NodeTestCase {

    private class ConnectListener implements NodeEventListener {

        public int connectCounter = 0;
        public int disconnectCounter = 0;

        public void reset() {
            connectCounter = 0;
            disconnectCounter = 0;
        }

        public void receive(NodeEvent event) {
            if (event instanceof ConnectionAddedEvent) {
                connectCounter++;
            } else if (event instanceof ConnectionRemovedEvent) {
                disconnectCounter++;
            }
        }

    }

    /**
     * Test basic connections.
     */
    public void testConnect() {
        Node number1 = rootMacro.createChild(numberNode, "number1");
        Node multiply1 = rootMacro.createChild(multiplyNode, "multiply1");
        Node upper1 = rootMacro.createChild(convertToUppercaseNode, "upper1");
        Port number1Result = number1.getPort("result");
        Port multiply1v1 = multiply1.getPort("v1");
        Port multiply1v2 = multiply1.getPort("v2");

        assertFalse(rootMacro.isConnected(multiply1.getPort("v1")));

        assertTrue(multiply1v1.canConnectTo(number1Result));
        assertTrue(multiply1v2.canConnectTo(number1Result));
        assertFalse(upper1.getPort("value").canConnectTo(number1Result));

        Connection conn = rootMacro.connect(multiply1v1, number1Result);
        assertTrue(rootMacro.isConnected(multiply1v1));
        assertTrue(rootMacro.isConnectedTo(multiply1v1, number1Result));
        assertTrue(rootMacro.isConnected(multiply1));
        assertTrue(rootMacro.isConnected(number1));
        assertEquals(multiply1v1, conn.getInput());
        assertEquals(number1Result, conn.getOutput());
        assertEquals(multiply1, conn.getInputNode());
        assertEquals(number1, conn.getOutputNode());

        try {
            rootMacro.connect(upper1.getPort("value"), number1.getPort("result"));
            fail("Value is of the wrong type and should not be connectable to number1's output.");
        } catch (Exception e) {
            assertErrorMessage(e, "data types are incompatible");
        }
    }

    /**
     * Test if values from connected ports are propagated.
     */
    public void testValuePropagation() {
        // We switch number1 and number2 to producer so they're not automatically updated when cooking the macro.
        Node number1 = rootMacro.createChild(numberNode, "number1");
        number1.setMode(Node.Mode.PRODUCER);
        number1.setValue("value", 15);
        Node number2 = rootMacro.createChild(numberNode, "number2");
        number2.setMode(Node.Mode.PRODUCER);
        number2.setValue("value", 2);
        Node multiply = rootMacro.createChild(multiplyNode, "multiply1");
        rootMacro.connect(multiply.getPort("v1"), number1.getPort("result"));
        rootMacro.connect(multiply.getPort("v2"), number2.getPort("result"));
        assertNotNull(multiply.getPort("result"));
        assertEquals(0, multiply.getValue("result"));
        // Updating the number has no effect on the multiplier.
        number1.execute(new CookContext());
        assertEquals(0, multiply.getValue("result"));
        // Updating the multiplier does not automatically update the dependencies.
        multiply.execute(new CookContext());
        assertEquals(0, multiply.getValue("result"));
        // You need to update the macro to update the dependencies.
        rootMacro.execute(new CookContext());
        assertEquals(30, multiply.getValue("result"));
        // Disconnect the multiply node to stop value propagation.
        rootMacro.disconnect(multiply);
        number1.setValue("value", 3);
        number2.setValue("value", 4);
        rootMacro.execute(new CookContext());
        // Multiply still has the old value because it is no longer connected.
        assertEquals(30, multiply.getValue("result"));
    }

    /**
     * Test disconnecting a port.
     */
    public void testDisconnect() {
        Node number1 = rootMacro.createChild(numberNode);
        Node number2 = rootMacro.createChild(numberNode);
        Node multiply = rootMacro.createChild(multiplyNode);
        number1.setValue("value", 5);
        number2.setValue("value", 2);
        assertEquals(0, number1.getValue("result"));
        assertEquals(0, number2.getValue("result"));
        rootMacro.connect(multiply.getPort("v1"), number1.getPort("result"));
        rootMacro.connect(multiply.getPort("v2"), number2.getPort("result"));
        assertTrue(rootMacro.isConnected(multiply.getPort("v1")));
        assertTrue(rootMacro.isConnected(number1.getPort("result")));
        rootMacro.execute(new CookContext());
        assertEquals(5, number1.getValue("result"));
        assertEquals(2, number2.getValue("result"));
        assertEquals(5, multiply.getValue("v1"));
        assertEquals(2, multiply.getValue("v2"));
        assertEquals(10, multiply.getValue("result"));
        assertEquals(2, rootMacro.getConnections().size());

        rootMacro.disconnect(multiply.getPort("v1"));
        assertFalse(rootMacro.isConnected(multiply.getPort("v1")));
        assertFalse(rootMacro.isConnected(number1.getPort("result")));
        assertEquals(1, rootMacro.getConnections().size());
        // The value of the input ports are set to the default value after disconnection.
        assertEquals(0, multiply.getValue("v1"));
        assertEquals(2, multiply.getValue("v2"));
    }

    /**
     * Test all flags related to connections.
     */
    public void testConnectionFlags() {
        // Setup a simple network where number1 <- add1.
        Node number1 = rootMacro.createChild(numberNode);
        Node add1 = rootMacro.createChild(addNode);
        rootMacro.connect(add1.getPort("v1"), number1.getPort("result"));
        // Remove the specific connection and check if everything was removed.
        rootMacro.disconnect(add1.getPort("v1"));
        assertFalse(rootMacro.isConnected(number1));
        assertFalse(rootMacro.isConnected(add1));
        assertFalse(rootMacro.isConnected(number1.getPort("result")));
        assertFalse(rootMacro.isConnected(add1.getPort("v1")));
        assertEquals(0, rootMacro.getConnections().size());
    }

    /**
     * Check if all connections are destroyed when removing a node.
     */
    public void testRemoveNode() {
        // Setup a simple network where number1 <- negate1 <- add1.
        Node number1 = rootMacro.createChild(numberNode);
        Node negate1 = rootMacro.createChild(negateNode);
        Node add1 = rootMacro.createChild(addNode);
        rootMacro.connect(negate1.getPort("value"), number1.getPort("result"));
        rootMacro.connect(add1.getPort("v1"), negate1.getPort("result"));

        // Remove the node.  This should also remove all connections.
        rootMacro.removeChild(negate1);
        assertFalse(rootMacro.isConnected(number1));
        assertFalse(rootMacro.isConnected(add1));
        assertEquals(0, rootMacro.getConnections().size());
    }

    /**
     * Test if a new connection automatically replaces an existing one.
     */
    public void testOnlyOneConnect() {
        Node number1 = rootMacro.createChild(numberNode);
        Node number2 = rootMacro.createChild(numberNode);
        Node constant = rootMacro.createChild(numberNode);
        Node add1 = rootMacro.createChild(addNode);
        rootMacro.connect(add1.getPort("v1"), number1.getPort("result"));
        rootMacro.connect(add1.getPort("v2"), constant.getPort("result"));
        assertTrue(rootMacro.isConnected(number1));
        assertFalse(rootMacro.isConnected(number2));
        assertTrue(rootMacro.isConnected(add1));
        assertTrue(rootMacro.isConnected(constant));
        assertEquals(2, rootMacro.getConnections().size());

        // Now create a new connection number2 <- add1
        rootMacro.connect(add1.getPort("v1"), number2.getPort("result"));
        assertFalse(rootMacro.isConnected(number1));
        assertTrue(rootMacro.isConnected(number2));
        assertTrue(rootMacro.isConnected(add1));
        assertTrue(rootMacro.isConnected(constant));
        assertEquals(2, rootMacro.getConnections().size());
    }

    /**
     * Test the events generated when connecting/disconnecting.
     */
    public void testConnectionEvents() {
        ConnectListener l = new ConnectListener();
        // Setup a basic network with number1 <- add1
        testLibrary.addListener(l);
        Node number1 = rootMacro.createChild(numberNode);
        Node add1 = rootMacro.createChild(addNode);
        // No connect/disconnect events have been fired.
        assertEquals(0, l.connectCounter);
        assertEquals(0, l.disconnectCounter);
        // Creating a connection fires the event.
        rootMacro.connect(add1.getPort("v1"), number1.getPort("result"));
        assertEquals(1, l.connectCounter);
        assertEquals(0, l.disconnectCounter);
        l.reset();
        // Create a second number and connect it to the add constant.
        // This should fire a disconnect event from number1, and a connect
        // event to number2.
        Node number2 = rootMacro.createChild(numberNode);
        rootMacro.connect(add1.getPort("v1"), number2.getPort("result"));
        assertEquals(1, l.connectCounter);
        assertEquals(1, l.disconnectCounter);
        l.reset();
        // Disconnect the constant node. This should remove all (1) connections,
        // and cause one disconnect event.
        rootMacro.disconnect(add1);
        assertEquals(0, l.connectCounter);
        assertEquals(1, l.disconnectCounter);
        l.reset();
        // Connect number1 and number2 to v1/v2 on the add node.
        rootMacro.connect(add1.getPort("v1"), number1.getPort("result"));
        rootMacro.connect(add1.getPort("v2"), number2.getPort("result"));
        assertEquals(2, l.connectCounter);
        assertEquals(0, l.disconnectCounter);
        l.reset();
        // Disconnect the add node. This should trigger two disconnection events.
        rootMacro.disconnect(add1);
        assertEquals(0, l.connectCounter);
        assertEquals(2, l.disconnectCounter);
        l.reset();
        testLibrary.removeListener(l);
    }

}
