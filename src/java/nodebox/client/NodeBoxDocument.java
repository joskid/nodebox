package nodebox.client;

import com.google.common.collect.ImmutableList;
import nodebox.function.*;
import nodebox.handle.HandleDelegate;
import nodebox.movie.Movie;
import nodebox.movie.VideoFormat;
import nodebox.node.*;
import nodebox.ui.*;
import nodebox.util.FileUtils;

import javax.swing.*;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A NodeBoxDocument manages a NodeLibrary.
 */
public class NodeBoxDocument extends JFrame implements WindowListener, HandleDelegate {

    private static final Logger LOG = Logger.getLogger(NodeBoxDocument.class.getName());
    private static final String WINDOW_MODIFIED = "windowModified";

    public static String lastFilePath;
    public static String lastExportPath;

    private static NodeClipboard nodeClipboard;

    private File documentFile;
    private boolean documentChanged;
    private AnimationTimer animationTimer;
    private boolean loaded = false;

    private UndoManager undoManager = new UndoManager();
    private boolean holdEdits = false;
    private String lastEditType = null;
    private Object lastEditObject = null;

    // State
    private final NodeLibraryController controller;
    private String activeNetworkPath = "";
    private String activeNodeName = "";
    private double frame;

    // Rendering
    private final AtomicBoolean isRendering = new AtomicBoolean(false);
    private final AtomicBoolean shouldRender = new AtomicBoolean(false);
    private final ExecutorService renderService;

    // GUI components
    private final NodeBoxMenuBar menuBar;
    private final AnimationBar animationBar;
    private final AddressBar addressBar;
    private final ViewerPane viewerPane;
    private final DataSheet dataSheet;
    private final PortView portView;
    private final NetworkView networkView;
    private JSplitPane parameterNetworkSplit;
    private JSplitPane topSplit;

    public static NodeBoxDocument getCurrentDocument() {
        return Application.getInstance().getCurrentDocument();
    }

    private static final Node demoRoot;

    static {
        Node value1 = Node.ROOT.withName("toNumbers1")
                .withPosition(new nodebox.graphics.Point(20, 20))
                .withFunction("math/toNumbers")
                .withListPolicy(ListPolicy.LIST_AWARE)
                .withInputAdded(Port.stringPort("s", "11 22 33"))
                .withOutputAdded(Port.floatPort("numbers", 0));
        Node value2 = Node.ROOT.withName("toNumbers2")
                .withPosition(new nodebox.graphics.Point(20, 100))
                .withFunction("math/toNumbers")
                .withListPolicy(ListPolicy.LIST_AWARE)
                .withInputAdded(Port.stringPort("s", "100 200 300"))
                .withOutputAdded(Port.floatPort("numbers", 0));
        Node range1 = Node.ROOT.withName("range1")
                .withPosition(new nodebox.graphics.Point(20, 200))
                .withFunction("math/range")
                .withListPolicy(ListPolicy.LIST_AWARE)
                .withInputAdded(Port.floatPort("start", 0))
                .withInputAdded(Port.floatPort("end", 10))
                .withInputAdded(Port.floatPort("step", 1))
                .withOutputAdded(Port.floatPort("numbers", 0));
        Node makePoint1 = Node.ROOT.withName("makePoint1")
                .withPosition(new nodebox.graphics.Point(120, 120))
                .withFunction("corevector/makePoint")
                .withInputAdded(Port.floatPort("x", 0))
                .withInputAdded(Port.floatPort("y", 0))
                .withOutputAdded(Port.pointPort("point", nodebox.graphics.Point.ZERO));
        Node makePoint2 = Node.ROOT.withName("makePoint2")
                .withPosition(new nodebox.graphics.Point(120, 220))
                .withFunction("corevector/makePoint")
                .withInputAdded(Port.floatPort("x", 0))
                .withInputAdded(Port.floatPort("y", 0))
                .withOutputAdded(Port.pointPort("point", nodebox.graphics.Point.ZERO));
        Node add = Node.ROOT.withName("add")
                .withPosition(new nodebox.graphics.Point(120, 20))
                .withFunction("math/add")
                .withInputAdded(Port.floatPort("v1", 0))
                .withInputAdded(Port.floatPort("v2", 0))
                .withOutputAdded(Port.floatPort("number", 0));
        Node rect = Node.ROOT.withName("rect")
                .withPosition(new nodebox.graphics.Point(220, 20))
                .withFunction("corevector/rect")
                .withInputAdded(Port.pointPort("position", nodebox.graphics.Point.ZERO))
                .withInputAdded(Port.floatPort("width", 100))
                .withInputAdded(Port.floatPort("height", 100))
                .withInputAdded(Port.pointPort("roundness", nodebox.graphics.Point.ZERO))
                .withOutputAdded(Port.customPort("geometry", "nodebox.graphics.Geometry"));
        Node color = Node.ROOT.withName("color")
                .withPosition(new nodebox.graphics.Point(320, 20))
                .withFunction("corevector/color")
                .withInputAdded(Port.customPort("geometry", "nodebox.graphics.Geometry"))
                .withInputAdded(Port.colorPort("fill", nodebox.graphics.Color.BLACK))
                .withInputAdded(Port.colorPort("stroke", nodebox.graphics.Color.BLACK))
                .withInputAdded(Port.floatPort("strokeWidth", 0))
                .withOutputAdded(Port.customPort("geometry", "nodebox.graphics.Geometry"));
        Node sleepy = Node.ROOT.withName("sleepy")
                .withPosition(new nodebox.graphics.Point(320, 100))
                .withFunction("math/slowNumber")
                .withInputAdded(Port.floatPort("value", 0))
                .withOutputAdded(Port.floatPort("value", 0));

        demoRoot = Node.ROOT
                .withInputAdded(Port.colorPort("background", nodebox.graphics.Color.WHITE))
                .withInputAdded(Port.floatPort("width", 500))
                .withInputAdded(Port.floatPort("height", 500))
                .withChildAdded(value1)
                .withChildAdded(value2)
                .withChildAdded(range1)
                .withChildAdded(add)
                .withChildAdded(makePoint1)
                .withChildAdded(makePoint2)
                .withChildAdded(rect)
                .withChildAdded(color)
                .withChildAdded(sleepy)
                .connect("toNumbers1", "numbers", "add", "v1")
                .connect("toNumbers2", "numbers", "add", "v2")
                .connect("toNumbers1", "numbers", "makePoint1", "x")
                .connect("toNumbers2", "numbers", "makePoint1", "y")
                .connect("rect", "geometry", "color", "geometry")
                .withRenderedChildName("add");
    }

