package nodebox.node;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


/**
 * Parses the ndbx file format.
 */
public class NDBXHandler extends DefaultHandler {

    enum ParseState {
        INVALID, IN_VALUE, IN_EXPRESSION
    }

    public static final String TAG_NDBX = "ndbx";
    public static final String TAG_VAR = "var";
    public static final String TAG_NODE = "node";
    public static final String TAG_PORT = "port";
    public static final String TAG_VALUE = "value";
    public static final String TAG_EXPRESSION = "expression";
    public static final String TAG_CONNECTION = "conn";

    public static final String ATTR_NDBX_FORMAT_VERSION = "formatVersion";
    public static final String ATTR_VAR_NAME = "name";
    public static final String ATTR_VAR_VALUE = "value";
    public static final String ATTR_NODE_NAME = "name";
    public static final String ATTR_NODE_TYPE = "type";
    public static final String ATTR_NODE_X = "x";
    public static final String ATTR_NODE_Y = "y";
    public static final String ATTR_NODE_EXPORTED = "exported";
    public static final String ATTR_PORT_NAME = "name";
    public static final String ATTR_CONNECTION_OUTPUT = "output";
    public static final String ATTR_CONNECTION_INPUT = "input";

    private NodeLibraryManager manager;
    private NodeLibrary library;
    private Node rootNode;
    private Node currentNode;
    private Port currentPort;
    private ParseState state = ParseState.INVALID;
    private StringBuffer characterData;

    public NDBXHandler(NodeLibrary library, NodeLibraryManager manager) {
        this.manager = manager;
        this.library = library;
        this.rootNode = library.getRootMacro();
        currentNode = rootNode;
    }

    public NodeLibrary geNodeLibrary() {
        return library;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (qName.equals(TAG_NDBX)) {
            startNdbxTag(attributes);
        } else if (qName.equals(TAG_VAR)) {
            startVarTag(attributes);
        } else if (qName.equals(TAG_NODE)) {
            startNodeTag(attributes);
        } else if (qName.equals(TAG_PORT)) {
            startPortTag(attributes);
        } else if (qName.equals(TAG_VALUE)) {
            startValueTag(attributes);
        } else if (qName.equals(TAG_EXPRESSION)) {
            startExpressionTag(attributes);
        } else if (qName.equals(TAG_CONNECTION)) {
            startConnectionTag(attributes);
        } else {
            throwParseException("Unknown start tag '%s'", qName);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equals(TAG_NDBX)) {
            // Top level element -- parsing finished.
        } else if (qName.equals(TAG_VAR)) {
            // Do nothing after var tag
        } else if (qName.equals(TAG_NODE)) {
            // Traverse up to the parent.
            // This can result in currentNode being null if we traversed all the way up
            currentNode = currentNode.getParent();
        } else if (qName.equals(TAG_PORT)) {
            currentPort = null;
        } else if (qName.equals(TAG_VALUE)) {
            setValue(characterData.toString());
            resetState();
        } else if (qName.equals(TAG_EXPRESSION)) {
            setExpression(characterData.toString());
            resetState();
        } else if (qName.equals(TAG_CONNECTION)) {
            // Do nothing after connection tag
        } else {
            // This should never happen, since the SAX parser has already formally validated the document.
            // Unknown tags should be caught in startElement.
            throwParseException("Unknown end tag '%s'", qName);
        }
    }

    /**
     * Called after valid character data was processed.
     * <p/>
     * This makes sure no extraneous data is added.
     */
    private void resetState() {
        state = ParseState.INVALID;
        characterData = null;
    }

    /**
     * Parse the ndbx tag. This is the start of the file.
     *
     * @param attributes the tag attributes
     * @throws SAXException if the format version is null or unsupported.
     */
    private void startNdbxTag(Attributes attributes) throws SAXException {
        String formatVersion = attributes.getValue(ATTR_NDBX_FORMAT_VERSION);
        checkNotNull(formatVersion, "NodeBox file does not have required attribute formatVersion.");
        checkParseState(formatVersion.equals("0.10"), "Format version %s is not supported.", formatVersion);
    }

