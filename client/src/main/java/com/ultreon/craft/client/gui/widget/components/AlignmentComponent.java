package com.ultreon.craft.client.gui.widget.components;

import com.ultreon.craft.client.gui.Alignment;
import com.ultreon.craft.client.gui.widget.Widget;
import com.ultreon.craft.util.ElementID;

public class AlignmentComponent extends UIComponent {
    private Alignment alignment;

    public AlignmentComponent(Alignment alignment) {
        super();
        this.alignment = alignment;
    }

    public Alignment get() {
        return alignment;
    }

    public void set(Alignment alignment) {
        this.alignment = alignment;
    }

    @Override
    public void handleImGui(String path, ElementID key, Widget widget) {
    }
}
