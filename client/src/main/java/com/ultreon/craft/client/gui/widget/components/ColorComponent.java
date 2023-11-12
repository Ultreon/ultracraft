package com.ultreon.craft.client.gui.widget.components;

import com.ultreon.craft.component.GameComponent;
import com.ultreon.craft.util.Color;
import com.ultreon.libs.commons.v0.Identifier;

public class ColorComponent extends UIComponent {
    private Color color;

    public ColorComponent(Identifier id, Color color) {
        super(id);
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }
}
