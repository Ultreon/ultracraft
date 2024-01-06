package com.ultreon.craft.client.gui.widget.components;

import com.ultreon.craft.client.gui.widget.Widget;
import com.ultreon.craft.text.TextObject;
import com.ultreon.craft.util.ElementID;
import com.ultreon.craft.util.ImGuiEx;

public class TextComponent extends UIComponent {
    private TextObject text;

    public TextComponent() {
        this(TextObject.empty());
    }

    public TextComponent(TextObject text) {
        super();
        this.text = text;
    }

    public TextObject get() {
        return this.text;
    }

    public void set(TextObject text) {
        this.text = text;
    }

    public String getRaw() {
        return this.text.getText();
    }

    public void setRaw(String text) {
        this.text = TextObject.literal(text);
    }

    public void translate(String path, Object... args) {
        this.text = TextObject.translation(path, args);
    }

    @Override
    public void handleImGui(String path, ElementID key, Widget widget) {
        ImGuiEx.editString("Text (" + key + "): ", path, this::getRaw, this::setRaw);
    }
}
