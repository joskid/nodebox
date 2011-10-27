package nodebox.node;

public class DependencyError extends RuntimeException {

    private Port dependency;
    private Port dependent;

    public DependencyError(Port dependency, Port dependent, String message) {
        super(message);
        this.dependency = dependency;
        this.dependent = dependent;
    }

    public Port getDependency() {
        return dependency;
    }

    public Port getDependent() {
        return dependent;
    }
}
