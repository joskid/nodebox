package nodebox.node;

import com.google.common.base.Splitter;
import nodebox.function.FunctionRepository;

/**
 * This class provides mutable access to the immutable NodeLibrary.
 *
 * This class is single-threaded. You can only access it from one concurrent thread.
 * However, the internal nodeLibrary is immutable, so you can keep looking at a NodeLibrary while the controller
 * generates a new one.
 */
public class NodeLibraryController {

    private NodeLibrary nodeLibrary;

    public NodeLibrary getNodeLibrary() {
        return nodeLibrary;
    }

    public static NodeLibraryController create(String libraryName, FunctionRepository functionRepository) {
        return new NodeLibraryController(NodeLibrary.create(libraryName, Node.ROOT, functionRepository));
    }

    public NodeLibraryController(NodeLibrary nodeLibrary) {
        this.nodeLibrary = nodeLibrary;
    }

    public void createNode(String parentPath, Node prototype) {

    }


    public void addNode(String parentPath, Node node) {

    }

    public void undo() {
    }

    public void redo() {

    }


    public void connect(Node outputNode, Node inputNode, Port inputPort) {
        new Connection(outputNode, inputNode, inputPort);
    }

    // TODO Write a algorithm that recursively replaces nodes at higher levels.

    //public void replaceNodeInPath()



}
