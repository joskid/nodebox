package nodebox.node;

import nodebox.node.event.*;
import nodebox.util.FileUtils;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A Node library stores a set of (possibly hierarchical) nodes.
 * <p/>
 * Node libraries are both documents and libraries. By mentioning a node on the library search path, you can get
 * all the nodes.
 * <p/>
 * Node libraries can be backed by files, and saved in that same file, or they can be stored in memory only.
 * <p/>
 * This implementation of the node library only stores a root node.
 * Calling get(String) on the library actually forwards the call to getChild() on the root node.
 */
public class NodeLibrary {

    private String name;
    private File file;
    private Macro rootMacro;
    private HashMap<String, String> variables;
    private NodeCode code;
    private NodeEventBus eventBus = new NodeEventBus();

    /**
     * Load a library from the given XML.
     * <p/>
     * This library is not added to the manager. The manager is used only to look up prototypes.
     * You can add the library to the manager yourself using manager.add(), or by calling
     * manager.load().
     *
     * @param libraryName the name of the new library
     * @param xml         the xml data of the library
     * @param manager     the manager used to look up node prototypes.
     * @return a new node library
     * @throws RuntimeException When the string could not be parsed.
     * @see nodebox.node.NodeLibraryManager#add(NodeLibrary)
     * @see nodebox.node.NodeLibraryManager#load(String, String)
     */
    public static NodeLibrary fromXml(String libraryName, String xml, NodeLibraryManager manager) throws RuntimeException {
        try {
            NodeLibrary library = new NodeLibrary(libraryName);
            load(library, new ByteArrayInputStream(xml.getBytes("UTF8")), manager);
            return library;
        } catch (ParserConfigurationException e) {
            throw new RuntimeException("Error in the XML parser configuration", e);
        } catch (SAXException e) {
            throw new RuntimeException("Error while parsing: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new RuntimeException("I/O error while parsing.", e);
        }
    }

    /**
     * Load a library from the given file.
     * <p/>
     * This library is not added to the manager. The manager is used only to look up prototypes.
     * You can add the library to the manager yourself using manager.add(), or by calling
     * manager.load().
     *
     * @param f       the file to load
     * @param manager the manager used to look up node prototypes.
     * @return a new node library
     * @throws RuntimeException When the file could not be found, or parsing failed.
     * @see nodebox.node.NodeLibraryManager#add(NodeLibrary)
     * @see nodebox.node.NodeLibraryManager#load(File)
     */
    public static NodeLibrary fromFile(File f, NodeLibraryManager manager) throws RuntimeException {
        try {
            // The library name is the file name without the ".ndbx" extension.
            // Chop off the .ndbx
            String libraryName = FileUtils.stripExtension(f);
            NodeLibrary library = new NodeLibrary(libraryName, f);
            load(library, new FileInputStream(f), manager);
            return library;
        } catch (ParserConfigurationException e) {
            throw new RuntimeException("Error in the XML parser configuration", e);
        } catch (SAXException e) {
            throw new RuntimeException("Error while parsing: " + e.getMessage(), e);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("File not found " + f, e);
        } catch (IOException e) {
            throw new RuntimeException("I/O error while parsing " + f, e);
        }
    }

    /**
     * This method gets called from the public load method and does the actual parsing.
     * <p/>
     * The method requires a newly created (empty) library. Nodes are added to this library.
     *
     * @param library the newly created library
     * @param is      the input stream data
     * @param manager the manager used for looking up prototypes.
     * @throws IOException                  when the data could not be loaded
     * @throws ParserConfigurationException when the parser is incorrectly configured
     * @throws SAXException                 when the data could not be parsed
     */
    private static void load(NodeLibrary library, InputStream is, NodeLibraryManager manager) throws IOException, ParserConfigurationException, SAXException {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        SAXParser parser = spf.newSAXParser();
        NDBXHandler handler = new NDBXHandler(library, manager);
        parser.parse(is, handler);
    }

    public NodeLibrary(String name) {
        this(name, null);
    }

    public NodeLibrary(String name, File file) {
        checkNotNull(name);
        this.name = name;
        this.file = file;
        this.rootMacro = Macro.createRootMacro(this);
        this.variables = new HashMap<String, String>();
    }

    public String getName() {
        return name;
    }

    public File getFile() {
        return file;
    }

    //// Node management ////

    public Macro getRootMacro() {
        return rootMacro;
    }

    public List<Node> getExportedNodes() {
        Collection<Node> allChildren = rootMacro.getChildren();
        List<Node> exportedChildren = new ArrayList<Node>(allChildren.size());
        for (Node child : allChildren) {
            if (child.isExported()) {
                exportedChildren.add(child);
            }
        }
        return exportedChildren;
    }

    /**
     * Get a node from this library.
     * <p/>
     * Only exported nodes are returned. If you want all nodes, use getRootMacro().getChild()
     *
     * @param name the name of the node
     * @return the node, or null if a node with this name could not be found.
     */
    public Node get(String name) {
        if ("root".equals(name)) return rootMacro;
        return rootMacro.getExportedChild(name);
    }

    public Node remove(String name) {
        Node node = rootMacro.getChild(name);
        if (node == null) return null;
        rootMacro.removeChild(node);
        return node;
    }

    public boolean remove(Node node) {
        return rootMacro.removeChild(node);
    }

    public boolean contains(String nodeName) {
        return rootMacro.hasChild(nodeName);
    }

    //// Variables ////

    public String[] getVariableNames() {
        return variables.keySet().toArray(new String[variables.keySet().size()]);
    }

    public String getVariable(String name) {
        return variables.get(name);
    }

    public void setVariable(String name, String value) {
        variables.put(name, value);
    }

    //// Code ////

    public void setCode(NodeCode code) {
        this.code = code;
    }

    public NodeCode getCode() {
        return code;
    }

    //// Persistence /////

    public void store() throws IOException, IllegalArgumentException {
        if (file == null)
            throw new IllegalArgumentException("Library was not loaded from a file and no file given to store.");
        store(file);
    }

    public void store(File f) throws IOException {
        file = f;
        NDBXWriter.write(this, f);
    }

    /**
     * Get the full XML data for this library and all of its nodes.
     *
     * @return an XML string
     */
    public String toXml() {
        return NDBXWriter.asString(this);
    }

    //// Events ////

    public void addListener(NodeEventListener l) {
        eventBus.addListener(l);
    }

    public boolean removeListener(NodeEventListener l) {
        return eventBus.removeListener(l);
    }

    public void fireNodeDirty(Node source) {
        eventBus.send(new NodeDirtyEvent(source));
    }

    public void fireNodeUpdated(Node source, CookContext context) {
        eventBus.send(new NodeUpdatedEvent(source, context));
    }

    public void fireNodePositionChanged(Node source) {
        eventBus.send(new NodePositionChangedEvent(source));

    }

    public void fireNodeAttributesChanged(Node source) {
        eventBus.send(new NodeAttributesChangedEvent(source));
    }

    public void fireChildAdded(Macro source, Node child) {
        eventBus.send(new ChildAddedEvent(source, child));
    }

    public void fireChildRemoved(Macro source, Node child) {
        eventBus.send(new ChildRemovedEvent(source, child));
    }

    public void fireConnectionAdded(Node source, Connection c) {
        eventBus.send(new ConnectionAddedEvent(source, c));
    }

    public void fireConnectionRemoved(Node source, Connection c) {
        eventBus.send(new ConnectionRemovedEvent(source, c));
    }

    public void fireRenderedChildChanged(Node source, Node child) {
        eventBus.send(new RenderedChildChangedEvent(source, child));
    }

    public void fireValueChanged(Node source, Port port) {
        eventBus.send(new ValueChangedEvent(source, port));
    }

    public void fireNodePortsChangedEvent(Node source) {
        eventBus.send(new NodePortsChangedEvent(source));
    }

    public void firePortAttributesChangedEvent(Node source, Port port) {
        eventBus.send(new PortAttributesChangedEvent(source, port));
    }

    //// Standard overrides ////

    @Override
    public String toString() {
        return getName();
    }
}
