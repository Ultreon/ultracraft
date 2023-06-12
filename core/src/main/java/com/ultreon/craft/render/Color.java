package com.ultreon.craft.render;

import com.ultreon.craft.util.InvalidValueException;

import org.jetbrains.annotations.ApiStatus;

import java.util.regex.Pattern;

public class Color {
    public static Color black = Color.rgb(0x000000);
    public static Color darkGray = Color.rgb(0x404040);
    public static Color gray = Color.rgb(0x808080);
    public static Color lightGray = Color.rgb(0xc0c0c0);
    public static Color white = Color.rgb(0xffffff);
    public static Color red = Color.rgb(0xff0000);
    public static Color orange = Color.rgb(0xff8000);
    public static Color gold = Color.rgb(0xffb000);
    public static Color yellow = Color.rgb(0xffff00);
    public static Color yellowGreen = Color.rgb(0x80ff00);
    public static Color green = Color.rgb(0x00ff00);
    public static Color mint = Color.rgb(0x00ff80);
    public static Color cyan = Color.rgb(0x00ffff);
    public static Color azure = Color.rgb(0x0080ff);
    public static Color blue = Color.rgb(0x0000ff);
    public static Color purple = Color.rgb(0x8000ff);
    public static Color magenta = Color.rgb(0xff00ff);
    public static Color rose = Color.rgb(0xff0080);
    public static Color transparent = Color.rgba(0x00000000);
    private final java.awt.Color awtColor;

    private Color(long red, long green, long blue, long alpha) {
        this.awtColor = new java.awt.Color((int) red, (int) green, (int) blue, (int) alpha);
    }

    private Color(int red, int green, int blue, int alpha) {
        this.awtColor = new java.awt.Color(red, green, blue, alpha);
    }

    private Color(java.awt.Color color) {
        this.awtColor = color;
    }

    public static Color hsb(float h, float s, float b) {
        return new Color(java.awt.Color.getHSBColor(h, s, b));
    }

    public static Color rgb(int red, int green, int blue) {
        return new Color(red, green, blue, 255);
    }

    public static Color rgb(float red, float green, float blue) {
        return new Color((int) (red * 255), (int) (green * 255), (int) (blue * 255), 255);
    }

    public static Color rgba(int red, int green, int blue, int alpha) {
        return new Color(red, green, blue, alpha);
    }

    public static Color rgba(float red, float green, float blue, float alpha) {
        return new Color((int) (red * 255), (int) (green * 255), (int) (blue * 255), (int) (alpha * 255));
    }

    public static Color rgb(int color) {
        var rgb = ((long) color) % 0x100000000L;
        return new Color((rgb & 0xff0000L) >> 16, (rgb & 0x00ff00L) >> 8, rgb & 0x0000ffL, 255);
    }

    public static Color rgba(int color) {
        var rgba = ((long) color) % 0x100000000L;
        return new Color((rgba & 0xff000000L) >> 24, (rgba & 0x00ff0000L) >> 16, (rgba & 0x0000ff00L) >> 8, rgba & 0x000000ffL);
    }

    public static Color argb(int color) {
        var argb = ((long) color) % 0x100000000L;
        return new Color((argb & 0x00ff0000L) >> 16, (argb & 0x0000ff00L) >> 8, argb & 0x000000ffL, (argb & 0xff000000L) >> 24);
    }

    public static Color bgr(int color) {
        var bgr = ((long) color) % 0x100000000L;
        return new Color(bgr & 0x0000ffL, (bgr & 0x00ff00L) >> 8, (bgr & 0xff0000L) >> 16, 255);
    }

    public static Color bgra(int color) {
        var bgra = ((long) color) % 0x100000000L;
        return new Color((bgra & 0x0000ff00L) >> 8, (bgra & 0x00ff0000L) >> 16, (bgra & 0xff000000L) >> 24, bgra & 0x000000ffL);
    }

