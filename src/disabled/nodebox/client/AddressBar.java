package nodebox.client;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import org.python.google.common.base.Joiner;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;

public class AddressBar extends JPanel implements MouseListener {

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

    private ImmutableList<String> parts = ImmutableList.of();
    private int[] positions;
    private int armed = -1;
    private OnPartClickListener onPartClickListener;
    private JProgressBar progressBar;

    public AddressBar() {
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
    }

    public ImmutableList<String> getParts() {
        return parts;
    }

    public void setParts(Iterable<String> parts) {
        this.parts = ImmutableList.copyOf(parts);
        repaint();
    }

    public void setPath(String path) {
        setParts(Splitter.on("/").split(path));
    }

    /**
     * Returns the part-click callback registered for this address bar.
     *
     * @return The callback, or null if one is not registered.
     */
    public OnPartClickListener getOnPartClickListener() {
        return onPartClickListener;
    }

    /**
     * Register a callback to be invoked when a part was clicked in the address bar.
     *
     * @param l the callback that will run.
     */
    public void setOnPartClickListener(OnPartClickListener l) {
        onPartClickListener = l;
    }

    public boolean getProgressVisible() {
        return progressBar.isVisible();
    }

    public void setProgressVisible(boolean visible) {
        progressBar.setVisible(visible);
    }

    @Override
    protected void paintComponent(Graphics g) {
        positions = new int[parts.size()];
        Graphics2D g2 = (Graphics2D) g;

        g2.setFont(Theme.SMALL_BOLD_FONT);

        g2.drawImage(addressGradient, 0, 0, getWidth(), 25, null);

        int x = 10;

        int i = 0;
        for (String part : parts) {
            if (i == armed) {
                g2.setColor(Theme.TEXT_ARMED_COLOR);
            } else {
                g2.setColor(Theme.TEXT_NORMAL_COLOR);
            }
            SwingUtils.drawShadowText(g2, part, x, 16);

            int width = g2.getFontMetrics().stringWidth(part);
            x += width + 5;
            positions[i] = x + 10;
            g2.drawImage(addressArrow, x, 1, null);
            x += 15;
            i++;
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
        String selectedPart = parts.get(partIndex);
        if (selectedPart != null && onPartClickListener != null)
            onPartClickListener.onPartClicked(pathForIndex(partIndex));
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

    private String pathForIndex(int endIndex) {
        return Joiner.on("/").join(parts.subList(0, endIndex));
    }

    @Override
    public void doLayout() {
        final int width = getWidth();
        progressBar.setBounds(width - 23, 3, 20, 20);
    }

    /**
     * Callback listener to be invoked when an address part has been clicked.
     */
    public static interface OnPartClickListener {

        /**
         * Called when a part has been clicked.
         *
         * @param fullPath The full path of the part that was clicked.
         */
        public void onPartClicked(String fullPath);

    }

}
