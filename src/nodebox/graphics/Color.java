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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Color implements Cloneable {

    private static final Pattern HEX_STRING_PATTERN = Pattern.compile("^#([0-9a-f]{2})([0-9a-f]{2})([0-9a-f]{2})([0-9a-f]{2})$");

    public enum Mode {
        RGB, HSB, CMYK
    }

    private double r, g, b, a;

    public static double clamp(double v) {
        return Math.max(0.0, Math.min(1.0, v));
    }

    /**
     * Create an empty (black) color object.
     */
    public Color() {
        this(0, 0, 0, 1.0, Mode.RGB);
    }

    /**
     * Create a new color with the given grayscale value.
     *
     * @param v the gray component.
     */
    public Color(double v) {
        this(v, v, v, 1.0, Mode.RGB);
    }

    /**
     * Create a new color with the given grayscale and alpha value.
     *
     * @param v the grayscale value.
     * @param a the alpha value.
     */

    public Color(double v, double a) {
        this(v, v, v, a, Mode.RGB);
    }

    /**
     * Create a new color with the the given R/G/B value.
     *
     * @param x the red or hue component.
     * @param y the green or saturation component.
     * @param z the blue or brightness component.
     * @param m the specified color mode.
     */
    public Color(double x, double y, double z, Mode m) {
        this(x, y, z, 1.0, m);
    }

    /**
     * Create a new color with the the given R/G/B value.
     *
     * @param r the red component.
     * @param g the green component.
     * @param b the blue component.
     */
    public Color(double r, double g, double b) {
        this(r, g, b, 1.0, Mode.RGB);
    }

    /**
     * Create a new color with the the given R/G/B/A or H/S/B/A value.
     *
     * @param r the red component.
     * @param g the green component.
     * @param b the blue component.
     * @param a the alpha component.
     */
    public Color(double r, double g, double b, double a) {
        this(r, g, b, a, Mode.RGB);
    }

    /**
     * Create a new color with the the given R/G/B/A or H/S/B/A value.
     *
     * @param x the red or hue component.
     * @param y the green or saturation component.
     * @param z the blue or brightness component.
     * @param a the alpha component.
     * @param m the specified color mode.
     */
    public Color(double x, double y, double z, double a, Mode m) {
        // TODO: implement color conversion.
        this.r = clamp(x);
        this.g = clamp(y);
        this.b = clamp(z);
        this.a = clamp(a);
    }

    public Color(String colorName) {
        // We can only parse RGBA hex color values.
        Matcher m = HEX_STRING_PATTERN.matcher(colorName);
        if (!m.matches())
            throw new IllegalArgumentException("The given value '" + colorName + "' is not of the format #112233ff.");
        int r255 = Integer.parseInt(m.group(1), 16);
        int g255 = Integer.parseInt(m.group(2), 16);
        int b255 = Integer.parseInt(m.group(3), 16);
        int a255 = Integer.parseInt(m.group(4), 16);
        this.r = r255 / 255.0;
        this.g = g255 / 255.0;
        this.b = b255 / 255.0;
        this.a = a255 / 255.0;
    }

    /**
     * Create a new color with the the given color.
     * <p/>
     * The color object is cloned; you can change the original afterwards.
     * If the color object is null, the new color is turned off (same as nocolor).
     *
     * @param color the color object.
     */
    public Color(java.awt.Color color) {
        this.r = color.getRed() / 255.0;
        this.g = color.getGreen() / 255.0;
        this.b = color.getBlue() / 255.0;
        this.a = color.getAlpha() / 255.0;
    }

    /**
     * Create a new color with the the given color.
     * <p/>
     * The color object is cloned; you can change the original afterwards.
     * If the color object is null, the new color is turned off (same as nocolor).
     *
     * @param other the color object.
     */
    public Color(Color other) {
        this.r = other.r;
        this.g = other.g;
        this.b = other.b;
        this.a = other.a;
    }

    public double getRed() {
        return r;
    }

    public double getGreen() {
        return g;
    }

    public double getBlue() {
        return b;
    }

    public double getAlpha() {
        return a;
    }

    public boolean isVisible() {
        return a > 0.0;
    }

    private void updateRGB() {

    }

    private void updateHSB() {

    }

    private void updateCMYK() {

    }

    public java.awt.Color getAwtColor() {
        return new java.awt.Color((float) getRed(), (float) getGreen(), (float) getBlue(), (float) getAlpha());
    }

    @Override
    public Color clone() {
        return new Color(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Color)) return false;
        Color other = (Color) obj;
        // Because of the conversion to/from hex, we can have rounding errors.
        // Therefore, we only compare what we can store, i.e. values in the 0-255 range.
        return Math.round(r * 255) == Math.round(other.r * 255)
                && Math.round(g * 255) == Math.round(other.g * 255)
                && Math.round(b * 255) == Math.round(other.b * 255)
                && Math.round(a * 255) == Math.round(other.a * 255);
    }

    /**
     * Parse a hexadecimal value and return a Color object.
     * <p/>
     * The value needs to have four components. (R,G,B,A)
     *
     * @param value the hexadecimal color value, e.g. #995423ff
     * @return a Color object.
     */
    public static Color parseColor(String value) {
        return new Color(value);
    }

    private String paddedHexString(int v) {
        String s = Integer.toHexString(v);
        if (s.length() == 1) {
            return "0" + s;
        } else if (s.length() == 2) {
            return s;
        } else {
            throw new AssertionError("Value too large (must be between 0-255, was " + v + ").");
        }
    }

    /**
     * Returns the color as a 8-bit hexadecimal value, e.g. #ae45cdff
     *
     * @return the color as a 8-bit hexadecimal value
     */
    @Override
    public String toString() {
        int r256 = (int) Math.round(r * 255);
        int g256 = (int) Math.round(g * 255);
        int b256 = (int) Math.round(b * 255);
        int a256 = (int) Math.round(a * 255);
        return "#"
                + paddedHexString(r256)
                + paddedHexString(g256)
                + paddedHexString(b256)
                + paddedHexString(a256);
    }
}
