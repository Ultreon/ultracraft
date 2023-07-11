package com.ultreon.craft;

import com.badlogic.gdx.ApplicationListener;
import org.jetbrains.annotations.UnknownNullability;

public class GameLibGDXWrapper implements ApplicationListener {
    private final String[] argv;
    @UnknownNullability
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
        this.game.resize(width, height);
    }

    @Override
    public void render() {
        this.game.render();
    }

    @Override
    public void pause() {
        this.game.pause();
    }

    @Override
    public void resume() {
        this.game.resume();
    }

    @Override
    public void dispose() {
        this.game.dispose();
    }
}
