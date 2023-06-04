package com.ultreon.craft.render.gui.screens;

import com.badlogic.gdx.math.MathUtils;
import com.ultreon.craft.UltreonCraft;
import com.ultreon.craft.render.Color;
import com.ultreon.craft.render.Renderer;
import com.ultreon.craft.world.SavedWorld;
import com.ultreon.craft.world.World;

import java.io.IOException;
import java.util.Objects;

public class WorldLoadScreen extends Screen {
    private final SavedWorld savedWorld;

    public WorldLoadScreen(SavedWorld savedWorld) {
        super("Loading World");
        this.savedWorld = savedWorld;
    }

    @Override
    public void show() {
        super.show();

        this.game.world = new World(this.savedWorld, 16, 16);
        new Thread(this::run, "World Loading").start();
    }

    public void run() {
        MathUtils.random.setSeed(0);

        try {
            Objects.requireNonNull(this.game.world).load();
        } catch (IOException e) {
            UltreonCraft.crash(e);
            return;
        }

        this.game.respawn();

        this.game.renderWorld = true;
        this.game.runLater(() -> this.game.showScreen(null));
    }

    @Override
    protected void renderBackground(Renderer renderer) {
        fill(renderer, 0, 0, this.width, this.height, 0xff202020);

        renderer.setColor(Color.rgb(0xffffff));
        renderer.text(this.title, this.width / 2 - (int) this.titleLayout.width / 2, this.height - this.height / 3);
    }

    @Override
    public boolean canCloseOnEsc() {
        return false;
    }
}