    public NodeBoxDocument() {
        this(NodeLibrary.create("untitled", demoRoot,
                FunctionRepository.of(
                        MathFunctions.LIBRARY,
                        PythonLibrary.loadScript("corevector", "libraries/corevector/corevector.py"))));
    }

    public NodeBoxDocument(NodeLibrary nodeLibrary) {
        renderService = Executors.newFixedThreadPool(1);
        controller = NodeLibraryController.withLibrary(nodeLibrary);
        JPanel rootPanel = new JPanel(new BorderLayout());
        this.viewerPane = new ViewerPane(this);
        dataSheet = viewerPane.getDataSheet();
        PortPane portPane = new PortPane(this);
        portView = portPane.getPortView();
        NetworkPane networkPane = new NetworkPane(this);
        networkView = networkPane.getNetworkView();
        parameterNetworkSplit = new CustomSplitPane(JSplitPane.VERTICAL_SPLIT, portPane, networkPane);
        topSplit = new CustomSplitPane(JSplitPane.HORIZONTAL_SPLIT, viewerPane, parameterNetworkSplit);
        addressBar = new AddressBar();
        addressBar.setOnSegmentClickListener(new AddressBar.OnSegmentClickListener() {
            public void onSegmentClicked(String fullPath) {
                setActiveNetwork(fullPath);
            }
        });

        rootPanel.add(addressBar, BorderLayout.NORTH);
        rootPanel.add(topSplit, BorderLayout.CENTER);

        // Animation properties.
        animationTimer = new AnimationTimer(this);
        animationBar = new AnimationBar(this);
        rootPanel.add(animationBar, BorderLayout.SOUTH);

        setContentPane(rootPanel);
        setLocationByPlatform(true);
        setSize(1100, 800);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(this);
        updateTitle();
        menuBar = new NodeBoxMenuBar(this);
        setJMenuBar(menuBar);
        loaded = true;

        setActiveNetwork("/");
    }

    public NodeBoxDocument(File file) throws RuntimeException {
        this(NodeLibrary.load(file, NodeRepository.of()));
        lastFilePath = file.getParentFile().getAbsolutePath();
        setDocumentFile(file);
    }

    //// Node Library management ////

    public NodeLibrary getNodeLibrary() {
        return controller.getNodeLibrary();
    }

    /**
     * Restore the node library to a different undo state.
     *
     * @param nodeLibrary The node library to restore.
     * @param networkPath The active network path.
     * @param nodeName    The active node name. Can be an empty string.
     */
    public void restoreState(NodeLibrary nodeLibrary, String networkPath, String nodeName) {
        controller.setNodeLibrary(nodeLibrary);
        setActiveNetwork(networkPath);
        setActiveNode(nodeName);
    }

    //// Node operations ////

