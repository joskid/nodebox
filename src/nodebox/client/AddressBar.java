package nodebox.client;

import nodebox.node.Macro;
import nodebox.node.Node;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class AddressBar extends JPanel implements MouseListener, DocumentFocusListener {

    public static Image addressGradient;
    public static Image addressArrow;

    static {
        try {
            addressGradient = ImageIO.read(new File("res/address-gradient.png"));
            addressArrow = ImageIO.read(new File("res/address-arrow.png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //private ArrayList<Node> parts = new List<Node>[]{"root", "poster", "background"};
    private int[] positions;
    private int armed = -1;

    private NodeBoxDocument document;
    private Macro macro;
    private JProgressBar progressBar;

    public AddressBar(NodeBoxDocument document) {
        this.document = document;
        addMouseListener(this);
        setMinimumSize(new Dimension(0, 25));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        setPreferredSize(new Dimension(Integer.MAX_VALUE, 25));
        setLayout(null);
        progressBar = new JProgressBar();
        progressBar.putClientProperty("JProgressBar.style", "circular");
        progressBar.setIndeterminate(true);
        progressBar.setBorderPainted(false);
        progressBar.setVisible(false);
        add(progressBar);
        currentMacroChanged(document.getActiveMacro());
    }

    public void currentMacroChanged(Macro macro) {
        this.macro = macro;
        repaint();
    }

    public void focusedNodeChanged(Node node) {
    }

    public boolean getProgressVisible() {
        return progressBar.isVisible();
    }

    public void setProgressVisible(boolean visible) {
        progressBar.setVisible(visible);
    }

    private java.util.List<Macro> getParents() {
        ArrayList<Macro> parts = new ArrayList<Macro>();
        if (macro == null) return parts;
        Macro currentMacro = macro;
        parts.add(0, currentMacro);
        while (currentMacro.getParent() != null) {
            parts.add(0, currentMacro.getParent());
            currentMacro = currentMacro.getParent();
        }
        return parts;
    }

    @Override
    protected void paintComponent(Graphics g) {
        java.util.List<Macro> parents = getParents();
        positions = new int[parents.size()];
        Graphics2D g2 = (Graphics2D) g;

        g2.setFont(Theme.SMALL_BOLD_FONT);

        g2.drawImage(addressGradient, 0, 0, getWidth(), 25, null);

        int x = 14;

        for (int i = 0; i < parents.size(); i++) {
            Node part = parents.get(i);
            if (i == armed) {
                g2.setColor(Theme.TEXT_ARMED_COLOR);
            } else {
                g2.setColor(Theme.TEXT_NORMAL_COLOR);
            }
            SwingUtils.drawShadowText(g2, part.getName(), x, 16);

            int width = (int) g2.getFontMetrics().stringWidth(part.getName());
            x += width + 5;
            positions[i] = x + 10;
            g2.drawImage(addressArrow, x, 1, null);
            x += 15;
        }
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
        int mx = e.getX();
        armed = partIndex(mx);
        repaint();
    }

    public void mouseReleased(MouseEvent e) {
        armed = -1;
        int mx = e.getX();
        int partIndex = partIndex(mx);
        if (partIndex == -1) return;
        java.util.List<Macro> nodes = getParents();
        Macro selectedMacro = nodes.get(partIndex);
        //System.out.println("part = " + selectedMacro);
        if (selectedMacro != null)
            document.setActiveMacro(selectedMacro);
        repaint();
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
        armed = -1;
        repaint();
    }


    private int partIndex(int x) {
        if (positions == null) return -1;
        for (int i = 0; i < positions.length; i++) {
            if (x < positions[i])
                return i;
        }
        return -1;
    }

    @Override
    public void doLayout() {
        final int width = getWidth();
        progressBar.setBounds(width - 23, 3, 20, 20);
    }
}