    public static Color abgr(int color) {
        var abgr = ((long) color) % 0x100000000L;
        return new Color(abgr & 0x000000ffL, (abgr & 0x0000ff00L) >> 8, (abgr & 0x00ff0000L) >> 16, (abgr & 0xff000000L) >> 24);
    }

    public static Color hex(String hex) {
        if (Pattern.matches("#[0-9a-fA-F]{6}", hex)) {
            int rgb = Integer.valueOf(hex.substring(1), 16);
            return Color.rgb(rgb);
        } else if (Pattern.matches("#[0-9a-fA-F]{8}", hex)) {
            int rgb = Integer.valueOf(hex.substring(1), 16);
            return Color.rgba(rgb);
        } else if (Pattern.matches("#[0-9a-fA-F]{3}", hex)) {
            int rgb = Integer.valueOf(new String(new char[]{
                    hex.charAt(1), hex.charAt(1),
                    hex.charAt(2), hex.charAt(2),
                    hex.charAt(3), hex.charAt(3)}), 16);
            return Color.rgb(rgb);
        } else if (Pattern.matches("#[0-9a-fA-F]{4}", hex)) {
            int rgb = Integer.valueOf(new String(new char[]{
                    hex.charAt(1), hex.charAt(1),
                    hex.charAt(2), hex.charAt(2),
                    hex.charAt(3), hex.charAt(3),
                    hex.charAt(4), hex.charAt(4)}), 16);
            return Color.rgba(rgb);
        } else {
            if (hex.length() >= 1) {
                if (hex.charAt(0) != '#') {
                    throw new InvalidValueException("First character create color code isn't '#'.");
                } else if (hex.length() != 3 && hex.length() != 4 && hex.length() != 6 && hex.length() != 8) {
                    throw new InvalidValueException("Invalid hex length, should be 3, 4, 6 or 8 in length.");
                } else {
                    throw new InvalidValueException("Invalid hex value. Hex values may only contain numbers and letters a to f.");
                }
            } else {
                throw new InvalidValueException("The color hex is empty, it should start with a hex, and then 3, 4, 6 or 8 hexadecimal digits.");
            }
        }
    }

    @ApiStatus.Internal
    public static Color awt(java.awt.Color awt) {
        return new Color(awt);
    }

    public static Color gdx(com.badlogic.gdx.graphics.Color color) {
        return new Color((int) (color.r * 255), (int) (color.g * 255), (int) (color.b * 255), (int) (color.a * 255));
    }

    public java.awt.Color toAwt() {
        return awtColor;
    }

    public Color brighter() {
        return new Color(awtColor.brighter());
    }

    public Color darker() {
        return new Color(awtColor.darker());
    }

    public int getRed() {
        return awtColor.getRed();
    }

    public int getGreen() {
        return awtColor.getGreen();
    }

    public int getBlue() {
        return awtColor.getBlue();
    }

    public int getAlpha() {
        return awtColor.getAlpha();
    }

    public int getTransparency() {
        return awtColor.getTransparency();
    }

    public int getRgb() {
        return awtColor.getRGB();
    }

    public Color withRed(int red) {
        return new Color(red, getGreen(), getBlue(), getAlpha());
    }

    public Color withGreen(int green) {
        return new Color(getRed(), green, getBlue(), getAlpha());
    }

    public Color withBlue(int blue) {
        return new Color(getRed(), getGreen(), blue, getAlpha());
    }

    public Color withAlpha(int alpha) {
        return new Color(getRed(), getGreen(), getBlue(), alpha);
    }

    @Override
    public String toString() {
        return "#%02x%02x%02x%02x".formatted(getRed(), getGreen(), getBlue(), getAlpha());
    }

    public com.badlogic.gdx.graphics.Color toGdx() {
        return new com.badlogic.gdx.graphics.Color(getRed() / 255f, getGreen() / 255f, getBlue() / 255f, getAlpha() / 255f);
    }
}
