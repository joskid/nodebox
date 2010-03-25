package nodebox.graphics;

import nodebox.node.Parameter;

import java.util.Iterator;
import java.util.List;

public interface GraphicsContext {

    public enum RectMode {
        CORNER, CORNERS, CENTER, RADIUS
    }

    public enum EllipseMode {
        CENTER, RADIUS, CORNER, CORNERS
    }

    public static enum VarType {
        NUMBER(Parameter.Type.FLOAT, Parameter.Widget.FLOAT),
        TEXT(Parameter.Type.STRING, Parameter.Widget.TEXT),
        BOOLEAN(Parameter.Type.INT, Parameter.Widget.TOGGLE),
        FONT(Parameter.Type.STRING, Parameter.Widget.FONT);

        public final Parameter.Type type;
        public final Parameter.Widget widget;

        VarType(Parameter.Type type, Parameter.Widget widget) {
            this.type = type;
            this.widget = widget;
        }
    }

    public enum ArrowType { NORMAL, FORTYFIVE }

    public static Text.Align LEFT = Text.Align.LEFT;
    public static Text.Align RIGHT = Text.Align.RIGHT;
    public static Text.Align CENTER = Text.Align.CENTER;
    public static Text.Align JUSTIFY = Text.Align.JUSTIFY;

    public static Color.Mode RGB = Color.Mode.RGB;
    public static Color.Mode HSB = Color.Mode.HSB;
    public static Color.Mode CMYK = Color.Mode.CMYK;

    public static VarType NUMBER = VarType.NUMBER;
    public static VarType TEXT = VarType.TEXT;
    public static VarType BOOLEAN = VarType.BOOLEAN;
    public static VarType FONT = VarType.FONT;

    public static ArrowType NORMAL = ArrowType.NORMAL;
    public static ArrowType FORTYFIVE = ArrowType.FORTYFIVE;

    public RectMode rectmode();

    public RectMode rectmode(RectMode m);

    public Path rect(Rect r);

    public Path rect(float x, float y, float width, float height);

    public Path rect(Rect r, float roundness);

    public Path rect(float x, float y, float width, float height, float roundness);

    public Path rect(float x, float y, float width, float height, float rx, float ry);

    public EllipseMode ellipsemode();

    public EllipseMode ellipsemode(EllipseMode m);

    public Path oval(float x, float y, float width, float height);

    public Path oval(float x, float y, float width, float height, boolean draw);

    public Path ellipse(float x, float y, float width, float height);

    public Path ellipse(float x, float y, float width, float height, boolean draw);

    public Path line(float x1, float y1, float x2, float y2);

    public Path line(float x1, float y1, float x2, float y2, boolean draw);

    public Path star(float cx, float cy);

    public Path star(float cx, float cy, int points);

    public Path star(float cx, float cy, int points, float outer);

    public Path star(float cx, float cy, int points, float outer, float inner);

    public Path star(float cx, float cy, int points, float outer, float inner, boolean draw);

    public Path arrow(float x, float y);

    public Path arrow(float x, float y, ArrowType type);

    public Path arrow(float x, float y, float width);

    public Path arrow(float x, float y, float width, boolean draw);

    public Path arrow(float x, float y, float width, ArrowType type);

    public Path arrow(float x, float y, float width, ArrowType type, boolean draw);

    public void beginpath();

    public void beginpath(float x, float y);

    public void moveto(float x, float y);

    public void lineto(float x, float y);

    public void curveto(float x1, float y1, float x2, float y2, float x3, float y3);

    public void closepath();

    public Path endpath();

    public Path endpath(boolean draw);

    public void drawpath(Path path);

    public void drawpath(Iterable<Point> points);

    public boolean autoclosepath();

    public boolean autoclosepath(boolean c);

    public Path findpath(List<Point> points);

    public Path findpath(List<Point> points, float curvature);

    public void beginclip(Path p);

    public void endclip();

    public Transform.Mode transform();

    public Transform.Mode transform(Transform.Mode mode);


    public void push();

    public void pop();

    public void reset();

    public void translate(float tx, float ty);

    public void rotate(float r);

    public void scale(float scale);

    public void scale(float sx, float sy);

    public void skew(float skew);

    public void skew(float kx, float ky);

    public String outputmode();

    public String outputmode(String mode);

    public Color.Mode colormode();

    public Color.Mode colormode(Color.Mode mode);

    /**
     * Create an empty (black) color object.
     *
     * @return the new color.
     */
    public Color color();

    /**
     * Create a new color with the given grayscale value.
     *
     * @param x the gray component.
     * @return the new color.
     */
    public Color color(float x);

    /**
     * Create a new color with the given grayscale and alpha value.
     *
     * @param x the grayscale value.
     * @param y the alpha value.
     * @return the new color.
     */
    public Color color(float x, float y);

