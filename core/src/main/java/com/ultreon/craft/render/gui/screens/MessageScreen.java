package com.ultreon.craft.render.gui.screens;

import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.MathUtils;
import com.ultreon.craft.Task;
import com.ultreon.craft.UltreonCraft;
import com.ultreon.craft.render.Color;
import com.ultreon.craft.render.Renderer;
import com.ultreon.craft.world.SavedWorld;
import com.ultreon.craft.world.World;
import com.ultreon.libs.commons.v0.Identifier;

import java.io.IOException;

public class MessageScreen extends Screen {

    public MessageScreen(String message) {
        super(message);
    }

    @Override
    protected void renderBackground(Renderer renderer) {
        fill(renderer, 0, 0, this.width, this.height, 0xff202020);

        renderer.setColor(Color.rgb(0xffffff));
        renderer.drawCenteredText(this.title, this.width / 2, this.height / 3);
    }

    @Override
    public boolean canClose() {
        return false;
    }
}