    /**
     * Create a node in the active network.
     * This node is based on a prototype.
     *
     * @param prototype The prototype node.
     * @param pt        The initial node position.
     */
    public void createNode(Node prototype, nodebox.graphics.Point pt) {
        startEdits("Create Node");
        Node newNode = controller.createNode(activeNetworkPath, prototype);
        String newNodePath = Node.path(activeNetworkPath, newNode);
        controller.setNodePosition(newNodePath, pt);
        controller.setRenderedChild(activeNetworkPath, newNode.getName());
        setActiveNode(newNode);
        stopEdits();

        networkView.updateNodes();
        networkView.singleSelect(newNode);
        portView.setActiveNode(newNode);
    }


    /**
     * Change the node position of the given node.
     *
     * @param node  The node to move.
     * @param point The point to move to.
     */
    public void setNodePosition(Node node, nodebox.graphics.Point point) {
        checkNotNull(node);
        checkNotNull(point);
        checkArgument(getActiveNetwork().hasChild(node));
        // Note that we're passing in the parent network of the node.
        // This means that all move changes to the parent network are grouped
        // together under one edit, instead of for each node individually.
        addEdit("Move Node", "moveNode", getActiveNetwork());
        String nodePath = Node.path(activeNetworkPath, node);
        controller.setNodePosition(nodePath, point);

        networkView.updatePosition(node);
    }

    /**
     * Change the node name.
     *
     * @param node The node to rename.
     * @param name The new node name.
     */
    public void setNodeName(Node node, String name) {
        checkNotNull(node);
        checkNotNull(name);
        controller.renameNode(Node.path(activeNetworkPath, node), name);

        networkView.updateNodes();
        // Renaming the node can have an effect on expressions, so recalculate the network.
        requestRender();
    }

    /**
     * Set the node metadata to the given metadata.
     * Note that this method is not called when the node position or name changes.
     *
     * @param node     The node to change.
     * @param metadata A map of metadata.
     */
    public void setNodeMetadata(Node node, Object metadata) {
        // TODO: Implement
        // TODO: Make NodeAttributesEditor use this.
        // Metadata changes could mean the icon has changed.
        networkView.updateNodes();
        if (node == getActiveNode()) {
            portView.updateAll();
            // Updating the metadata could cause changes to a handle.
            viewerPane.repaint();
            dataSheet.repaint();
        }
        requestRender();
    }

    /**
     * Change the rendered node to the given node
     *
     * @param node the node to set rendered
     */
    public void setRenderedNode(Node node) {
        checkNotNull(node);
        checkArgument(getActiveNetwork().hasChild(node));
        addEdit("Set Rendered");
        controller.setRenderedChild(activeNetworkPath, node.getName());

        networkView.updateNodes();
        networkView.singleSelect(node);
        requestRender();
    }

    public void setNodeExported(Node node, boolean exported) {
        throw new UnsupportedOperationException("Not implemented yet.");
        //addEdit("Set Exported");
    }

    /**
     * Remove the given node from the active network.
     *
     * @param node The node to remove.
     */
    public void removeNode(Node node) {
        addEdit("Remove Node");
        removeNodeImpl(node);
        networkView.updateAll();
        requestRender();
    }

    /**
     * Remove the given nodes from the active network.
     *
     * @param nodes The node to remove.
     */
    public void removeNodes(Iterable<Node> nodes) {
        addEdit("Delete Nodes");
        for (Node node : nodes) {
            removeNodeImpl(node);
        }
        networkView.updateAll();
        requestRender();
    }

    /**
     * Helper method used by removeNode and removeNodes to do the removal and update the port view, if needed.
     *
     * @param node The node to remove.
     */
    private void removeNodeImpl(Node node) {
        checkNotNull(node, "Node to remove cannot be null.");
        checkArgument(getActiveNetwork().hasChild(node), "Node to remove is not in active network.");
        controller.removeNode(activeNetworkPath, node.getName());
        // If the removed node was the active one, reset the port view.
        if (node == getActiveNode()) {
            setActiveNode((Node) null);
        }
    }

    /**
     * Create a connection from the given output to the given input.
     *
     * @param outputNode The output node.
     * @param outputPort The output port.
     * @param inputNode  The input node.
     * @param inputPort  The input port.
     */
    public void connect(Node outputNode, Port outputPort, Node inputNode, Port inputPort) {
        addEdit("Connect");
        controller.connect(activeNetworkPath, outputNode, outputPort, inputNode, inputPort);
        requestRender();
    }

    /**
     * Remove the given connection from the network.
     *
     * @param connection the connection to remove
     */
    public void disconnect(Connection connection) {
        addEdit("Disconnect");
        controller.disconnect(activeNetworkPath, connection);

        networkView.updateConnections();
        requestRender();
    }

    /**
     * @param node          the node on which to add the port
     * @param parameterName the name of the new port
     */
    public void addPort(Node node, String parameterName) {
        addEdit("Add Parameter");
        throw new UnsupportedOperationException("Not implemented yet.");
        // TODO Port port = Port.portForType();
//        if (node == getActiveNode()) {
//            portView.updateAll();
//            viewer.repaint();
//        }
    }

