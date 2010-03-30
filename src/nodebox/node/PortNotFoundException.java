package nodebox.node;

public class PortNotFoundException extends RuntimeException {

    private final Node node;
    private final String name;

    public PortNotFoundException(Node node, String name) {
        this.node = node;
        this.name = name;
    }

    public Node getNode() {
        return node;
    }

    public String getName() {
        return name;
    }

}
