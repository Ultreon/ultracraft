package com.ultreon.craft.client.config.gui;

import com.badlogic.gdx.Gdx;
import com.ultreon.craft.config.crafty.CraftyConfig;
import com.ultreon.craft.client.gui.Bounds;
import com.ultreon.craft.client.gui.GuiBuilder;
import com.ultreon.craft.client.gui.Renderer;
import com.ultreon.craft.client.gui.screens.Screen;
import com.ultreon.craft.client.gui.widget.SelectionList;
import com.ultreon.craft.text.TextObject;
import com.ultreon.craft.util.Color;

public class CraftyConfigGui extends Screen {
    protected CraftyConfigGui() {
        super(TextObject.translation("ultracraft.screen.config"));
    }

    @Override
    public void build(GuiBuilder builder) {
        builder.add(new SelectionList<CraftyConfig>(40).entries(CraftyConfig.getConfigs()).itemRenderer(this::renderItem).bounds(() -> new Bounds(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight())));
    }

    private void renderItem(Renderer renderer, CraftyConfig value, int y, int mouseX, int mouseY, boolean selected, float deltaTime) {
        String fileName = value.getFileName();
        renderer.textLeft(fileName, 20, y + 20, Color.rgb(selected ? 0xFF0000 : 0xFFFFFF));
    }
}