    /**
     * Parse the variable tag. Variables are key/value pairs that are stored in the NodeBox library.
     *
     * @param attributes the tag attributes
     * @throws SAXException if name or value are null.
     */
    private void startVarTag(Attributes attributes) throws SAXException {
        String name = attributes.getValue(ATTR_VAR_NAME);
        String value = attributes.getValue(ATTR_VAR_VALUE);
        checkNotNull(name, "Name attribute is required in var tags.");
        checkNotNull(value, "Value attribute is required in var tags.");
        library.setVariable(name, value);
    }

    /**
     * Parse the node tag. This tag is tree-like: it can be inside of the ndbx tag or other node tags.
     *
     * @param attributes the tag attributes
     * @throws SAXException if the name or type are not given, the type is invalid,
     *                      or if trying to create child nodes for non-macros.
     */
    private void startNodeTag(Attributes attributes) throws SAXException {
        String name = attributes.getValue(ATTR_NODE_NAME);
        String type = attributes.getValue(ATTR_NODE_TYPE);
        checkNotNull(name, "Name attribute is required in node tags.");
        checkNotNull(type, "Type attribute is required in node tags.");
        Class nodeClass = null;
        try {
            nodeClass = Class.forName(type);
        } catch (ClassNotFoundException e) {
            throwParseException("Node type %s not found.", type);
        }
        // Create the child at the root of the node library or the current parent
        Node newNode;
        checkNotNull(currentNode, "Current node cannot be null.");
        checkParseState(currentNode instanceof Macro, "Child nodes are only supported for macros.");
        if (currentNode == rootNode) {
            newNode = currentNode;
        } else {
            Macro macro = (Macro) currentNode;
            newNode = macro.createChild(nodeClass, name);
        }
        // Parse additional node flags.
        String x = attributes.getValue(ATTR_NODE_X);
        String y = attributes.getValue(ATTR_NODE_Y);
        if (x != null)
            newNode.setX(Double.parseDouble(x));
        if (y != null)
            newNode.setY(Double.parseDouble(y));
        if ("true".equals(attributes.getValue(ATTR_NODE_EXPORTED)))
            newNode.setExported(true);
        // Go down into the current node; this will now become the current network.
        currentNode = newNode;
    }

    /**
     * Parse the port tag. This tag is inside of the node tag.
     *
     * @param attributes the tag attributes
     * @throws SAXException if the name is null or the port could not be found.
     */
    private void startPortTag(Attributes attributes) throws SAXException {
        String name = attributes.getValue(ATTR_PORT_NAME);
        checkNotNull(name, "Name attribute is required in port tags.");
        currentPort = currentNode.getPort(name);
        checkNotNull(currentPort, "Port %s on not found.");
    }

    /**
     * Parse the value tag. This tag is inside of the port tag.
     *
     * @param attributes tag attributes
     * @throws SAXException if the current port is null
     */
    private void startValueTag(Attributes attributes) throws SAXException {
        checkNotNull(currentPort, "Value tag encountered without current port.");
        state = ParseState.IN_VALUE;
        characterData = new StringBuffer();
    }

    /**
     * Set the value on the current port.
     *
     * @param valueAsString the value of the port, to be parsed.
     * @throws org.xml.sax.SAXException when there is no current node, if port was not found or if the value could not be parsed.
     */
    private void setValue(String valueAsString) throws SAXException {
        checkNotNull(currentPort, "End of value tag encountered without current port.");
        Object value = null;
        try {
            value = currentPort.parseValue(valueAsString);
        } catch (IllegalArgumentException e) {
            throwParseException("%s: could not parse value '%s'.", currentPort.getAbsolutePath(), valueAsString);
        }
        try {
            currentPort.setValue(value);
        } catch (IllegalArgumentException e) {
            throwParseException("%s: value '%s' is invalid for port.", currentPort.getAbsolutePath(), valueAsString);
        }
    }


