package com.ultreon.craft.client.gui.widget.components;

import com.ultreon.craft.client.gui.widget.Widget;
import com.ultreon.craft.util.Color;
import com.ultreon.craft.util.Identifier;
import com.ultreon.craft.util.ImGuiEx;

public class ColorComponent extends UIComponent {
    private Color color;

    public ColorComponent(Color color) {
        super();
        this.color = color;
    }

    public Color get() {
        return this.color;
    }

    public void set(Color color) {
        this.color = color;
    }

    public void rgba(int r, int g, int b, int a) {
        this.color = Color.rgba(r, g, b, a);
    }

    public void rgba(float r, float g, float b, float a) {
        this.color = Color.rgba(r, g, b, a);
    }

    public void rgb(int r, int g, int b) {
        this.color = Color.rgb(r, g, b);
    }

    public void rgb(float r, float g, float b) {
        this.color = Color.rgb(r, g, b);
    }

    public void rgb(int rgb) {
        this.color = Color.rgb(rgb);
    }

    public void argb(int argb) {
        this.color = Color.argb(argb);
    }

    public void hex(String hex) {
        this.color = Color.hex(hex);
    }

    @Override
    public void handleImGui(String path, Identifier key, Widget widget) {
        ImGuiEx.editColor3("Color (" + key + "): ", path, this::get, this::set);
    }
}
