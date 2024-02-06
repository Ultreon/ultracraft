package com.ultreon.craft.client.gui.widget.components;

import com.ultreon.craft.client.gui.widget.Widget;
import com.ultreon.craft.text.TextObject;
import com.ultreon.craft.util.ElementID;
import org.jetbrains.annotations.Nullable;

public class TextComponent extends UIComponent {
    @Nullable
    private TextObject text;

    public TextComponent() {
        this(null);
    }

    public TextComponent(@Nullable TextObject text) {
        super();
        this.text = text;
    }

    @Nullable
    public TextObject get() {
        return this.text;
    }

    public void set(@Nullable TextObject text) {
        this.text = text;
    }

    public String getRaw() {
        if (this.text == null) {
            return null;
        }
        return this.text.getText();
    }

    public void setRaw(String text) {
        if (text == null) {
            this.text = null;
        }
        this.text = TextObject.literal(text);
    }

    public void translate(String path, Object... args) {
        this.text = TextObject.translation(path, args);
    }

    @Override
    public void handleImGui(String path, ElementID key, Widget widget) {

    }
}
