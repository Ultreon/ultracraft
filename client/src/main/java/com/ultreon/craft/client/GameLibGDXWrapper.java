package com.ultreon.craft.client;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import org.jetbrains.annotations.Nullable;

public class GameLibGDXWrapper implements ApplicationListener {
    private final String[] argv;
    @Nullable
    private UltracraftClient client;

    public GameLibGDXWrapper(String[] argv) {
        this.argv = argv;
    }

    @Override
    public void create() {
        try {
            Gdx.app.setLogLevel(Application.LOG_DEBUG);
            this.client = new UltracraftClient(this.argv);
        } catch (Throwable t) {
            UltracraftClient.crash(t);
        }
    }

    @Override
    public void resize(int width, int height) {
        if (this.client != null) this.client.resize(width, height);
    }

    @Override
    public void render() {
        if (this.client != null) this.client.render();
    }

    @Override
    public void pause() {
        if (this.client != null) this.client.pause();
    }

    @Override
    public void resume() {
        if (this.client != null) this.client.resume();
    }

    @Override
    public void dispose() {
        if (this.client != null) this.client.dispose();
    }
}
