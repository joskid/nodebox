package nodebox.node;

import nodebox.function.FunctionRepository;
import nodebox.graphics.Point;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * This class provides mutable access to the immutable NodeLibrary.
 * <p/>
 * This class is single-threaded. You can only access it from one concurrent thread.
 * However, the internal nodeLibrary is immutable, so you can keep looking at a NodeLibrary while the controller
 * generates a new one.
 */
public class NodeLibraryController {

    private NodeLibrary nodeLibrary;

    public static NodeLibraryController create() {
        return new NodeLibraryController(NodeLibrary.create("untitled", Node.ROOT, FunctionRepository.of()));
    }

    public static NodeLibraryController create(String libraryName, FunctionRepository functionRepository) {
        return new NodeLibraryController(NodeLibrary.create(libraryName, Node.ROOT, functionRepository));
    }

    public static NodeLibraryController withLibrary(NodeLibrary nodeLibrary) {
        return new NodeLibraryController(nodeLibrary);
    }

    public NodeLibraryController(NodeLibrary nodeLibrary) {
        this.nodeLibrary = nodeLibrary;
    }

    public NodeLibrary getNodeLibrary() {
        return nodeLibrary;
    }

    public void setNodeLibrary(NodeLibrary nodeLibrary) {
        this.nodeLibrary = nodeLibrary;
    }

    public Node getNode(String nodePath) {
        return nodeLibrary.getNodeForPath(nodePath);
    }

    public Node createNode(String parentPath, Node prototype) {
        Node newNode = prototype.extend();
        addNode(parentPath, newNode);
        return newNode;
    }

    public void setNodePosition(String nodePath, Point point) {
        Node newNode = getNode(nodePath).withPosition(point);
        replaceNodeInPath(nodePath, newNode);
    }

    public void setRenderedChild(String parentPath, String nodeName) {
        Node newParent = getNode(parentPath).withRenderedChildName(nodeName);
        replaceNodeInPath(parentPath, newParent);
    }

    public void addNode(String parentPath, Node node) {
        Node newParent = getNode(parentPath).withChildAdded(node);
        replaceNodeInPath(parentPath, newParent);
    }

    public void removeNode(String parentPath, String nodeName) {
        Node newParent = getNode(parentPath).withChildRemoved(nodeName);
        replaceNodeInPath(parentPath, newParent);
    }

    public void removePort(String nodePath, String portName) {
        Node newNode = getNode(nodePath).withInputRemoved(portName);
        replaceNodeInPath(nodePath, newNode);
    }

    public void renameNode(String nodePath, String newName) {
        throw new UnsupportedOperationException();
    }

    public void setPortValue(String nodePath, String portName, Object value) {
        Node newNode = getNode(nodePath).withInputValue(portName, value);
        replaceNodeInPath(nodePath, newNode);
    }

    public void connect(String parentPath, Node outputNode, Node inputNode, Port inputPort) {
        Node newParent = getNode(parentPath).connect(outputNode.getName(), inputNode.getName(), inputPort.getName());
        replaceNodeInPath(parentPath, newParent);
    }

    public void disconnect(String parentPath, Connection connection) {

    }

    /**
     * Replace the node at the given path with the new node.
     * Afterwards, the nodeLibrary field is set to the new NodeLibrary.
     *
     * @param nodePath The node path. This path needs to exist.
     * @param node     The new node to put in place of the old node.
     */
    public void replaceNodeInPath(String nodePath, Node node) {
        checkArgument(nodePath.startsWith("/"), "Node path needs to be an absolute path, starting with '/'.");
        nodePath = nodePath.substring(1);
        Node newRoot;
        if (nodePath.isEmpty()) {
            newRoot = node;
        } else {
            // TODO Recursively replace nodes at higher levels.
            checkArgument(!nodePath.contains("/"), "Subpaths are not supported yet.");
            newRoot = nodeLibrary.getRoot().withChildReplaced(nodePath, node);
        }
        nodeLibrary = NodeLibrary.create(nodeLibrary.getName(), newRoot, nodeLibrary.getFunctionRepository());
    }

}
