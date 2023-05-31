package com.ultreon.craft.render.gui.screens;

import com.badlogic.gdx.math.MathUtils;
import com.ultreon.craft.render.Color;
import com.ultreon.craft.render.Renderer;
import com.ultreon.craft.world.World;

public class WorldLoadScreen extends Screen {
    public WorldLoadScreen() {
        super("Loading World");
    }

    @Override
    public void show() {
        super.show();

        game.world = new World(game.blocksTextureAtlas, 16, 16);
        new Thread(this::run, "World Loading").start();
    }

    public void run() {
        MathUtils.random.setSeed(0);
        game.world.generateWorld();

        game.respawn();

        game.renderWorld = true;
        game.runLater(() -> game.showScreen(null));
    }

    @Override
    protected void renderBackground(Renderer renderer) {
        fill(renderer, 0, 0, width, height, 0xff202020);

        renderer.setColor(Color.rgb(0xffffff));
        renderer.text(title, width / 2 - (int)titleLayout.width / 2, height - height / 3);
    }

    @Override
    public boolean canCloseOnEsc() {
        return false;
    }
}
