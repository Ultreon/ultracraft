package com.ultreon.craft.client.gui.widget.components;

import com.ultreon.craft.client.gui.widget.Widget;
import com.ultreon.craft.component.GameComponent;
import com.ultreon.libs.commons.v0.Identifier;

public class UIComponent extends GameComponent<Widget> {
    public UIComponent() {
        super();
    }

    public void handleImGui(String path, Identifier key, Widget widget) {
        // Handles in subclasses
    }
}
