package nodebox.node;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Manages a set of node libraries.
 */
public class NodeRepository {

    public static NodeRepository of() {
        return new NodeRepository(ImmutableMap.<String,NodeLibrary>of());
    }

    public static NodeRepository of(NodeLibrary... libraries) {
        ImmutableMap.Builder<String, NodeLibrary> builder = ImmutableMap.builder();
        for (NodeLibrary library : libraries) {
            builder.put(library.getName(), library);
        }
        // TODO  The core library is always included.
        return new NodeRepository(builder.build());
    }

    private final ImmutableMap<String, NodeLibrary> libraryMap;

    private NodeRepository(ImmutableMap<String, NodeLibrary> nodeLibraries) {
        libraryMap = nodeLibraries;
    }

    /**
     * Get a node based on an identifier.
     * <p/>
     * The node identifier is in the form libraryname.nodename. The libraryname can have multiple dots, e.g.
     * "colors.tints.blue". This signifies the "blue" node in the "colors.tints" libraryname.
     *
     * @param identifier a node identifier
     * @return a Node or null if a node could not be found.
     */
    public Node getNode(String identifier) {
        checkNotNull(identifier);
        if (identifier.equals("_root")) return Node.ROOT;
        String[] names = identifier.split(".");
        checkArgument(names.length == 2);
        String libraryName = names[0];
        NodeLibrary library = libraryMap.get(libraryName);
        checkNotNull(library, "Library %s not found.", libraryName);
        String nodeName = names[1];
        Node node = library.getRoot().getChild(nodeName);
        checkNotNull(node, "Node %s not found.", identifier);
        return node;
    }

    public List<Node> getNodes() {
        ImmutableList.Builder<Node> builder = ImmutableList.builder();
        for (NodeLibrary library : libraryMap.values()) {
            builder.addAll(library.getRoot().getChildren());
        }
        return builder.build();
    }

}
