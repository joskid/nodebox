package nodebox.node;

public class ChildNotFoundException extends RuntimeException {

    private final Macro parent;
    private final String name;

    public ChildNotFoundException(Macro parent, String name) {
        this.parent = parent;
        this.name = name;
    }

    public Macro getParent() {
        return parent;
    }

    public String getName() {
        return name;
    }

}
