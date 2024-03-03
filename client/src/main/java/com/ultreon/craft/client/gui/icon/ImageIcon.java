package com.ultreon.craft.client.gui.icon;

import com.ultreon.craft.util.Identifier;

public record ImageIcon(Identifier id, int width, int height) implements Icon {
    public ImageIcon(Identifier id) {
        this(id, 16, 16);
    }

    @Override
    public int u() {
        return 0;
    }

    @Override
    public int v() {
        return 0;
    }

    @Override
    public int texWidth() {
        return this.width;
    }

    @Override
    public int texHeight() {
        return this.height;
    }
}
