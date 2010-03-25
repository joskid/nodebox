package nodebox.node;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The processing context contains metadata about the processing operation.
 * <p/>
 * Note: the context is currently empty. Later on, we will add the frame number etc.
 */
public class ProcessingContext {

    private long frame;
    private Set<Node> executedNodes = new HashSet<Node>();
    private HashMap<Node, HashMap<String, Object>> nodeValues = new HashMap<Node, HashMap<String, Object>>();
    private ByteArrayOutputStream outputBytes;
    private ByteArrayOutputStream errorBytes;
    private PrintStream outputStream;
    private PrintStream errorStream;

    public ProcessingContext() {
        frame = 1;
        outputBytes = new ByteArrayOutputStream();
        outputStream = new PrintStream(outputBytes);
        errorBytes = new ByteArrayOutputStream();
        errorStream = new PrintStream(errorBytes);
    }

    public ProcessingContext(ProcessingContext parentContext) {
        nodeValues = new HashMap<Node, HashMap<String, Object>>(parentContext.nodeValues);
    }

    //// Frame ////

    public long getFrame() {
        return frame;
    }

    //// Node values ////

    public Map<String, Object> valuesForNode(Node node) {
        return nodeValues.get(node);
    }

    public Object getValueForNodeKey(Node node, String key) {
        Map<String, Object> values = nodeValues.get(node);
        if (values == null) return null;
        return values.get(key);
    }

    public Set<String> keysForNodes(Node node) {
        Map<String, Object> values = nodeValues.get(node);
        if (values == null) return new HashSet<String>(0);
        return values.keySet();
    }

    public void setValueForNodeKey(Node node, String key, Object value) {
        HashMap<String, Object> values = nodeValues.get(node);
        if (values == null) {
            values = new HashMap<String, Object>();
            nodeValues.put(node, values);
        }
        values.put(key, value);
    }

    //// Executed ////

    public boolean hasExecuted(Node node) {
        return executedNodes.contains(node);
    }

    public void addToExecutedNodes(Node node) {
        executedNodes.add(node);
    }

    //// Output/error streams  ////

    public PrintStream getOutputStream() {
        return outputStream;
    }

    public PrintStream getErrorStream() {
        return errorStream;
    }

    public String getOutput() {
        return outputBytes.toString();
    }

    public String getError() {
        return errorBytes.toString();
    }

}
