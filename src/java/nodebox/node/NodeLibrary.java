package nodebox.node;

import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import nodebox.function.FunctionLibrary;
import nodebox.function.FunctionRepository;
import nodebox.graphics.Point;
import nodebox.util.FileUtils;
import nodebox.util.LoadException;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static com.google.common.base.Preconditions.*;

public class NodeLibrary {

    public static final Splitter PORT_NAME_SPLITTER = Splitter.on(".");

    public static NodeLibrary create(String libraryName, Node root) {
        return create(libraryName, root, FunctionRepository.of());
    }

    public static NodeLibrary create(String libraryName, Node root, FunctionRepository functionRepository) {
        return new NodeLibrary(libraryName, null, root, functionRepository);
    }

    public static NodeLibrary load(String libraryName, String xml, NodeRepository nodeRepository) throws LoadException {
        checkNotNull(libraryName, "Library name cannot be null.");
        checkNotNull(xml, "XML string cannot be null.");
        try {
            return load(libraryName, null, new StringReader(xml), nodeRepository);
        } catch (XMLStreamException e) {
            throw new LoadException("<none>", "Could not read NDBX string", e);
        }
    }

    public static NodeLibrary load(File f, NodeRepository nodeRepository) throws LoadException {
        checkNotNull(f, "File cannot be null.");
        String libraryName = FileUtils.stripExtension(f);
        try {
            return load(libraryName, f, new FileReader(f), nodeRepository);
        } catch (FileNotFoundException e) {
            throw new LoadException(f.getAbsolutePath(), "File not found.");
        } catch (XMLStreamException e) {
            throw new LoadException(f.getAbsolutePath(), "Could not read NDBX file", e);
        }
    }

    private final String name;
    private final File file;
    private final Node root;
    private final FunctionRepository functionRepository;

    private NodeLibrary(String name, File file, Node root, FunctionRepository functionRepository) {
        checkNotNull(name, "Name cannot be null.");
        checkNotNull(root, "Root node cannot be null.");
        checkNotNull(functionRepository, "Function repository cannot be null.");
        this.name = name;
        this.root = root;
        this.functionRepository = functionRepository;
        this.file = file;
    }

    public String getName() {
        return name;
    }

    public File getFile() {
        return file;
    }

    public Node getRoot() {
        return root;
    }

    public Node getNodeForPath(String path) {
        checkArgument(path.startsWith("/"), "Only absolute paths are supported.");
        if (path.length() == 1) return root;

        Node node = root;
        path = path.substring(1);
        for (String name : Splitter.on("/").split(path)) {
            node = node.getChild(name);
            if (node == null) return null;
        }
        return node;
    }

    public FunctionRepository getFunctionRepository() {
        return functionRepository;
    }

    //// Loading ////

    private static NodeLibrary load(String libraryName, File file, Reader r, NodeRepository nodeRepository) throws XMLStreamException {
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        XMLStreamReader reader = xmlInputFactory.createXMLStreamReader(r);
        NodeLibrary nodeLibrary = null;
        while (reader.hasNext()) {
            int eventType = reader.next();
            if (eventType == XMLStreamConstants.START_ELEMENT) {
                String tagName = reader.getLocalName();
                if (tagName.equals("ndbx")) {
                    nodeLibrary = parseNDBX(libraryName, file, reader, nodeRepository);
                } else {
                    throw new XMLStreamException("Only tag ndbx allowed, not " + tagName, reader.getLocation());
                }
            }
        }
        return nodeLibrary;
    }

    private static NodeLibrary parseNDBX(String libraryName, File file, XMLStreamReader reader, NodeRepository nodeRepository) throws XMLStreamException {
        List<FunctionLibrary> functionLibraries = new LinkedList<FunctionLibrary>();
        Node rootNode = Node.ROOT;

        while (true) {
            int eventType = reader.next();
            if (eventType == XMLStreamConstants.START_ELEMENT) {
                String tagName = reader.getLocalName();
                if (tagName.equals("link")) {
                    FunctionLibrary functionLibrary = parseLink(reader);
                    functionLibraries.add(functionLibrary);
                } else if (tagName.equals("node")) {
                    rootNode = parseNode(reader, rootNode, nodeRepository);
                } else {
                    throw new XMLStreamException("Unknown tag " + tagName, reader.getLocation());
                }
            } else if (eventType == XMLStreamConstants.END_ELEMENT) {
                String tagName = reader.getLocalName();
                if (tagName.equals("ndbx"))
                    break;
            }
        }
        FunctionLibrary[] fl = functionLibraries.toArray(new FunctionLibrary[functionLibraries.size()]);
        return new NodeLibrary(libraryName,file, rootNode, FunctionRepository.of(fl));
    }

    private static FunctionLibrary parseLink(XMLStreamReader reader) throws XMLStreamException {
        String linkRelation = reader.getAttributeValue(null, "rel");
        checkState(linkRelation.equals("functions"));
        String ref = reader.getAttributeValue(null, "href");
        // loading should happen lazily?
        return FunctionLibrary.load(ref);
    }

