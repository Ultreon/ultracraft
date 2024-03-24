package com.ultreon.craft.text;

import com.ultreon.craft.util.Identifier;

public class FontTexture {
    private final char c;
    private final Identifier font;

    public FontTexture(char c, Identifier font) {
        this.c = c;
        this.font = font;
    }

    public FontTexture(int id, Identifier font) {
        this((char) id, font);
    }

    public char getChar() {
        return this.c;
    }

    public Identifier getFont() {
        return this.font;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;

        FontTexture that = (FontTexture) o;

        if (this.c != that.c) return false;
        return this.font.equals(that.font);
    }

    @Override
    public int hashCode() {
        int result = this.c;
        result =  31 * result + this.font.hashCode();
        return result;
    }
}