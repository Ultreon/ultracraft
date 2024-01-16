package com.ultreon.craft.client.gui.widget.components;

import com.ultreon.craft.client.gui.widget.Widget;
import com.ultreon.craft.util.ElementID;

public class ScaleComponent extends UIComponent {
    private int scale;

    public ScaleComponent(int scale) {
        super();
        this.scale = scale;
    }

    public int get() {
        return this.scale;
    }

    public void set(int scale) {
        this.scale = scale;
    }

    @Override
    public void handleImGui(String path, ElementID key, Widget widget) {
    }
}
