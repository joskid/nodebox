/*
 * This file is part of NodeBox.
 *
 * Copyright (C) 2008 Frederik De Bleser (frederik@pandora.be)
 *
 * NodeBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NodeBox is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with NodeBox. If not, see <http://www.gnu.org/licenses/>.
 */
package nodebox.graphics;

public class CanvasContext extends AbstractGraphicsContext {

    private Canvas canvas;

    //// Initialization ////

    public CanvasContext() {
        canvas = new Canvas();
        resetContext(true);
    }

    public CanvasContext(Canvas canvas) {
        this.canvas = canvas;
        resetContext(false);
    }

    @Override
    public void resetContext() {
        resetContext(true);
    }

    public void resetContext(boolean resetBackground) {
        super.resetContext();
        if (resetBackground)
            canvas.setBackground(new Color(1, 1, 1));
    }

    //// Setup methods ////

    public void size(float width, float height) {
        canvas.setWidth(width);
        canvas.setHeight(height);
    }

    public float getWidth() {
        return canvas.getWidth();
    }

    public float getHeight() {
        return canvas.getHeight();
    }

    public float getWIDTH() {
        return canvas.getWidth();
    }

    public float getHEIGHT() {
        return canvas.getHeight();
    }

    /**
     * Get the current background color.
     *
     * @return the current background color.
     */
    public Color background() {
        return canvas.getBackground();
    }

    /**
     * Set the current background color to given grayscale value.
     *
     * @param x the gray component.
     * @return the current background color.
     */
    public Color background(float x) {
        return canvas.setBackground(new Color(x, x, x));
    }

    /**
     * Set the current background color to given grayscale and alpha value.
     *
     * @param x the grayscale value.
     * @param y the alpha value.
     * @return the current background color.
     */
    public Color background(float x, float y) {
        return canvas.setBackground(new Color(x, x, x, y));
    }

    /**
     * Set the current background color to the given R/G/B value.
     *
     * @param x the red component.
     * @param y the green component.
     * @param z the blue component.
     * @return the current background color.
     */
    public Color background(float x, float y, float z) {
        return canvas.setBackground(new Color(x, y, z, colormode()));
    }

    /**
     * Set the current background color to the given R/G/B/A value.
     *
     * @param x the red component.
     * @param y the green component.
     * @param z the blue component.
     * @param a the alpha component.
     * @return the current background color.
     */
    public Color background(float x, float y, float z, float a) {
        return canvas.setBackground(new Color(x, y, z, a, colormode()));
    }

    /**
     * Set the current background color to the given color.
     * <p/>
     * The color object is cloned; you can change the original afterwards.
     * If the color object is null, the current background color is turned off (same as nobackground).
     *
     * @param c the color object.
     * @return the current background color.
     */
    public Color background(Color c) {
        return canvas.setBackground(c == null ? null : c.clone());
    }

    public void nobackground() {
        canvas.setBackground(null);
    }

    //// Attribute access ////

    public Canvas getCanvas() {
        return canvas;
    }

    //// Font commands ////

    public Text text(String text, float x, float y) {
        return text(text, x, y, 0, 0);
    }

    public Text text(String text, float x, float y, float width) {
        return text(text, x, y, width, 0);
    }

    public Text text(String text, float x, float y, float width, float height) {
        Text t = new Text(text, x, y, width, height);
        inheritFromContext(t);
        canvas.add(t);
        return t;
    }

    //// Image methods ////

    public Image image(String path, float x, float y) {
        Image img = new Image(path);
        img.setX(x);
        img.setY(y);
        inheritFromContext(img);
        canvas.add(img);
        return img;
    }

    public Size imagesize(String path) {
        Image img = new Image(path);
        return img.getSize();
    }

    /// Drawing methods ////

    /**
     * The draw method doesn't actually draw anything, but rather appends grobs to the canvas.
     * When the canvas gets drawn, this grob will be drawn also.
     *
     * @param grob the grob to append to the canvas
     */
    public void draw(Grob grob) {
        canvas.add(grob);
    }

    @Override
    protected void addPath(Path p) {
        canvas.add(p);
    }

    @Override
    protected void addText(Text t) {
        canvas.add(t);
    }

    //// Context inheritance ////

    private void inheritFromContext(Image i) {
        i.setTransform(transform.clone());
    }

}