    /**
     * Create a new color with the the given R/G/B value.
     *
     * @param x the red component.
     * @param y the green component.
     * @param z the blue component.
     * @return the new color.
     */
    public Color color(float x, float y, float z);

    /**
     * Create a new color with the the given R/G/B/A value.
     *
     * @param x the red component.
     * @param y the green component.
     * @param z the blue component.
     * @param a the alpha component.
     * @return the new color.
     */
    public Color color(float x, float y, float z, float a);

    /**
     * Create a new color with the the given color.
     * <p/>
     * The color object is cloned; you can change the original afterwards.
     * If the color object is null, the new color is turned off (same as nocolor).
     *
     * @param c the color object.
     * @return the new color.
     */
    public Color color(Color c);

    /**
     * Get the current fill color.
     *
     * @return the current fill color.
     */
    public Color fill();

    /**
     * Set the current fill color to given grayscale value.
     *
     * @param x the gray component.
     * @return the current fill color.
     */
    public Color fill(float x);

    /**
     * Set the current fill color to given grayscale and alpha value.
     *
     * @param x the grayscale value.
     * @param y the alpha value.
     * @return the current fill color.
     */
    public Color fill(float x, float y);

    /**
     * Set the current fill color to the given R/G/B value.
     *
     * @param x the red component.
     * @param y the green component.
     * @param z the blue component.
     * @return the current fill color.
     */
    public Color fill(float x, float y, float z);

    /**
     * Set the current fill color to the given R/G/B/A value.
     *
     * @param x the red component.
     * @param y the green component.
     * @param z the blue component.
     * @param a the alpha component.
     * @return the current fill color.
     */
    public Color fill(float x, float y, float z, float a);

    /**
     * Set the current fill color to the given color.
     * <p/>
     * The color object is cloned; you can change the original afterwards.
     * If the color object is null, the current fill color is turned off (same as nofill).
     *
     * @param c the color object.
     * @return the current fill color.
     */
    public Color fill(Color c);

    /**
     * Turn off the fill color.
     */
    public void nofill();

    /**
     * Get the current stroke color.
     *
     * @return the current stroke color.
     */
    public Color stroke();

    /**
     * Set the current stroke color to given grayscale value.
     *
     * @param x the gray component.
     * @return the current stroke color.
     */
    public Color stroke(float x);

    /**
     * Set the current stroke color to given grayscale and alpha value.
     *
     * @param x the grayscale value.
     * @param y the alpha value.
     * @return the current stroke color.
     */
    public Color stroke(float x, float y);

    /**
     * Set the current stroke color to the given R/G/B value.
     *
     * @param x the red component.
     * @param y the green component.
     * @param z the blue component.
     * @return the current stroke color.
     */
    public Color stroke(float x, float y, float z);

    /**
     * Set the current stroke color to the given R/G/B/A value.
     *
     * @param x the red component.
     * @param y the green component.
     * @param z the blue component.
     * @param a the alpha component.
     * @return the current stroke color.
     */
    public Color stroke(float x, float y, float z, float a);

    /**
     * Set the current stroke color to the given color.
     * <p/>
     * The color object is cloned; you can change the original afterwards.
     * If the color object is null, the current stroke color is turned off (same as nostroke).
     *
     * @param c the color object.
     * @return the current stroke color.
     */
    public Color stroke(Color c);

    /**
     * Turn off the stroke color.
     */
    public void nostroke();

    public float strokewidth();

    public float strokewidth(float w);

    public String font();

    public String font(String fontName);

    public String font(String fontName, float fontSize);

    public float fontsize();

    public float fontsize(float s);

    public float lineheight();

    public float lineheight(float lineHeight);

    public Text.Align align();

    public void align(Text.Align align);

    public Text text(String text, float x, float y);

    public Text text(String text, float x, float y, float width);

    public Text text(String text, float x, float y, float width, float height);

    public Text text(String text, float x, float y, float width, float height, boolean draw);

    public Path textpath(String text, float x, float y);

    public Path textpath(String text, float x, float y, float width);

    public Path textpath(String text, float x, float y, float width, float height);

    public Path textpath(String text, float x, float y, float width, float height, boolean draw);

    public Rect textmetrics(String text);

    public Rect textmetrics(String text, float width);

    public Rect textmetrics(String text, float width, float height);

    public float textwidth(String text);

    public float textwidth(String text, float width);

    public float textheight(String text);

    public float textheight(String text, float width);

    public void var(String name, VarType type);

    public void var(String name, VarType type, Object value);

    public void var(String name, VarType type, Object value, Float min, Float max);

    public double random();

    public long random(int max);

    public long random(int min, int max);

    public double random(double max);

    public double random(double min, double max);

    public Object choice(List objects);

    public Iterator<Point> grid(int columns, int rows);

    public Iterator<Point> grid(int columns, int rows, double columnSize, double rowSize);

    public void draw(Grob g);
}
