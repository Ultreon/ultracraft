package com.ultreon.craft.text;

import com.ultreon.craft.util.Color;
import com.ultreon.craft.util.ElementID;
import com.ultreon.data.types.MapType;
import org.jetbrains.annotations.Nullable;

public class TextStyle {
    private int size = 1;
    private boolean bold = false;
    private boolean italic = false;
    private boolean underline = false;
    private boolean strikethrough = false;
    private @Nullable HoverEvent<?> hoverEvent = null;
    private @Nullable ClickEvent clickEvent = null;
    private Color color = Color.WHITE;
    private ElementID font;

    public static TextStyle deserialize(MapType data) {
        TextStyle textStyle = new TextStyle();
        textStyle.color = Color.rgb(data.getInt("color"));
        textStyle.bold = data.getBoolean("bold");
        textStyle.italic = data.getBoolean("italic");
        textStyle.underline = data.getBoolean("underline");
        textStyle.strikethrough = data.getBoolean("strikethrough");
        return textStyle;
    }

    public MapType serialize() {
        MapType data = new MapType();
        data.putInt("color", this.color.getRgb());
        data.putBoolean("bold", this.bold);
        data.putBoolean("italic", this.italic);
        data.putBoolean("underline", this.underline);
        data.putBoolean("strikethrough", this.strikethrough);
        return data;
    }

    public int getSize() {
        return this.size;
    }

    public TextStyle size(int size) {
        this.size = size;
        return this;
    }

    public boolean isBold() {
        return this.bold;
    }

    public TextStyle bold(boolean bold) {
        this.bold = bold;
        return this;
    }

    public boolean isItalic() {
        return this.italic;
    }

    public TextStyle italic(boolean italic) {
        this.italic = italic;
        return this;
    }

    public boolean isUnderline() {
        return this.underline;
    }

    public TextStyle underline(boolean underline) {
        this.underline = underline;
        return this;
    }

    public boolean isStrikethrough() {
        return this.strikethrough;
    }

    public TextStyle strikethrough(boolean strikethrough) {
        this.strikethrough = strikethrough;
        return this;
    }

    public HoverEvent<?> getHoverEvent() {
        return this.hoverEvent;
    }

    public TextStyle hoverEvent(HoverEvent<?> hoverEvent) {
        this.hoverEvent = hoverEvent;
        return this;
    }

    public ClickEvent getClickEvent() {
        return this.clickEvent;
    }

    public TextStyle clickEvent(ClickEvent clickEvent) {
        this.clickEvent = clickEvent;
        return this;
    }

    public Color getColor() {
        return this.color;
    }

    public TextStyle color(Color color) {
        this.color = color;
        return this;
    }

    public ElementID getFont() {
        return this.font;
    }

    public TextStyle font(ElementID font) {
        this.font = font;
        return this;
    }
}