    /**
     * Remove the port from the node.
     *
     * @param node     The node on which to remove the port.
     * @param portName The name of the port
     */
    public void removePort(Node node, String portName) {
        checkArgument(getActiveNetwork().hasChild(node));
        addEdit("Remove Parameter");
        controller.removePort(Node.path(activeNetworkPath, node), portName);

        if (node == getActiveNode()) {
            portView.updateAll();
            viewerPane.repaint();
            dataSheet.repaint();
        }
    }

    /**
     * Set the port of the active node to the given value.
     *
     * @param portName The name of the port on the active node.
     * @param value    The new value.
     */
    public void setPortValue(String portName, Object value) {
        checkNotNull(portName, "Port cannot be null.");
        Port port = getActiveNode().getInput(portName);
        checkArgument(port != null, "Port %s does not exist on node %s", portName, getActiveNode());
        addEdit("Change Value", "changeValue", port);
        controller.setPortValue(getActiveNodePath(), portName, value);

        // TODO set variables on the root port.
//        if (port.getNode() == nodeLibrary.getRoot()) {
//            nodeLibrary.setVariable(port.getName(), port.asString());
//        }

        portView.updatePortValue(port, value);
        // Setting a port might change enable expressions, and thus change the enabled state of a port row.
        portView.updateEnabledState();
        // Setting a port might change the enabled state of the handle.
        // viewer.setHandleEnabled(activeNode != null && activeNode.hasEnabledHandle());
        requestRender();
    }

    public void revertPortToDefault(Port port) {
        addEdit("Revert Parameter to Default");
        throw new UnsupportedOperationException("Not implemented yet.");

        //portView.updatePort(parameter);
        //renderNetwork();
    }

