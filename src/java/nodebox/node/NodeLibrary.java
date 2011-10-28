package nodebox.node;

import com.google.common.base.Splitter;
import nodebox.function.FunctionLibrary;
import nodebox.function.FunctionRepository;
import nodebox.util.FileUtils;
import nodebox.util.LoadException;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.util.LinkedList;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public class NodeLibrary {

    public static NodeLibrary create(String libraryName, Node root, FunctionRepository functionRepository) {
        return new NodeLibrary(libraryName, root, functionRepository);
    }

    public static NodeLibrary load(String libraryName, String xml, NodeRepository nodeRepository) throws LoadException {
        checkNotNull(libraryName, "Library name cannot be null.");
        checkNotNull(xml, "XML string cannot be null.");
        try {
            return load(libraryName, new StringReader(xml), nodeRepository);
        } catch (XMLStreamException e) {
            throw new LoadException("<none>", "Could not read NDBX string", e);
        }
    }

    public static NodeLibrary load(File f, NodeRepository nodeRepository) throws LoadException {
        checkNotNull(f, "File cannot be null.");
        String libraryName = FileUtils.stripExtension(f);
        try {
            return load(libraryName, new FileReader(f), nodeRepository);
        } catch (FileNotFoundException e) {
            throw new LoadException(f.getAbsolutePath(), "File not found.");
        } catch (XMLStreamException e) {
            throw new LoadException(f.getAbsolutePath(), "Could not read NDBX file", e);
        }
    }

    private final String name;
    private final Node root;
    private final FunctionRepository functionRepository;

    private NodeLibrary(String name, Node root, FunctionRepository functionRepository) {
        checkNotNull(name, "Name cannot be null.");
        checkNotNull(root, "Root node cannot be null.");
        checkNotNull(functionRepository, "Function repository cannot be null.");
        this.name = name;
        this.root = root;
        this.functionRepository = functionRepository;
    }

    public String getName() {
        return name;
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

    private static NodeLibrary load(String libraryName, Reader r, NodeRepository nodeRepository) throws XMLStreamException {
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        XMLStreamReader reader = xmlInputFactory.createXMLStreamReader(r);
        NodeLibrary nodeLibrary = null;
        while (reader.hasNext()) {
            int eventType = reader.next();
            if (eventType == XMLStreamConstants.START_ELEMENT) {
                String tagName = reader.getLocalName();
                if (tagName.equals("ndbx")) {
                    nodeLibrary = parseNDBX(libraryName, reader, nodeRepository);
                } else {
                    throw new XMLStreamException("Only tag ndbx allowed, not " + tagName, reader.getLocation());
                }
            }
        }
        return nodeLibrary;
    }

    private static NodeLibrary parseNDBX(String libraryName, XMLStreamReader reader, NodeRepository nodeRepository) throws XMLStreamException {
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
                } else if (tagName.equals("conn")) {

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
        return new NodeLibrary(libraryName, rootNode, FunctionRepository.of(fl));
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
     * @param parent         The parent node to which to attach this node.
     * @param nodeRepository The node library dependencies.
     * @return The new parent node.
     * @throws XMLStreamException if a parse error occurs.
     */
    private static Node parseNode(XMLStreamReader reader, Node parent, NodeRepository nodeRepository) throws XMLStreamException {
        String prototypeId = reader.getAttributeValue(null, "prototype");
        String name = reader.getAttributeValue(null, "name");
        String function = reader.getAttributeValue(null, "function");
        String outputType = reader.getAttributeValue(null, "outputType");

        Node prototype = nodeRepository.getNode(prototypeId);
        prototype = prototype != null ? prototype : Node.ROOT;
        Node node = prototype.extend().withName(name).withFunction(function).withOutputType(outputType);
        while (true) {
            int eventType = reader.next();
            if (eventType == XMLStreamConstants.START_ELEMENT) {
                String tagName = reader.getLocalName();
                if (tagName.equals("port")) {
                    parsePort(reader);
                } else {
                    throw new XMLStreamException("Unknown tag " + tagName, reader.getLocation());
                }
            } else if (eventType == XMLStreamConstants.END_ELEMENT) {
                String tagName = reader.getLocalName();
                if (tagName.equals("node"))
                    break;
            }
        }
        parent = parent.withChildAdded(node);
        return parent;
    }

    private static Port parsePort(XMLStreamReader reader) throws XMLStreamException {
        String name = reader.getAttributeValue(null, "name");
        String type = reader.getAttributeValue(null, "type");
        Port port = Port.portForType(name, type);
        while (true) {
            int eventType = reader.next();
            if (eventType == XMLStreamConstants.START_ELEMENT) {
                String tagName = reader.getLocalName();
                if (tagName.equals("value")) {
                    String valueAsString = parsePortValue(reader);
                    port = Port.parsedPort(name, type, valueAsString);
                } else {
                    throw new XMLStreamException("Unknown tag " + tagName, reader.getLocation());
                }

            } else if (eventType == XMLStreamConstants.END_ELEMENT) {
                String tagName = reader.getLocalName();
                if (tagName.equals("port"))
                    break;
            }
        }
        checkNotNull(port);
        return port;
    }

    private static String parsePortValue(XMLStreamReader reader) throws XMLStreamException {
        String text = null;
        while (true) {
            int eventType = reader.next();
            if (eventType == XMLStreamConstants.CHARACTERS) {
                text = reader.getText();
            }
            if (eventType == XMLStreamConstants.START_ELEMENT) {
                String tagName = reader.getLocalName();
                throw new XMLStreamException("Unknown tag " + tagName, reader.getLocation());
            } else if (eventType == XMLStreamConstants.END_ELEMENT) {
                String tagName = reader.getLocalName();
                if (tagName.equals("value"))
                    break;
            }
        }
        checkNotNull(text);
        return text;
    }

    //// Saving ////

    /**
     * Write the NodeLibrary to a file.
     * @param file The file to save.
     */
    public void store(File file) throws IOException {
        throw new UnsupportedOperationException("Not implemented");
    }

}
