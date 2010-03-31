package nodebox.node;

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
import java.util.Locale;

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
            rootElement.setAttribute("formatVersion", "0.10");

            // Write out all the variables.
            for (String variableName : library.getVariableNames()) {
                String variableValue = library.getVariable(variableName);
                Element varElement = doc.createElement("var");
                doc.appendChild(varElement);
                varElement.setAttribute("value", variableValue);
            }

            // Write out the all children and connections of the root macro.
            // The root macro itself is not written.
            Macro rootMacro = library.getRootMacro();
            writeChildren(doc, rootElement, rootMacro);
            writeConnections(doc, rootElement, rootMacro);

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
     * Write a node to the stream.
     *
     * @param doc    the document to write to.
     * @param parent the parent element.
     * @param node   the node to write.
     */
    private static void writeNode(Document doc, Element parent, Node node) {
        String xPosition = String.format(Locale.US, "%.0f", node.getX());
        String yPosition = String.format(Locale.US, "%.0f", node.getY());
        Element el = doc.createElement("node");
        parent.appendChild(el);
        el.setAttribute("name", node.getName());
        el.setAttribute("type", node.getClass().getCanonicalName());
        el.setAttribute("x", xPosition);
        el.setAttribute("y", yPosition);
        if (node.isExported())
            el.setAttribute("exported", "true");

        // Add the ports
        for (Port port : node.getPorts()) {
            writePort(doc, el, port);
        }

        // A macro contains child nodes and connections.
        if (node instanceof Macro) {
            Macro macro = (Macro) node;
            writeChildren(doc, el, macro);
            writeConnections(doc, el, macro);
        }
    }

    private static void writePort(Document doc, Element parent, Port port) {
        Element key = doc.createElement("key");
        parent.appendChild(key);
        Text keyText = doc.createTextNode(port.getName());
        key.appendChild(keyText);

        Element value = doc.createElement("value");
        parent.appendChild(value);
        Text valueText = doc.createTextNode(port.getValue().toString());
        valueText.appendChild(valueText);
    }


    private static void writeChildren(Document doc, Element parent, Macro macro) {
        for (Node child : macro.getChildren()) {
            writeNode(doc, parent, child);
        }
    }

    private static void writeConnections(Document doc, Element parent, Macro macro) {
        // Add all child connections
        for (Connection c : macro.getConnections()) {
            writeConnection(doc, parent, c);
        }
    }

    private static void writeConnection(Document doc, Element parent, Connection conn) {
        Element el = doc.createElement("conn");
        parent.appendChild(el);
        el.setAttribute("input", String.format("%s.%s", conn.getInputNode().getName(), conn.getInput().getName()));
        el.setAttribute("output", String.format("%s.%s", conn.getOutputNode().getName(), conn.getOutput().getName()));
    }

}
