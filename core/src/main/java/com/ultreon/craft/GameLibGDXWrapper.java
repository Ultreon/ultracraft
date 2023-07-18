package com.ultreon.craft;

import com.badlogic.gdx.ApplicationListener;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

public class GameLibGDXWrapper implements ApplicationListener {
    private final String[] argv;
    @Nullable
    private UltreonCraft game;

    public GameLibGDXWrapper(String[] argv) {
        this.argv = argv;
    }

    @Override
    public void create() {
        try {
            this.game = new UltreonCraft(this.argv);
        } catch (Throwable t) {
            UltreonCraft.crash(t);
        }
    }

    @Override
    public void resize(int width, int height) {
        if (this.game != null) this.game.resize(width, height);
    }

    @Override
    public void render() {
        if (this.game != null) this.game.render();
    }

    @Override
    public void pause() {
        if (this.game != null) this.game.pause();
    }

    @Override
    public void resume() {
        if (this.game != null) this.game.resume();
    }

    @Override
    public void dispose() {
        if (this.game != null) this.game.dispose();
    }
}
