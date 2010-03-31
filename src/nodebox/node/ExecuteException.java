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

/**
 * Thrown when an error happened during the execution of a node.
 */
public class ExecuteException extends RuntimeException {

    private final Node node;

    public ExecuteException(Node node, Throwable cause) {
        super(cause);
        checkNotNull(node);
        this.node = node;
    }

    public ExecuteException(Node node, String message, Throwable cause) {
        super(message, cause);
        checkNotNull(node);
        this.node = node;
    }

    public Node getNode() {
        return node;
    }

}
