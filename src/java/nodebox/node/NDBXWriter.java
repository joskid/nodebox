package nodebox.node;

import nodebox.graphics.Point;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

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
import java.util.*;

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
            doc.appendChild(rootElement);
            rootElement.setAttribute("type", "file");
            rootElement.setAttribute("formatVersion", "1.0");

            // Write out all the variables.
//            for (String variableName : library.getVariableNames()) {
//                String variableValue = library.getVariable(variableName);
//                Element varElement = doc.createElement("var");
//                rootElement.appendChild(varElement);
//                varElement.setAttribute("name", variableName);
//                varElement.setAttribute("value", variableValue);
//            }

            // Write out all the nodes (skip the root)
            ArrayList<Node> children = new ArrayList<Node>();
            children.addAll(library.getRoot().getChildren());
            // Sort the children by name.
            Collections.sort(children, new NodeNameComparator());
            // The order in which the nodes are written is important!
            // Since a library can potentially store an instance and its prototype, make sure that the prototype gets
            // stored sequentially before its instance.
            // The reader expects prototypes to be defined before their instances.
            while (!children.isEmpty()) {
                Node child = children.iterator().next();
                writeOrderedChild(library, doc, rootElement, children, child);
            }

            // Write out all the child connections.
            for (Connection conn : library.getRoot().getConnections()) {
                writeConnection(doc, rootElement, conn);
            }

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
     * Write out the child. If the prototype of the child is also in this library, write that out first, recursively.
     *
     * @param library  the node library
     * @param doc      the XML document
     * @param parent   the parent element
     * @param children a list of children that were written already.
     *                 When a child is written, we remove it from the list.
     * @param child    the child to write
     */
    private static void writeOrderedChild(NodeLibrary library, Document doc, Element parent, List<Node> children, Node child) {
        Node prototype = child.getPrototype();
        if (children.contains(prototype)) // TODO doesn't do a deep check.
            writeOrderedChild(library, doc, parent, children, prototype);
        writeNode(doc, parent, child);
        children.remove(child);
    }

    private static void writeNode(Document doc, Element parent, Node node) {
        Point position = node.getPosition();
        String xPosition = String.format(Locale.US, "%.0f", position.x);
        String yPosition = String.format(Locale.US, "%.0f", position.y);
        Element el = doc.createElement("node");
        parent.appendChild(el);
        el.setAttribute("name", node.getName());
        // TODO How do we find the full name of a prototype?
        el.setAttribute("prototype", node.getPrototype().getName());
        el.setAttribute("x", xPosition);
        el.setAttribute("y", yPosition);
        // Before we wrote this on the child. Now we write it on the parent, if appropriate.
        if (!node.getRenderedChildName().isEmpty()) {
            el.setAttribute("renderedChild", node.getRenderedChildName());
        }

        // Add the description
        if (!node.getDescription().isEmpty()) {
            Element desc = doc.createElement("description");
            el.appendChild(desc);
            Text descText = doc.createTextNode(node.getDescription());
            desc.appendChild(descText);
        }

        // Add the ports
        for (Port port : node.getInputs()) {
            writePort(doc, el, node, port);
        }

        // Add all child nodes
        for (Node child : node.getChildren()) {
            writeNode(doc, el, child);
        }


        // Add all child connections
        for (Connection conn : node.getConnections()) {
            writeConnection(doc, el, conn);
        }
    }

    private static void writePort(Document doc, Element parent, Node node, Port port) {
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
        el.setAttribute("value", port.stringValue());
        parent.appendChild(el);
    }

    private static void writeConnection(Document doc, Element parent, Connection conn) {
        Element connElement = doc.createElement("conn");
        connElement.setAttribute("output", conn.getOutputNode().getName());
        connElement.setAttribute("input", conn.getInputNode().getName());
        connElement.setAttribute("port", conn.getInputPort().getName());
        parent.appendChild(connElement);
    }

    private static class NodeNameComparator implements Comparator<Node> {
        public int compare(Node node1, Node node2) {
            return node1.getName().compareTo(node2.getName());
        }
    }

}
