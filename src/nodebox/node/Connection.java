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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Represents a connection between two nodes.
 * <p/>
 * Connections are made between ports on the nodes.
 * <p/>
 * The connection runs from one of the the output ports of the output node to an input port on the input node.
 * <p/>
 * This class can only store the connection between one output and one input.
 */
public class Connection {

    private final Port input;
    private final Port output;

    /**
     * Creates a connection between the input (downstream) node and the output (upstream) node.
     *
     * @param input  the input (downstream) port
     * @param output the output (upstream) port
     */
    public Connection(Port input, Port output) {
        checkNotNull(input);
        checkNotNull(output);
        checkState(input != output, "Input and output ports cannot be the same.");
        checkState(input.getNode() != output.getNode(), "Input and output nodes cannot be the same.");
        checkState(input.canConnectTo(output), "Input cannot connect to the output.");
        this.output = output;
        this.input = input;
    }

    /**
     * Get the input (downstream) port.
     *
     * @return the input port.
     */
    public Port getInput() {
        return input;
    }

    /**
     * Get the node for to the input port.
     *
     * @return the node for the input port
     */
    public Node getInputNode() {
        return input.getNode();
    }

    /**
     * Get the output (upstream) port.
     *
     * @return the output port.
     */
    public Port getOutput() {
        return output;
    }

    /**
     * Get the node for to the output port.
     *
     * @return the node for the output port
     */
    public Node getOutputNode() {
        return output.getNode();
    }

    @Override
    public String toString() {
        return String.format("[%s <= %s]", output, input);
    }

}
