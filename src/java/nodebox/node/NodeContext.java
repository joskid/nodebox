package nodebox.node;

import nodebox.function.Function;
import nodebox.function.FunctionRepository;

import java.util.ArrayList;

import static com.google.common.base.Preconditions.checkNotNull;

public class NodeContext {

    private final double frame;

    public NodeContext(double frame) {
        this.frame = frame;
    }

    /**
     * Render the network by rendering its rendered child.
     *
     * @param repository The function repository.
     * @param network    The network to render.
     * @return The output value.
     * @throws NodeRenderException If processing fails.
     */
    public Object renderNetwork(FunctionRepository repository, Node network) throws NodeRenderException {
        checkNotNull(repository);
        checkNotNull(network);
        return renderChildNode(repository, network.getRenderedChild());
    }

    /**
     * Render the child node.
     * This doesn't calculate child dependencies.
     * On the network, renderNetwork the renderedChild.
     * Note that we pass in the network, not the node to renderNetwork!
     * This is because we can't go up from the node to the network to retrieve the connections.
     *
     * @param repository The function repository.
     * @param node       The node to render.
     * @return The output value.
     * @throws NodeRenderException If processing fails.
     */
    public Object renderChildNode(FunctionRepository repository, Node node) throws NodeRenderException {
        checkNotNull(repository);
        checkNotNull(node);

        // Get the function.
        String functionName = node.getFunction();
        Function function = repository.getFunction(functionName);

        // Set arguments on the function.
        ArrayList<Object> arguments = new ArrayList<Object>();
        for (Port p : node.getInputs()) {
            arguments.add(p.getValue());
        }

        // Invoke the function.
        try {
            return function.invoke(arguments.toArray());
        } catch (Exception e) {
            throw new NodeRenderException(node, e);
        }
    }

    public double getFrame() {
        return frame;
    }

}