    /**
     * Parse the <node> tag.
     *
     * @param reader         The XML stream.
     * @param parent         The parent node.
     * @param nodeRepository The node library dependencies.
     * @return The new node.
     * @throws XMLStreamException if a parse error occurs.
     */
    private static Node parseNode(XMLStreamReader reader, Node parent, NodeRepository nodeRepository) throws XMLStreamException {
        String prototypeId = reader.getAttributeValue(null, "prototype");
        String name = reader.getAttributeValue(null, "name");
        String description = reader.getAttributeValue(null, "description");
        String image = reader.getAttributeValue(null, "image");
        String function = reader.getAttributeValue(null, "function");
        String listStrategy = reader.getAttributeValue(null, "strategy");
        String position = reader.getAttributeValue(null, "position");
        String renderedChildName = reader.getAttributeValue(null, "renderedChild");
        Node prototype = prototypeId == null ? Node.ROOT : lookupNode(prototypeId, parent, nodeRepository);
        if (prototype == null) {
            throw new XMLStreamException("Prototype " + prototypeId + " could not be found.", reader.getLocation());
        }
        Node node = prototype.extend();

        if (name != null)
            node = node.withName(name);
        if (description != null)
            node = node.withDescription(description);
        if (image != null)
            node = node.withImage(image);
        if (function != null)
            node = node.withFunction(function);
        if (listStrategy != null)
            node = node.withListStrategy(listStrategy);
        if (position != null)
            node = node.withPosition(Point.valueOf(position));

        while (true) {
            int eventType = reader.next();
            if (eventType == XMLStreamConstants.START_ELEMENT) {
                String tagName = reader.getLocalName();
                if (tagName.equals("port")) {
                    String portName = reader.getAttributeValue(null, "name");
                    // Remove the port if it is already on the prototype.
                    if (node.hasInput(portName)) {
                        node = node.withInputChanged(portName, parsePort(reader));
                    } else {
                        node = node.withInputAdded(parsePort(reader));
                    }
                } else if (tagName.equals("node")) {
                    node = node.withChildAdded(parseNode(reader, node, nodeRepository));
                } else if (tagName.equals("conn")) {
                    node = node.withConnectionAdded(parseConnection(reader));
                } else {
                    throw new XMLStreamException("Unknown tag " + tagName, reader.getLocation());
                }
            } else if (eventType == XMLStreamConstants.END_ELEMENT) {
                String tagName = reader.getLocalName();
                if (tagName.equals("node"))
                    break;
            }
        }

        // This has to come at the end, since the child first needs to exist.
        if (renderedChildName != null)
            node = node.withRenderedChildName(renderedChildName);

        return node;
    }

    /**
     * Lookup the node in the node repository.
     * <p/>
     * If the node id consists of just a node name, without spaces, it is looked up in the parent node.
     *
     * @param nodeId         The node id.
     * @param parent         The parent node.
     * @param nodeRepository The node repository.
     * @return The existing node.
     */
    private static Node lookupNode(String nodeId, Node parent, NodeRepository nodeRepository) {
        if (nodeId.contains(".")) {
            return nodeRepository.getNode(nodeId);
        } else {
            return parent.getChild(nodeId);
        }
    }

    private static Port parsePort(XMLStreamReader reader) throws XMLStreamException {
        String name = reader.getAttributeValue(null, "name");
        String type = reader.getAttributeValue(null, "type");
        String value = reader.getAttributeValue(null, "value");
        String min = reader.getAttributeValue(null, "min");
        String max = reader.getAttributeValue(null, "max");
        return Port.parsedPort(name, type, value, min, max);
    }

    private static Connection parseConnection(XMLStreamReader reader) throws XMLStreamException {
        String outputNode = reader.getAttributeValue(null, "output");
        String input = reader.getAttributeValue(null, "input");
        Iterator<String> inputIterator = PORT_NAME_SPLITTER.split(input).iterator();
        String inputNode = inputIterator.next();
        String inputPort = inputIterator.next();
        return new Connection(outputNode, inputNode, inputPort);
    }

    //// Saving ////

    public String toXml() {
        return NDBXWriter.asString(this);
    }

    /**
     * Write the NodeLibrary to a file.
     *
     * @param file The file to save.
     * @throws java.io.IOException When file saving fails.
     */
    public void store(File file) throws IOException {
        NDBXWriter.write(this, file);
    }

    //// Object overrides ////

    @Override
    public int hashCode() {
        return Objects.hashCode(name, root, functionRepository);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof NodeLibrary)) return false;
        final NodeLibrary other = (NodeLibrary) o;
        return Objects.equal(name, other.name)
                && Objects.equal(root, other.root)
                && Objects.equal(functionRepository, other.functionRepository);
    }

    @Override
    public String toString() {
        return String.format("<NodeLibrary %s>", name);
    }

}
