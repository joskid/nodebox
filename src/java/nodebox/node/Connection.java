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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Represents a connection between two ports.
 * <p/>
 * Connections are made between ports on the nodes. The connection goes from the output port of the output node
 * (there is only one output port) to an input port on the input node.
 * <p/>
 * This class can only store the connection between one output and one input. Some nodes, such as the merge node,
 * have multiple outputs that connect to the same input. These are connected using multiple connection objects.
 */
public class Connection {

    private final Node outputNode;
    private final Node inputNode;
    private final Port inputPort;

    /**
     * Creates a connection between the output (upstream) node and input (downstream) node.
     *
     * @param outputNode The output (upstream) Node.
     * @param inputNode  The input (downstream) Node.
     * @param inputPort  The input (downstream) Port.
     */
    public Connection(Node outputNode, Node inputNode, Port inputPort) {
        checkNotNull(outputNode);
        checkNotNull(inputNode);
        checkNotNull(inputPort);
        checkArgument(inputNode.hasPort(inputPort.getName()), "Input port %s does not exist on port %s.", inputPort, inputNode);
        this.outputNode = outputNode;
        this.inputNode = inputNode;
        this.inputPort = inputPort;
    }

    public Node getOutputNode() {
        return outputNode;
    }

    public Node getInputNode() {
        return inputNode;
    }

    /**
     * Gets the input (downstream) port.
     *
     * @return the input port.
     */
    public Port getInputPort() {
        return inputPort;
    }

    @Override
    public String toString() {
        return String.format("%s <= %s.%s", getOutputNode(), getInputNode(), getInputPort());
    }

}