    public void setPortMetadata(Port port, String key, String value) {
        addEdit("Change Port Metadata");
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    //// Port pane callbacks ////

    public void editMetadata() {
        if (getActiveNode() == null) return;
//                JDialog editorDialog = new NodeAttributesDialog(NodeBoxDocument.this);
//                editorDialog.setSize(580, 751);
//                editorDialog.setLocationRelativeTo(NodeBoxDocument.this);
//                editorDialog.setVisible(true);
    }

    //// HandleDelegate implementation ////

    // TODO Merge setPortValue and setValue.
    public void setValue(Node node, String portName, Object value) {
        checkNotNull(node, "Node cannot be null");
        Port port = node.getInput(portName);
        checkNotNull(port, "Port '" + portName + "' is not a port on node " + node);
        setPortValue(portName, value);
    }

    public void silentSet(Node node, String portName, Object value) {
        try {
            Port port = node.getInput(portName);
            setPortValue(portName, value);
        } catch (Exception ignored) {
        }
    }

    // TODO Merge stopEditing and stopCombiningEdits.
    public void stopEditing(Node node) {
        stopCombiningEdits();
    }

    public void updateHandle(Node node) {
        if (viewerPane.getHandle() != null)
            viewerPane.getHandle().update();
        // TODO Make viewer repaint more fine-grained.
        viewerPane.repaint();
    }

    //// Active network / node ////

    /**
     * Return the network that is currently "open": shown in the network view.
     *
     * @return The currently active network.
     */
    public Node getActiveNetwork() {
        // TODO This might be a potential bottleneck.
        return getNodeLibrary().getNodeForPath(activeNetworkPath);
    }

    public String getActiveNetworkPath() {
        return activeNetworkPath;
    }

    public void setActiveNetwork(String path) {
        checkNotNull(path);
        activeNetworkPath = path;
        Node network = getNodeLibrary().getNodeForPath(path);

        if (network.getRenderedChild() != null) {
            setActiveNode(network.getRenderedChildName());
        } else if (!network.isEmpty()) {
            // Set the active node to the first child.
            setActiveNode(network.getChildren().iterator().next());
        } else {
            setActiveNode((Node) null);
        }

        addressBar.setPath(activeNetworkPath);
        //viewer.setHandleEnabled(activeNode != null && activeNode.hasEnabledHandle());
        networkView.updateNodes();
        viewerPane.repaint();
        dataSheet.repaint();
        requestRender();
    }

    /**
     * Set the active network to the parent network.
     */
    public void goUp() {
        throw new UnsupportedOperationException("Not implemented yet.");
    }


    /**
     * Return the node that is currently focused:
     * visible in the port view, and whose handles are displayed in the viewer.
     *
     * @return The active node. Can be null.
     */
    public Node getActiveNode() {
        if (activeNodeName.isEmpty()) {
            return getActiveNetwork();
        } else {
            return getNodeLibrary().getNodeForPath(getActiveNodePath());
        }
    }

    public String getActiveNodePath() {
        return Node.path(activeNetworkPath, activeNodeName);
    }

    public String getActiveNodeName() {
        return activeNodeName;
    }

    /**
     * Set the active node to the given node.
     * <p/>
     * The active node is the one whose parameters are displayed in the port pane,
     * and whose handle is displayed in the viewer.
     * <p/>
     * This will also change the active network if necessary.
     *
     * @param node the node to change to.
     */
    public void setActiveNode(Node node) {
        setActiveNode(node != null ? node.getName() : "");
    }

    public void setActiveNode(String nodeName) {
        if (getActiveNodeName().equals(nodeName)) return;
        stopCombiningEdits();
        if (nodeName.isEmpty()) {
            activeNodeName = "";
        } else {
            checkArgument(getActiveNetwork().hasChild(nodeName));
            activeNodeName = nodeName;
        }

        Node n = getActiveNode();
        //createHandleForActiveNode();
        //editorPane.setActiveNode(activeNode);
        viewerPane.repaint(); // For the handle
        portView.setActiveNode(n == null ? getActiveNetwork() : n);
        networkView.singleSelect(n);
    }

//    private void createHandleForActiveNode() {
//        if (activeNode != null) {
//            Handle handle = null;
//            try {
//                handle = activeNode.createHandle();
//                // If the handle was created successfully, remove the messages.
//                editorPane.clearMessages();
//            } catch (Exception e) {
//                editorPane.setMessages(e.toString());
//            }
//            if (handle != null) {
//                handle.setHandleDelegate(this);
//                // TODO Remove this. Find out why the handle needs access to the viewer (only repaint?) and put that in the HandleDelegate.
//                handle.setViewer(viewer);
//                viewer.setHandleEnabled(activeNode.hasEnabledHandle());
//            }
//            viewer.setHandle(handle);
//        } else {
//            viewer.setHandle(null);
//        }
//    }

    //// Animation ////

    public double getFrame() {
        return frame;
    }

    public void setFrame(double frame) {
        this.frame = frame;

        animationBar.setFrame(frame);
        requestRender();
    }

    public void nextFrame() {
        setFrame(getFrame() + 1);
    }

    public void playAnimation() {
        animationTimer.start();
    }

    public void stopAnimation() {
        animationTimer.stop();
    }

    public void rewindAnimation() {
        stopAnimation();
        setFrame(1);
    }

    //// Rendering ////

    /**
     * Request a renderNetwork operation.
     * <p/>
     * This method does a number of checks to see if the renderNetwork goes through.
     * <p/>
     * The renderer could already be running.
     * <p/>
     * If all checks pass, a renderNetwork request is made.
     */
    public void requestRender() {
        // If we're already rendering, request the next renderNetwork.
        if (isRendering.get()) {
            shouldRender.set(true);
        } else {
            // If we're not rendering, start rendering.
            render();
        }
    }


    /**
     * Called when the active network will start rendering.
     * Called on the Swing EDT so you can update the GUI.
     *
     * @param context The node context.
     */
    public void startRendering(final NodeContext context) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                addressBar.setProgressVisible(true);
            }
        });
    }

    /**
     * Called when the active network has finished rendering.
     * Called on the Swing EDT so you can update the GUI.
     *
     * @param context         The node context.
     * @param renderedNetwork The network that was rendered.
     */
    public void finishedRendering(final NodeContext context, final Node renderedNetwork) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Node renderedChild = renderedNetwork.getRenderedChild();
                Port firstOutputPort = renderedChild.getOutputs().iterator().next();
                java.util.List<Object> results = context.getResults(renderedChild, firstOutputPort);
                addressBar.setProgressVisible(false);
                viewerPane.setOutputValues(results);
                networkView.checkErrorAndRepaint();
            }
        });
    }

    /**
     * Returns the first output value, or null if the map of output values is empty.
     *
     * @param outputValues The map of output values.
     * @return The output value.
     */
    private Object firstOutputValue(final Map<String, Object> outputValues) {
        if (outputValues.isEmpty()) return null;
        return outputValues.values().iterator().next();
    }

    private void render() {
        // If we're already rendering, return.
        if (isRendering.get()) return;

        // Before starting the renderNetwork, turn the "should render" flag off and the "is rendering" flag on.
        synchronized (shouldRender) {
            synchronized (isRendering) {
                shouldRender.set(false);
                isRendering.set(true);
            }
        }

        final NodeLibrary renderLibrary = getNodeLibrary();
        final Node renderNetwork = getActiveNetwork();
        renderService.submit(new Runnable() {
            public void run() {
                final NodeContext context = new NodeContext(renderLibrary.getFunctionRepository(), frame);
                startRendering(context);
                try {
                    context.renderNetwork(renderNetwork);
                } catch (NodeRenderException e) {
                    LOG.log(Level.WARNING, "Error while processing", e);
                } catch (Exception e) {
                    LOG.log(Level.WARNING, "Other error while processing", e);
                }

                // We finished rendering so set the renderNetwork flag off.
                isRendering.set(false);

                finishedRendering(context, renderNetwork);

                // If, in the meantime, we got a new renderNetwork request, call the renderNetwork method again.
                if (shouldRender.get()) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            render();
                        }
                    });
                }
            }
        });
    }

