package com.ultreon.craft.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

public class PlayerInput {
    public boolean forward;
    public boolean backward;
    public boolean strafeLeft;
    public boolean strafeRight;
    public boolean up;
    public boolean down;

    public PlayerInput() {
    }

    public void tick() {
        forward = Gdx.input.isKeyPressed(Input.Keys.W) && Gdx.input.isCursorCatched();
        backward = Gdx.input.isKeyPressed(Input.Keys.S) && Gdx.input.isCursorCatched();
        strafeLeft = Gdx.input.isKeyPressed(Input.Keys.A) && Gdx.input.isCursorCatched();
        strafeRight = Gdx.input.isKeyPressed(Input.Keys.D) && Gdx.input.isCursorCatched();
        up = Gdx.input.isKeyPressed(Input.Keys.SPACE) && Gdx.input.isCursorCatched();
        down = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) && Gdx.input.isCursorCatched();
    }
}
