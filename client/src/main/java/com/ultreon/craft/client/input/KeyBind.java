package com.ultreon.craft.client.input;

import com.badlogic.gdx.Gdx;

public class KeyBind {
    private final String name;
    private int keyCode;

    public KeyBind(String name, int keyCode) {
        this.name = name;
        this.keyCode = keyCode;
    }

    public boolean isPressed() {
        return Gdx.input.isKeyPressed(this.keyCode);
    }

    public boolean isReleased() {
        return !Gdx.input.isKeyPressed(this.keyCode);
    }

    public boolean isJustPressed() {
        return Gdx.input.isKeyJustPressed(this.keyCode);
    }

    public int getKeyCode() {
        return this.keyCode;
    }

    public void setKeyCode(int keyCode) {
        this.keyCode = keyCode;
    }

    public String getName() {
        return this.name;
    }
}