//    public void setActiveNodeCode(String source) {
//        if (activeNode == null) return;
//        NodeCode code = new PythonCode(source);
//        codeParameter.set(code);
//        if (codeParameter.getName().equals("_handle")) {
//            //createHandleForActiveNode();
//        }
//        networkView.codeChanged(activeNode, false);
//        renderNetwork();
//    }

    //// Undo ////

    /**
     * Edits are no longer recorded until you call stopEdits. This allows you to batch edits.
     *
     * @param command the command name of the edit batch
     */
    public void startEdits(String command) {
        addEdit(command);
        holdEdits = true;
    }

    /**
     * Edits are recorded again.
     */
    public void stopEdits() {
        holdEdits = false;
    }

    /**
     * Add an edit to the undo manager.
     * <p/>
     * Since we don't specify the edit type or name, further edits will not be staggered.
     *
     * @param command the command name.
     */
    public void addEdit(String command) {
        if (!holdEdits) {
            markChanged();
            undoManager.addEdit(new NodeLibraryUndoableEdit(this, command));
            menuBar.updateUndoRedoState();
            stopCombiningEdits();
        }
    }

    /**
     * Add an edit to the undo manager.
     *
     * @param command the command name.
     * @param type    the type of edit
     * @param object  the edited object. This will be compared using ==.
     */
    public void addEdit(String command, String type, Object object) {
        if (!holdEdits) {
            markChanged();
            if (lastEditType != null && lastEditType.equals(type) && lastEditObject == object) {
                // If the last edit type and last edit id are the same,
                // we combine the two edits into one.
                // Since we've already saved the last state, we don't need to do anything.
            } else {
                addEdit(command);
                lastEditType = type;
                lastEditObject = object;
            }
        }
    }

    /**
     * Normally edits of the same type and object are combined into one.
     * Calling this method will ensure that you create a  new edit.
     * <p/>
     * Use this method e.g. for breaking apart overzealous edit grouping.
     */
    public void stopCombiningEdits() {
        // We just reset the last edit type and object so that addEdit will be forced to create a new edit.
        lastEditType = null;
        lastEditObject = null;

    }

    public UndoManager getUndoManager() {
        return undoManager;
    }

    public void undo() {
        if (!undoManager.canUndo()) return;
        undoManager.undo();
        menuBar.updateUndoRedoState();
    }

    public void redo() {
        if (!undoManager.canRedo()) return;
        undoManager.redo();
        menuBar.updateUndoRedoState();
    }

    //// Code editor actions ////

    public void fireCodeChanged(Node node, boolean changed) {
        networkView.codeChanged(node, changed);
    }

    //// Document actions ////

    public File getDocumentFile() {
        return documentFile;
    }

    public void setDocumentFile(File documentFile) {
        this.documentFile = documentFile;
        updateTitle();
    }

    public boolean isChanged() {
        return documentChanged;
    }

    public boolean close() {
        stopAnimation();
        if (shouldClose()) {
            Application.getInstance().removeDocument(this);
            dispose();
            // On Mac the application does not close if the last window is closed.
            if (!Platform.onMac()) {
                // If there are no more documents, exit the application.
                if (Application.getInstance().getDocumentCount() == 0) {
                    System.exit(0);
                }
            }
            return true;
        } else {
            return false;
        }
    }

    private boolean shouldClose() {
        if (isChanged()) {
            SaveDialog sd = new SaveDialog();
            int retVal = sd.show(this);
            if (retVal == JOptionPane.YES_OPTION) {
                return save();
            } else if (retVal == JOptionPane.NO_OPTION) {
                return true;
            } else if (retVal == JOptionPane.CANCEL_OPTION) {
                return false;
            }
        }
        return true;
    }

    public boolean save() {
        if (documentFile == null) {
            return saveAs();
        } else {
            return saveToFile(documentFile);
        }
    }

    public boolean saveAs() {
        File chosenFile = FileUtils.showSaveDialog(this, lastFilePath, "ndbx", "NodeBox File");
        if (chosenFile != null) {
            if (!chosenFile.getAbsolutePath().endsWith(".ndbx")) {
                chosenFile = new File(chosenFile.getAbsolutePath() + ".ndbx");
            }
            lastFilePath = chosenFile.getParentFile().getAbsolutePath();
            setDocumentFile(chosenFile);
            NodeBoxMenuBar.addRecentFile(documentFile);
            return saveToFile(documentFile);
        }
        return false;
    }

    public void revert() {
        // TODO: Implement revert
        JOptionPane.showMessageDialog(this, "Revert is not implemented yet.", "NodeBox", JOptionPane.ERROR_MESSAGE);
    }

    private boolean saveToFile(File file) {
        try {
            getNodeLibrary().store(file);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "An error occurred while saving the file.", "NodeBox", JOptionPane.ERROR_MESSAGE);
            LOG.log(Level.SEVERE, "An error occurred while saving the file.", e);
            return false;
        }
        documentChanged = false;
        updateTitle();
        return true;
    }

    private void markChanged() {
        if (!documentChanged && loaded) {
            documentChanged = true;
            updateTitle();
            getRootPane().putClientProperty(WINDOW_MODIFIED, Boolean.TRUE);
        }
    }

    private void updateTitle() {
        String postfix = "";
        if (!Platform.onMac()) {
            postfix = (documentChanged ? " *" : "");
        } else {
            getRootPane().putClientProperty("Window.documentModified", documentChanged);
        }
        if (documentFile == null) {
            setTitle("Untitled" + postfix);
        } else {
            setTitle(documentFile.getName() + postfix);
            getRootPane().putClientProperty("Window.documentFile", documentFile);
        }
    }

    //// Export ////

    public void doExport() {
        File chosenFile = FileUtils.showSaveDialog(this, lastExportPath, "pdf", "PDF file");
        if (chosenFile == null) return;
        lastExportPath = chosenFile.getParentFile().getAbsolutePath();
        exportToFile(chosenFile, ImageFormat.PDF);
    }

    private boolean exportToFile(File file, ImageFormat format) {
        return exportToFile(file, getActiveNetwork(), format);
    }

    private boolean exportToFile(File file, Object outputValue, ImageFormat format) {
        file = format.ensureFileExtension(file);
        if (outputValue instanceof nodebox.graphics.Canvas) {
            nodebox.graphics.Canvas c = (nodebox.graphics.Canvas) outputValue;
            c.save(file);
            return true;
        } else {
            throw new RuntimeException("This type of output cannot be exported " + outputValue);
        }
    }

    public boolean exportRange() {
        File exportDirectory = lastExportPath == null ? null : new File(lastExportPath);
        if (exportDirectory != null && !exportDirectory.exists())
            exportDirectory = null;
        ExportRangeDialog d = new ExportRangeDialog(this, exportDirectory);
        d.setLocationRelativeTo(this);
        d.setVisible(true);
        if (!d.isDialogSuccessful()) return false;
        String exportPrefix = d.getExportPrefix();
        File directory = d.getExportDirectory();
        int fromValue = d.getFromValue();
        int toValue = d.getToValue();
        ImageFormat format = d.getFormat();
        if (directory == null) return false;
        lastExportPath = directory.getAbsolutePath();
        exportRange(exportPrefix, directory, fromValue, toValue, format);
        return true;
    }

    public void exportRange(final String exportPrefix, final File directory, final int fromValue, final int toValue, final ImageFormat format) {
        exportThreadedRange(getNodeLibrary(), fromValue, toValue, new ExportDelegate() {
            @Override
            public void frameDone(double frame, Object outputValue) {
                File exportFile = new File(directory, exportPrefix + "-" + frame);
                exportToFile(exportFile, outputValue, format);
            }
        });
    }

    public boolean exportMovie() {
        ExportMovieDialog d = new ExportMovieDialog(this, lastExportPath == null ? null : new File(lastExportPath));
        d.setLocationRelativeTo(this);
        d.setVisible(true);
        if (!d.isDialogSuccessful()) return false;
        File chosenFile = d.getExportPath();
        if (chosenFile != null) {
            lastExportPath = chosenFile.getParentFile().getAbsolutePath();
            exportToMovieFile(chosenFile, d.getVideoFormat(), d.getFromValue(), d.getToValue());
            return true;
        }
        return false;
    }

    private void exportToMovieFile(File file, final VideoFormat videoFormat, final int fromValue, final int toValue) {
        file = videoFormat.ensureFileExtension(file);
        final long width = getNodeLibrary().getRoot().getInput("width").intValue();
        final long height = getNodeLibrary().getRoot().getInput("height").intValue();
        final Movie movie = new Movie(file.getAbsolutePath(), videoFormat, (int) width, (int) height, false);
        exportThreadedRange(controller.getNodeLibrary(), fromValue, toValue, new ExportDelegate() {
            @Override
            public void frameDone(double frame, Object outputValue) {
                if (outputValue instanceof nodebox.graphics.Canvas) {
                    nodebox.graphics.Canvas c = (nodebox.graphics.Canvas) outputValue;
                    movie.addFrame(c.asImage());
                }
            }

            @Override
            void exportDone() {
                progressDialog.setTitle("Converting frames to movie...");
                progressDialog.reset();
                FramesWriter w = new FramesWriter(progressDialog);
                movie.save(w);
            }
        });
    }

    private abstract class ExportDelegate {
        protected InterruptibleProgressDialog progressDialog;

        void frameDone(double frame, Object outputValue) {
        }

        void exportDone() {
        }
    }

    private void exportThreadedRange(final NodeLibrary library, final int fromValue, final int toValue, final ExportDelegate exportDelegate) {
        int frameCount = toValue - fromValue;
        final InterruptibleProgressDialog d = new InterruptibleProgressDialog(this, "Exporting " + frameCount + " frames...");
        d.setTaskCount(toValue - fromValue + 1);
        d.setVisible(true);
        exportDelegate.progressDialog = d;

        final NodeLibrary exportLibrary = getNodeLibrary();
        final Node exportNetwork = library.getRoot();
        final ExportViewer viewer = new ExportViewer();
        Thread t = new Thread(new Runnable() {
            public void run() {
                try {
                    for (int frame = fromValue; frame <= toValue; frame++) {
                        if (Thread.currentThread().isInterrupted())
                            break;

                        NodeContext context = new NodeContext(exportLibrary.getFunctionRepository(), frame);
                        context.renderNetwork(exportNetwork);
                        Node renderedChild = exportNetwork.getRenderedChild();
                        Port firstOutputPort = renderedChild.getOutputs().iterator().next();
                        Object result = context.getResults(renderedChild, firstOutputPort);
                        viewer.setOutputValue(result);
                        exportDelegate.frameDone(frame, result);

                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                d.tick();
                            }
                        });
                    }
                    exportDelegate.exportDone();
                } catch (Exception e) {
                    LOG.log(Level.WARNING, "Error while exporting", e);
                } finally {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            d.setVisible(false);
                            viewer.setVisible(false);
                        }
                    });
                }
            }
        });
        d.setThread(t);
        t.start();
        viewer.setVisible(true);
    }

    //// Copy / Paste ////

    private class NodeClipboard {
        private final Node network;
        private final ImmutableList<Node> nodes;

        private NodeClipboard(Node network, Iterable<Node> nodes) {
            this.network = network;
            this.nodes = ImmutableList.copyOf(nodes);
        }
    }

    public void cut() {
        copy();
        deleteSelection();
    }

    public void copy() {
        // When copying, save a reference to the nodes and the parent network.
        // Since the model is immutable, we don't need to make defensive copies.
        nodeClipboard = new NodeClipboard(getActiveNetwork(), networkView.getSelectedNodes());
    }

    public void paste() {
        addEdit("Paste node");
        if (nodeClipboard == null) return;

        java.util.List<Node> newNodes = new ArrayList<Node>(nodeClipboard.nodes.size());
        for (Node node : nodeClipboard.nodes) {
            Node newNode = node.withPosition(node.getPosition().moved(20, 80));
            controller.addNode(activeNetworkPath, newNode);
            newNodes.add(newNode);
        }

        networkView.updateAll();
        networkView.select(newNodes);
    }

    public void deleteSelection() {
        removeNodes(networkView.getSelectedNodes());
    }

    /**
     * Start the dialog that allows a user to create a new node.
     */
    public void showNodeSelectionDialog() {
//        NodeSelectionDialog dialog = new NodeSelectionDialog(this, doc.getNodeLibrary(), doc.getManager());
//        Point pt = getMousePosition();
//        if (pt == null) {
//            pt = new Point((int) (Math.random() * 300), (int) (Math.random() * 300));
//        }
//        pt = (Point) getCamera().localToView(pt);
//        dialog.setVisible(true);
//        if (dialog.getSelectedNode() != null) {
//            doc.createNode(dialog.getSelectedNode(), pt);
//        }
    }

    public void reload() {
        throw new UnsupportedOperationException();
        //editorPane.reload();
    }

    //// Window events ////

    public void windowOpened(WindowEvent e) {
        //viewEditorSplit.setDividerLocation(0.5);
        parameterNetworkSplit.setDividerLocation(0.5);
        topSplit.setDividerLocation(0.5);
    }

    public void windowClosing(WindowEvent e) {
        close();
    }

    public void windowClosed(WindowEvent e) {
    }

    public void windowIconified(WindowEvent e) {
    }

    public void windowDeiconified(WindowEvent e) {
    }

    public void windowActivated(WindowEvent e) {
        Application.getInstance().setCurrentDocument(this);
    }

    public void windowDeactivated(WindowEvent e) {
    }

    private class FramesWriter extends StringWriter {
        private final ProgressDialog dialog;

        public FramesWriter(ProgressDialog d) {
            super();
            dialog = d;
        }

        @Override
        public void write(String s, int n1, int n2) {
            super.write(s, n1, n2);
            if (s.startsWith("frame=")) {
                int frame = Integer.parseInt(s.substring(6, s.indexOf("fps")).trim());
                dialog.updateProgress(frame);
            }
        }
    }
}