    /**
     * Parse the expression tag. This tag is inside of the port tag.
     *
     * @param attributes the tag attributes
     * @throws SAXException if there is no current port
     */
    private void startExpressionTag(Attributes attributes) throws SAXException {
        checkNotNull(currentPort, "Expression tag encountered without current port.");
        state = ParseState.IN_EXPRESSION;
        characterData = new StringBuffer();
    }

    /**
     * Set the expression on the current port.
     *
     * @param expression the expression string
     * @throws SAXException if there is no current port.
     */
    private void setExpression(String expression) throws SAXException {
        checkNotNull(currentPort, "End of expression tag encountered without current port.");
        currentPort.setExpression(expression);
    }

    /**
     * Parse the connection tag. This tag is inside of the port tag.
     *
     * @param attributes the tag attributes
     * @throws SAXException if the input/output format is incorrect or the node/ports cannot be found or connected.
     */
    private void startConnectionTag(Attributes attributes) throws SAXException {
        String inputAsString = attributes.getValue(ATTR_CONNECTION_INPUT);
        String outputAsString = attributes.getValue(ATTR_CONNECTION_OUTPUT);
        checkNotNull(inputAsString, "Input attribute is required in connection tags.");
        checkNotNull(outputAsString, "Output attribute is required in connection tags.");
        String[] inputSegments = inputAsString.split("\\.");
        checkParseState(inputSegments.length == 2, "Invalid format for input attribute '%s'.", inputAsString);
        String[] outputSegments = outputAsString.split("\\.");
        checkParseState(outputSegments.length == 2, "Invalid format for output attribute '%s'.", outputAsString);

        checkParseState(currentNode instanceof Macro, "The current node %s is not a macro.", currentNode);
        Macro macro = (Macro) currentNode;
        Node inputNode = macro.getChild(inputSegments[0]);
        checkNotNull(inputNode, "Child input node %s could not be found.", inputAsString);
        Port inputPort = inputNode.getPort(inputSegments[1]);
        checkNotNull(inputPort, "Child input port %s could not be found.", inputAsString);
        Node outputNode = macro.getChild(outputSegments[0]);
        checkNotNull(outputNode, "Child output node %s could not be found.", outputAsString);
        Port outputPort = outputNode.getPort(outputSegments[1]);
        checkNotNull(outputPort, "Child output port %s could not be found.", outputAsString);

        macro.connect(inputPort, outputPort);
    }


    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        switch (state) {
            case IN_VALUE:
                if (currentPort == null)
                    throw new SAXException("Value encountered, but no current port.");
                break;
            case IN_EXPRESSION:
                if (currentPort == null)
                    throw new SAXException("Expression encountered, but no current port.");
                break;
            default:
                // Bail out when we don't recognize this state.
                return;
        }
        // We have a valid character state, so we can safely append to characterData.
        characterData.append(ch, start, length);
    }

    /**
     * Check if the reference object is not null and throw a parse exception otherwise.
     *
     * @param reference the reference object to check
     * @param msg       the error message
     * @param args      string formatting arguments to the error message
     * @throws SAXException if the reference object is null
     */
    private void checkNotNull(Object reference, String msg, Object... args) throws SAXException {
        if (reference == null) {
            throwParseException(msg, args);
        }
    }

    /**
     * Check if the expression evaluates to true and throw a parse exception otherwise.
     *
     * @param expression the expression to evaluate
     * @param msg        the error message
     * @param args       string formatting arguments to the error message
     * @throws SAXException if the expression is false
     */
    private void checkParseState(boolean expression, String msg, Object... args) throws SAXException {
        if (!expression) {
            throwParseException(msg, args);
        }
    }

    /**
     * Throw a parse exception with the given message.
     *
     * @param msg  the error message
     * @param args string formatting arguments to the error message
     * @throws SAXException always throws an exception
     */
    private void throwParseException(String msg, Object... args) throws SAXException {
        throw new SAXException(String.format(msg, args));

    }


}
