package nodebox.node;

import nodebox.function.CoreFunctions;
import nodebox.function.FunctionLibrary;
import nodebox.function.FunctionRepository;
import nodebox.graphics.Point;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Writes the ndbx file format.
 */
public class NDBXWriter {

    public static void write(NodeLibrary library, File file) {
        StreamResult streamResult = new StreamResult(file);
        write(library, streamResult);

    }

    public static void write(NodeLibrary library, Writer writer) {
        StreamResult streamResult = new StreamResult(writer);
        write(library, streamResult);
    }

    public static void write(NodeLibrary library, StreamResult streamResult) {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.newDocument();

            // Build the header.
            Element rootElement = doc.createElement("ndbx");
            rootElement.setAttribute("type", "file");
            rootElement.setAttribute("formatVersion", "1.0");
            doc.appendChild(rootElement);

            // Write out all the variables.
//            for (String variableName : library.getVariableNames()) {
//                String variableValue = library.getVariable(variableName);
//                Element varElement = doc.createElement("var");
//                rootElement.appendChild(varElement);
//                varElement.setAttribute("name", variableName);
//                varElement.setAttribute("value", variableValue);
//            }

            // Write the function repository.
            writeFunctionRepository(doc, rootElement, library.getFunctionRepository());

            // Write the root node.
            writeNode(doc, rootElement, library.getRoot());

            // Convert the document to XML.
            DOMSource domSource = new DOMSource(doc);
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer serializer = tf.newTransformer();
            serializer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            serializer.setOutputProperty(OutputKeys.INDENT, "yes");
            serializer.transform(domSource, streamResult);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }
    }

    public static String asString(NodeLibrary library) {
        StringWriter writer = new StringWriter();
        write(library, writer);
        return writer.toString();
    }

    /**
     * Write out links to the function repositories used.
     *
     * @param doc                the XML document
     * @param parent             the parent element
     * @param functionRepository the function repository to write
     */
    private static void writeFunctionRepository(Document doc, Element parent, FunctionRepository functionRepository) {
        for (FunctionLibrary library : functionRepository.getLibraries()) {
            // The core functions library is implicitly included.
            if (library == CoreFunctions.LIBRARY) continue;
            Element el = doc.createElement("link");
            el.setAttribute("rel", "functions");
            el.setAttribute("href", library.getLink());
            parent.appendChild(el);
        }
    }

    /**
     * Find the libraryname.nodename of the given node.
     * Searches the list of default node repositories to find it.
     *
     * @param node The node to find.
     * @return the node id, in the format libraryname.nodename.
     */
    private static String findNodeId(Node node) {
        NodeLibrary library = NodeRepository.DEFAULT.nodeLibraryForNode(node);
        if (library == null) {
            return node.getName();
        } else {
            return String.format("%s.%s", library.getName(), node.getName());
        }
    }

    /**
     * Write out the node.
     *
     * @param doc    the XML document
     * @param parent the parent element
     * @param node   the node to write
     */
    private static void writeNode(Document doc, Element parent, Node node) {
        Element el = doc.createElement("node");
        parent.appendChild(el);

        // Write prototype
        if (shouldWriteAttribute(node, Node.Attribute.PROTOTYPE) && node.getPrototype() != Node.ROOT)
            el.setAttribute("prototype", findNodeId(node.getPrototype()));

        // Write name
        if (shouldWriteAttribute(node, Node.Attribute.NAME))
            el.setAttribute("name", node.getName());

        // Write description
        if (shouldWriteAttribute(node, Node.Attribute.DESCRIPTION))
            el.setAttribute("description", node.getDescription());

        // Write output type
        if (shouldWriteAttribute(node, Node.Attribute.OUTPUT_TYPE))
            el.setAttribute("outputType", node.getOutputType());

        // Write image
        if (shouldWriteAttribute(node, Node.Attribute.IMAGE))
            el.setAttribute("image", node.getImage());

        // Write function
        if (shouldWriteAttribute(node, Node.Attribute.FUNCTION))
            el.setAttribute("function", node.getFunction());

        // Write list policy
        if (shouldWriteAttribute(node, Node.Attribute.LIST_STRATEGY))
            el.setAttribute("strategy", node.getListStrategy());

        // Write position
        if (shouldWriteAttribute(node, Node.Attribute.POSITION)) {
            Point position = node.getPosition();
            el.setAttribute("position", String.format("%.0f,%.0f", position.x, position.y));
        }

        // Write rendered child
        if (shouldWriteAttribute(node, Node.Attribute.RENDERED_CHILD_NAME))
            el.setAttribute("renderedChild", node.getRenderedChildName());

        // Add the input ports
        if (shouldWriteAttribute(node, Node.Attribute.INPUTS)) {
            for (Port port : node.getInputs()) {
                writePort(doc, el, node, port, Port.Direction.INPUT);
            }
        }

        // Add the children
        if (shouldWriteAttribute(node, Node.Attribute.CHILDREN)) {
            // Sort the children.
            ArrayList<Node> children = new ArrayList<Node>();
            children.addAll(node.getChildren());
            Collections.sort(children, new NodeNameComparator());
            // The order in which the nodes are written is important!
            // Since a library can potentially store an instance and its prototype, make sure that the prototype gets
            // stored sequentially before its instance.
            // The reader expects prototypes to be defined before their instances.
            while (!children.isEmpty()) {
                Node child = children.get(0);
                writeOrderedChild(doc, el, children, child);
            }
        }

        // Add all child connections
        if (shouldWriteAttribute(node, Node.Attribute.CONNECTIONS)) {
            for (Connection conn : node.getConnections()) {
                writeConnection(doc, el, conn);
            }
        }
    }

    /**
     * Check if the given attribute should be written.
     * <p/>
     * The attribute should be written if  it's value is different from the prototype value.
     *
     * @param node      The node.
     * @param attribute The name of the attribute.
     * @return true if the attribute should be written.
     */
    private static boolean shouldWriteAttribute(Node node, Node.Attribute attribute) {
        checkArgument(node != Node.ROOT, "You cannot write out the _root node.");
        Object prototypeValue = node.getPrototype().getAttributeValue(attribute);
        Object nodeValue = node.getAttributeValue(attribute);
        if (attribute != Node.Attribute.PROTOTYPE) {
            checkNotNull(prototypeValue, "Attribute %s of node %s is empty.", attribute, node.getPrototype());
            checkNotNull(nodeValue, "Attribute %s of node %s is empty.", attribute, node);
            return !prototypeValue.equals(nodeValue);
        } else {
            return prototypeValue != nodeValue;
        }
    }

    /**
     * Write out the child. If the prototype of the child is also in this library, write that out first, recursively.
     *
     * @param doc      the XML document
     * @param parent   the parent element
     * @param children a list of children that were written already.
     *                 When a child is written, we remove it from the list.
     * @param child    the child to write
     */
    private static void writeOrderedChild(Document doc, Element parent, List<Node> children, Node child) {
        Node prototype = child.getPrototype();
        if (children.contains(prototype))
            writeOrderedChild(doc, parent, children, prototype);
        writeNode(doc, parent, child);
        children.remove(child);
    }

    private static void writePort(Document doc, Element parent, Node node, Port port, Port.Direction direction) {
        // We only write out the ports that have changed with regards to the prototype.
        Node protoNode = node.getPrototype();
        Port protoPort = null;
        if (protoNode != null)
            protoPort = protoNode.getInput(port.getName());
        // If the port and its prototype are equal, don't write anything.
        if (port.equals(protoPort)) return;
        Element el = doc.createElement("port");
        el.setAttribute("name", port.getName());
        el.setAttribute("type", port.getType());
        if (port.isStandardType()) // TODO && direction == Port.Direction.INPUT
            el.setAttribute("value", port.stringValue());
        if (port.getMinimumValue() != null)
        el.setAttribute("min", String.valueOf(port.getMinimumValue()));
        if (port.getMaximumValue() != null)
            el.setAttribute("max", String.valueOf(port.getMaximumValue()));
        parent.appendChild(el);
    }

    private static void writeConnection(Document doc, Element parent, Connection conn) {
        Element connElement = doc.createElement("conn");
        connElement.setAttribute("output", String.format("%s", conn.getOutputNode()));
        connElement.setAttribute("input", String.format("%s.%s", conn.getInputNode(), conn.getInputPort()));
        parent.appendChild(connElement);
    }

    private static class NodeNameComparator implements Comparator<Node> {
        public int compare(Node node1, Node node2) {
            return node1.getName().compareTo(node2.getName());
        }
    }

}
