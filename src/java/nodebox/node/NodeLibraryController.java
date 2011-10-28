package nodebox.node;

import com.google.common.base.Splitter;
import nodebox.function.FunctionRepository;
import nodebox.graphics.Point;

/**
 * This class provides mutable access to the immutable NodeLibrary.
 *
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


    public Node createNode(String parentPath, Node prototype) {
        return null;
    }


    public void setNodePosition(String nodePath, Point point) {
    }

    public void addNode(String parentPath, Node node) {

    }

    public void removeNode(String parentPath, String nodeName) {

    }

    public void removePort(String nodePath, String portName) {

    }

    public void renameNode(String nodePath, String newName) {

    }

    public void setPortValue(String nodePath, Port port, Object value) {

    }




    public void connect(Node outputNode, Node inputNode, Port inputPort) {
        new Connection(outputNode, inputNode, inputPort);
    }

    public void disconnect(String parentPath, Connection connection) {


    }

    public void undo() {
    }

    public void redo() {

    }



    // TODO Write a algorithm that recursively replaces nodes at higher levels.

    //public void replaceNodeInPath()


    public void setRenderedChild(String parentPath, String nodeName) {

    }


}
